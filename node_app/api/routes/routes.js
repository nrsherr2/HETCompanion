const API_PATH = '/api';
const V1_PATH = API_PATH + '/v1'

module.exports = function(app) {
    
    // /api Endpoint
    var api = require('../controllers/api.js');
    app.route(API_PATH)
        .all(api.versions)


    // /api/v1 endpoint
    var v1 = require('../controllers/v1/index.js')
    app.route(V1_PATH)
        .all(v1.index)


    // /api/v1/save
    var v1Save = require('../controllers/v1/save.js')
    app.route(V1_PATH + '/save')
        .post(v1Save.save)

};