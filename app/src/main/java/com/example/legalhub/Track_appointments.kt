package com.example.legalhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*


class Track_appointments : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var statusSeekBar: SeekBar
    private lateinit var statusLabel: TextView
    private lateinit var appointmentDetailsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_appointments)
        setupDrawer()

        auth = FirebaseAuth.getInstance()
        statusSeekBar = findViewById(R.id.statusSeekBar)
        statusLabel = findViewById(R.id.statusLabel)
        appointmentDetailsTextView = findViewById(R.id.appointmentDetailsTextView)


        fetchUserAppointments()

    }



    private fun fetchUserAppointments() {
        val user = auth.currentUser
        val userId = user?.uid

        if (userId != null) {
            val appointmentsReference = FirebaseDatabase.getInstance().getReference("appointments")

            // Query appointments for the current user
            appointmentsReference.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val appointmentsList = mutableListOf<Appointment>()

                            for (appointmentSnapshot in dataSnapshot.children) {
                                val appointment = appointmentSnapshot.getValue(Appointment::class.java)
                                appointment?.let {
                                    appointmentsList.add(it)
                                }
                            }

                            if (appointmentsList.isNotEmpty()) {
                                // Display or process the list of appointments as needed
                                displayAppointmentDetails(appointmentsList)
                            } else {
                                showToast("No appointments found for the current user.")
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




    private fun displayAppointmentDetails(appointments: List<Appointment>) {
        if (appointments.isNotEmpty()) {
            val appointment = appointments[0]

            // Display the status and details of the appointment
            statusLabel.text = "Status: ${appointment.status}"
            appointmentDetailsTextView.text =
                "Lawyer: ${appointment.lawyerName}\nDate: ${appointment.date}\nTime: ${appointment.time}"

            // Calculate the progress based on the appointment status and current date
            val progress = calculateSeekBarProgress(appointment)
            statusSeekBar.progress = progress
        } else {
            // Handle the case when the list is empty (no appointments found)
            statusLabel.text = "No appointments found"
            appointmentDetailsTextView.text = ""
            statusSeekBar.progress = 0
        }
    }


    private fun calculateSeekBarProgress(appointment: Appointment): Int {
        val currentDate = Calendar.getInstance().time
        val appointmentDate = SimpleDateFormat("yyyy-MM-dd").parse(appointment.date)

        return when (appointment.status) {
            "pending" -> 0
            "confirmed" -> calculateConfirmedProgress(currentDate, appointmentDate)
            else -> 0
        }
    }

    private fun calculateConfirmedProgress(currentDate: Date, appointmentDate: Date): Int {
        val calendarCurrent = Calendar.getInstance()
        calendarCurrent.time = currentDate

        val calendarAppointment = Calendar.getInstance()
        calendarAppointment.time = appointmentDate

        val daysUntilAppointment = ((calendarAppointment.timeInMillis - calendarCurrent.timeInMillis) / (1000 * 60 * 60 * 24)).toInt()

        // Define your logic for progress calculation based on the time difference
        return when {
            daysUntilAppointment >= 7 -> 10
            daysUntilAppointment >= 3 -> 30
            else -> 60
        }
    }

    private fun showToast(message: String) {
        // Display a toast with the provided message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
                    val intent = Intent(this, HomePage::class.java)
                    startActivity(intent)
                }
                R.id.nav_app -> {
                    // Open the AppointmentsActivity (replace AppointmentsActivity::class.java with your actual AppointmentsActivity class)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
                R.id.nav_track -> {
                    // Open the TrackingActivity (replace TrackingActivity::class.java with your actual TrackingActivity class)
                    val intent = Intent(this, Track_appointments::class.java)
                    startActivity(intent)
                }
                R.id.nav_serv -> {
                    // Open the ServiceActivity (replace ServiceActivity::class.java with your actual ServiceActivity class)
                    val intent = Intent(this, Service_list::class.java)
                    startActivity(intent)
                }
                R.id.nav_help -> {
                    // Open the HelpActivity (replace HelpActivity::class.java with your actual HelpActivity class)
                    val intent = Intent(this, Help::class.java)
                    startActivity(intent)
                }
                R.id.nav_pass -> {
                    // Open the HelpActivity (replace HelpActivity::class.java with your actual HelpActivity class)
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

}