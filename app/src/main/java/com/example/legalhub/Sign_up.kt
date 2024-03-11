package com.example.legalhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.legalhub.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

data class User(
    val id: String = "",  // Firebase UID or other unique identifier
    val username: String = "",
    val role: String = ""  // "user" or "lawyer", for example
    // Add other user details as needed
)
class Sign_up : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth

        binding.textView.setOnClickListener {
            val intent = Intent(this, Sign_in::class.java)
            startActivity(intent)
            finish()
        }

        binding.button.setOnClickListener {
            signUp()
        }
    }

    private fun signUp() {
        val email = binding.emailEt.text.toString()
        val pass = binding.passET.text.toString()
        val confirmPass = binding.confirmPassEt.text.toString()
        val username = binding.usernameEt.text.toString() // Add this line

        if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
            if (pass == confirmPass) {
                val isLawyer = pass == "admin1"

                firebaseAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = firebaseAuth.currentUser

                            // Save user details including username to Firebase Realtime Database
                            saveUserToDatabase(user?.uid, username, isLawyer)

                            // Additional logic for role-specific actions can be added here

                            navigateToHomePage(isLawyer)
                        } else {
                            Toast.makeText(this, task.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserToDatabase(userId: String?, username: String, isLawyer: Boolean) {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        if (userId != null) {
            val user = User(
                id = userId,
                username = username,
                role = if (isLawyer) "lawyer" else "user"
                // Add other user details as needed
            )

            usersRef.child(userId).setValue(user)
        }
    }

    private fun navigateToHomePage(isLawyer: Boolean) {
        val intent = if (isLawyer) {
            Intent(this, LawyerHomePage::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}