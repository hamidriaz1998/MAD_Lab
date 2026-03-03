package com.example.bmi

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result)

        val tvDetails = findViewById<TextView>(R.id.tvDetails)
        val tvBMI = findViewById<TextView>(R.id.tvBMI)
        val tvCategory = findViewById<TextView>(R.id.tvCategory)
        val btnRecalculate = findViewById<Button>(R.id.btnRecalculate)

        val name = intent.getStringExtra("name")
        val age = intent.getStringExtra("age")
        val bmi = intent.getDoubleExtra("bmi", 0.0)

        tvDetails.text = "Name:  $name\nAge:    $age"

        val category: String
        val color: Int

        when {
            bmi < 18.5 -> {
                category = "Underweight"
                color = Color.parseColor("#2196F3")
            }
            bmi < 25.0 -> {
                category = "Normal"
                color = Color.parseColor("#4CAF50")
            }
            else -> {
                category = "Overweight"
                color = Color.parseColor("#F44336")
            }
        }

        tvBMI.text = "%.1f".format(bmi)
        tvBMI.setTextColor(color)
        tvCategory.text = category

        btnRecalculate.setOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
