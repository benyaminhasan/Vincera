package com.example.vincera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DashboardActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvGreeting: TextView
    private lateinit var tvTrainedToday: TextView
    private lateinit var tvWorkoutCount: TextView
    private lateinit var tvLastTraining: TextView
    private lateinit var tvStreak: TextView
    private lateinit var btnTrainingStarten: Button
    private lateinit var btnTrainingsplaene: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        tvGreeting = findViewById(R.id.tvGreeting)
        tvTrainedToday = findViewById(R.id.tvTrainedToday)
        tvWorkoutCount = findViewById(R.id.tvWorkoutCount)
        tvLastTraining = findViewById(R.id.tvLastTraining)
        tvStreak = findViewById(R.id.tvStreak)
        btnTrainingStarten = findViewById(R.id.btnTrainingStarten)
        btnTrainingsplaene = findViewById(R.id.btnTrainingsplaene)

        val uid = auth.currentUser?.uid
        if (uid == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        loadUserName(uid)
        loadWorkoutStats(uid)

        btnTrainingStarten.setOnClickListener {
            startActivity(Intent(this, WorkoutActivity::class.java))
        }

        btnTrainingsplaene.setOnClickListener {
            startActivity(Intent(this, TrainingsplaeneActivity::class.java))
        }

        setupBottomNavigation(R.id.nav_home)
    }

    private fun loadUserName(uid: String) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("name") ?: ""
                tvGreeting.text = if (name.isNotEmpty()) "Hallo, $name!" else getString(R.string.dashboard_title)
            }
    }
    // Lädt alle Workouts des Nutzers und berechnet Anzahl, letzes Training und Trainings-Streak
    private fun loadWorkoutStats(uid: String) {
        firestore.collection("users").document(uid).collection("workouts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val timestamps = result.documents.mapNotNull { it.getLong("timestamp") }

                tvWorkoutCount.text = timestamps.size.toString()

                if (timestamps.isEmpty()) {
                    tvTrainedToday.text = getString(R.string.no)
                    tvLastTraining.text = getString(R.string.no_data)
                    tvStreak.text = "0"
                    return@addOnSuccessListener
                }

                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
                tvLastTraining.text = sdf.format(Date(timestamps.first()))

                val today = dayStart(System.currentTimeMillis())
                val trainedToday = timestamps.any { dayStart(it) == today }
                tvTrainedToday.text = if (trainedToday) getString(R.string.yes) else getString(R.string.no)

                val distinctDays = timestamps.map { dayStart(it) }.distinct().sortedDescending()
                var streak = 0
                var expectedDay = today
                for (day in distinctDays) {
                    if (day == expectedDay) {
                        streak++
                        expectedDay -= DAY_MILLIS
                    } else if (day < expectedDay) {
                        break
                    }
                }
                tvStreak.text = streak.toString()
            }
    }
    // Rundet einen Zeitstempel auf Mitternacht ab, um Tage vergleichbar zu machen
    private fun dayStart(millis: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = millis
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    companion object {
        private const val DAY_MILLIS = 24 * 60 * 60 * 1000L
    }
}