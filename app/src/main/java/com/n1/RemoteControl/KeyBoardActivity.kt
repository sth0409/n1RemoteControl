package com.n1.RemoteControl

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import kotlinx.android.synthetic.main.activity_key_board.*
import android.view.KeyEvent.KEYCODE_DEL
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.KeyEvent
import android.view.View
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Vibrator
import android.text.TextUtils
import android.widget.Toast
import com.zhouyou.http.EasyHttp
import com.zhouyou.http.callback.CallBack
import com.zhouyou.http.exception.ApiException
import kotlinx.android.synthetic.main.activity_controller.*
import org.json.JSONObject


class KeyBoardActivity : AppCompatActivity() {
    var host = ""
    lateinit var sp: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_key_board)
        sp = getSharedPreferences("remotecontrol", Context.MODE_PRIVATE)

//        AllCharacterKeyboardUtil(this)
        et.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
                if (event!!.getAction() == KeyEvent.ACTION_DOWN) {
                    sendCode(event!!.keyCode)
                }

                return false
            }

        })
        et.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty()) {
                    checkLast(getLast(s))
                }
            }
        })


    }

    private fun checkLast(last: Char) {
        var lastInt: Int = last as Int
        if (lastInt in 48..57) {
            //数字
            sendCode(lastInt - 41)
        } else if (lastInt in 48..57 || lastInt in 97..122) {
            //字母
            var lastIntUpperCase: Int = last.toUpperCase() as Int
            sendCode(lastIntUpperCase - 36)
        }
    }

    private fun getLast(s: CharSequence?): Char {

        return s!!.last()


    }

    private fun sendCode(code: Int) {

        if (TextUtils.isEmpty(sp.getString("ip", ""))) {
            Toast.makeText(this, "请输入ip", Toast.LENGTH_LONG)
            return
        }
        host = sp.getString("ip", "").toString()
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
                            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
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
