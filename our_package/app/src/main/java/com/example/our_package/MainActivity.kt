package com.example.our_package

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.view.View.VISIBLE
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val editText = findViewById<EditText>(R.id.editTextText)
        val button = findViewById<Button>(R.id.button)
        val textView = findViewById<TextView>(R.id.textView)
        button.setOnClickListener {
            val name = editText.text
            if (!name.isEmpty()){
                textView.text = "Hello, $name"
                textView.visibility = VISIBLE
            } else{
                textView.text = "Invalid name. Try again"
                textView.visibility = VISIBLE
                textView.setTextColor(Color.RED)
            }
        }
    }
}