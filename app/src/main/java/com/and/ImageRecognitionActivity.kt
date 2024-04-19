package com.and

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.and.databinding.ActivityImageRecognitionBinding
import com.canhub.cropper.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

class ImageRecognitionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityImageRecognitionBinding
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var cropImageLauncher: ActivityResultLauncher<CropImageContractOptions>
    private lateinit var cameraActivityResult: ActivityResultLauncher<Intent>
    private val recognizedTexts: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageRecognitionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupButtonListeners()
        setupActivityLaunchers()
    }

    private fun setupButtonListeners() {
        binding.buttonPickImage.setOnClickListener { pickImage() }
        binding.buttonCamera.setOnClickListener { startCameraActivity() }
        binding.buttonStartCrawling.setOnClickListener { startCrawlingActivity() }
    }

    private fun setupActivityLaunchers() {
        pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageUri = result.data?.data
                imageUri?.let { uri -> launchImageCrop(uri) }
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val path = result.data?.getStringExtra("path")
                val uri = Uri.parse(path)
                launchImageCrop(uri)
            }
        }

        cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
            result?.uriContent?.let { uri -> performTextRecognition(uri) }
        }
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun startCameraActivity() {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("extension", "jpg")
        cameraLauncher.launch(intent)
    }

    private fun launchImageCrop(uri: Uri) {
        val options = CropImageContractOptions(uri, CropImageOptions())
        cropImageLauncher.launch(options)
    }

    private fun performTextRecognition(uri: Uri) {
        val image = InputImage.fromFilePath(this, uri)
        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                recognizedTexts.add(visionText.text)
                binding.textViewResult.text = visionText.text
                binding.imageView.setImageURI(uri)
            }
            .addOnFailureListener { exception ->
                binding.textViewResult.text = "Text recognition failed with exception: $exception"
            }
    }

    private fun startCrawlingActivity() {
        val intent = Intent(this, Crawling::class.java)
        intent.putStringArrayListExtra("recognizedTexts", ArrayList(recognizedTexts))
        startActivity(intent)
    }
}
