package com.and

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.system.exitProcess

class LoginActivity : AppCompatActivity() {
    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var loginInfo: android.content.SharedPreferences

    private var backPressedTime : Long = 0

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime < 2500) {
                moveTaskToBack(true)
                finishAndRemoveTask()
                exitProcess(0)
            }
            Toast.makeText(this@LoginActivity, "한번 더 클릭 시 종료 됩니다.", Toast.LENGTH_SHORT).show()
            backPressedTime = System.currentTimeMillis()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.onBackPressedDispatcher.addCallback(this, callback)

        loginInfo = getSharedPreferences("setting", MODE_PRIVATE)

        // 로그인 버튼 클릭 리스너 설정
        binding.btnStartKakaoLogin.setOnClickListener {
            kakaoLogin()
        }

        val naverClientId = getString(R.string.social_login_info_naver_client_id)
        val naverClientSecret = getString(R.string.social_login_info_naver_client_secret)
        val naverClientName = getString(R.string.social_login_info_naver_client_name)
        NaverIdLoginSDK.initialize(this, naverClientId, naverClientSecret , naverClientName)

        setLayoutState(false)

        binding.tvNaverLogin.setOnClickListener {
            startNaverLogin()
        }
    }

    override fun onResume() {
        super.onResume()
        this.onBackPressedDispatcher.addCallback(this, callback)
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
        setLayoutState(true)
        setLogin(true) // 로그인 버튼을 숨김
        UserApiClient.instance.me { user, error1 ->
            if (error1 != null) {
                Log.e(ContentValues.TAG, "사용자 정보 요청 실패", error1)
                setLogin(false)
                setLayoutState(false)
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

                                    setAutoLogin(myEmail)

                                    Setting.email = myEmail

                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish() // LoginActivity를 액티비티 스택에서 제거
                                }
                            }

                            override fun onCancelled(reason: DatabaseError) {
                                Log.d("error", reason.details)
                                setLogin(false)
                                setLayoutState(false)
                            }
                        })
                }
            }
        }
    }

    private fun setLogin(bool: Boolean) {
        binding.btnStartKakaoLogin.visibility = if (bool) View.GONE else View.VISIBLE
    }

    private fun startNaverLogin(){
        var naverToken :String? = ""
        setLayoutState(true)
        setLogin(true)

        val profileCallback = object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                response.profile?.also {
                    it.email?.also { email ->
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

                                    setAutoLogin(myEmail)

                                    Setting.email = myEmail

                                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                    startActivity(intent)
                                    finish() // LoginActivity를 액티비티 스택에서 제거
                                }
                            }

                            override fun onCancelled(reason: DatabaseError) {
                                Log.d("error", reason.details)
                                setLogin(false)
                                setLayoutState(false)
                            }
                        })
                    }
                }
            }

            override fun onFailure(httpStatus: Int, message: String) {
                setLogin(false)
                setLayoutState(false)
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(this@LoginActivity, "errorCode: ${errorCode}\n" +
                        "errorDescription: ${errorDescription}", Toast.LENGTH_SHORT).show()
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        val oauthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                naverToken = NaverIdLoginSDK.getAccessToken()
                //로그인 유저 정보 가져오기
                NidOAuthLogin().callProfileApi(profileCallback)
            }
            override fun onFailure(httpStatus: Int, message: String) {
                setLogin(false)
                setLayoutState(false)
                val errorCode = NaverIdLoginSDK.getLastErrorCode().code
                val errorDescription = NaverIdLoginSDK.getLastErrorDescription()
                Toast.makeText(this@LoginActivity, "errorCode: ${errorCode}\n" +
                        "errorDescription: ${errorDescription}", Toast.LENGTH_SHORT).show()
            }
            override fun onError(errorCode: Int, message: String) {
                onFailure(errorCode, message)
            }
        }

        NaverIdLoginSDK.authenticate(this, oauthLoginCallback)
    }
    private fun setLayoutState(login: Boolean){
        if(login){
            binding.tvNaverLogin.visibility = View.GONE
        }else{
            binding.tvNaverLogin.visibility = View.VISIBLE
        }
    }

    private fun setAutoLogin(email: String) {
        val editor = loginInfo.edit()
        editor.putString("email",email)
        editor.apply()
    }
}
