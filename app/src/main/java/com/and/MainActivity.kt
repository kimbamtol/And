package com.and

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.and.databinding.ActivityMainBinding
import com.and.dialogfragment.LoadingDialogFragment
import com.and.setting.NetworkManager
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

    companion object {
        var currentPage = "ManageDrug"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.d(TAG, "onCreate called")

        this.onBackPressedDispatcher.addCallback(this, callback)

        val startFragment = ManageDrugFragment()
        changeFragment(startFragment)

        val loadingDialogFragment = LoadingDialogFragment()
        loadingDialogFragment.show(supportFragmentManager, "loading")

        binding.apply {
            menuBn.run {
                setOnItemSelectedListener { item ->
                    when (item.itemId) {
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

        dataViewModel = ViewModelProvider(this).get(UserDataViewModel::class.java)

        dataViewModel.successGetData.observe(this) { success ->
            loadingDialogFragment.dismiss()

            if (success) {
                Log.d(TAG, "Data loaded successfully")
                binding.menuBn.visibility = View.VISIBLE

                when (currentPage) {
                    "ManageDrug" -> {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle("우측 위의 경고 버튼을 클릭 하여\n복용 금지 예상 알약 리스트를 꼭 확인해주세요!")
                        builder.setPositiveButton("네", null)
                        builder.setCancelable(false)
                        builder.show()
                        changeFragment(startFragment)
                    }

                    "Calendar" -> changeFragment(CalendarFragment())
                    "MyPage" -> changeFragment(MyPageFragment())
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

    fun changeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.screen_fl, fragment)
            .commitAllowingStateLoss()
    }

    fun toastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
