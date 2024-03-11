package com.example.legalhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.legalhub.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Sign_in : AppCompatActivity() {
    private  lateinit var  binding:ActivitySignInBinding
    private  lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()
        binding.textView.setOnClickListener{
            val intent = Intent(this, Sign_up::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener{
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()


            if (email.isNotEmpty() && pass.isNotEmpty()) {

                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = firebaseAuth.currentUser
                        user?.let { checkUserRoleAndNavigate(it) }
                    } else {
                        Toast.makeText(this, it.exception.toString(), Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                Toast.makeText(this, "Empty fields are not allowed", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun checkUserRoleAndNavigate(user: FirebaseUser) {
        val userId = user.uid
        val usersReference = FirebaseDatabase.getInstance().getReference("users")

        usersReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userRole = dataSnapshot.child("role").getValue(String::class.java)

                if (userRole == "lawyer") {
                    // User is a lawyer, navigate to LawyerHomePage
                    val intent = Intent(this@Sign_in, LawyerHomePage::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // User is not a lawyer, navigate to HomePage
                    val intent = Intent(this@Sign_in, HomePage::class.java)
                    startActivity(intent)
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
                Toast.makeText(this@Sign_in, "Failed to check user role", Toast.LENGTH_SHORT).show()
            }
        })
    }
}