var exec = require('cordova/exec');

function BaiduPush() {
}

function failureCallback() {
    console.log('baidupush failure');
}

BaiduPush.prototype.startWork = function (apiKey, successCallback) {
    console.log('baidupush startWork with apikey : ' + apiKey);
    exec(successCallback, failureCallback, 'BaiduPush', 'startWork', [apiKey]);
};

var baiduPush = new BaiduPush();
module.exports = baiduPush;