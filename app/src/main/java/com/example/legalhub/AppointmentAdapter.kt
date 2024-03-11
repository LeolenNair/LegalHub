package com.example.legalhub
// src/main/java/com.example.yourapp/AppointmentAdapter.kt

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*


class AppointmentAdapter(private val context: Context, private val appointments: List<Appointment>) : BaseAdapter() {

    override fun getCount(): Int {
        return appointments.size
    }

    override fun getItem(position: Int): Any {
        return appointments[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            val inflater = LayoutInflater.from(context)
            view = inflater.inflate(R.layout.list_item_appointment, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val appointment = getItem(position) as Appointment
        viewHolder.bind(appointment)

        return view
    }

    private inner class ViewHolder(view: View) {
        val detailsTextView: TextView = view.findViewById(R.id.appointmentItemDetailsTextView)
        val confirmButton: Button = view.findViewById(R.id.confirmButton)
        val rescheduleButton: Button = view.findViewById(R.id.rescheduleButton)
        private lateinit var appointmentsReference: DatabaseReference
        private lateinit var appointmentId: String

        init {
            // Initialize the appointmentsReference using the database reference appropriate for your structure
            appointmentsReference = FirebaseDatabase.getInstance().getReference("appointments")
        }

        fun bind(appointment: Appointment) {
            this.appointmentId = appointment.id
            val details = "Username: ${appointment.username}\nDate: ${appointment.date}\nTime: ${appointment.time}"
            detailsTextView.text = details

            confirmButton.visibility = if (appointment.status == "pending") View.VISIBLE else View.GONE
            confirmButton.setOnClickListener {
                // Implement logic to confirm the appointment
                confirmAppointment()
            }

            rescheduleButton.setOnClickListener {
                // Implement logic to reschedule the appointment
                showDateTimePickerDialog()
            }
        }

        private fun confirmAppointment() {
            // Update the appointment status to "Confirmed" in the database
            appointmentsReference.child(appointmentId).child("status").setValue("Confirmed")
            showToast("Appointment confirmed!")

        }

        private fun showDateTimePickerDialog() {
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val selectedDate = "$year-${month + 1}-$dayOfMonth"
                    showTimePickerDialog(selectedDate)
                },
                currentYear,
                currentMonth,
                currentDay
            )

            datePickerDialog.show()
        }

        private fun showTimePickerDialog(selectedDate: String) {
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            val currentMinute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val selectedTime = "$hourOfDay:$minute"
                    rescheduleAppointment(selectedDate, selectedTime)
                },
                currentHour,
                currentMinute,
                true // 24-hour format
            )

            timePickerDialog.show()
        }

        private fun rescheduleAppointment(newDate: String, newTime: String) {
            // Update the date, time, and status to "Rescheduled" in the database
            appointmentsReference.child(appointmentId).apply {
                child("date").setValue(newDate)
                child("time").setValue(newTime)
                child("status").setValue("Rescheduled")
            }
            showToast("Appointment rescheduled!")
        }

        private fun showToast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }
}

