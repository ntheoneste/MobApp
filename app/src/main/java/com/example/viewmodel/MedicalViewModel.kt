package com.example.viewmodel

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppointmentReminder
import com.example.data.GeminiService
import com.example.data.HospitalVisit
import com.example.data.MedicalRepository
import com.example.receiver.ReminderReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicalViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MedicalRepository
    val allVisits: StateFlow<List<HospitalVisit>>
    val allReminders: StateFlow<List<AppointmentReminder>>

    // State for Cancer AI Q&A Advisor
    private val _geminiResponse = MutableStateFlow<String>("")
    val geminiResponse: StateFlow<String> = _geminiResponse.asStateFlow()

    private val _isGeminiLoading = MutableStateFlow<Boolean>(false)
    val isGeminiLoading: StateFlow<Boolean> = _isGeminiLoading.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<Pair<Boolean, String>>>(emptyList()) // true if user, false if AI
    val chatHistory: StateFlow<List<Pair<Boolean, String>>> = _chatHistory.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = MedicalRepository(
            database.hospitalVisitDao(),
            database.appointmentReminderDao()
        )

        allVisits = repository.allVisits.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allReminders = repository.allReminders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Add a friendly welcome message to Chat History
        _chatHistory.value = listOf(
            Pair(false, "Hello! I am your Cancer Care AI Guide. Ask me anything about cancer symptoms, types, prevention, early detection, or modern treatment options. How can I support you today?")
        )
    }

    // Hospital Visit functions
    fun addVisit(visit: HospitalVisit) {
        viewModelScope.launch {
            repository.insertVisit(visit)
        }
    }

    fun updateVisit(visit: HospitalVisit) {
        viewModelScope.launch {
            repository.updateVisit(visit)
        }
    }

    fun deleteVisit(visit: HospitalVisit) {
        viewModelScope.launch {
            repository.deleteVisit(visit)
        }
    }

    // Appointment Reminder functions
    fun addReminder(reminder: AppointmentReminder) {
        viewModelScope.launch {
            val insertedId = repository.insertReminder(reminder)
            val insertedReminder = reminder.copy(id = insertedId.toInt())
            scheduleNotification(insertedReminder)
        }
    }

    fun updateReminder(reminder: AppointmentReminder) {
        viewModelScope.launch {
            repository.updateReminder(reminder)
            scheduleNotification(reminder)
        }
    }

    fun deleteReminder(reminder: AppointmentReminder) {
        viewModelScope.launch {
            cancelNotification(reminder.id)
            repository.deleteReminder(reminder)
        }
    }

    // Cancer AI Assistant
    fun askCancerAdvisor(question: String) {
        if (question.isBlank()) return

        // Append user question
        _chatHistory.value = _chatHistory.value + Pair(true, question)
        _isGeminiLoading.value = true

        val systemInstruction = """
            You are a compassionate, expert Cancer Education and Early Prevention AI Assistant.
            Your purpose is to provide clear, scientifically-accurate, and supportive information regarding:
            1. Cancer risk factors, general warning signs, and symptoms.
            2. Methods of early detection, screens (like mammograms, colonoscopy), and early curative paths.
            3. Guidelines for maintaining healthy habits to reduce risk.
            
            Strict Guidelines:
            - Provide clear, high-quality information structured with bullet points.
            - NEVER give diagnostic claims for a specific person.
            - ALWAYS include a brief compassionate message reminding them to seek expert clinical consultation for any persistent symptoms or physical concerns.
            - Maintain an optimistic, informative, and professional tone.
        """.trimIndent()

        viewModelScope.launch {
            try {
                val response = GeminiService.askGemini(question, systemInstruction)
                _geminiResponse.value = response
                _chatHistory.value = _chatHistory.value + Pair(false, response)
            } catch (e: Exception) {
                val errorMsg = "Sorry, I encountered an issue: ${e.localizedMessage}. Please try again."
                _geminiResponse.value = errorMsg
                _chatHistory.value = _chatHistory.value + Pair(false, errorMsg)
            } finally {
                _isGeminiLoading.value = false
            }
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            Pair(false, "Hello! I am your Cancer Care AI Guide. Ask me anything about cancer symptoms, types, prevention, early detection, or modern treatment options. How can I support you today?")
        )
        _geminiResponse.value = ""
    }

    // System alarm scheduling
    private fun scheduleNotification(reminder: AppointmentReminder) {
        val context = getApplication<Application>().applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", reminder.id)
            putExtra("title", reminder.title)
            putExtra("notes", reminder.notes)
            putExtra("hospital", reminder.hospitalName)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (reminder.dateTimeMillis > System.currentTimeMillis()) {
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    reminder.dateTimeMillis,
                    pendingIntent
                )
                Log.d("MedicalViewModel", "Successfully scheduled alarm for ${reminder.title} at ${reminder.dateTimeMillis}")
            } catch (e: SecurityException) {
                // Exact alarms might require special permissions on Android 12+
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    reminder.dateTimeMillis,
                    pendingIntent
                )
                Log.d("MedicalViewModel", "Scheduled non-exact alarm as exact alarm permission is missing.")
            } catch (e: Exception) {
                Log.e("MedicalViewModel", "Failed to schedule alarm", e)
            }
        }
    }

    private fun cancelNotification(reminderId: Int) {
        val context = getApplication<Application>().applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminderId,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("MedicalViewModel", "Successfully cancelled alarm ID: $reminderId")
        }
    }
}
