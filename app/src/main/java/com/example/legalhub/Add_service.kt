package com.example.legalhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

data class Service(
    val id: String, // Unique identifier for the service
    val name: String,
    val description: String
){
    // Default (no-argument) constructor
    constructor() : this("", "", "")
}

class Add_service : AppCompatActivity() {
    private lateinit var serviceNameEditText: EditText
    private lateinit var serviceDescriptionEditText: EditText
    private lateinit var addServiceButton: Button

    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)
        setupDrawer()

        databaseReference = FirebaseDatabase.getInstance().getReference("services")

        serviceNameEditText = findViewById(R.id.serviceNameEditText)
        serviceDescriptionEditText = findViewById(R.id.serviceDescriptionEditText)
        addServiceButton = findViewById(R.id.addServiceButton)

        addServiceButton.setOnClickListener {
            addServiceToDatabase()
        }
    }
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var auth: FirebaseAuth
    private fun setupDrawer() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        val navView: NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    // Open the HomeActivity (replace HomeActivity::class.java with your actual HomeActivity class)
                    val intent = Intent(this, LawyerHomePage::class.java)
                    startActivity(intent)
                }

                R.id.nav_track -> {
                    //Open the TrackingActivity (replace TrackingActivity::class.java with your actual TrackingActivity class)
                    val intent = Intent(this, Lawyer_schedule::class.java)
                    startActivity(intent)
                }
                R.id.nav_manage -> {
                    //Open the TrackingActivity (replace TrackingActivity::class.java with your actual TrackingActivity class)
                    val intent = Intent(this, Manage_schedule::class.java)
                    startActivity(intent)
                }

                R.id.nav_serv_add -> {
                    // Open the ServiceActivity (replace ServiceActivity::class.java with your actual ServiceActivity class)
                    val intent = Intent(this, Add_service::class.java)
                    startActivity(intent)
                }
                R.id.nav_serv -> {
                    // Open the ServiceActivity (replace ServiceActivity::class.java with your actual ServiceActivity class)
                    val intent = Intent(this, Service_list::class.java)
                    startActivity(intent)
                }
                R.id.nav_pass -> {
                    // Open the ServiceActivity (replace ServiceActivity::class.java with your actual ServiceActivity class)
                    val intent = Intent(this, Change_password::class.java)
                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    logout()
                }
            }
            true
        }
        updateUserEmailInNavHeader()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateUserEmailInNavHeader(){
        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        // Update the email in the NavigationView header
        val navView: NavigationView = findViewById(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val userEmailTextView: TextView = headerView.findViewById(R.id.user_email)

        // Check if the user is logged in before updating the email
        currentUser?.let {
            userEmailTextView.text = it.email
        }
    }

    fun logout() {
        auth = FirebaseAuth.getInstance()
        auth.signOut()

        // Redirect to your login or home screen after logout
        val intent = Intent(this, Sign_in::class.java)
        startActivity(intent)
        finish() // Close the current activity
    }

    private fun addServiceToDatabase() {
        val serviceName = serviceNameEditText.text.toString().trim()
        val serviceDescription = serviceDescriptionEditText.text.toString().trim()

        if (serviceName.isNotEmpty() && serviceDescription.isNotEmpty()) {
            val serviceId = databaseReference.push().key

            if (serviceId != null) {
                val service = Service(id = serviceId, name = serviceName, description = serviceDescription)

                databaseReference.child(serviceId).setValue(service)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Service added successfully!", Toast.LENGTH_SHORT).show()
                        clearFields()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to add service: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "Please enter both service name and description", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        serviceNameEditText.text.clear()
        serviceDescriptionEditText.text.clear()
    }
}