package com.and

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.and.databinding.ActivityMainBinding
import com.and.viewModel.UserDataViewModel
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var dataViewModel: UserDataViewModel
    private val TAG = "MainActivity"

    private var backPressedTime : Long = 0

    private val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (System.currentTimeMillis() - backPressedTime < 2500) {
                moveTaskToBack(true)
                finishAndRemoveTask()
                exitProcess(0)
            }
            Toast.makeText(this@MainActivity, "한번 더 클릭 시 종료 됩니다.", Toast.LENGTH_SHORT).show()
            backPressedTime = System.currentTimeMillis()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate called")

        this.onBackPressedDispatcher.addCallback(this, callback)

        dataViewModel = ViewModelProvider(this).get(UserDataViewModel::class.java)

        dataViewModel.successGetData.observe(this) { success ->
            if (success) {
                Log.d(TAG, "Data loaded successfully")
                binding.menuBn.visibility = View.VISIBLE
                val startFragment = ManageDrugFragment()
                changeFragment(startFragment)

                val responseList = intent.getSerializableExtra("responseList") as? ArrayList<ArrayList<String>>
                val productList = intent.getStringArrayListExtra("productList")
                Log.d(TAG, "Received responseList: $responseList and productList: $productList")
                if (responseList != null && productList != null) {
                    compareAndUpdateDrugs(responseList, productList)
                } else {
                    Log.d(TAG, "responseList or productList is null")
                }

                binding.apply {
                    menuBn.run {
                        setOnItemSelectedListener { item ->
                            when(item.itemId) {
                                R.id.menu_manageDrug -> {
                                    val manageDrugFragment = ManageDrugFragment()
                                    changeFragment(manageDrugFragment)
                                    true
                                }
                                R.id.menu_Calendar -> {
                                    val calendarFragment = CalendarFragment()
                                    changeFragment(calendarFragment)
                                    true
                                }
                                else -> {
                                    val mypageFragment = MyPageFragment()
                                    changeFragment(mypageFragment)
                                    true
                                }
                            }
                        }
                    }
                }
            } else {
                Log.d(TAG, "Failed to load data")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun compareAndUpdateDrugs(responseList: ArrayList<ArrayList<String>>, productList: ArrayList<String>) {
        Log.d(TAG, "Comparing and updating drugs")
        val drugInfos = dataViewModel.drugInfos.value
        if (drugInfos.isNullOrEmpty()) {
            Log.d(TAG, "drugInfos is null or empty")
            return
        }

        drugInfos.forEach { category ->
            Log.d(TAG, "Checking category: ${category.category}")
            category.details.forEachIndexed { index, drugName ->
                responseList.forEachIndexed { responseIndex, responseDrugList ->
                    responseDrugList.forEach { responseDrug ->
                        if (drugName == responseDrug) {
                            Log.d(TAG, "Matching drug found: $drugName in category: ${category.category}")
                            val updatedDrugName = "$drugName <- 동시 복용 금지 -> ${productList[responseIndex]}"
                            category.details[index] = updatedDrugName
                            dataViewModel.updateDrugInfo(category)
                        } else {
                            Log.d(TAG, "No match for: $drugName in category: ${category.category}")
                        }
                    }
                }
            }
        }
    }

    fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.screen_fl, fragment)
            .commitAllowingStateLoss()
    }

    fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
