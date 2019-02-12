// List all api versions and their route
exports.versions = function(request, response) {
    response.json({
        versions: {
            v1: '/api/v1'
        }
    });
};
