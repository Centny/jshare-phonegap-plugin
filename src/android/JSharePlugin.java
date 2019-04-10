package io.snows.cordova.jshare;

import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Method;

import cn.jiguang.share.android.api.AuthListener;
import cn.jiguang.share.android.api.JShareInterface;
import cn.jiguang.share.android.api.Platform;
import cn.jiguang.share.android.model.AccessTokenInfo;
import cn.jiguang.share.android.model.BaseResponseInfo;
import cn.jiguang.share.android.utils.Logger;
import cn.jiguang.share.wechat.Wechat;


public class JSharePlugin extends CordovaPlugin {
    private static final String TAG = JSharePlugin.class.getSimpleName();

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        Logger.dd("type",Wechat.Name);
    }

    @Override
    public boolean execute(final String action, final JSONArray data, final CallbackContext callbackContext){
        cordova.getThreadPool().execute(() -> {
            try {
                Method method = JSharePlugin.class.getDeclaredMethod(action, JSONArray.class, CallbackContext.class);
                method.invoke(JSharePlugin.this, data, callbackContext);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
        });
        return true;
    }

    void authorize(JSONArray data, CallbackContext callback) throws Exception {
        if (data.length() < 1) {
            callback.error("platform argument is required");
            return;
        }
        String platform = data.get(0).toString();
        Logger.dd(TAG, "authorize by " + platform + ", data:" + data);
        JShareInterface.authorize(platform, new AuthListener() {
            @Override
            public void onComplete(Platform platform, int i, BaseResponseInfo data) {
                JSONObject res = new JSONObject();
                try {
                    res.put("code", 0);
                    Logger.dd(TAG, "onComplete:" + platform + ",data:" + data);
                    if (data instanceof AccessTokenInfo) {        //授权信息
                        String token = ((AccessTokenInfo) data).getToken();//token
                        long expiration = ((AccessTokenInfo) data).getExpiresIn();//token有效时间，时间戳
                        String refresh_token = ((AccessTokenInfo) data).getRefeshToken();//refresh_token
                        String openid = ((AccessTokenInfo) data).getOpenid();//openid
                        //授权原始数据，开发者可自行处理
                        String originData = data.getOriginData();
                        Logger.dd(TAG, "openid:" + openid + ",token:" + token + ",expiration:" + expiration + ",refresh_token:" + refresh_token);
                        Logger.dd(TAG, "originData:" + originData);
                        res.put("token", token);
                        res.put("expiration", expiration);
                        res.put("refresh_token", refresh_token);
                        res.put("openid", openid);
                        res.put("origin_data", originData);
                    }
                } catch (Exception e) {
                }
                callback.success(res);
            }

            @Override
            public void onError(Platform platform, int i, int i1, Throwable throwable) {
                JSONObject res = new JSONObject();
                try {
                    res.put("code", 10);
                    res.put("error", throwable.getMessage());
                } catch (Exception e) {
                }
                callback.success(res);
            }

            @Override
            public void onCancel(Platform platform, int i) {
                JSONObject res = new JSONObject();
                try {
                    res.put("code", 20);
                } catch (Exception e) {
                }
                callback.success(res);
            }
        });
    }
}
