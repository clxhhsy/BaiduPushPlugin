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
BaiduPush.prototype.stopWork = function (successCallback) {
    exec(successCallback, failureCallback, 'BaiduPush', 'stopWork', []);
};
BaiduPush.prototype.resumeWork = function (successCallback) {
    exec(successCallback, failureCallback, 'BaiduPush', 'resumeWork', []);
};
BaiduPush.prototype.listenNotificationClicked = function ( successCallback) {
    exec(successCallback, failureCallback, 'BaiduPush', 'listenNotificationClicked', []);
};
BaiduPush.prototype.listenMessage = function ( successCallback) {
    exec(successCallback, failureCallback, 'BaiduPush', 'listenMessage', []);
};
BaiduPush.prototype.listenNotificationArrived = function ( successCallback) {
    exec(successCallback, failureCallback, 'BaiduPush', 'listenNotificationArrived', []);
};

var baiduPush = new BaiduPush();
module.exports = baiduPush;