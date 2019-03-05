// Dependency Imports
const express = require('express');                   // Expressjs handles rest requests
const helmet = require('helmet');                     // Remove/set security related HTTP headers
const bodyParser = require('body-parser');            // For parsing JSON requests
const {OAuth2Client} = require('google-auth-library');// Google API for authenticating tokens

// Local Imports
var routes = require('./api/routes/routes.js');
const ClientException = require('./api/exceptions/ClientException.js');
const ServerException = require('./api/exceptions/ServerException.js');

// Config
const PORT = 8080;
const GOOGLE_CLIENT_ID = '594190355836-gtka26ga090eq4sjuo6d3fjudb08e09a.apps.googleusercontent.com';

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
        request.locals = {}; //Clear out and define locals variable space
        request.locals.googleUserId = userid;
        console.log(`Received UserID ${userid}`);
        next();
      }

      verify().catch(error => {
        console.log(`Authentication failed for ${request.ip}`);
        response.status(401).json({
          status: 401,
          message: 'Authentication failed.'
        });
      });
    } else {
      // else there's no authToken so return 401
      console.log(`Authentication Missing for ${request.ip}`);
      response.status(401).json({
        status: 401,
        message: 'Authentication failed.'
      });
    }
});

// Register the routes
// Note: This must be called before registering 404 not found handler
routes(app);

// Respond with a 404 error if the route is not found.
app.use(function(request, response) {
    response.status(404).send({
        status: 404,
        url: request.originalUrl,
        method: request.method,
        message: 'No endpoint exists with the requested path.'
    })
});

// Client Error Catching
app.use(function (err, request, response, next) {
    if (err instanceof ClientException) {
        response.status(400).json({
            status: 400,
            url: request.originalUrl,
            method: request.method,
            message: err.message
        });
    } else {
        next(err);
    }
});

// Server Error Catching
app.use(function (err, request, response, next) {
    if (err instanceof ServerException) {
        response.status(500).json({
            status: 500,
            url: request.originalUrl,
            method: request.method,
            message: err.message
        });
    } else {
        next(err);
    }
});

// Error Catch-All
app.use(function (err, request, response, next) {
    console.error(err.stack);
    response.status(500).json({
        status: 500,
        url: request.originalUrl,
        method: request.method,
        message: 'An unhandled internal error has occurred.'
    });
});

// Start listening for requests on the defined port
var server = app.listen(PORT);
console.log(`RESTful API server started on: ${PORT}`);

// Quit the app: Stop accepting requests and close all database connections
function quit() {
    console.log('Shutting down...');
    server.close();
    pool.end(function (err) {
        if (err) throw err;
    });
    process.exit(0);
}
