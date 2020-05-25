package com.n1.RemoteControl

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Vibrator
import android.text.TextUtils
import android.util.Log
import com.zhouyou.http.EasyHttp
import com.zhouyou.http.callback.CallBack
import com.zhouyou.http.exception.ApiException
import org.json.JSONObject

/**
 * 版权：易金 版权所有
 * <p>
 * 作者：suntinghui
 * <p>
 * 创建日期：2020/5/25 0025 14:29
 * <p>
 * 描述：
 * <p>
 * 修订历史：
 */
class NotificationBroadcast : BroadcastReceiver() {
    private lateinit var context: Context
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            this.context = context
        }
        var action = intent!!.action;
        Log.d("onReceive", action.toString())
        when (action) {
            "top" -> {
                sendCode(19)
            }
            "bottom" -> {

                sendCode(20)
            }
            "left" -> {

                sendCode(21)
            }
            "right" -> {

                sendCode(22)
            }
            "ok" -> {

                sendCode(23)
            }
            "back" -> {

                sendCode(4)
            }
        }


    }


    private fun sendCode(code: Int) {
        var sp = context.getSharedPreferences("remotecontrol", Context.MODE_PRIVATE)
        var host = sp.getString("ip", "")
        if (TextUtils.isEmpty(host)) {
            return
        }
        EasyHttp.post("/v1/keyevent")
            .baseUrl(host.plus(":8080"))
            .upJson(createJson(code))
            .execute(object : CallBack<String>() {
                override fun onError(e: ApiException?) {
                    //To change body of created functions use File | Settings | File Templates.
                }

                override fun onStart() {
                    //To change body of created functions use File | Settings | File Templates.
                }

                override fun onCompleted() {
                    //To change body of created functions use File | Settings | File Templates.
                }

                override fun onSuccess(t: String?) {
                    if (sp.getBoolean("vibrate", false)) {
                        var vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(100)
                    }


                }
            })
    }

    private fun createJson(code: Int): String? {
        var jb: JSONObject = JSONObject();
        jb.put("keycode", code)
        jb.put("longclick", false)
        return jb.toString();
    }

}