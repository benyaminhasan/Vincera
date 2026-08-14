package com.example.vincera

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    // Verbindet die Navigationsleiste mit den 5 Hauptscreens (Home/Pläne/Workout/Verlauf/Profil)
    protected fun setupBottomNavigation(selectedItemId: Int) {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation) ?: return
        bottomNav.selectedItemId = selectedItemId

        bottomNav.setOnItemSelectedListener { item ->
            if (item.itemId == selectedItemId) {
                return@setOnItemSelectedListener true
            }

            val targetActivity: Class<*> = when (item.itemId) {
                R.id.nav_home -> DashboardActivity::class.java
                R.id.nav_plaene -> TrainingsplaeneActivity::class.java
                R.id.nav_workout -> WorkoutActivity::class.java
                R.id.nav_verlauf -> VerlaufActivity::class.java
                R.id.nav_profil -> ProfilActivity::class.java
                else -> return@setOnItemSelectedListener false
            }

            startActivity(Intent(this, targetActivity))
            true
        }
    }
}