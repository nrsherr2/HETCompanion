// Dependency Imports
const express = require('express');                   // Expressjs handles rest requests
const helmet = require('helmet');                     // Remove/set security related HTTP headers
const bodyParser = require('body-parser');            // For parsing JSON requests
const {OAuth2Client} = require('google-auth-library');// Google API for authenticating tokens

// Local Imports
var routes = require('./api/routes/routes.js');
var {pool} = require('./mysql.js');

// Config
const PORT = 8080;
const GOOGLE_CLIENT_ID = '732133341279-0ku0f3vfe4b1dca3tobo3s4pvnc0g6gu.apps.googleusercontent.com'; //Tony's dev client id

// Create an instance of the express app
var app = express();

// Configure express to run behind nginx
app.set('trust proxy', 'loopback');

// Use helmet to set security headers
app.use(helmet());

// Only allow application/json body requests
app.use(bodyParser.json({
    type: 'application/json',
    limit: '100mb'
}));

// Log all requests received by the server
app.use(function(request, response, next) {
    console.log(`${request.method} request received from ${request.ip} to '${request.path}'. Request size: ${request.get('Content-Length')} bytes`);
    next();
});

// Check if the user is authenticated
app.use(function(request, response, next) {
    var isAuthenticated = false;
    var authToken = request.get('Authorization');

    if (authToken) {
        const client = new OAuth2Client(GOOGLE_CLIENT_ID);
        async function verify() {
            const ticket = await client.verifyIdToken({
                idToken: authToken,
                audience: GOOGLE_CLIENT_ID,
            });
            const payload = ticket.getPayload();
            const userid = payload['sub'];
            // If request specified a G Suite domain:
            //const domain = payload['hd'];

            // Save the Google userid for other methods to use
            res.locals.googleUserId = userid;
            isAuthenticated = true;
        }
        verify().catch(console.error);
    }

    if (isAuthenticated) {
        next();
    } else {
        console.log(`Authentication failed for ${request.ip}`);
        response.status(401).json({
            status: 401,
            message: 'Authentication failed.'
        })
    }
});

// Register the routes
// Note: This must be called before registering 404 not found handler
routes(app);

// Respond with a 404 error if the route is not found.
app.use(function(req, res) {
    res.status(404).send({
        status: 404,
        url: req.originalUrl,
        method: req.method,
        message: 'No endpoint exists with the requested path.'
    })
});

// Start listening for requests on the defined port
var server = app.listen(PORT);
console.log(`RESTful API server started on: ${PORT}`);

pool.query('SELECT 1 + 1 AS solution', function (err, rows, fields) {
    if (err) 
        throw err
    console.log('The solution is: ', rows[0].solution)
})

function quit() {
    console.log('Shutting down...');
    server.close();
    pool.end(function (err) {
        if (err) throw err;
    });
    process.exit(0);
}
