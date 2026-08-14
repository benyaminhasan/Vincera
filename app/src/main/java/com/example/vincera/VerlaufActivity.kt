package com.example.vincera

import android.app.AlertDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class VerlaufActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var rvVerlauf: RecyclerView
    private lateinit var btnFilter: Button
    private lateinit var adapter: WorkoutEntryAdapter

    private var alleEintraege: List<WorkoutEntryModel> = emptyList()
    private val angezeigteEintraege = mutableListOf<WorkoutEntryModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verlauf)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        rvVerlauf = findViewById(R.id.rvVerlauf)
        btnFilter = findViewById(R.id.btnFilter)

        adapter = WorkoutEntryAdapter(angezeigteEintraege) { eintrag ->
            showDetails(eintrag)
        }

        rvVerlauf.layoutManager = LinearLayoutManager(this)
        rvVerlauf.adapter = adapter

        btnFilter.setOnClickListener {
            showFilterDialog()
        }

        loadEintraege()

        setupBottomNavigation(R.id.nav_verlauf)
    }

    private fun loadEintraege() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).collection("workouts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                alleEintraege = result.documents.map { doc ->
                    WorkoutEntryModel(
                        id = doc.id,
                        plan = doc.getString("plan") ?: "",
                        uebung = doc.getString("uebung") ?: "",
                        gewicht = doc.getDouble("gewicht") ?: 0.0,
                        wiederholungen = doc.getLong("wiederholungen") ?: 0,
                        saetze = doc.getLong("saetze") ?: 0,
                        timestamp = doc.getLong("timestamp") ?: 0
                    )
                }

                if (alleEintraege.isEmpty()) {
                    Toast.makeText(this, getString(R.string.msg_no_workouts), Toast.LENGTH_SHORT).show()
                }

                adapter.updateData(alleEintraege)
            }
    }

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter_verlauf, null)
        val spinnerFilterPlan = dialogView.findViewById<Spinner>(R.id.spinnerFilterPlan)
        val etFilterDatum = dialogView.findViewById<EditText>(R.id.etFilterDatum)

        val planOptionen = mutableListOf(getString(R.string.option_alle))
        planOptionen.addAll(alleEintraege.map { it.plan }.distinct())

        spinnerFilterPlan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, planOptionen)

        AlertDialog.Builder(this)
            .setTitle(R.string.btn_filter)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_anwenden) { _, _ ->
                val gewaehlterPlan = spinnerFilterPlan.selectedItem as? String ?: getString(R.string.option_alle)
                val datumText = etFilterDatum.text.toString().trim()
                applyFilter(gewaehlterPlan, datumText)
            }
            .setNegativeButton(R.string.btn_abbrechen, null)
            .show()
    }
    // Filtert die geladenen Trainingseinträge lokal nach Plan und/oder Datum
    private fun applyFilter(plan: String, datumText: String) {
        var gefiltert = alleEintraege

        if (plan != getString(R.string.option_alle)) {
            gefiltert = gefiltert.filter { it.plan == plan }
        }

        if (datumText.isNotEmpty()) {
            val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
            try {
                val filterDate = sdf.parse(datumText)
                if (filterDate != null) {
                    gefiltert = gefiltert.filter { isSameDay(it.timestamp, filterDate.time) }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Ungültiges Datumsformat", Toast.LENGTH_SHORT).show()
            }
        }

        adapter.updateData(gefiltert)
    }

    private fun isSameDay(millis1: Long, millis2: Long): Boolean {
        val cal1 = Calendar.getInstance().apply { timeInMillis = millis1 }
        val cal2 = Calendar.getInstance().apply { timeInMillis = millis2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun showDetails(eintrag: WorkoutEntryModel) {
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
        val details = """
            Datum: ${sdf.format(Date(eintrag.timestamp))}
            Plan: ${eintrag.plan}
            Übung: ${eintrag.uebung}
            Gewicht: ${eintrag.gewicht} kg
            Wiederholungen: ${eintrag.wiederholungen}
            Sätze: ${eintrag.saetze}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle(R.string.btn_details)
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }
}