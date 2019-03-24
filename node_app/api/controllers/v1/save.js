const mysql = require('mysql'); // MySQL connection driver
const {createTextFile, getBucketContents} = require('../../../ibm.js');
const ClientException = require('../../exceptions/ClientException.js')
const ServerException = require('../../exceptions/ServerException.js')

const REQUIRED_KEYS = [ 'user_id', 'het_version' ];
const EXPECTED_DATA_KEYS = [ 'chest_ecg', 'chest_ppg', 'chest_inertia_x', 'chest_inertia_y', 'chest_inertia_z', 'wrist_inertia_x', 'wrist_inertia_y', 
    'wrist_inertia_z', 'wrist_ppg', 'wrist_oz', 'wrist_poz', 'wrist_roz', 'wrist_moz', 'wrist_temperature', 'wrist_humidity' ];
const REQUIRED_DATA_SUB_KEYS = [ 'initial_timestamp', 'delta', 'data' ];
const BUCKET_NAME = 'het-streaming';

exports.save = async function(request, response) {
    // Data Validation
    receivedKeys = Object.keys(request.body);
    missingKeys = REQUIRED_KEYS.filter((key) => !receivedKeys.includes(key));
    receivedDataKeys = EXPECTED_DATA_KEYS.filter((key) => receivedKeys.includes(key));
    initialTimestamp = Number.MAX_SAFE_INTEGER;

    if (missingKeys.length > 0)
        throw new ClientException(`Missing required keys! Missing: ${missingKeys}`);

    for (i = 0; i < receivedDataKeys.length; i++) {
        receivedSubKeys = Object.keys(request.body[receivedDataKeys[i]]);
        missingSubKeys = REQUIRED_DATA_SUB_KEYS.filter((key) => !receivedSubKeys.includes(key));

        if (missingSubKeys.length > 0)
            throw new ClientException(`Missing expected subkeys in ${receivedDataKeys[i]}! Missing: ${missingSubKeys}`)

        // Calculate the smallest timestamp received
        if (request.body[receivedDataKeys[i]].initial_timestamp < initialTimestamp) 
            initialTimestamp = request.body[receivedDataKeys[i]].initial_timestamp;
    }

    // Figure out which path to save the data to
    var currentFiles = await getBucketContents(BUCKET_NAME, `Subject${request.body.user_id}/HET_v${request.body.het_version}_chest-`);
    var maxCsvFuzzed = 0;
    var maxDataPath;
    var maxDataNumber = 0;
    // Loop through received files for the user
    currentFiles.forEach(function(file) {
        // Split the file path/name into just the timestamp
        fuzzed = parseInt(file.Key.split('/')[1].split('-')[1]);
	if (fuzzed > maxCsvFuzzed) {
	    maxDataNumber = 0;
	}
        // This equal comparison is added to have the highest Data#.csv
	if (fuzzed >= maxCsvFuzzed) {
            maxCsvFuzzed = fuzzed;
	    dataFileNumber = parseInt(file.Key.split('/')[2].split('.')[0].substring(4));
	    if (dataFileNumber > maxDataNumber) {
		maxDataNumber = dataFileNumber;
            	maxDataPath = file.Key;
	    }
        }
    });

    if (maxCsvFuzzed === 0) {
        // No data received from the bucket
	pathTimestamp = initialTimestamp;
        dataFileName = 'Data1.csv';
    } else {
        // Data received, calculate whether we should use a new or old folder
        now = new Date();
        midnight = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0);
        msSinceMidnight = now.getTime() - midnight.getTime();

        if (initialTimestamp - maxCsvFuzzed < msSinceMidnight) {
            // Write to same folder
            pathTimestamp = maxCsvFuzzed;
            dataFileName = maxDataPath.split('/')[2].split('.')[0].substring(4);

            if (dataFileName != null) {
                dataFileName = `Data${parseInt(dataFileName) + 1}.csv`;
            } else {
                dataFileName = 'Data1.csv';
            }
        } else {
            // Write to new folder with received data timestamp
	    pathTimestamp = initialTimestamp;
            dataFileName = 'Data1.csv';
        }
    }
    
    // Save data to bucket
    var chestDataPath = `Subject${request.body.user_id}/HET_v${request.body.het_version}_chest-${pathTimestamp}`;
    var wristDataPath = `Subject${request.body.user_id}/HET_v${request.body.het_version}_wrist-${pathTimestamp}`;

    var chestData = 'type,timestamp,data\n';
    var wristData = 'type,timestamp,data\n';

    // Generate the csv data strings
    for (i = 0; i < receivedDataKeys.length; i++) {
        dataKey = receivedDataKeys[i];
        if (dataKey.startsWith('chest_')) {
            for (c = 0; c < request.body[dataKey].data.length; c++) {
                chestData += dataKey.replace('chest_', '') + ',';
                if (c === 0) {
                    lastTimeStamp = request.body[dataKey].initial_timestamp;
                    chestData += lastTimeStamp + ',';
                } else {
                    lastTimeStamp = request.body[dataKey].delta[c-1] + lastTimeStamp;
                    chestData += lastTimeStamp + ',';
                }
                chestData += request.body[dataKey].data[c] + '\n';
            }
        } else if (dataKey.startsWith('wrist_')) {
            for (c = 0; c < request.body[dataKey].data.length; c++) {
                wristData += dataKey.replace('wrist_', '') + ',';
                if (c === 0) {
                    lastTimeStamp = request.body[dataKey].initial_timestamp;
                    wristData += lastTimeStamp + ',';
                } else {
                    lastTimeStamp = request.body[dataKey].delta[c-1] + lastTimeStamp;
                    wristData += lastTimeStamp + ',';
                }
                wristData += request.body[dataKey].data[c] + '\n';
            }
        } else {
            throw new ClientException(`Data type ${dataKey} does not seem to be chest or wrist!`);
        }
    }

    var chestPromise = createTextFile(BUCKET_NAME, chestDataPath + `/${dataFileName}`, chestData);
    var wristPromise = createTextFile(BUCKET_NAME, wristDataPath + `/${dataFileName}`, wristData);

    Promise.all([chestPromise, wristPromise]).then(() => {
        response.status(201).json({
            status: 201,
            message: 'Successfully saved'
        });
    }).catch((e) => {
        response.status(500).json({
            status: 500,
            message: 'Error saving data to IBM Cloud'
        });
    });
};
