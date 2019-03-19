const fs = require("fs");
const AWS = require('ibm-cos-sdk');

var secretsFile = JSON.parse(fs.readFileSync('api-key.json'));
var config = {
    endpoint: 'https://s3-api.us-geo.objectstorage.softlayer.net',
    apiKeyId: secretsFile.apiKey,
    ibmAuthEndpoint: 'https://iam.ng.bluemix.net/oidc/token',
    serviceInstanceId: secretsFile.resource_instance_id,
};

var cos = new AWS.S3(config);

function createTextFile(bucketName, itemName, fileText) {
    return cos.putObject({
        Bucket: bucketName, 
        Key: itemName, 
        Body: fileText
    }).promise()
}

function getBucketContents(bucketName, prefix) {
    console.log(`Retrieving bucket contents from: ${bucketName}`);
    return cos.listObjects({
        Bucket: bucketName,
        Prefix: prefix
    },).promise()
    .then((data) => {
        if (data != null && data.Contents != null) {
            // data.Contents[i].Key
            return data.Contents;
        }    
    })
    .catch((e) => {
        console.error(`ERROR: ${e.code} - ${e.message}\n`);
        return [];
    });
}

function getItem(bucketName, itemName) {
    console.log(`Retrieving item from bucket: ${bucketName}, key: ${itemName}`);
    return cos.getObject({
        Bucket: bucketName, 
        Key: itemName
    }).promise()
    .then((data) => {
        if (data != null) {
            console.log('File Contents: ' + Buffer.from(data.Body).toString());
        }    
    })
    .catch((e) => {
        console.error(`ERROR: ${e.code} - ${e.message}\n`);
    });
}

function deleteItem(bucketName, itemName) {
    console.log(`Deleting item: ${itemName}`);
    return cos.deleteObject({
        Bucket: bucketName,
        Key: itemName
    }).promise()
    .then(() =>{
        console.log(`Item: ${itemName} deleted!`);
    })
    .catch((e) => {
        console.error(`ERROR: ${e.code} - ${e.message}\n`);
    });
}


module.exports = {
    createTextFile: createTextFile,
    getBucketContents: getBucketContents
};

