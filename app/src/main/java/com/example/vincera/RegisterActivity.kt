package com.example.vincera

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var etRegName: EditText
    private lateinit var etRegEmail: EditText
    private lateinit var etRegPassword: EditText
    private lateinit var btnRegister: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        etRegName = findViewById(R.id.etRegName)
        etRegEmail = findViewById(R.id.etRegEmail)
        etRegPassword = findViewById(R.id.etRegPassword)
        btnRegister = findViewById(R.id.btnRegister)

        btnRegister.setOnClickListener {
            val name = etRegName.text.toString().trim()
            val email = etRegEmail.text.toString().trim()
            val password = etRegPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Legt einen neuen Firebase-Auth-Account an und speichert das Profil in Firestone
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        val userProfile = hashMapOf(
                            "name" to name,
                            "email" to email,
                            "gewicht" to 0.0,
                            "zielgewicht" to 0.0,
                            "trainingsziel" to ""
                        )

                        if (uid != null) {
                            firestore.collection("users").document(uid)
                                .set(userProfile)
                                .addOnSuccessListener {
                                    Log.d("RegisterActivity", "Registrierung erfolgreich: $email")
                                    Toast.makeText(this, getString(R.string.register_success), Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, DashboardActivity::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    Log.e("RegisterActivity", "Profil konnte nicht gespeichert werden: ${it.message}")
                                    Toast.makeText(this, "Profil konnte nicht gespeichert werden", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Log.e("RegisterActivity", "Registrierung fehlgeschlagen")
                        Toast.makeText(this, getString(R.string.register_error), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}