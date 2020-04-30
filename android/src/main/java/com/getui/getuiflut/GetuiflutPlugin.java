package com.getui.getuiflut;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.os.Handler;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.igexin.sdk.PushManager;
import com.igexin.sdk.Tag;
import com.igexin.sdk.message.GTNotificationMessage;
import com.igexin.sdk.message.GTTransmitMessage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * GetuiflutPlugin
 */
public class GetuiflutPlugin extends BroadcastReceiver implements MethodCallHandler, ActivityAware, FlutterPlugin, PluginRegistry.NewIntentListener {

    private static final String TAG = "GetuiflutPlugin";
    private static final int FLUTTER_CALL_BACK_CID = 1;
    private static final int FLUTTER_CALL_BACK_MSG = 2;

    private MethodChannel channel;
    private Activity mainActivity;
    private Context applicationContext;


    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        GetuiflutPlugin instance = new GetuiflutPlugin();
        instance.setActivity(registrar.activity());
        registrar.addNewIntentListener(instance);
        instance.onAttachedToEngine(registrar.context(), registrar.messenger());
    }

    public static void setPluginRegistrant(PluginRegistry.PluginRegistrantCallback callback) {
        FlutterIntentService.setPluginRegistrant(callback);

    }

    @Override
    public void onAttachedToEngine(FlutterPlugin.FlutterPluginBinding binding) {
        onAttachedToEngine(binding.getApplicationContext(), binding.getBinaryMessenger());
    }

    @Override
    public void onDetachedFromEngine(FlutterPluginBinding binding) {

    }


    private void onAttachedToEngine(Context context, BinaryMessenger binaryMessenger) {
        this.applicationContext = context;
        channel = new MethodChannel(binaryMessenger, "getuiflut");
        final MethodChannel backgroundCallbackChannel =
                new MethodChannel(binaryMessenger, "com.getui.getuiflut/getui_messaging_background");

        channel.setMethodCallHandler(this);
        backgroundCallbackChannel.setMethodCallHandler(this);
        FlutterIntentService.setBackgroundChannel(backgroundCallbackChannel);

        // Register broadcast receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FlutterIntentService.ACTION_REMOTE_MESSAGE);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(applicationContext);
        manager.registerReceiver(this, intentFilter);
    }

    private void setActivity(Activity flutterActivity) {
        this.mainActivity = flutterActivity;
    }


    @Override
    public void onAttachedToActivity(ActivityPluginBinding binding) {
        binding.addOnNewIntentListener(this);
        this.mainActivity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        this.mainActivity = null;

    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
        binding.addOnNewIntentListener(this);
        this.mainActivity = binding.getActivity();

    }

    @Override
    public void onDetachedFromActivity() {
        this.mainActivity = null;

    }

    @Override
    public boolean onNewIntent(Intent intent) {
//      boolean res = sendMessageFromIntent("onResume", intent);
//      if (res && mainActivity != null) {
//        mainActivity.setIntent(intent);
//      }
//      return res;
        return false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        if (action.equals(FlutterIntentService.ACTION_REMOTE_MESSAGE)) {
            String method = intent.getStringExtra(FlutterIntentService.EXTRA_REMOTE_METHOD);
            if ("onReceiveClientId".equals(method)) {
                String msg = intent.getStringExtra(FlutterIntentService.EXTRA_REMOTE_MESSAGE);
                channel.invokeMethod("onReceiveClientId", msg);
                Log.d("flutterHandler", "onReceiveClientId >>> " + msg);
            } else if ("onReceiveOnlineState".equals(method)) {
                String msg = intent.getStringExtra(FlutterIntentService.EXTRA_REMOTE_MESSAGE);
                channel.invokeMethod("onReceiveOnlineState", msg);
                Log.d("flutterHandler", "onReceiveOnlineState >>> " + msg);
            } else if ("onReceiveMessageData".equals(method)) {
                GTTransmitMessage msg = (GTTransmitMessage) intent.getSerializableExtra(FlutterIntentService.EXTRA_REMOTE_MESSAGE);
                final Map<String, Object> payload = new HashMap<>();
                payload.put("messageId", msg.getMessageId());
                payload.put("payload", new String(msg.getPayload()));
                payload.put("payloadId", msg.getPayloadId());
                payload.put("taskId", msg.getTaskId());
                channel.invokeMethod("onReceiveMessageData", payload);
                Log.d("flutterHandler", "onReceiveMessageData >>> " + payload);
            } else if ("onNotificationMessageArrived".equals(method)) {
                GTNotificationMessage msg = (GTNotificationMessage) intent.getSerializableExtra(FlutterIntentService.EXTRA_REMOTE_MESSAGE);
                Map<String, Object> notification = new HashMap<>();
                notification.put("messageId", msg.getMessageId());
                notification.put("taskId", msg.getTaskId());
                notification.put("title", msg.getTitle());
                notification.put("content", msg.getContent());
                channel.invokeMethod("onNotificationMessageArrived", notification);
                Log.d("flutterHandler", "onNotificationMessageArrived >>> " + notification);
            } else if ("onNotificationMessageClicked".equals(method)) {
                GTNotificationMessage msg = (GTNotificationMessage) intent.getSerializableExtra(FlutterIntentService.EXTRA_REMOTE_MESSAGE);
                Map<String, Object> notification = new HashMap<>();
                notification.put("messageId", msg.getMessageId());
                notification.put("taskId", msg.getTaskId());
                notification.put("title", msg.getTitle());
                notification.put("content", msg.getContent());
                channel.invokeMethod("onNotificationMessageClicked", notification);
                Log.d("flutterHandler", "onNotificationMessageClicked >>> " + notification);
            } else {
                Log.d(TAG, "default Message type...");
            }

        }
    }


    enum MessageType {
        Default,
        onReceiveMessageData,
        onNotificationMessageArrived,
        onNotificationMessageClicked
    }

    enum StateType {
        Default,
        onReceiveClientId,
        onReceiveOnlineState
    }


    public final Map<Integer, Result> callbackMap = new HashMap<>();


    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (call.method.equals("initGetuiPush")) {
            long setupCallbackHandle = 0;
            long backgroundMessageHandle = 0;
            Map<String, Long> callbacks = ((Map<String, Long>) call.arguments);
            setupCallbackHandle = callbacks.get("setupHandle");
            backgroundMessageHandle = callbacks.get("backgroundHandle");
            FlutterIntentService.setBackgroundSetupHandle(mainActivity, setupCallbackHandle);
            FlutterIntentService.startBackgroundIsolate(mainActivity, setupCallbackHandle);
            FlutterIntentService.setBackgroundMessageHandle(
                    mainActivity, backgroundMessageHandle);
            initGtSdk();
        } else if (call.method.equals("GetuiService#initialized")) {
            FlutterIntentService.onInitialized();
            result.success(true);
        } else if (call.method.equals("getClientId")) {
            result.success(getClientId());
        } else if (call.method.equals("resume")) {
            resume();
        } else if (call.method.equals("stopPush")) {
            stopPush();
        } else if (call.method.equals("bindAlias")) {
            Log.d(TAG, "bindAlias:" + call.argument("alias").toString());
            bindAlias(call.argument("alias").toString(), "");
        } else if (call.method.equals("unbindAlias")) {
            Log.d(TAG, "unbindAlias:" + call.argument("alias").toString());
            unbindAlias(call.argument("alias").toString(), "");
        } else if (call.method.equals("setTag")) {
            Log.d(TAG, "tags:" + (ArrayList<String>) call.argument("tags"));
            setTag((ArrayList<String>) call.argument("tags"));
        } else if (call.method.equals("onActivityCreate")) {
            Log.d(TAG, "do onActivityCreate");
            onActivityCreate();
        } else {
            result.notImplemented();
        }
    }

    private void initGtSdk() {
        Log.d(TAG, "init getui sdk...test");
        PushManager.getInstance().initialize(applicationContext, FlutterPushService.class);
        PushManager.getInstance().registerPushIntentService(applicationContext, FlutterIntentService.class);
    }


    private void onActivityCreate() {
        try {
            Method method = PushManager.class.getDeclaredMethod("registerPushActivity", Context.class, Class.class);
            method.setAccessible(true);
            method.invoke(PushManager.getInstance(), applicationContext, GetuiPluginActivity.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private String getClientId() {
        Log.d(TAG, "get client id");
        return PushManager.getInstance().getClientid(applicationContext);
    }

    private void resume() {
        Log.d(TAG, "resume push service");
        PushManager.getInstance().turnOnPush(applicationContext);
    }

    private void stopPush() {
        Log.d(TAG, "stop push service");
        PushManager.getInstance().turnOffPush(applicationContext);
    }

    /**
     * 绑定别名功能:后台可以根据别名进行推送
     *
     * @param alias 别名字符串
     * @param aSn   绑定序列码, Android中无效，仅在iOS有效
     */
    public void bindAlias(String alias, String aSn) {
        PushManager.getInstance().bindAlias(applicationContext, alias);
    }

    /**
     * 取消绑定别名功能
     *
     * @param alias 别名字符串
     * @param aSn   绑定序列码, Android中无效，仅在iOS有效
     */
    public void unbindAlias(String alias, String aSn) {
        PushManager.getInstance().unBindAlias(applicationContext, alias, false);
    }

    /**
     * 给用户打标签 , 后台可以根据标签进行推送
     *
     * @param tags 别名数组
     */
    public void setTag(List<String> tags) {
        if (tags == null || tags.size() == 0) {
            return;
        }

        Tag[] tagArray = new Tag[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            Tag tag = new Tag();
            tag.setName(tags.get(i));
            tagArray[i] = tag;
        }

        PushManager.getInstance().setTag(applicationContext, tagArray, "setTag");
    }
}
