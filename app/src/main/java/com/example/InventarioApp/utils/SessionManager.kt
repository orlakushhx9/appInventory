package com.example.InventarioApp.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.InventarioApp.ui.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.atomic.AtomicBoolean

class SessionManager private constructor(private val context: Context) {
    
    companion object {
        private const val SESSION_TIMEOUT_MS = 60000L 
        private var instance: SessionManager? = null
        
        fun getInstance(context: Context): SessionManager {
            if (instance == null) {
                instance = SessionManager(context.applicationContext)
            }
            return instance!!
        }
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private val isSessionActive = AtomicBoolean(false)
    private val logoutRunnable = Runnable {
        logoutUser()
    }
    
    /**
     * Inicia la sesión y el temporizador de inactividad
     * Solo debe llamarse cuando el usuario ya está logueado
     */
    fun startSession() {
        isSessionActive.set(true)
        resetSessionTimer()
        android.util.Log.d("SessionManager", "Sesión iniciada - Timeout: ${SESSION_TIMEOUT_MS}ms")
        
        // Iniciar un timeout adicional como respaldo
        startBackupTimeout()
    }
    
    /**
     * Detiene la sesión
     */
    fun stopSession() {
        isSessionActive.set(false)
        handler.removeCallbacks(logoutRunnable)
    }
    
    /**
     * Reinicia el temporizador de sesión
     */
    fun resetSessionTimer() {
        if (isSessionActive.get()) {
            handler.removeCallbacks(logoutRunnable)
            handler.postDelayed(logoutRunnable, SESSION_TIMEOUT_MS)
            android.util.Log.d("SessionManager", "Temporizador reiniciado - ${SESSION_TIMEOUT_MS}ms")
        }
    }
    
    /**
     * Configura el detector de actividad para una actividad
     */
    fun setupActivityMonitoring(activity: Activity) {
        // Usar dispatchTouchEvent en lugar de OnTouchListener
        activity.window.decorView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || 
                event.action == MotionEvent.ACTION_MOVE ||
                event.action == MotionEvent.ACTION_UP) {
                resetSessionTimer()
                android.util.Log.d("SessionManager", "Actividad detectada - reiniciando timer")
            }
            false // No consumir el evento
        }
        
        android.util.Log.d("SessionManager", "Monitoreo de actividad configurado para: ${activity.javaClass.simpleName}")
    }
    
    /**
     * Configura el detector de actividad para una vista específica
     */
    fun setupViewMonitoring(view: View) {
        view.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN || 
                event.action == MotionEvent.ACTION_MOVE ||
                event.action == MotionEvent.ACTION_UP) {
                resetSessionTimer()
                android.util.Log.d("SessionManager", "Actividad en vista detectada - reiniciando timer")
            }
            false // No consumir el evento
        }
    }
    
    /**
     * Cierra la sesión del usuario
     */
    private fun logoutUser() {
        android.util.Log.d("SessionManager", "Ejecutando logout por timeout")
        if (isSessionActive.get()) {
            isSessionActive.set(false)
            
            // Cerrar sesión de Firebase
            FirebaseAuth.getInstance().signOut()
            
            // Mostrar mensaje al usuario
            Toast.makeText(
                context,
                "Sesión cerrada por inactividad",
                Toast.LENGTH_LONG
            ).show()
            
            // Navegar a la pantalla de login
            val intent = Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }
    }
    
    /**
     * Verifica si la sesión está activa
     */
    fun isSessionActive(): Boolean {
        return isSessionActive.get()
    }
    
    /**
     * Obtiene el tiempo restante de la sesión en milisegundos
     */
    fun getRemainingSessionTime(): Long {
        return if (isSessionActive.get()) {
            val currentTime = System.currentTimeMillis()
            val lastActivityTime = currentTime - SESSION_TIMEOUT_MS
            // Nota: Esta es una aproximación ya que no guardamos el tiempo exacto de la última actividad
            SESSION_TIMEOUT_MS
        } else {
            0L
        }
    }
    
    /**
     * Método para pruebas - fuerza el logout inmediatamente
     */
    fun forceLogout() {
        android.util.Log.d("SessionManager", "Forzando logout")
        logoutUser()
    }
    
    /**
     * Método para pruebas - inicia un timeout de prueba
     */
    fun startTestTimeout() {
        android.util.Log.d("SessionManager", "Iniciando timeout de prueba")
        isSessionActive.set(true)
        handler.removeCallbacks(logoutRunnable)
        handler.postDelayed(logoutRunnable, SESSION_TIMEOUT_MS)
        android.util.Log.d("SessionManager", "Timeout de prueba iniciado - ${SESSION_TIMEOUT_MS}ms")
    }
    
    /**
     * Timeout de respaldo que se ejecuta independientemente
     */
    private fun startBackupTimeout() {
        android.util.Log.d("SessionManager", "Iniciando timeout de respaldo")
        handler.postDelayed({
            if (isSessionActive.get()) {
                android.util.Log.d("SessionManager", "Timeout de respaldo ejecutándose")
                logoutUser()
            }
        }, SESSION_TIMEOUT_MS + 1000) // 1 segundo adicional como margen
    }
}
