package com.n1.RemoteControl

import android.annotation.TargetApi
import android.app.*
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.RemoteViews
import com.zhouyou.http.EasyHttp
import com.zhouyou.http.callback.CallBack
import com.zhouyou.http.exception.ApiException
import kotlinx.android.synthetic.main.activity_controller.*
import org.json.JSONObject
import android.graphics.Color
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Vibrator
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat


class MainActivity : AppCompatActivity() {
    var host = ""
    lateinit var sp: SharedPreferences



    /**
     * 上:19
    下:20
    左:21
    右:22
    返回:4
    音量加:24
    音量减:25
    主界面:3
    菜单:82
    确认键:23
    电源:26
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_controller)

        sp = getSharedPreferences("remotecontrol", Context.MODE_PRIVATE)


        tv_ip.text = sp.getString("ip", "")
        var isVibrate = sp.getBoolean("vibrate", false)
        if (isVibrate) {
            btn_vibrate.setImageResource(R.drawable.vibrate_on)
        } else {
            btn_vibrate.setImageResource(R.drawable.vibrate_off)
        }
        btn_vibrate.setOnClickListener {
            isVibrate = !isVibrate
            if (isVibrate) {
                btn_vibrate.setImageResource(R.drawable.vibrate_on)
            } else {
                btn_vibrate.setImageResource(R.drawable.vibrate_off)
            }
            sp.edit().putBoolean("vibrate", isVibrate).commit()
        }


        btn_keyboard.setOnClickListener { startActivity(Intent(this,KeyBoardActivity::class.java)) }
        btn_back.setOnClickListener { sendCode(4) }
        btn_bottom.setOnClickListener { sendCode(20) }
        btn_top.setOnClickListener { sendCode(19) }
        btn_close.setOnClickListener { sendCode(26) }
        btn_home.setOnClickListener { sendCode(3) }
        btn_left.setOnClickListener { sendCode(21) }
        btn_right.setOnClickListener { sendCode(22) }
        btn_menu.setOnClickListener { sendCode(82) }
        btn_minus.setOnClickListener { sendCode(26) }
        btn_plus.setOnClickListener { sendCode(24) }
        btn_ok.setOnClickListener { sendCode(23) }
        btn_setting.setOnClickListener { onSetting() }
        btn_ip.setOnClickListener {
            var dialog: AlertDialog.Builder = AlertDialog.Builder(MainActivity@ this)
            var views = LayoutInflater.from(this).inflate(R.layout.view_input_dialog, null)
            views.findViewById<TextView>(R.id.btn_url_dialog)
                .setOnClickListener {
                    var sp = this.getSharedPreferences("remotecontrol", Context.MODE_PRIVATE)
                    var ip = views.findViewById<EditText>(R.id.et_url).text.trim()
                    sp.edit().putString("ip", "http://" + ip.toString()).commit()
                    tv_ip.text = "http://" + ip.toString()
                }
            dialog.setView(views)
            dialog.show()
        }
        setNotification()
    }

    private fun setNotification() {
        var remoteView = RemoteViews(packageName, R.layout.remote_view)

        remoteView.setOnClickPendingIntent(R.id.btn_n_top, createPendingIntent(R.id.btn_n_top))
        remoteView.setOnClickPendingIntent(
            R.id.btn_n_bottom,
            createPendingIntent(R.id.btn_n_bottom)
        )
        remoteView.setOnClickPendingIntent(R.id.btn_n_left, createPendingIntent(R.id.btn_n_left))
        remoteView.setOnClickPendingIntent(R.id.btn_n_right, createPendingIntent(R.id.btn_n_right))
        remoteView.setOnClickPendingIntent(R.id.btn_n_ok, createPendingIntent(R.id.btn_n_ok))
        remoteView.setOnClickPendingIntent(R.id.btn_n_back, createPendingIntent(R.id.btn_n_back))
        var notificationB = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "nc")
        } else {
            Notification.Builder(this)
        }
        notificationB.setContent(remoteView)
            .setOngoing(true)
            .setDefaults(NotificationCompat.FLAG_ONLY_ALERT_ONCE)
            .setPriority(Notification.PRIORITY_HIGH)
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.mipmap.ic_launcher)
//            .setContentIntent(
//                PendingIntent.getBroadcast(
//                    this,
//                    2,
//                    Intent("action.view"),
//                    FLAG_UPDATE_CURRENT
//                )
//            )


        var nfm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel("nc", "安卓10a", NotificationManager.IMPORTANCE_MIN)

            channel.lightColor = Color.GREEN//小红点颜色
            channel.enableLights(false);
            channel.enableVibration(false);
            channel.setSound(null, null);
            channel.setShowBadge(false) //是否在久按桌面图标时显示此渠道的通知
            nfm.createNotificationChannel(channel)
        }
        nfm.notify(100, notificationB.build())
    }

    private fun createPendingIntent(code: Int): PendingIntent? {
        var pendingIntent: PendingIntent? = null
        var intent = Intent()
        intent.setClass(this, NotificationBroadcast::class.java)
        when (code) {
            R.id.btn_n_top -> {
                intent.setAction("top")
                pendingIntent =
                    PendingIntent.getBroadcast(this, 1, intent, 0)
            }
            R.id.btn_n_bottom -> {
                intent.setAction("bottom")
                pendingIntent =
                    PendingIntent.getBroadcast(this, 1, intent, 0)
            }
            R.id.btn_n_left -> {

                intent.setAction("left")
                pendingIntent =
                    PendingIntent.getBroadcast(this, 1, intent, 0)
            }
            R.id.btn_n_right -> {
                intent.setAction("right")
                pendingIntent =
                    PendingIntent.getBroadcast(this, 1, intent, 0)
            }
            R.id.btn_n_ok -> {
                intent.setAction("ok")
                pendingIntent =
                    PendingIntent.getBroadcast(this, 1, intent, 0)
            }
            R.id.btn_n_back -> {
                intent.setAction("back")
                pendingIntent =
                    PendingIntent.getBroadcast(this, 1, intent, 0)
            }
        }
        return pendingIntent
    }

    private fun onSetting() {
        if (TextUtils.isEmpty(tv_ip.text)) {
            Toast.makeText(this, "请输入ip", Toast.LENGTH_LONG)
            return
        }
        host = tv_ip.text.toString()
        EasyHttp.post("/v1/action")
            .baseUrl(host.plus(":8080"))
            .upJson(createJsonSetting())
            .execute(object : CallBack<String>() {
                override fun onSuccess(t: String?) {
                    //To change body of created functions use File | Settings | File Templates.
//                    Vibrator vibrator = (Vibrator)this.getSystemService(this.VIBRATOR_SERVICE);
//vibrator.vibrate(1000);
                    if (sp.getBoolean("vibrate", false)) {
                        var vibrator =
                            this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                        vibrator.vibrate(100)
                    }

                }

                override fun onError(e: ApiException?) {
                    //To change body of created functions use File | Settings | File Templates.
                }

                override fun onStart() {
                    //To change body of created functions use File | Settings | File Templates.

                }

                override fun onCompleted() {
                    //To change body of created functions use File | Settings | File Templates.
                }

            })
    }

    private fun createJsonSetting(): String? {
        var jsonObject: JSONObject = JSONObject()
        jsonObject.put("action", "setting")
        return jsonObject.toString()
    }

    private fun sendCode(code: Int) {

        if (TextUtils.isEmpty(tv_ip.text)) {
            Toast.makeText(this, "请输入ip", Toast.LENGTH_LONG)
            return
        }
        host = tv_ip.text.toString()
        EasyHttp.post("/v1/keyevent")
            .baseUrl(host.plus(":8080"))
            .upJson(createJson(code))
            .execute(object : CallBack<String>() {
                override fun onError(e: ApiException?) {
                }

                override fun onStart() {
                    //To change body of created functions use File | Settings | File Templates.

                }

                override fun onCompleted() {
                    //To change body of created functions use File | Settings | File Templates.
                }

                override fun onSuccess(t: String?) {
                    if (sp.getBoolean("vibrate", false)) {
                        var vibrator =
                            this@MainActivity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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
