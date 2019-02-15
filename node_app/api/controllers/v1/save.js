const mysql = require('mysql'); // MySQL connection driver

var {pool} = require('../../../mysql.js');
const ClientException = require('../../exceptions/ClientException.js')
const ServerException = require('../../exceptions/ServerException.js')

const EXPECTED_KEYS = [ 'chest_ecg', 'chest_ppg', 'chest_inertia', 'wrist_inertia', 'wrist_ppg', 'wrist_oz', 'wrist_poz', 'wrist_roz', 'wrist_moz', 'wrist_temperature', 'wrist_humidity' ];
const EXPECTED_SUB_KEYS = [ 'initial_timestamp', 'delta', 'data' ];

exports.save = function(request, response) {
    // Data Validation
    receivedKeys = Object.keys(request.body);
    missingKeys = EXPECTED_KEYS.filter((key) => !receivedKeys.includes(key));

    if (missingKeys.length > 0)
        throw new ClientException(`Missing expected keys! Missing: ${missingKeys}`);

    for (i = 0; i < EXPECTED_KEYS.length; i++) {
        receivedSubKeys = Object.keys(request.body[EXPECTED_KEYS[i]]);
        missingSubKeys = EXPECTED_SUB_KEYS.filter((key) => !receivedSubKeys.includes(key));

        if (missingSubKeys.length > 0)
            throw new ClientException(`Missing expected subkeys in ${EXPECTED_KEYS[i]}! Missing: ${missingSubKeys}`)
    }
   
    // Save data to database
    throw new ServerException(`Saving to the database is not setup yet.`);
    data = {
        user: response.locals.googleUserId,
        type: EXPECTED_KEYS[0],
        timestamp: new Date(1549481651000),
        data: 16777215
    };
    pool.query('INSERT INTO data SET ?', data, function (error, results, fields) {
        if (error) throw error;
        // Neat!
      });

    response.status(201).json({
        status: 201,
        message: 'Successfully saved'
    });
};