package com.and

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.JsonParser

interface PublicDataService {
    @GET("/1471000/DURPrdlstInfoService03/getUsjntTabooInfoList03")
    fun getUsjntTabooInfoList(
        @Query("serviceKey") serviceKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("type") type: String,
        @Query("typeName") typeName: String,
        @Query("itemName") itemName: String
    ): Call<ResponseBody>
}

class Crawling : AppCompatActivity() {
    private val TAG = "Crawling"
    private lateinit var recognizedTexts: ArrayList<String>
    private val productList: MutableList<String> = mutableListOf()
    private val responseList: MutableSet<String> = mutableSetOf()
    //Main에서 불러온 details를 저장할 리스트
    private val Main_productList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recognition)

        //Main_productList를 불러오자.

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.responseTextView)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recognizedTexts = intent.getStringArrayListExtra("recognizedTexts") ?: arrayListOf()
        productList.addAll(recognizedTexts)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(PublicDataService::class.java)
        val apiKey = "MVyZ7I0PmLzGvOmdu+dRPluZEK1tvBAagt70/uZZD5qbYD6nmJWeAmQfXq3Uuz7mMxGxfV35s4Ox7AiE0bPoQA=="

        for (page in 1..10) {
            productList.forEach { productName ->
                val call = service.getUsjntTabooInfoList(
                    serviceKey = apiKey,
                    pageNo = page,
                    numOfRows = 10,
                    type = "json",
                    typeName = "병용금기",
                    itemName = productName
                )

                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()?.string()
                            Log.d(TAG, "Response for $productName: $responseBody")
                            parseAndAddProductNames(responseBody)
                        } else {
                            Log.e(TAG, "Request for $productName failed: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e(TAG, "Request for $productName failed", t)
                    }
                })
            }
        }
    }

    private fun parseAndAddProductNames(responseBody: String?) {
        responseBody?.let {
            val jsonObject = JsonParser().parse(responseBody).asJsonObject
            // 먼저 "body"가 실제로 JsonObject인지 확인
            val bodyObject = jsonObject.getAsJsonObject("body")
            // "body" 객체 내부에서 "items" 배열을 가져옴
            val items = bodyObject.getAsJsonArray("items")

            for (item in items) {
                val mainIngr = item.asJsonObject.get("MIXTURE_ITEM_NAME").asString
                val productName = mainIngr
                //val productName = mainIngr.substringAfter("]").trim()  // 제품명 추출
                responseList.add(productName)
            }
            runOnUiThread {
                updateResponseTextView()  // UI 스레드에서 TextView 업데이트
            }
        }
    }

    private fun updateResponseTextView() {
        val text = responseList.joinToString(separator = "\n")
        val textView = findViewById<TextView>(R.id.responseTextView)
        textView.text = text
    }

}


