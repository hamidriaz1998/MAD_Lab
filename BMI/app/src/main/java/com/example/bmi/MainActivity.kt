package com.example.bmi

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val etName = findViewById<EditText>(R.id.etName)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val btnCalculate = findViewById<Button>(R.id.btnCalculate)

        btnCalculate.setOnClickListener {
            val name = etName.text.toString()
            val age = etAge.text.toString()
            val height = etHeight.text.toString().toDouble()
            val weight = etWeight.text.toString().toDouble()

            val heightMeter = height / 100
            val bmi = weight / (heightMeter * heightMeter)

            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("age", age)
            intent.putExtra("bmi", bmi)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
