exports.save = function(request, response) {
    // TODO: validate/save the data
    response.status(201).json({
        status: 201,
        message: 'Successfully saved'
    });
};