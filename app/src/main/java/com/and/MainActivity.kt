package com.and

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.io.IOException
import com.and.databinding.ActivityMainBinding // 뷰 바인딩을 import
class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding// 뷰 바인딩을 사용할 변수를 선언

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater) // 뷰 바인딩을 초기화
        setContentView(binding.root) // 액티비티의 컨텐츠 뷰를 뷰 바인딩의 루트 뷰로 설정

        // move to ImageRecognitionActivity
        binding.buttonToNext.setOnClickListener {
            val intent = Intent(this@MainActivity, ImageRecognitionActivity::class.java)
            startActivity(intent)
        }
    }

}