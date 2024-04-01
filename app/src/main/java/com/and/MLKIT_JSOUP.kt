package com.and

import android.os.Bundle
import android.util.Log
import org.jsoup.Jsoup
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MLKIT_JSOUP : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mlkit_jsoup)

        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val viewModel: MainViewModel by viewModels()
        viewModel.fetchProductNames()
    }

    private fun enableEdgeToEdge() {
        // Edge-to-Edge 설정 코드
    }
}

class MainViewModel : ViewModel() {
    fun fetchProductNames() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val document = Jsoup.connect("https://www.druginfo.co.kr/p/contra-product-search/").get()
                val productNames = document.select("a.contra-link").mapNotNull {
                    it.parent()?.parent()?.select("a")?.firstOrNull()?.text()
                }
            } catch (e: Exception) {
                Log.e("JSOUP_ERROR", "Error fetching product names", e)
            }
        }
    }
}
