exports.index = function(request, response) {
    response.status(302).header('Location', 'api/').end();
};
