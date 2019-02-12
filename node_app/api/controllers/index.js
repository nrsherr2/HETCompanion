exports.index = function(request, response) {
    response.status(302).set('Location', 'api/');
};