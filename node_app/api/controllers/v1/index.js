// Respond with a list of available endpoints
exports.index = function(request, response) {
    response.json({
        save: "/api/v1/save"
    });
};
