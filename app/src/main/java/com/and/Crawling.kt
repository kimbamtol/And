package com.and

import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONException
import java.net.URLEncoder

class Crawling : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var textViewResults: TextView // 결과를 표시할 TextView
    private lateinit var recognizedTexts: ArrayList<String>
    private val productList: MutableList<String> = mutableListOf() // 추출된 제품 상세 정보를 저장할 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crawling)

        webView = findViewById(R.id.webView)
        textViewResults = findViewById(R.id.textViewResults) // TextView 초기화

        // 인텐트에서 recognizedTexts 리스트 받아오기
        recognizedTexts = intent.getStringArrayListExtra("recognizedTexts") ?: arrayListOf()

        // WebView 설정
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                val javascript = """
            var productList = [];
            var productName = document.querySelector('.table-cell a em').innerText;
            var productDescription = document.querySelector('.table-cell a').lastChild.textContent.trim();
            productList.push(productDescription);
            if (productList.length > 0) {
                productList;
            } else {
                null;
            }
        """.trimIndent()

                webView.evaluateJavascript(javascript) { result ->
                    if (result != null && result != "null") {
                        try {
                            // JavaScript에서 직접 반환된 문자열을 사용
                            val productListJson = JSONArray(result)
                            for (i in 0 until productListJson.length()) {
                                val productName = productListJson.getString(i)
                                productList.add(productName)
                            }
                            // UI 업데이트
                            runOnUiThread {
                                showProductList()
                            }
                        } catch (e: JSONException) {
                            Log.e("Crawling", "JSON parsing error: ", e)
                            // 오류 처리 로직
                        }
                    }
                }
            }
        }


        if (recognizedTexts.isNotEmpty()) {
            val text = recognizedTexts[0]
            val encodedText = URLEncoder.encode(text, "EUC-KR")
            val searchUrl = "https://www.druginfo.co.kr/p/contra-product-search/index.aspx?fl=proKorName&q=$encodedText"
            webView.loadUrl(searchUrl)
        }
    }

    private fun showProductList() {
        val stringBuilder = StringBuilder()
        for (productName in productList) {
            stringBuilder.append(productName).append("\n")
        }
        textViewResults.text = stringBuilder.toString()
    }
}
