package com.example.legalhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class Manage_schedule : AppCompatActivity() {
    private lateinit var appointmentsListView: ListView
    private lateinit var auth: FirebaseAuth
    private lateinit var appointmentsList: MutableList<Appointment>
    private lateinit var appointmentsAdapter: AppointmentAdapter
    private lateinit var appointmentsReference: DatabaseReference
    private lateinit var authListener: FirebaseAuth.AuthStateListener
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_schedule)
        setupDrawer()

        auth = FirebaseAuth.getInstance()
        appointmentsListView = findViewById(R.id.appointmentsListView)
        appointmentsList = mutableListOf()
        appointmentsAdapter = AppointmentAdapter(this, appointmentsList)
        appointmentsListView.adapter = appointmentsAdapter

        appointmentsReference = FirebaseDatabase.getInstance().getReference("appointments")

        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in
                fetchLawyerAppointments(user.uid)
            } else {
                // User is signed out
                // Handle the case where the user is not signed in
            }
        }
    }

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

    private fun fetchLawyerAppointments(lawyerId: String) {
        appointmentsReference.orderByChild("lawyerId").equalTo(lawyerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        appointmentsList.clear()
                        for (appointmentSnapshot in dataSnapshot.children) {
                            val appointment = appointmentSnapshot.getValue(Appointment::class.java)
                            appointment?.let {
                                appointmentsList.add(it)
                            }
                        }
                        appointmentsAdapter.notifyDataSetChanged()
                    } else {
                        showToast("No appointments found for the current lawyer.")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    showToast("Failed to fetch appointments: ${databaseError.message}")
                }
            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authListener)
    }

    override fun onStop() {
        super.onStop()
        if (authListener != null) {
            auth.removeAuthStateListener(authListener)
        }
    }
}