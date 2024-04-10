package com.and

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.buttonOpenMLActivity).setOnClickListener {
            val intent = Intent(this, MLActivity::class.java)
            startActivity(intent)
        }
    }
}
