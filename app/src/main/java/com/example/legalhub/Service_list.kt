package com.example.legalhub

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*




class Service_list : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var serviceTextView: TextView
    private lateinit var serviceList: MutableList<Service>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_list)

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("services")
        serviceList = mutableListOf()

        // Assuming you have a TextView in your layout with id serviceTextView
        serviceTextView = findViewById(R.id.serviceTextView)

        fetchServices()
    }

    private fun fetchServices() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    if (snapshot.exists()) {
                        serviceList.clear()

                        val serviceStringBuilder = StringBuilder()

                        for (serviceSnapshot in snapshot.children) {
                            val service = serviceSnapshot.getValue(Service::class.java)
                            service?.let {
                                serviceList.add(it)
                                // Append service information to the StringBuilder
                                serviceStringBuilder.append("Service: ${it.name}\nDescription: ${it.description}\n\n")
                            }
                        }

                        // Display the services in the TextView
                        serviceTextView.text = serviceStringBuilder.toString()

                        Log.d("FetchServices", "Services fetched successfully. Count: ${serviceList.size}")
                    } else {
                        Log.d("FetchServices", "No data available for services.")
                    }
                } catch (e: Exception) {
                    // Handle any unexpected exception during data processing
                    Log.e("FetchServices", "Error processing services data: ${e.message}")
                    showToast("Error processing services data: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error during data fetching
                Log.e("FetchServices", "Failed to fetch services: ${error.message}")
                showToast("Failed to fetch services: ${error.message}")
            }
        })
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this@Service_list, message, Toast.LENGTH_SHORT).show()
        }
    }
}