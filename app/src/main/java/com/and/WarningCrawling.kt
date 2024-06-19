package com.and

import android.util.Log
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class WarningCrawling(private val recognizedTexts: MutableList<String>) {
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

    fun interface OnSuccessListener {
        fun onSuccessGetData(productList: MutableList<String>, responseList: MutableSet<MutableList<String>>)
    }

    private val apiKey = "MVyZ7I0PmLzGvOmdu+dRPluZEK1tvBAagt70/uZZD5qbYD6nmJWeAmQfXq3Uuz7mMxGxfV35s4Ox7AiE0bPoQA=="
    private val productList: MutableList<String> = mutableListOf()
    private val responseList: MutableSet<MutableList<String>> = mutableSetOf()
    private var retrofit: Retrofit
    private var service: PublicDataService

    var onSuccessListener: OnSuccessListener? = null

    init {
        productList.apply {
            addAll(recognizedTexts)
            forEach { _ ->
                responseList.add(mutableListOf())
            }
        }

        retrofit = Retrofit.Builder()
            .baseUrl("https://apis.data.go.kr")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        service = retrofit.create(PublicDataService::class.java)
    }

    fun getWarningDrug() {
        val jobs = mutableListOf<Job>()
        CoroutineScope(Dispatchers.IO).launch {
            productList.forEachIndexed { index, productName ->
                jobs += async {
                    for (page in 1..3) {
                        val response = service.getUsjntTabooInfoList(
                            serviceKey = apiKey,
                            pageNo = page,
                            numOfRows = 100,
                            type = "json",
                            typeName = "병용금기",
                            itemName = productName
                        ).execute()

                        if (response.isSuccessful) {
                            val responseBody = response.body()?.string()
                            parseAndAddProductNames(responseBody, index)
                        }
                    }
                }
            }

            jobs.joinAll()

            withContext(Dispatchers.Main) {
                onSuccessListener?.onSuccessGetData(productList, responseList)
            }
        }
    }

    private fun parseAndAddProductNames(responseBody: String?, index: Int) {
        responseBody?.let {
            val jsonObject = JsonParser.parseString(responseBody).asJsonObject
            val bodyObject = jsonObject.getAsJsonObject("body")
            val items = bodyObject.getAsJsonArray("items")

            val productNames = mutableListOf<String>()
            if (items == null)
                return
            
            for (item in items) {
                val mainIngr = item.asJsonObject.get("MIXTURE_ITEM_NAME").asString
                productNames.add(mainIngr)
            }

            synchronized(responseList) {
                // Add parsed product names to the corresponding index in responseList safely
                if (index < responseList.size) {
                    responseList.elementAtOrElse(index) { mutableListOf() }.addAll(productNames.distinct())
                } else {
                    responseList.add(mutableListOf<String>().apply { addAll(productNames.distinct()) })
                }
            }
        }
    }
}