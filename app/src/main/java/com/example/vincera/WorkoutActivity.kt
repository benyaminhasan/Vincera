package com.example.vincera

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WorkoutActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var spinnerPlan: Spinner
    private lateinit var spinnerUebung: Spinner
    private lateinit var etGewicht: EditText
    private lateinit var etWiederholungen: EditText
    private lateinit var etSaetze: EditText
    private lateinit var btnTrainingSpeichern: Button

    private var plaeneListe: List<TrainingsplanModel> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        spinnerPlan = findViewById(R.id.spinnerPlan)
        spinnerUebung = findViewById(R.id.spinnerUebung)
        etGewicht = findViewById(R.id.etGewicht)
        etWiederholungen = findViewById(R.id.etWiederholungen)
        etSaetze = findViewById(R.id.etSaetze)
        btnTrainingSpeichern = findViewById(R.id.btnTrainingSpeichern)

        spinnerPlan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateUebungenSpinner(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnTrainingSpeichern.setOnClickListener {
            saveWorkout()
        }

        loadTrainingsplaene()

        setupBottomNavigation(R.id.nav_workout)
    }

    private fun loadTrainingsplaene() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).collection("trainingsplaene")
            .get()
            .addOnSuccessListener { result ->
                plaeneListe = result.documents.map { doc ->
                    TrainingsplanModel(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        uebungen = doc.getString("uebungen") ?: ""
                    )
                }

                if (plaeneListe.isEmpty()) {
                    Toast.makeText(this, getString(R.string.msg_no_plans), Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val planNamen = plaeneListe.map { it.name }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, planNamen)
                spinnerPlan.adapter = adapter

                updateUebungenSpinner(0)
            }
    }

    private fun updateUebungenSpinner(planPosition: Int) {
        if (planPosition < 0 || planPosition >= plaeneListe.size) return

        val uebungenText = plaeneListe[planPosition].uebungen
        val uebungenListe = uebungenText.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, uebungenListe)
        spinnerUebung.adapter = adapter
    }
    // Validiert die Eingaben und speichert einen neuen Trainingseintrag in Firestore
    private fun saveWorkout() {
        val uid = auth.currentUser?.uid
        if (uid == null || plaeneListe.isEmpty()) {
            Toast.makeText(this, getString(R.string.msg_no_plans), Toast.LENGTH_SHORT).show()
            return
        }

        val planPosition = spinnerPlan.selectedItemPosition
        val planName = plaeneListe[planPosition].name
        val uebung = spinnerUebung.selectedItem as? String ?: ""

        val gewichtText = etGewicht.text.toString().trim()
        val wiederholungenText = etWiederholungen.text.toString().trim()
        val saetzeText = etSaetze.text.toString().trim()

        if (uebung.isEmpty() || gewichtText.isEmpty() || wiederholungenText.isEmpty() || saetzeText.isEmpty()) {
            Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show()
            return
        }

        val gewicht = gewichtText.toDoubleOrNull()
        val wiederholungen = wiederholungenText.toIntOrNull()
        val saetze = saetzeText.toIntOrNull()

        if (gewicht == null || wiederholungen == null || saetze == null) {
            Toast.makeText(this, "Bitte gültige Zahlen eingeben", Toast.LENGTH_SHORT).show()
            return
        }

        val workoutData = hashMapOf(
            "plan" to planName,
            "uebung" to uebung,
            "gewicht" to gewicht,
            "wiederholungen" to wiederholungen,
            "saetze" to saetze,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users").document(uid).collection("workouts")
            .add(workoutData)
            .addOnSuccessListener {
                Log.d("WorkoutActivity", "Training gespeichert: $uebung, $gewicht kg, $wiederholungen Wdh, $saetze Sätze")
                Toast.makeText(this, getString(R.string.workout_saved), Toast.LENGTH_SHORT).show()
                etGewicht.text.clear()
                etWiederholungen.text.clear()
                etSaetze.text.clear()
            }
            .addOnFailureListener {
                Log.e("WorkoutActivity", "Fehler beim Speichern: ${it.message}")
                Toast.makeText(this, getString(R.string.workout_error), Toast.LENGTH_SHORT).show()
            }
    }
}