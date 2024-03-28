package com.and

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
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

        // 이미지 선택 버튼 클릭 시 갤러리 앱 열기
        binding.buttonPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }
    }
    //ml kit 사용한 text 인식
    private fun recognizeText(image: InputImage, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        try {
            val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            recognizer.process(image)
                .addOnSuccessListener {visionText ->
                    val resultText=visionText.text
                    onSuccess(resultText)
                }
                .addOnFailureListener {e->
                    onFailure(e)
                }
        } catch (e: IOException) {
            e.printStackTrace()

        }
    }
    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val imageUri = data?.data

            // 선택된 이미지를 InputImage로 변환하여 recognizeText 함수 호출
            imageUri?.let {
                val image: InputImage = InputImage.fromFilePath(this, it)
                recognizeText(image,
                    onSuccess = { resultText ->
                        // 텍스트 인식 성공 시 결과를 화면에 표시
                        binding.textViewResult.text = resultText
                    },
                    onFailure = { exception ->
                        // 텍스트 인식 실패 시 메시지 출력
                        binding.textViewResult.text = "Text recognition failed with exception: $exception"
                    })
            }
        }
    }

}