const mysql = require('mysql'); // MySQL connection driver
const ibm = require('../../../ibm.js');
const ClientException = require('../../exceptions/ClientException.js')
const ServerException = require('../../exceptions/ServerException.js')

const REQUIRED_KEYS = [ 'user_id', 'het_version' ];
const EXPECTED_DATA_KEYS = [ 'chest_ecg', 'chest_ppg', 'chest_inertia_x', 'chest_inertia_y', 'chest_inertia_z', 'wrist_inertia_x', 'wrist_inertiay', 
    'wrist_inertia_z', 'wrist_ppg', 'wrist_oz', 'wrist_poz', 'wrist_roz', 'wrist_moz', 'wrist_temperature', 'wrist_humidity' ];
const REQUIRED_DATA_SUB_KEYS = [ 'initial_timestamp', 'delta', 'data' ];

exports.save = function(request, response) {
    // Data Validation
    receivedKeys = Object.keys(request.body);
    missingKeys = REQUIRED_KEYS.filter((key) => !receivedKeys.includes(key));
    receivedDataKeys = EXPECTED_DATA_KEYS.filter((key) => receivedKeys.includes(key));
    chestInitialTimestamp = Number.MAX_SAFE_INTEGER;
    wristInitialTimestamp = Number.MAX_SAFE_INTEGER;

    if (missingKeys.length > 0)
        throw new ClientException(`Missing required keys! Missing: ${missingKeys}`);

    for (i = 0; i < receivedDataKeys.length; i++) {
        receivedSubKeys = Object.keys(request.body[receivedDataKeys[i]]);
        missingSubKeys = REQUIRED_DATA_SUB_KEYS.filter((key) => !receivedSubKeys.includes(key));

        if (missingSubKeys.length > 0)
            throw new ClientException(`Missing expected subkeys in ${receivedDataKeys[i]}! Missing: ${missingSubKeys}`)

        // Calculate the smallest timestamp received
        if (receivedDataKeys[i].startsWith('chest_')) {
            if (request.body[receivedDataKeys[i]].initial_timestamp < initialTimestamp) 
                chestInitialTimestamp = request.body[receivedDataKeys[i]].initial_timestamp;
        } else if (receivedDataKeys[i].startsWith('wrist_')) {
            if (request.body[receivedDataKeys[i]].initial_timestamp < initialTimestamp) 
                wristInitialTimestamp = request.body[receivedDataKeys[i]].initial_timestamp;
        } else {
            throw new ServerException(`Unknown key ${receivedDataKeys[i]}`);
        }
    }
   
    // Save data to bucket
    var chestDataPath = `Subject${request.body.user_id}/HET_v${request.body.het_version}_chest-${initialTimestamp}`;
    var wristDataPath = `Subject${request.body.user_id}/HET_v${request.body.het_version}_wrist-${initialTimestamp}`;

    var chestData = 'type,timestamp,data\n';
    var wristData = 'type,timestamp,data\n';

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

    var chestPromise = ibm('het-streaming', chestDataPath + '/Data.csv', chestData);
    var wristPromise = ibm('het-streaming', wristDataPath + '/Data.csv', wristData);

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