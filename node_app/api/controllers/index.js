// Redirect requests to /api to get a list of API versions
exports.index = function(request, response) {
    response.status(302).header('Location', 'api/').end();
};
