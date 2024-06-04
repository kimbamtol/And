package com.and

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.and.setting.Setting
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var loginInfo: android.content.SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        setContentView(R.layout.activity_splash)
        splashScreen.setKeepOnScreenCondition { true }

        // Kakao SDK 초기화
        KakaoSdk.init(this, this.getString(R.string.kakao_app_key))

        loginInfo = getSharedPreferences("setting", MODE_PRIVATE)

        val loginId = loginInfo.getString("email", null)
        if (loginId != null) {
            Setting.email = loginId
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}