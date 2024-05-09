package com.and

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.and.databinding.ActivityMainBinding
import com.and.viewModel.UserDataViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding// 뷰 바인딩을 사용할 변수를 선언
    private lateinit var dataViewModel: UserDataViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataViewModel = ViewModelProvider(this).get(UserDataViewModel::class.java)

        dataViewModel.successGetData.observe(this) {
            binding.menuBn.visibility = View.VISIBLE

            val startFragment = ManageDrugFragment()
            changeFragment(startFragment)

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
                                val mypageFragment = MypageFragment()
                                changeFragment(mypageFragment)
                                true
                            }
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
}