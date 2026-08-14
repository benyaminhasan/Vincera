package com.example.vincera

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TrainingsplaeneActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var rvTrainingsplaene: RecyclerView
    private lateinit var btnErstellenPlan: Button
    private lateinit var adapter: TrainingsplanAdapter

    private val plaeneListe = mutableListOf<TrainingsplanModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainingsplaene)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        rvTrainingsplaene = findViewById(R.id.rvTrainingsplaene)
        btnErstellenPlan = findViewById(R.id.btnErstellenPlan)

        adapter = TrainingsplanAdapter(
            plaeneListe,
            onEdit = { plan -> showPlanDialog(plan) },
            onDelete = { plan -> confirmDelete(plan) }
        )

        rvTrainingsplaene.layoutManager = LinearLayoutManager(this)
        rvTrainingsplaene.adapter = adapter

        btnErstellenPlan.setOnClickListener {
            showPlanDialog(null)
        }

        loadPlaene()

        setupBottomNavigation(R.id.nav_plaene)
    }

    private fun loadPlaene() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).collection("trainingsplaene")
            .get()
            .addOnSuccessListener { result ->
                val neueListe = result.documents.map { doc ->
                    TrainingsplanModel(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        uebungen = doc.getString("uebungen") ?: ""
                    )
                }
                adapter.updateData(neueListe)
            }
    }

    private fun showPlanDialog(plan: TrainingsplanModel?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_trainingsplan, null)
        val etPlanName = dialogView.findViewById<EditText>(R.id.etPlanName)
        val etPlanUebungen = dialogView.findViewById<EditText>(R.id.etPlanUebungen)

        if (plan != null) {
            etPlanName.setText(plan.name)
            etPlanUebungen.setText(plan.uebungen)
        }

        val title = if (plan == null) getString(R.string.dialog_title_new_plan) else getString(R.string.dialog_title_edit_plan)

        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(R.string.btn_speichern) { _, _ ->
                val name = etPlanName.text.toString().trim()
                val uebungen = etPlanUebungen.text.toString().trim()

                if (name.isEmpty()) {
                    Toast.makeText(this, "Bitte einen Plan-Namen eingeben", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                savePlan(plan?.id, name, uebungen)
            }
            .setNegativeButton(R.string.btn_abbrechen, null)
            .show()
    }
    // Erstellt einen neuen Trainingsplan oder aktualisiert einen bestehenden (CRUD: Create/Update)
    private fun savePlan(planId: String?, name: String, uebungen: String) {
        val uid = auth.currentUser?.uid ?: return

        val planData = hashMapOf(
            "name" to name,
            "uebungen" to uebungen
        )

        val collection = firestore.collection("users").document(uid).collection("trainingsplaene")

        if (planId != null) {
            collection.document(planId).set(planData)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.plan_saved), Toast.LENGTH_SHORT).show()
                    loadPlaene()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Fehler beim Speichern", Toast.LENGTH_SHORT).show()
                }
        } else {
            collection.add(planData)
                .addOnSuccessListener {
                    Toast.makeText(this, getString(R.string.plan_saved), Toast.LENGTH_SHORT).show()
                    loadPlaene()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Fehler beim Speichern", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun confirmDelete(plan: TrainingsplanModel) {
        AlertDialog.Builder(this)
            .setMessage(R.string.confirm_delete_plan)
            .setPositiveButton(R.string.btn_loeschen) { _, _ ->
                deletePlan(plan)
            }
            .setNegativeButton(R.string.btn_abbrechen, null)
            .show()
    }
    // Löscht einen Trainingsplan aus Firestore (CRUD: Delete)
    private fun deletePlan(plan: TrainingsplanModel) {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).collection("trainingsplaene")
            .document(plan.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.plan_deleted), Toast.LENGTH_SHORT).show()
                loadPlaene()
            }
    }
}