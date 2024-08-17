package com.and

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.navercorp.nid.NaverIdLoginSDK

class GlobalApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        val naverClientId = BuildConfig.Naver_Client_Id
        val naverClientSecret = BuildConfig.Naver_Client_Secret
        val naverClientName = getString(R.string.social_login_info_naver_client_name)
        KakaoSdk.init(this, BuildConfig.Kakao_API_KEY)
        NaverIdLoginSDK.initialize(this, naverClientId, naverClientSecret , naverClientName)
    }
}