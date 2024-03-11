package com.example.legalhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.HashMap

data class Lawyer(
    val id: String? = null,
    val username: String? = null,
)




data class Appointment(
    val id: String = "",
    val userId: String = "",
    val lawyerName: String = "",
    val lawyerId: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "",
    var username: String = ""
)



class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var toggle: ActionBarDrawerToggle
    private lateinit var lawyerSpinner: Spinner
    private lateinit var datePicker: DatePicker
    private lateinit var timePicker: TimePicker
    private lateinit var scheduleButton: Button

    private lateinit var databaseReference: DatabaseReference
    private lateinit var lawyers: MutableList<Lawyer>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupDrawer()

        lawyerSpinner = findViewById(R.id.lawyerSpinner)
        datePicker = findViewById(R.id.datePicker)
        timePicker = findViewById(R.id.timePicker)
        scheduleButton = findViewById(R.id.scheduleButton)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        lawyers = mutableListOf()

        fetchLawyers()

        scheduleButton.setOnClickListener {
            scheduleAppointment()
        }

    }

    private fun fetchLawyers() {
        databaseReference.orderByChild("role").equalTo("lawyer").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                lawyers.clear()

                for (userSnapshot in snapshot.children) {
                    try {
                        // Change Users::class.java to Lawyer::class.java to match the expected data structure
                        val user = userSnapshot.getValue(Lawyer::class.java)
                        if (user != null) {
                            val lawyer = Lawyer(id = user.id, username = user.username)
                            lawyers.add(lawyer)
                            Log.d("FetchLawyers", "Lawyer fetched: ${user.id}, ${user.username}")
                            Toast.makeText(this@MainActivity, "Lawyers fetched", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.e("FetchLawyers", "User is null")
                            Toast.makeText(this@MainActivity, "User is null", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("FetchLawyers", "Error converting User to Lawyer: ${e.message}")
                        Toast.makeText(this@MainActivity, "Error fetching lawyers", Toast.LENGTH_SHORT).show()
                    }
                }

                // Populate the spinner with lawyer names


                val lawyerNames = lawyers.map { it.username }
                Log.d("Spinner", "Lawyer names: $lawyerNames")
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, lawyerNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                lawyerSpinner.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FetchLawyers", "Failed to fetch lawyers: ${error.message}")
                Toast.makeText(this@MainActivity, "Failed to fetch lawyers", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getDateFromDatePicker(datePicker: DatePicker): String {
        val day = datePicker.dayOfMonth
        val month = datePicker.month + 1  // Month is zero-based
        val year = datePicker.year

        return "$year-$month-$day"
    }

    private fun getTimeFromTimePicker(timePicker: TimePicker): String {
        val hour = timePicker.hour
        val minute = timePicker.minute

        // Format the time as HH:mm
        return String.format("%02d:%02d", hour, minute)
    }

    private fun isValidDateTime(date: String, time: String): Boolean {
        // Check if the date is within Monday to Friday and time is between 8 am to 5 pm
        val calendar = Calendar.getInstance()
        val currentDate = Calendar.getInstance()

        val selectedYear = date.split("-")[0].toInt()
        val selectedMonth = date.split("-")[1].toInt() - 1  // Month is zero-based
        val selectedDay = date.split("-")[2].toInt()
        val selectedHour = time.split(":")[0].toInt()

        calendar.set(selectedYear, selectedMonth, selectedDay)

        return (calendar.get(Calendar.DAY_OF_WEEK) in Calendar.MONDAY..Calendar.FRIDAY
                && selectedHour in 8..17
                && calendar.after(currentDate))  // Check if the selected date is not in the past
    }


    private fun scheduleAppointment() {
        val user = auth.currentUser
        val userId = user?.uid
        val selectedLawyer = lawyerSpinner.selectedItem as? String
        val selectedDate = getDateFromDatePicker(datePicker)
        val selectedTime = getTimeFromTimePicker(timePicker)

        val usersReference = FirebaseDatabase.getInstance().getReference("users")

        when {
            userId == null -> {
                Toast.makeText(this, "User is not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
            }
            selectedLawyer == null -> {
                Toast.makeText(this, "Please select a lawyer.", Toast.LENGTH_SHORT).show()
            }
            !isValidDateTime(selectedDate, selectedTime) -> {
                Toast.makeText(this, "Invalid date/time selected. Appointments must be scheduled on weekdays between 8 am and 5 pm.", Toast.LENGTH_SHORT).show()
            }
            else -> {
                // Fetch the username of the currently logged-in user
                usersReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val username = snapshot.child("username").getValue(String::class.java)

                        if (username != null) {
                            // Perform availability check for the selected lawyer
                            if (isLawyerAvailable(selectedLawyer, selectedDate, selectedTime)) {
                                // Proceed to schedule the appointment
                                saveAppointment(userId, username, selectedLawyer, selectedDate, selectedTime)
                                Toast.makeText(this@MainActivity, "Appointment scheduled successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this@MainActivity, "$selectedLawyer is not available at the chosen date and time", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle the error
                    }
                })
            }
        }
    }




    private fun saveAppointment(userId: String?, username: String?, lawyerName: String, date: String, time: String) {
        if (userId != null && username != null) {
            val appointmentsReference = FirebaseDatabase.getInstance().getReference("appointments")

            val selectedLawyer = lawyers.find { it.username == lawyerName }
            val lawyerId = selectedLawyer?.id ?: ""

            // Generate a unique key for the appointment
            val appointmentId = appointmentsReference.push().key

            // Create a HashMap to represent the appointment data
            val appointmentData = HashMap<String, Any>()
            appointmentData["id"] = appointmentId ?: ""
            appointmentData["userId"] = userId
            appointmentData["username"] = username // Save the logged-in user's username
            appointmentData["lawyerName"] = lawyerName
            appointmentData["lawyerId"] = lawyerId
            appointmentData["date"] = date
            appointmentData["time"] = time
            appointmentData["status"] = "pending"

            // Save the appointment data to the "appointments" node in the database
            appointmentsReference.child(appointmentId!!).setValue(appointmentData)
        }
    }



    private fun isLawyerAvailable(lawyerId: String, date: String, time: String): Boolean {
        // Get a reference to the "appointments" node in the database
        val appointmentsReference = FirebaseDatabase.getInstance().getReference("appointments")

        var isAvailable = true

        // Query existing appointments for the specified lawyer, date, and time
        appointmentsReference.orderByChild("lawyerId").equalTo(lawyerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (appointmentSnapshot in dataSnapshot.children) {
                        val appointment = appointmentSnapshot.getValue(Appointment::class.java)
                        if (appointment != null && appointment.date == date && appointment.time == time) {
                            // There is a conflicting appointment
                            isAvailable = false
                            break
                        }
                    }

                    //  Implement your logic here based on the result (isAvailable)
                    // For example, you might update UI elements or perform additional actions.
                    if (isAvailable) {
                        // The lawyer is available at the specified date and time
                        Toast.makeText(
                            applicationContext,
                            "Lawyer is available at $date $time",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // There is a conflicting appointment
                        Toast.makeText(
                            applicationContext,
                            "Lawyer is not available at $date $time",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                    Toast.makeText(applicationContext, "Failed to check availability", Toast.LENGTH_SHORT).show()
                }
            })

        return isAvailable
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