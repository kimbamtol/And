package com.and

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import java.io.Serializable

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
    private val responseList: MutableSet<MutableList<String>> = mutableSetOf()

    private var pendingResponses = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_recognition)

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

        // Initialize responseList with empty lists for each product
        productList.forEach { _ ->
            responseList.add(mutableListOf())
        }

        productList.forEachIndexed { index, productName ->
            for (page in 1..3) {
                val call = service.getUsjntTabooInfoList(
                    serviceKey = apiKey,
                    pageNo = page,
                    numOfRows = 10,
                    type = "json",
                    typeName = "병용금기",
                    itemName = productName
                )

                pendingResponses++

                call.enqueue(object : Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if (response.isSuccessful) {
                            val responseBody = response.body()?.string()
                            Log.d(TAG, "Response for $productName: $responseBody")
                            parseAndAddProductNames(responseBody, index)
                        } else {
                            Log.e(TAG, "Request for $productName failed: ${response.code()}")
                        }
                        checkPendingResponses()
                    }

                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                        Log.e(TAG, "Request for $productName failed", t)
                        checkPendingResponses()
                    }
                })
            }
        }
    }

    private fun parseAndAddProductNames(responseBody: String?, index: Int) {
        responseBody?.let {
            val jsonObject = JsonParser.parseString(responseBody).asJsonObject
            val bodyObject = jsonObject.getAsJsonObject("body")
            val items = bodyObject.getAsJsonArray("items")

            val productNames = mutableListOf<String>()
            for (item in items) {
                val mainIngr = item.asJsonObject.get("MIXTURE_ITEM_NAME").asString
                productNames.add(mainIngr)
            }

            // Add parsed product names to the corresponding index in responseList safely
            if (index < responseList.size) {
                responseList.elementAtOrElse(index) { mutableListOf() }.addAll(productNames)
            } else {
                responseList.add(mutableListOf<String>().apply { addAll(productNames) })
            }
        }
    }


    private fun checkPendingResponses() {
        pendingResponses--
        if (pendingResponses == 0) {
            runOnUiThread {
                navigateToMainActivity()
            }
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putStringArrayListExtra("productList", ArrayList(productList))

        val serializableResponseList = ArrayList(responseList.map { ArrayList(it) } as ArrayList<ArrayList<String>>)
        intent.putExtra("responseList", serializableResponseList as Serializable)

        Log.d(TAG, "Navigating to MainActivity with responseList: $responseList and productList: $productList")
        startActivity(intent)
        finish()
    }
}
