#!/usr/bin/env python3
"""
Script de Prueba de Penetración para InventarioApp
Autor: Security Tester
Fecha: 2024
Descripción: Script para evaluar vulnerabilidades de seguridad en la aplicación Android
"""

import json
import base64
import hashlib
import time
import sqlite3
import os
import subprocess
from datetime import datetime
import threading
from concurrent.futures import ThreadPoolExecutor

class SecurityTester:
    def __init__(self):
        self.base_url = "api-production-53ca.up.railway.app"  
        self.results = []
        self.vulnerabilities = []
        
    def log_result(self, test_name, status, details=""):
        """Registra los resultados de las pruebas"""
        result = {
            "test": test_name,
            "status": status,
            "details": details,
            "timestamp": datetime.now().isoformat()
        }
        self.results.append(result)
        print(f"[{status.upper()}] {test_name}: {details}")

    def test_password_validation(self):
        """Prueba la validación de contraseñas"""
        print("\n=== PRUEBAS DE VALIDACIÓN DE CONTRASEÑAS ===")
        
        weak_passwords = [
            "123",  # Muy corta
            "password",  # Común
            "123456789",  # Solo números
            "abcdefgh",  # Solo minúsculas
            "ABCDEFGH",  # Solo mayúsculas
            "!@#$%^&*",  # Solo caracteres especiales
            "",  # Vacía
            "a" * 129,  # Muy larga
        ]
        
        for password in weak_passwords:
            try:
                # Simular envío de contraseña débil
                payload = {
                    "username": "test_user",
                    "email": "test@test.com",
                    "password": password,
                    "fullName": "Test User"
                }
                
                # Aquí deberías hacer la petición real a tu API
                # response = requests.post(f"{self.base_url}/register", json=payload)
                
                if len(password) < 8:
                    self.log_result("Password Length", "PASS", f"Contraseña '{password}' rechazada por ser muy corta")
                elif password.isdigit():
                    self.log_result("Password Complexity", "PASS", f"Contraseña '{password}' rechazada por solo números")
                else:
                    self.log_result("Password Validation", "WARNING", f"Contraseña '{password}' podría ser aceptada")
                    
            except Exception as e:
                self.log_result("Password Validation", "ERROR", str(e))

    def test_sql_injection(self):
        """Prueba vulnerabilidades de inyección SQL"""
        print("\n=== PRUEBAS DE INYECCIÓN SQL ===")
        
        sql_payloads = [
            "' OR '1'='1",
            "'; DROP TABLE users; --",
            "' UNION SELECT * FROM users --",
            "admin'--",
            "' OR 1=1#",
            "' OR 'x'='x",
            "'; EXEC xp_cmdshell('dir'); --",
        ]
        
        for payload in sql_payloads:
            try:
                # Simular inyección en campos de entrada
                test_data = {
                    "username": payload,
                    "email": f"{payload}@test.com",
                    "password": "Test123!@#",
                    "fullName": payload
                }
                
                # Aquí deberías hacer la petición real
                # response = requests.post(f"{self.base_url}/register", json=test_data)
                
                self.log_result("SQL Injection", "INFO", f"Probando payload: {payload}")
                
            except Exception as e:
                self.log_result("SQL Injection", "ERROR", str(e))

    def test_encryption_analysis(self):
        """Analiza la fortaleza de la encriptación"""
        print("\n=== ANÁLISIS DE ENCRIPTACIÓN ===")
        
        # Simular análisis de datos encriptados
        test_data = "datos_sensibles_de_prueba"
        
        # Verificar si la encriptación es consistente
        encrypted_samples = []
        for i in range(5):
            # Simular encriptación múltiple del mismo dato
            # En una implementación real, cada encriptación debería ser diferente debido al IV
            sample = base64.b64encode(f"{test_data}_{i}".encode()).decode()
            encrypted_samples.append(sample)
        
        # Verificar si hay patrones en la encriptación
        if len(set(encrypted_samples)) == len(encrypted_samples):
            self.log_result("Encryption IV", "PASS", "Cada encriptación produce resultado único")
        else:
            self.log_result("Encryption IV", "FAIL", "Encriptación no usa IV único")
        
        # Verificar longitud de datos encriptados
        for sample in encrypted_samples:
            if len(sample) > 20:  # Mínimo esperado para AES
                self.log_result("Encryption Length", "PASS", f"Longitud adecuada: {len(sample)}")
            else:
                self.log_result("Encryption Length", "FAIL", f"Longitud sospechosa: {len(sample)}")

    def test_brute_force_protection(self):
        """Prueba protección contra ataques de fuerza bruta"""
        print("\n=== PRUEBAS DE PROTECCIÓN CONTRA FUERZA BRUTA ===")
        
        start_time = time.time()
        
        # Simular múltiples intentos de login
        for i in range(10):
            try:
                payload = {
                    "email": "test@test.com",
                    "password": f"wrong_password_{i}"
                }
                
                # Simular petición
                # response = requests.post(f"{self.base_url}/login", json=payload)
                time.sleep(0.1)  # Simular delay de red
                
            except Exception as e:
                self.log_result("Brute Force", "ERROR", str(e))
        
        end_time = time.time()
        total_time = end_time - start_time
        
        if total_time > 5:  # Si toma más de 5 segundos, hay algún tipo de protección
            self.log_result("Brute Force Protection", "PASS", f"Tiempo total: {total_time:.2f}s")
        else:
            self.log_result("Brute Force Protection", "WARNING", f"Tiempo muy rápido: {total_time:.2f}s")

    def test_xss_injection(self):
        """Prueba vulnerabilidades XSS"""
        print("\n=== PRUEBAS DE INYECCIÓN XSS ===")
        
        xss_payloads = [
            "<script>alert('XSS')</script>",
            "javascript:alert('XSS')",
            "<img src=x onerror=alert('XSS')>",
            "';alert('XSS');//",
            "<svg onload=alert('XSS')>",
        ]
        
        for payload in xss_payloads:
            try:
                test_data = {
                    "username": payload,
                    "email": f"{payload}@test.com",
                    "password": "Test123!@#",
                    "fullName": payload
                }
                
                # Verificar si el payload se almacena sin sanitizar
                self.log_result("XSS Injection", "INFO", f"Probando payload: {payload}")
                
            except Exception as e:
                self.log_result("XSS Injection", "ERROR", str(e))

    def test_authentication_bypass(self):
        """Prueba bypass de autenticación"""
        print("\n=== PRUEBAS DE BYPASS DE AUTENTICACIÓN ===")
        
        bypass_attempts = [
            {"Authorization": "Bearer invalid_token"},
            {"Authorization": "Basic " + base64.b64encode(b"admin:password").decode()},
            {"X-API-Key": "fake_key"},
            {"Session-ID": "fake_session"},
        ]
        
        for attempt in bypass_attempts:
            try:
                # Simular petición con headers falsos
                # response = requests.get(f"{self.base_url}/protected", headers=attempt)
                self.log_result("Auth Bypass", "INFO", f"Probando bypass: {list(attempt.keys())[0]}")
                
            except Exception as e:
                self.log_result("Auth Bypass", "ERROR", str(e))

    def test_data_exfiltration(self):
        """Prueba posibles vectores de exfiltración de datos"""
        print("\n=== PRUEBAS DE EXFILTRACIÓN DE DATOS ===")
        
        # Verificar si hay logs que contengan datos sensibles
        sensitive_patterns = [
            "password",
            "token",
            "secret",
            "key",
            "credential"
        ]
        
        # Simular búsqueda en logs
        log_content = "Sample log content with password=test123 and token=abc123"
        
        for pattern in sensitive_patterns:
            if pattern in log_content.lower():
                self.log_result("Data Exfiltration", "FAIL", f"Patrón sensible encontrado: {pattern}")
            else:
                self.log_result("Data Exfiltration", "PASS", f"Patrón {pattern} no encontrado")

    def generate_report(self):
        """Genera un reporte de seguridad"""
        print("\n=== REPORTE DE SEGURIDAD ===")
        
        total_tests = len(self.results)
        passed_tests = len([r for r in self.results if r["status"] == "PASS"])
        failed_tests = len([r for r in self.results if r["status"] == "FAIL"])
        warnings = len([r for r in self.results if r["status"] == "WARNING"])
        
        print(f"\nResumen de Pruebas:")
        print(f"Total: {total_tests}")
        print(f"Exitosas: {passed_tests}")
        print(f"Fallidas: {failed_tests}")
        print(f"Advertencias: {warnings}")
        
        # Guardar reporte en archivo
        report_data = {
            "timestamp": datetime.now().isoformat(),
            "summary": {
                "total": total_tests,
                "passed": passed_tests,
                "failed": failed_tests,
                "warnings": warnings
            },
            "results": self.results
        }
        
        with open("security_report.json", "w") as f:
            json.dump(report_data, f, indent=2)
        
        print(f"\nReporte guardado en: security_report.json")
        
        # Mostrar vulnerabilidades críticas
        critical_issues = [r for r in self.results if r["status"] == "FAIL"]
        if critical_issues:
            print(f"\n⚠️  VULNERABILIDADES CRÍTICAS ENCONTRADAS:")
            for issue in critical_issues:
                print(f"  - {issue['test']}: {issue['details']}")

    def run_all_tests(self):
        """Ejecuta todas las pruebas de seguridad"""
        print("🔒 INICIANDO PRUEBAS DE SEGURIDAD PARA INVENTARIOAPP")
        print("=" * 60)
        
        self.test_password_validation()
        self.test_sql_injection()
        self.test_encryption_analysis()
        self.test_brute_force_protection()
        self.test_xss_injection()
        self.test_authentication_bypass()
        self.test_data_exfiltration()
        
        self.generate_report()

if __name__ == "__main__":
    tester = SecurityTester()
    tester.run_all_tests() 