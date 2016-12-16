package com.forbetter.plugins.baidupush;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.LOG;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 百度推送插件类
 * <p>
 * Created On 2016年-12月-16日 10:24
 *
 * @author Chen Li
 */

public class BaidupushPlugin extends CordovaPlugin {

    private static final String LOG_TAG = "BaiduPushPlugin";

    private ExecutorService threadPool = cordova.getThreadPool();

    /* JS回调上下文接口 */
    public static CallbackContext StartWorkCallbackContext = null;
    public static CallbackContext StopWorkCallbackContext = null;
    public static CallbackContext ResumeWorkCallbackContext = null;
    public static CallbackContext NotificationClickCallbackContext = null;
    public static CallbackContext NotificationArriveCallbackContext = null;
    public static CallbackContext MessageArriveCallbackContext = null;

    private final static List<String> methodList = Arrays.asList(
            "startWork",
            "stopWork",
            "resumeWork",
            "listenNotificationClicked",
            "listenMessage",
            "listenNotificationArrived"
    );

    public static BaidupushPlugin instance;

    public BaidupushPlugin() {
        instance = this;
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        return handlerExecute(action, args, callbackContext);
    }

    private boolean handlerExecute(String action, JSONArray args, CallbackContext callbackContext) {
        if (!methodList.contains(action))
            return false;
        threadPool.execute(() -> {
            try {
                Method method = BaidupushPlugin.class.getDeclaredMethod(action, JSONArray.class, CallbackContext.class);
                method.invoke(BaidupushPlugin.this, args, callbackContext);

            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                LOG.e(LOG_TAG, e.toString());
            }
        });
        return true;
    }

    void startWork(JSONArray params, CallbackContext callbackContext) {
        try {
            StartWorkCallbackContext = callbackContext;
            String apiKey = params.getString(0);
            PushManager.startWork(cordova.getActivity().getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY, apiKey);
        } catch (JSONException e) {
            LOG.e(LOG_TAG, e.toString());
        }
    }

    void stopWork(JSONArray params, CallbackContext callbackContext) {
        StopWorkCallbackContext = callbackContext;
        PushManager.stopWork(cordova.getActivity().getApplicationContext());
    }

    void resumeWork(JSONArray params, CallbackContext callbackContext) {
        ResumeWorkCallbackContext = callbackContext;
        PushManager.resumeWork(cordova.getActivity().getApplicationContext());
    }

    void listenNotificationClicked(JSONArray params, CallbackContext callbackContext) {
        NotificationClickCallbackContext = callbackContext;
        holdCallback(callbackContext);
    }

    void listenMessage(JSONArray params, CallbackContext callbackContext) {
        MessageArriveCallbackContext = callbackContext;
        holdCallback(callbackContext);
    }

    void listenNotificationArrived(JSONArray params, CallbackContext callbackContext) {
        NotificationArriveCallbackContext = callbackContext;
        holdCallback(callbackContext);
    }

    private void holdCallback(CallbackContext callbackContext) {
        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LOG.i(LOG_TAG, "BaiduPushPlugin destory");
        instance = null;
        resetCallbackContext();
    }

    private void resetCallbackContext() {
        releaseCallbackContext(NotificationClickCallbackContext);
        releaseCallbackContext(MessageArriveCallbackContext);
        releaseCallbackContext(NotificationArriveCallbackContext);
        StartWorkCallbackContext = null;
        StopWorkCallbackContext = null;
        ResumeWorkCallbackContext = null;
        NotificationClickCallbackContext = null;
        NotificationArriveCallbackContext = null;
        MessageArriveCallbackContext = null;
    }

    private void releaseCallbackContext(CallbackContext callbackContext) {
        if (callbackContext != null) {
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(false);
            callbackContext.sendPluginResult(pluginResult);
        }
    }
}
