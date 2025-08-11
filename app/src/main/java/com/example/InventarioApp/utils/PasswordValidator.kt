package com.example.InventarioApp.utils

import java.util.regex.Pattern

enum class PasswordStrength {
    WEAK, MEDIUM, STRONG
}

data class PasswordValidationResult(
    val isValid: Boolean,
    val strength: PasswordStrength,
    val missingRequirements: List<String>,
    val strengthMessage: String
)

object PasswordValidator {
    
    private const val MIN_LENGTH = 12
    private const val MAX_LENGTH = 128
    
    fun validatePassword(password: String): PasswordValidationResult {
        val missingRequirements = mutableListOf<String>()
        
        // Verificar longitud mínima
        if (password.length < MIN_LENGTH) {
            missingRequirements.add("Mínimo $MIN_LENGTH caracteres")
        }
        
        // Verificar longitud mínima para contraseñas seguras
        if (password.length < 16) {
            missingRequirements.add("Recomendado: al menos 16 caracteres para mayor seguridad")
        }
        
        // Verificar longitud máxima
        if (password.length > MAX_LENGTH) {
            missingRequirements.add("Máximo $MAX_LENGTH caracteres")
        }
        
        // Verificar mayúsculas
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            missingRequirements.add("Al menos una mayúscula")
        }
        
        // Verificar minúsculas
        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            missingRequirements.add("Al menos una minúscula")
        }
        
        // Verificar números
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            missingRequirements.add("Al menos un número")
        }
        
        // Verificar caracteres especiales
        if (!Pattern.compile("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(password).find()) {
            missingRequirements.add("Al menos un carácter especial (!@#\$%^&*()_+-=[]{}|;':\",./<>?)")
        }
        
        // Calcular fortaleza
        val strength = calculateStrength(password)
        val strengthMessage = when (strength) {
            PasswordStrength.WEAK -> "Contraseña Débil - No cumple requisitos mínimos"
            PasswordStrength.MEDIUM -> "Contraseña Media - Cumple requisitos básicos"
            PasswordStrength.STRONG -> "Contraseña Fuerte - Excelente seguridad"
        }
        
        return PasswordValidationResult(
            isValid = missingRequirements.isEmpty(),
            strength = strength,
            missingRequirements = missingRequirements,
            strengthMessage = strengthMessage
        )
    }
    
    private fun calculateStrength(password: String): PasswordStrength {
        var score = 0
        
        // Longitud
        if (password.length >= 12) score += 1
        if (password.length >= 16) score += 1
        if (password.length >= 20) score += 1
        
        // Complejidad
        if (Pattern.compile("[A-Z]").matcher(password).find()) score += 1
        if (Pattern.compile("[a-z]").matcher(password).find()) score += 1
        if (Pattern.compile("[0-9]").matcher(password).find()) score += 1
        if (Pattern.compile("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(password).find()) score += 1
        
        // Verificar que no sea una contraseña común
        if (!isCommonPassword(password)) score += 1
        
        // Verificar que no tenga patrones repetitivos
        if (!hasRepeatingPatterns(password)) score += 1
        
        return when {
            score <= 3 -> PasswordStrength.WEAK
            score <= 5 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.STRONG
        }
    }
    
    private fun isCommonPassword(password: String): Boolean {
        val commonPasswords = setOf(
            "password", "123456", "123456789", "qwerty", "abc123", "password123",
            "admin", "letmein", "welcome", "monkey", "dragon", "master", "user"
        )
        return commonPasswords.contains(password.lowercase())
    }
    
    private fun hasRepeatingPatterns(password: String): Boolean {
        // Verificar secuencias numéricas
        if (password.matches(Regex(".*(123|234|345|456|567|678|789|012|111|222|333|444|555|666|777|888|999).*"))) {
            return true
        }
        
        // Verificar caracteres repetidos consecutivos
        if (password.matches(Regex(".*(.)\\1{2,}.*"))) {
            return true
        }
        
        return false
    }
} 