const mysql = require('mysql'); // MySQL connection driver

// Configure MySQL Connection Pool
var pool = mysql.createPool({
    connectionLimit : 25,
    host            : '192.168.100.100',
    port            : 3306,
    user            : 'root',      // TODO: Change to web!
    password        : 'password',
    database        : 'ASSIST',
    timezone        : 'Z'          // UTC Standard Time
});

pool.on('acquire', function (connection) {
    console.log('[MySQL] Connection %d acquired', connection.threadId);
});

exports.pool = pool;
