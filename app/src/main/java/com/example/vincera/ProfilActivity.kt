package com.example.vincera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfilActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var etProfilName: EditText
    private lateinit var etProfilGewicht: EditText
    private lateinit var etProfilZielgewicht: EditText
    private lateinit var etProfilTrainingsziel: EditText
    private lateinit var btnProfilSpeichern: Button
    private lateinit var btnLogout: Button
    private lateinit var btnPasswortZuruecksetzen: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        etProfilName = findViewById(R.id.etProfilName)
        etProfilGewicht = findViewById(R.id.etProfilGewicht)
        etProfilZielgewicht = findViewById(R.id.etProfilZielgewicht)
        etProfilTrainingsziel = findViewById(R.id.etProfilTrainingsziel)
        btnProfilSpeichern = findViewById(R.id.btnProfilSpeichern)
        btnLogout = findViewById(R.id.btnLogout)
        btnPasswortZuruecksetzen = findViewById(R.id.btnPasswortZuruecksetzen)

        loadProfil()
        setupBottomNavigation(R.id.nav_profil)

        btnProfilSpeichern.setOnClickListener {
            saveProfil()
        }

        btnLogout.setOnClickListener {
            Log.d("ProfilActivity", "Logout ausgeführt")
            auth.signOut()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnPasswortZuruecksetzen.setOnClickListener {
            resetPasswort()
        }
    }

    private fun loadProfil() {
        val uid = auth.currentUser?.uid ?: return

        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                etProfilName.setText(doc.getString("name") ?: "")
                etProfilGewicht.setText((doc.getDouble("gewicht") ?: 0.0).toString())
                etProfilZielgewicht.setText((doc.getDouble("zielgewicht") ?: 0.0).toString())
                etProfilTrainingsziel.setText(doc.getString("trainingsziel") ?: "")
            }
    }
    // Aktualisiert die Profildaten des Nutzers in Firestore
    private fun saveProfil() {
        val uid = auth.currentUser?.uid ?: return

        val name = etProfilName.text.toString().trim()
        val gewicht = etProfilGewicht.text.toString().trim().toDoubleOrNull() ?: 0.0
        val zielgewicht = etProfilZielgewicht.text.toString().trim().toDoubleOrNull() ?: 0.0
        val trainingsziel = etProfilTrainingsziel.text.toString().trim()

        val updates = hashMapOf<String, Any>(
            "name" to name,
            "gewicht" to gewicht,
            "zielgewicht" to zielgewicht,
            "trainingsziel" to trainingsziel
        )

        firestore.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.profile_saved), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, getString(R.string.profile_error), Toast.LENGTH_SHORT).show()
            }
    }
    // Löst den Firebase-Passwort-Reset per E-Mail aus
    private fun resetPasswort() {
        val email = auth.currentUser?.email
        if (email.isNullOrEmpty()) {
            Toast.makeText(this, "Keine E-Mail-Adresse gefunden", Toast.LENGTH_SHORT).show()
            return
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Toast.makeText(this, getString(R.string.reset_email_sent), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Fehler beim Senden", Toast.LENGTH_SHORT).show()
            }
    }
}