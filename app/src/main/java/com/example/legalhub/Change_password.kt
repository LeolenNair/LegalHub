package com.example.legalhub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class Change_password : AppCompatActivity() {
    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var changePasswordButton: Button

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        currentPasswordEditText = findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        changePasswordButton = findViewById(R.id.changePasswordButton)

        auth = FirebaseAuth.getInstance()

        changePasswordButton.setOnClickListener {
            changePassword()
        }
    }

    private fun changePassword() {
        val currentPassword = currentPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()

        if (newPassword != confirmPassword) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser

        if (user != null && user.email != null) {
            // Reauthenticate with the current password
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // Password successfully reauthenticated, now change the password
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { changePasswordTask ->
                                if (changePasswordTask.isSuccessful) {
                                    Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to change password", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Authentication failed. Check your current password.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}