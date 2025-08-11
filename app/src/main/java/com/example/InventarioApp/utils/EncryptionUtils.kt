package com.example.InventarioApp.utils

import android.util.Base64
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import java.security.SecureRandom

object EncryptionUtils {
    
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_SIZE = 256
    
    // Clave secreta - en producción debería estar en un lugar seguro
    private const val SECRET_KEY = "InventarioApp2024SecureKey123!@#"
    
    fun encrypt(data: String): String {
        try {
            val key = generateKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            
            // Generar IV aleatorio
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)
            
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            val encrypted = cipher.doFinal(data.toByteArray())
            
            // Combinar IV + datos encriptados
            val combined = iv + encrypted
            return Base64.encodeToString(combined, Base64.DEFAULT)
            
        } catch (e: Exception) {
            throw RuntimeException("Error al encriptar datos", e)
        }
    }
    
    fun decrypt(encryptedData: String): String {
        try {
            val key = generateKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            
            val decoded = Base64.decode(encryptedData, Base64.DEFAULT)
            
            // Separar IV y datos encriptados
            val iv = decoded.copyOfRange(0, 16)
            val encrypted = decoded.copyOfRange(16, decoded.size)
            
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            
            val decrypted = cipher.doFinal(encrypted)
            return String(decrypted)
            
        } catch (e: Exception) {
            throw RuntimeException("Error al desencriptar datos", e)
        }
    }
    
    private fun generateKey(): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(SECRET_KEY.toByteArray())
        return SecretKeySpec(hash, ALGORITHM)
    }
    
    // Función para crear un payload encriptado para la API
    fun createEncryptedPayload(data: Map<String, Any>): String {
        val jsonString = data.toJsonString()
        return encrypt(jsonString)
    }
    
    // Función para desencriptar respuesta de la API
    fun decryptApiResponse(encryptedResponse: String): String {
        return decrypt(encryptedResponse)
    }
    
    private fun Map<String, Any>.toJsonString(): String {
        val jsonBuilder = StringBuilder("{")
        val entries = this.entries.toList()
        
        entries.forEachIndexed { index, entry ->
            jsonBuilder.append("\"${entry.key}\":")
            
            when (val value = entry.value) {
                is String -> jsonBuilder.append("\"$value\"")
                is Number -> jsonBuilder.append(value.toString())
                is Boolean -> jsonBuilder.append(value.toString())
                else -> jsonBuilder.append("\"${value.toString()}\"")
            }
            
            if (index < entries.size - 1) {
                jsonBuilder.append(",")
            }
        }
        
        jsonBuilder.append("}")
        return jsonBuilder.toString()
    }
} 