package com.and

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.and.databinding.ActivityLoginBinding
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

        val naverClientId = getString(R.string.social_login_info_naver_client_id)
        val naverClientSecret = getString(R.string.social_login_info_naver_client_secret)
        val naverClientName = getString(R.string.social_login_info_naver_client_name)
        NaverIdLoginSDK.initialize(this, naverClientId, naverClientSecret , naverClientName)

        setLayoutState(false)

        binding.tvNaverLogin.setOnClickListener {
            startNaverLogin()
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
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // LoginActivity를 액티비티 스택에서 제거
        setLogin(true) // 로그인 버튼을 숨김
    }

    private fun setLogin(bool: Boolean) {
        binding.btnStartKakaoLogin.visibility = if (bool) View.GONE else View.VISIBLE
    }

    private fun startNaverLogin(){
        var naverToken :String? = ""

        val profileCallback = object : NidProfileCallback<NidProfileResponse> {
            override fun onSuccess(response: NidProfileResponse) {
                val intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
                setLayoutState(true)
            }
            override fun onFailure(httpStatus: Int, message: String) {
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
}
