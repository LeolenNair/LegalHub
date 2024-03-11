package com.example.legalhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class Lawyer_schedule : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var appointmentsListView: ListView
    private lateinit var upcomingAppointmentTextView: TextView
    private lateinit var upcomingAppointmentDetailsTextView: TextView
    private lateinit var appointmentsList: MutableList<Appointment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lawyer_schedule)
        setupDrawer()

        auth = FirebaseAuth.getInstance()
        appointmentsListView = findViewById(R.id.appointmentsListView)
        upcomingAppointmentTextView = findViewById(R.id.upcomingAppointmentTextView)
        upcomingAppointmentDetailsTextView = findViewById(R.id.upcomingAppointmentDetailsTextView)

        appointmentsList = mutableListOf()

        fetchLawyerAppointments()
    }

    lateinit var toggle: ActionBarDrawerToggle
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
    private fun fetchLawyerAppointments() {
        val user = auth.currentUser
        val userId = user?.uid

        if (userId != null) {
            val appointmentsReference = FirebaseDatabase.getInstance().getReference("appointments")
            val usersReference = FirebaseDatabase.getInstance().getReference("users")

            // Query appointments for the current lawyer
            appointmentsReference.orderByChild("lawyerId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (appointmentSnapshot in dataSnapshot.children) {
                                val appointment = appointmentSnapshot.getValue(Appointment::class.java)
                                appointment?.let {
                                    // Fetch the username for the appointment
                                    fetchUsernameForAppointment(it, usersReference)
                                    appointmentsList.add(it)
                                }
                            }

                            if (appointmentsList.isNotEmpty()) {
                                // Display appointments in the ListView
                                displayAppointmentsList(appointmentsList)

                                // Find and display the upcoming appointment
                                val upcomingAppointment = findUpcomingAppointment(appointmentsList)
                                displayUpcomingAppointment(upcomingAppointment)
                            } else {
                                showToast("No appointments found for the current lawyer.")
                            }
                        } else {
                            showToast("No appointments data available.")
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle database errors
                        showToast("Failed to fetch appointments: ${databaseError.message}")
                    }
                })
        } else {
            showToast("User ID is null. Please make sure the user is authenticated.")
        }
    }

    private fun fetchUsernameForAppointment(appointment: Appointment, usersReference: DatabaseReference) {
        val userId = appointment.userId
        if (userId != null) {
            usersReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = snapshot.child("username").getValue(String::class.java)
                    appointment.username = username ?: "" // Use an empty string as a default value if the username is null
                    displayAppointmentsList(appointmentsList) // Update the displayed list with the new data
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle the error
                }
            })
        }
    }



    private fun displayAppointmentsList(appointments: List<Appointment>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, appointments.map {
            "Client: ${it.username}, Date: ${it.date}, Time: ${it.time}"
        })
        appointmentsListView.adapter = adapter

        // Set item click listener if needed
        appointmentsListView.setOnItemClickListener(AdapterView.OnItemClickListener { _, _, position, _ ->
            // Handle item click if needed
            val selectedAppointment = appointments[position]
            showToast("Clicked on Username: ${selectedAppointment.username}, Date: ${selectedAppointment.date}, Time: ${selectedAppointment.time}")
        })
    }


    private fun findUpcomingAppointment(appointments: List<Appointment>): Appointment? {
        val currentDateMillis = System.currentTimeMillis()
        val upcomingAppointments = appointments
            .filter { it.status == "pending" }
            .filter { getDateMillis(it.date, it.time) > currentDateMillis }
            .sortedBy { getDateMillis(it.date, it.time) }

        return upcomingAppointments.firstOrNull()
    }

    private fun displayUpcomingAppointment(upcomingAppointment: Appointment?) {
        if (upcomingAppointment != null) {
            upcomingAppointmentTextView.visibility = View.VISIBLE
            upcomingAppointmentDetailsTextView.visibility = View.VISIBLE

            // Display the username along with the appointment details
            val appointmentDetails = "Client: ${upcomingAppointment.username}\nDate: ${upcomingAppointment.date}\nTime: ${upcomingAppointment.time}"
            upcomingAppointmentDetailsTextView.text = appointmentDetails
        } else {
            upcomingAppointmentTextView.visibility = View.GONE
            upcomingAppointmentDetailsTextView.visibility = View.GONE
        }
    }

    private fun getDateMillis(date: String, time: String): Long {
        val dateTime = "$date $time"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return dateFormat.parse(dateTime)?.time ?: 0
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}