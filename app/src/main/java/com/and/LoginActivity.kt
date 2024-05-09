package com.and

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.and.databinding.ActivityLoginBinding
import com.and.datamodel.UserDataModel
import com.and.setting.FBRef
import com.and.setting.Setting
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kakao.sdk.common.util.Utility
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // HashKey확인
        val keyHash = Utility.getKeyHash(this)
        Log.e("Key", "keyHash: $keyHash")

        // Kakao SDK 초기화
        KakaoSdk.init(this, this.getString(R.string.kakao_app_key))

        // 로그인 버튼 클릭 리스너 설정
        binding.btnStartKakaoLogin.setOnClickListener {
            kakaoLogin()
        }
    }

    private fun kakaoLogin() {
        // 카카오계정으로 로그인 공통 callback 구성
        val callback: (OAuthToken?, Throwable?) -> Unit = callback@{ token, error ->
            if (error != null) {
                if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                    return@callback // 로그인 취소 처리
                }
                setLogin(false) // 로그인 실패 처리
            } else if (token != null) {
                updateUIAndFinish() // 로그인 성공 후 UI 업데이트 및 MainActivity로 이동
            }
        }

        // 카카오톡이 설치되어 있으면 카카오톡으로 로그인, 그렇지 않으면 카카오계정으로 로그인 시도
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
            UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
        } else {
            UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
        }
    }

    private fun updateUIAndFinish() {
        setLogin(true) // 로그인 버튼을 숨김
        UserApiClient.instance.me { user, error1 ->
            if (error1 != null) {
                Log.e(ContentValues.TAG, "사용자 정보 요청 실패", error1)
                setLogin(false)
            } else if (user != null) {
                user.kakaoAccount?.email?.let { email ->
                    val myEmail = email.split(".")[0]
                    FBRef.userRef.child(myEmail).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                lifecycleScope.launch {
                                    val singInJob = async(Dispatchers.IO) {
                                        if (!snapshot.exists()) {
                                            val userInfo = UserDataModel(myEmail = email)
                                            FBRef.userRef.child(myEmail).child("userInfo")
                                                .setValue(userInfo).await()
                                        }
                                    }

                                    singInJob.await()

                                    Setting.email = myEmail

                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish() // LoginActivity를 액티비티 스택에서 제거
                                }
                            }

                            override fun onCancelled(reason: DatabaseError) {
                                Log.d("error", reason.details)
                                setLogin(false)
                            }
                        })
                }
            }
        }
    }

    private fun setLogin(bool: Boolean) {
        binding.btnStartKakaoLogin.visibility = if (bool) View.GONE else View.VISIBLE
    }
}
