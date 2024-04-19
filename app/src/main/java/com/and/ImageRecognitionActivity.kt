package com.and

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import com.and.databinding.ActivityImageRecognitionBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import java.io.File
import java.io.IOException

class ImageRecognitionActivity : AppCompatActivity() {
    private lateinit var binding : ActivityImageRecognitionBinding// 뷰 바인딩을 사용할 변수를 선언
//    private val button: Button by lazy {
//        findViewById(R.id.button1)
//    }
//
//    private val imageView: ImageView by lazy {
//        findViewById(R.id.imageView1)
//    }

    private lateinit var cameraActivityResult: ActivityResultLauncher<Intent>
    private val recognizedTexts: MutableList<String> = mutableListOf() // 인식된 텍스트를 저장할 리스트
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_image_recognition)
        binding = ActivityImageRecognitionBinding.inflate(layoutInflater) // 뷰 바인딩을 초기화
        setContentView(binding.root) // 액티비티의 컨텐츠 뷰를 뷰 바인딩의 루트 뷰로 설정

        // 이미지 선택 버튼 클릭 시 갤러리 앱 열기
        binding.buttonPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            pickImage.launch(intent)
        }
        // 카메라 버튼 클릭시
        binding.buttonCamera.setOnClickListener {
            val intent = Intent(this@ImageRecognitionActivity, CameraActivity::class.java)
                intent.putExtra("extension","jpg")
                intent.putExtra("ratio", AspectRatio.RATIO_16_9)
            cameraActivityResult.launch(intent)//카메라로 찍고


        }

        binding.buttonStartCrawling.setOnClickListener {
            startCrawlingActivity()
        }

        mainCameraActivityResult()
    }


    private fun mainCameraActivityResult() {
        cameraActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.let { intent ->
                    intent.getStringExtra("path")?.let { path ->
                        binding.imageView.run {//찍은 사진 image 뷰로 보여줌
                            setImageBitmap(urlToBitmap(File(path).absolutePath))
                        }
                        val image = InputImage.fromFilePath(this, Uri.parse(path))
                        recognizeText(//찍은 사진 text 인식
                            image,
                            onSuccess = { resultText ->
                                // 텍스트 인식 성공 시 결과를 화면에 표시
                                binding.textViewResult.text = resultText
                            },
                            onFailure = { exception ->
                                // 텍스트 인식 실패 시 메시지 출력
                                binding.textViewResult.text = "Text recognition failed with exception: $exception"
                            }
                        )
                    }
                }
            }

        }
    }



    private fun urlToBitmap(url: String) = BitmapFactory.decodeFile(url)

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
                        // 성공시 결과를 리스트에 저장
                        recognizedTexts.add(resultText)
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
    private fun startCrawlingActivity() {
        val intent = Intent(this, Crawling::class.java)
        intent.putStringArrayListExtra("recognizedTexts", ArrayList(recognizedTexts))
        startActivity(intent)
    }


}
