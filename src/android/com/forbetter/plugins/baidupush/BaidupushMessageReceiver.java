package com.forbetter.plugins.baidupush;/* 
 * 文件名:BaidupushMessageReceiver
 * 描述:
 * 修改人:li.chen
 * 修改时间:2016/12/16
 * 跟踪单号：
 * 修改单号：
 * 修改内容：
 */
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import com.baidu.android.pushservice.PushMessageReceiver;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Push消息处理receiver。请编写您需要的回调函数， 一般来说： onBind是必须的，用来处理startWork返回值；
 * onMessage用来接收透传消息； onSetTags、onDelTags、onListTags是tag相关操作的回调；
 * onNotificationClicked在通知被点击时回调； onUnbind是stopWork接口的返回值回调
 * <p>
 * 返回值中的errorCode，解释如下：
 * 0 - Success
 * 10001 - Network Problem
 * 10101  Integrate Check Error
 * 30600 - Internal Server Error
 * 30601 - Method Not Allowed
 * 30602 - Request Params Not Valid
 * 30603 - Authentication Failed
 * 30604 - Quota Use Up Payment Required
 * 30605 -Data Required Not Found
 * 30606 - Request Time Expires Timeout
 * 30607 - Channel Token Timeout
 * 30608 - Bind Relation Not Found
 * 30609 - Bind Number Too Many
 *
 * @author Chen Li
 * @version 0.0.1
 */
public class BaidupushMessageReceiver extends PushMessageReceiver {

    private static final String LOG_TAG = "BaidupushMessageReceiver";
    private static JSONObject cacheMsg = null;

    private enum CB_TYPE {
        onBind,
        onUnbind,
        onSetTags,
        onDelTags,
        onListTags,
        onMessage,
        onNotificationClicked,
        onNotificationArrived
    }

    /**
     * 调用PushManager.startWork后，sdk将对push
     * server发起绑定请求，这个过程是异步的。绑定请求的结果通过onBind返回。 如果您需要用单播推送，需要把这里获取的channel
     * id和user id上传到应用server中，再调用server接口用channel id和user id给单个手机或者用户推送。
     *
     * @param context   BroadcastReceiver的执行Context
     * @param errorCode 绑定接口返回值，0 - 成功
     * @param appId     应用id。errorCode非0时为null
     * @param userId    应用user id。errorCode非0时为null
     * @param channelId 应用channel id。errorCode非0时为null
     * @param requestId 向服务端发起的请求id。在追查问题时有用；
     */
    @Override
    public void onBind(Context context, int errorCode, String appId, String userId, String channelId, String requestId) {
        LOG.d(LOG_TAG, "onBind [errorCode=" + errorCode + ",appId=" + appId +
                ",userId=" + userId + ",channelId=" + channelId + ",requestId=" + requestId + "]");
        try {
            JSONObject data = new JSONObject();
            data.put("errorCode", errorCode);
            data.put("appId", appId);
            data.put("userId", userId);
            data.put("channelId", channelId);
            data.put("requestId", requestId);
            data.put("deviceType", 3);
            JSONObject result = new JSONObject();
            result.put("data", data);
            result.put("type", CB_TYPE.onBind);
            if (cacheMsg != null) {
                result.put("pending", cacheMsg);
                cacheMsg = null;
            }
            sendPushData(context, result, BaidupushPlugin.StartWorkCallbackContext);
            sendPushData(context, result, BaidupushPlugin.ResumeWorkCallbackContext);
            BaidupushPlugin.StartWorkCallbackContext = null;
            BaidupushPlugin.ResumeWorkCallbackContext = null;
        } catch (JSONException e) {
            LOG.e(LOG_TAG, e.toString(), e);
        }

    }


    /**
     * PushManager.stopWork() 的回调函数。
     *
     * @param context   上下文
     * @param errorCode 错误码。0表示从云推送解绑定成功；非0表示失败。
     * @param requestId 分配给对云推送的请求的id
     */
    @Override
    public void onUnbind(Context context, int errorCode, String requestId) {
        LOG.d(LOG_TAG, "onUnbind [errorCode=" + errorCode + ",requestId=" + requestId + "]");
        try {
            JSONObject data = new JSONObject();
            data.put("errorCode", errorCode);
            setStringData(data, "requestId", requestId);
            JSONObject result = new JSONObject();
            result.put("data", data);
            result.put("type", CB_TYPE.onUnbind);
            sendPushData(context, result, BaidupushPlugin.StopWorkCallbackContext);
            BaidupushPlugin.StopWorkCallbackContext = null;
        } catch (JSONException e) {
            LOG.e(LOG_TAG, e.toString());
        }
    }


    /**
     * setTags() 的回调函数。
     *
     * @param context     上下文
     * @param errorCode   错误码。0表示某些tag已经设置成功；非0表示所有tag的设置均失败。
     * @param successTags 设置成功的tag
     * @param failTags    设置失败的tag
     * @param requestId   分配给对云推送的请求的id
     */
    @Override
    public void onSetTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {

    }

    /**
     * delTags() 的回调函数。
     *
     * @param context     上下文
     * @param errorCode   错误码。0表示某些tag已经删除成功；非0表示所有tag均删除失败。
     * @param successTags 成功删除的tag
     * @param failTags    删除失败的tag
     * @param requestId   分配给对云推送的请求的id
     */
    @Override
    public void onDelTags(Context context, int errorCode, List<String> successTags, List<String> failTags, String requestId) {

    }

    /**
     * listTags() 的回调函数。
     *
     * @param context   上下文
     * @param errorCode 错误码。0表示列举tag成功；非0表示失败。
     * @param tags      当前应用设置的所有tag。
     * @param requestId 分配给对云推送的请求的id
     */
    @Override
    public void onListTags(Context context, int errorCode, List<String> tags, String requestId) {

    }

    /**
     * 接收透传消息的函数。
     *
     * @param context             上下文
     * @param message             推送的消息
     * @param customContentString 自定义内容,为空或者json字符串
     */
    @Override
    public void onMessage(Context context, String message, String customContentString) {
        LOG.d(LOG_TAG, "onMessage [message=" + message + ",customContentString=" + customContentString + "]");
        try {
            JSONObject data;
            if (customContentString != null && !"".equals(customContentString.trim())) {
                data = new JSONObject(customContentString);
            } else {
                data = new JSONObject();
            }
            setStringData(data, "message", message);
            JSONObject result = new JSONObject();
            result.put("data", data);
            result.put("type", CB_TYPE.onMessage);
            sendPushData(context, result, BaidupushPlugin.MessageArriveCallbackContext);
        } catch (JSONException e) {
            LOG.e(LOG_TAG, e.toString(), e);
        }
    }

    /**
     * 接收通知点击的函数。
     *
     * @param context             上下文
     * @param title               推送的通知的标题
     * @param description         推送的通知的描述
     * @param customContentString 自定义内容，为空或者json字符串
     */
    @Override
    public void onNotificationClicked(Context context, String title, String description, String customContentString) {
        LOG.d(LOG_TAG, "onNotificationClicked [title=" + title + ",description=" + description +
                ",customContentString=" + customContentString + "]");
        try {
            JSONObject data;
            if (customContentString != null && !"".equals(customContentString)) {
                data = new JSONObject(customContentString);
            } else {
                data = new JSONObject();
            }
            setStringData(data, "title", title);
            setStringData(data, "description", description);
            JSONObject result = new JSONObject();
            result.put("data", data);
            result.put("type", CB_TYPE.onNotificationClicked);
            if (isAppExist(context)) {
                sendPushData(context, result, BaidupushPlugin.NotificationClickCallbackContext);
            } else {
                startApp(context);
                cacheMsg = result;
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }

    }


    /**
     * 接收通知到达的函数。
     *
     * @param context             上下文
     * @param title               推送的通知的标题
     * @param description         推送的通知的描述
     * @param customContentString 自定义内容，为空或者json字符串
     */
    @Override
    public void onNotificationArrived(Context context, String title, String description, String customContentString) {
        LOG.d(LOG_TAG, "onNotificationArrived [title=" + title + ",description=" + description +
                ",customContentString=" + customContentString + "]");
        try {
            JSONObject data;
            if (customContentString != null && !"".equals(customContentString)) {
                data = new JSONObject(customContentString);
            } else {
                data = new JSONObject();
            }
            setStringData(data, "title", title);
            setStringData(data, "description", description);
            JSONObject result = new JSONObject();
            result.put("data", data);
            result.put("type", CB_TYPE.onNotificationArrived);
            sendPushData(context, result, BaidupushPlugin.NotificationArriveCallbackContext);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
        }
    }

    /*
     * 接收推送消息，并返回前端
     */
    private void sendPushData(Context context, JSONObject object, CallbackContext callbackContext) {
        if (context != null && isAppExist(context)) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, object);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        }
    }

    /*
     * 判断应用是否已经启动
     *
     */
    private boolean isAppAlive(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processInfos = activityManager.getRunningAppProcesses();
        String packageName = context.getPackageName();
        for (RunningAppProcessInfo processInfo : processInfos) {
            if (processInfo.processName.equals(packageName) && processInfo.importance  == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                LOG.i(LOG_TAG, String.format("the %s is running ,isAppAlive return true", packageName));
                return true;
            }
        }
        LOG.i(LOG_TAG, String.format("the %s is not running, isAppAlive return false", packageName));
        return false;
    }
	
	private static boolean isAppExist(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);		
		List<RunningTaskInfo>  tasksInfo = activityManager.getRunningTasks(1);
		return tasksInfo.get(0).baseActivity.getClassName().contains("MainActivity");
    }

    /*
     * 填充对象，如果字符串为空，不填充
     */
    private void setStringData(JSONObject data, String name, String value) {
        try {
            if (value != null && !"".equals(value.trim())) {
                data.put(name, value);
            }
        } catch (JSONException e) {
            LOG.e(LOG_TAG, e.toString());
        }
    }

    /*
     * 启动应用
     */
    private void startApp(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(context.getPackageName());
        context.startActivity(intent);
    }

}
