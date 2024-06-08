package com.and.dialogfragment

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.and.CameraActivity
import com.and.databinding.FragmentSelectAddDetailWayBinding
import com.and.datamodel.DrugDataModel
import com.and.setting.NetworkManager
import com.and.viewModel.UserDataViewModel
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions

class SelectAddDetailWayFragment : DialogFragment() {
    private var _binding: FragmentSelectAddDetailWayBinding? = null
    private val binding get() = _binding!!
    private val userDataViewModel: UserDataViewModel by activityViewModels()
    private val loadingDialogFragment = LoadingDialogFragment()
    private var selectedCategory = DrugDataModel()
    private lateinit var recognizedTexts: MutableList<String>

    private var pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let { uri ->
                launchImageCrop(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val path = result.data?.getStringExtra("path")
            val uri = Uri.parse(path)
            launchImageCrop(uri)
        }
    }

    private var cropImageLauncher = registerForActivityResult(CropImageContract()) { result ->
        loadingDialogFragment.show(requireActivity().supportFragmentManager, "loading")
        result.uriContent?.let { uri -> performTextRecognition(uri) }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSelectAddDetailWayBinding.inflate(inflater, container, false)
        selectedCategory = arguments?.getParcelable("selectedCategory", DrugDataModel::class.java) ?: DrugDataModel()
        binding.apply {
            writeBtn.setOnClickListener {
                val writeDialogFragment = WriteDialogFragment()
                writeDialogFragment.clickYesListener = WriteDialogFragment.OnClickYesListener {
                    if (!NetworkManager.checkNetworkState(requireContext())) {
                        return@OnClickYesListener
                    }
                    try {
                        userDataViewModel.addDetail(selectedCategory, listOf(it))
                    } catch (e: Exception) {
                        return@OnClickYesListener
                    } finally {
                        dismiss()
                    }
                }
                writeDialogFragment.show(requireActivity().supportFragmentManager, "writeDetail")
            }

            galleryBtn.setOnClickListener {
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = "image/*"
                pickImageLauncher.launch(intent)
            }

            cameraBtn.setOnClickListener {
                val intent = Intent(requireContext(), CameraActivity::class.java)
                intent.putExtra("extension", "jpg")
                cameraLauncher.launch(intent)
            }
        }

        isCancelable = true
        this.dialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.dialog?.window!!.setGravity(Gravity.BOTTOM)
        this.dialog?.window!!.attributes.y = 40
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        resizeDialog()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun resizeDialog() {
        val params: ViewGroup.LayoutParams? = this.dialog?.window?.attributes
        val deviceWidth = Resources.getSystem().displayMetrics.widthPixels
        params?.width = (deviceWidth * 0.95).toInt()
        this.dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    private fun launchImageCrop(uri: Uri) {
        val options = CropImageContractOptions(uri, CropImageOptions())
        cropImageLauncher.launch(options)
    }

    private fun performTextRecognition(uri: Uri) {
        val image = InputImage.fromFilePath(requireContext(), uri)
        val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                recognizedTexts = visionText.text.split("\n")
                    .map { filterKoreanText(it).trim() }// Apply filterKoreanText to each line
                    .filter { it.isNotEmpty() } // Filter out empty lines
                    .toMutableList()
                
                if(recognizedTexts.isEmpty()) {
                    Toast.makeText(requireContext(), "인식된 텍스트가 없어요.", Toast.LENGTH_SHORT).show()
                    loadingDialogFragment.dismiss()
                    return@addOnSuccessListener
                }

                val selectDetailAddedFragment = SelectDetailAddedFragment()
                selectDetailAddedFragment.apply {
                    val bundle = Bundle()
                    bundle.putParcelable("selectedCategory", selectedCategory)
                    bundle.putStringArrayList("recognizedTexts", ArrayList(recognizedTexts))
                    arguments = bundle
                }
                loadingDialogFragment.dismiss()
                selectDetailAddedFragment.show(requireActivity().supportFragmentManager, "selectRecognizedTexts")
                dismiss()
            }
            .addOnFailureListener { exception ->
                loadingDialogFragment.dismiss()
                Toast.makeText(requireContext(), "Text recognition failed: $exception", Toast.LENGTH_SHORT).show()
            }
    }

    private fun filterKoreanText(text: String): String {
        // Remove parentheses and their content
        var filteredText = text.replace(Regex("\\(.*?\\)|\\[.*?\\]|\\{.*?\\}"), "")
        // Remove non-Korean characters and special symbols
        filteredText = filteredText.replace(Regex("[^가-힣\\s]"), "")
        // Collapse multiple spaces into a single space
        filteredText = filteredText.replace(Regex("\\s+"), " ").trim()
        return filteredText
    }
}