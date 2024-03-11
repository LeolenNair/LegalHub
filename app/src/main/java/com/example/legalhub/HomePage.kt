package com.example.legalhub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class HomePage : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        setupDrawer()
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