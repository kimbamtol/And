package com.and

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.DefaultItemAnimator
import com.and.adpater.CategoryListAdapter
import com.and.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            val categoryList = mutableListOf("Mon", "Tues")
            val adapter = CategoryListAdapter(categoryList)
            val itemAnimator = DefaultItemAnimator() // 리사이클러 뷰 아이템 변경시 애니메이션 설정
            itemAnimator.supportsChangeAnimations = false
            categoryRecyclerView.itemAnimator = itemAnimator
            categoryRecyclerView.adapter = adapter

            searchDrugBtn.setOnClickListener {
                if(searchview.visibility == View.GONE) {
                    searchview.visibility = View.VISIBLE
                } else {
                    searchview.visibility = View.GONE
                }
            }

            addDrugBtn.setOnClickListener {
                val addDrugDialogFragment = AddDrugDialogFragment()
                addDrugDialogFragment.onButtonClickListener = object : AddDrugDialogFragment.OnButtonClickListener {
                    override fun onAddCategoryBtnClick(text: String) {
                        categoryList.add(text)
                        adapter.notifyItemInserted(categoryList.indexOf(text))
                    }

                    override fun onAddDetailBtnClick() {
                        return
                    }

                }
                addDrugDialogFragment.show(this@MainActivity.supportFragmentManager, "addDrug")
            }

            buttonOpenMLActivity.setOnClickListener {
                val intent = Intent(this@MainActivity, MLActivity::class.java)
                startActivity(intent)
            }

            searchview.setOnQueryTextListener(object: androidx.appcompat.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    adapter.filter.filter(newText) // 입력 값에 따라 도감 필터
                    return false
                }
            })
        }
    }
}