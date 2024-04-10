package com.and

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.and.databinding.ActivityMlactivityBinding// 변경된 뷰 바인딩 import
import java.io.IOException

class MLActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMlactivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMlactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }
    }

    private fun recognizeText(image: InputImage, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        try {
            val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val resultText = visionText.text
                    onSuccess(resultText)
                }
                .addOnFailureListener { e ->
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

            imageUri?.let {
                val image: InputImage = InputImage.fromFilePath(this, it)
                recognizeText(image,
                    onSuccess = { resultText ->
                        binding.textViewResult.text = resultText
                    },
                    onFailure = { exception ->
                        binding.textViewResult.text = "Text recognition failed with exception: $exception"
                    })
            }
        }
    }
}
