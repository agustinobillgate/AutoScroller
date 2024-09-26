package com.example.autoscroller

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val startButton = findViewById<Button>(R.id.startButton)
        val stopButton = findViewById<Button>(R.id.stopButton)
        val speedInput = findViewById<EditText>(R.id.speedInput)
        val distanceInput = findViewById<EditText>(R.id.distanceInput)
        val directionOption = findViewById<RadioGroup>(R.id.directionOptions)

        setupDirectionOptions(directionOption)

        startButton.setOnClickListener {
            val speed = speedInput.text.toString().toLongOrNull() ?: 1000
            val distance = distanceInput.text.toString().toIntOrNull() ?: 50

            if (isAccessibilityServiceEnabled()) {
                val intent = Intent(this, AutoScrollService::class.java)
                intent.putExtra("scrollSpeed", speed)
                intent.putExtra("scrollDistance", distance)
                intent.putExtra("scrollDirection", getSelectedDirection(directionOption))
                startService(intent)
                Toast.makeText(this, "Scrolling started", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enable the accessibility service", Toast.LENGTH_SHORT).show()
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        }

        stopButton.setOnClickListener {
            val intent = Intent(this, AutoScrollService::class.java)
            stopService(intent)
            Toast.makeText(this, "Scrolling stopped", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupDirectionOptions(directionOption: RadioGroup) {
        directionOption.removeAllViews()

        addRadioButton(directionOption, "Top to Bottom", 1)
        addRadioButton(directionOption, "Bottom to Top", 2)
        addRadioButton(directionOption, "Left to Right", 3)
        addRadioButton(directionOption, "Right to Left", 4)

        directionOption.check(1)
    }

    private fun addRadioButton(group: RadioGroup, text: String, id: Int) {
        val button = RadioButton(this)
        button.text = text
        button.id = id
        group.addView(button)
    }

    private fun getSelectedDirection(group: RadioGroup): Int {
        return group.checkedRadioButtonId
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expectedComponentName = "com.example.autoscroller/.AutoScrollService"
        val enabledServices = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)

        if (enabledServices != null && enabledServices.contains(expectedComponentName)) {
            return true
        }

        val accessibilityEnabled = Settings.Secure.getInt(contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED, 0)
        return accessibilityEnabled == 1
    }
}