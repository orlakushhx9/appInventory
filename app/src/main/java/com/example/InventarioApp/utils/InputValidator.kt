package com.example.InventarioApp.utils

import android.text.TextUtils
import java.util.regex.Pattern

object InputValidator {
    
    // Patrones para prevenir inyección de SQL y XSS
    private val SQL_INJECTION_PATTERN = Pattern.compile(
        ".*(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION|SCRIPT|JAVASCRIPT|ONLOAD|ONERROR|ONCLICK|WHERE|FROM|AND|OR|NOT|LIKE|INTO|VALUES|SET|JOIN|HAVING|GROUP|ORDER|BY|LIMIT|OFFSET|ASC|DESC|DISTINCT|COUNT|SUM|AVG|MAX|MIN)\\b).*",
        Pattern.CASE_INSENSITIVE
    )
    
    private val XSS_PATTERN = Pattern.compile(
        ".*(<script|javascript:|vbscript:|onload|onerror|onclick|onmouseover|onfocus|onblur|<iframe|<object|<embed|<form).*",
        Pattern.CASE_INSENSITIVE
    )
    
    private val HTML_TAG_PATTERN = Pattern.compile("<[^>]*>")
    
    // Patrón para email válido (más estricto)
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    )
    
    // Patrón para números enteros positivos
    private val POSITIVE_INTEGER_PATTERN = Pattern.compile("^[1-9]\\d*$")
    
    // Patrón para números decimales positivos
    private val POSITIVE_DECIMAL_PATTERN = Pattern.compile("^[1-9]\\d*(\\.\\d+)?$|^0\\.\\d+$")
    
    // Patrón para nombres (solo letras, espacios y algunos caracteres especiales)
    private val NAME_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s\\-']{2,50}$")
    
    // Patrón para códigos de producto (letras, números y guiones)
    private val PRODUCT_CODE_PATTERN = Pattern.compile("^[A-Za-z0-9\\-]{3,20}$")
    
    /**
     * Valida email y previene inyecciones
     */
    fun validateEmail(email: String): ValidationResult {
        if (TextUtils.isEmpty(email)) {
            return ValidationResult(false, "El email es requerido")
        }
        
        if (containsInjection(email)) {
            return ValidationResult(false, "El email contiene caracteres no permitidos")
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult(false, "Formato de email inválido")
        }
        
        if (email.length > 254) {
            return ValidationResult(false, "El email es demasiado largo")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Valida contraseña y previene inyecciones (validación estricta)
     */
    fun validatePassword(password: String): ValidationResult {
        if (TextUtils.isEmpty(password)) {
            return ValidationResult(false, "La contraseña es requerida")
        }
        
        if (containsInjection(password)) {
            return ValidationResult(false, "La contraseña contiene caracteres no permitidos")
        }
        
        if (password.length < 12) {
            return ValidationResult(false, "La contraseña debe tener al menos 12 caracteres")
        }
        
        if (password.length > 128) {
            return ValidationResult(false, "La contraseña es demasiado larga")
        }
        
        // Validaciones adicionales de seguridad
        if (!Pattern.compile("[A-Z]").matcher(password).find()) {
            return ValidationResult(false, "La contraseña debe contener al menos una mayúscula")
        }
        
        if (!Pattern.compile("[a-z]").matcher(password).find()) {
            return ValidationResult(false, "La contraseña debe contener al menos una minúscula")
        }
        
        if (!Pattern.compile("[0-9]").matcher(password).find()) {
            return ValidationResult(false, "La contraseña debe contener al menos un número")
        }
        
        if (!Pattern.compile("[!@#\$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]").matcher(password).find()) {
            return ValidationResult(false, "La contraseña debe contener al menos un carácter especial")
        }
        
        // Verificar patrones comunes de contraseñas débiles
        if (isCommonPassword(password)) {
            return ValidationResult(false, "La contraseña es demasiado común, elige una más segura")
        }
        
        // Verificar secuencias repetitivas
        if (hasRepeatingPatterns(password)) {
            return ValidationResult(false, "La contraseña no debe contener secuencias repetitivas")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Valida nombre de producto y previene inyecciones
     */
    fun validateProductName(name: String): ValidationResult {
        if (TextUtils.isEmpty(name)) {
            return ValidationResult(false, "El nombre del producto es requerido")
        }
        
        if (containsInjection(name)) {
            return ValidationResult(false, "El nombre contiene caracteres no permitidos")
        }
        
        if (name.length < 2) {
            return ValidationResult(false, "El nombre debe tener al menos 2 caracteres")
        }
        
        if (name.length > 100) {
            return ValidationResult(false, "El nombre es demasiado largo")
        }
        
        // Remover HTML tags si existen
        val cleanName = HTML_TAG_PATTERN.matcher(name).replaceAll("")
        if (cleanName != name) {
            return ValidationResult(false, "El nombre no puede contener etiquetas HTML")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Valida cantidad (número entero positivo)
     */
    fun validateQuantity(quantity: String): ValidationResult {
        if (TextUtils.isEmpty(quantity)) {
            return ValidationResult(false, "La cantidad es requerida")
        }
        
        if (containsInjection(quantity)) {
            return ValidationResult(false, "La cantidad contiene caracteres no permitidos")
        }
        
        if (!POSITIVE_INTEGER_PATTERN.matcher(quantity).matches()) {
            return ValidationResult(false, "La cantidad debe ser un número entero positivo")
        }
        
        val quantityInt = quantity.toIntOrNull()
        if (quantityInt == null || quantityInt <= 0) {
            return ValidationResult(false, "La cantidad debe ser mayor a 0")
        }
        
        if (quantityInt > 999999) {
            return ValidationResult(false, "La cantidad es demasiado alta")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Valida precio (número decimal positivo)
     */
    fun validatePrice(price: String): ValidationResult {
        if (TextUtils.isEmpty(price)) {
            return ValidationResult(false, "El precio es requerido")
        }
        
        if (containsInjection(price)) {
            return ValidationResult(false, "El precio contiene caracteres no permitidos")
        }
        
        if (!POSITIVE_DECIMAL_PATTERN.matcher(price).matches()) {
            return ValidationResult(false, "El precio debe ser un número positivo")
        }
        
        val priceDouble = price.toDoubleOrNull()
        if (priceDouble == null || priceDouble <= 0) {
            return ValidationResult(false, "El precio debe ser mayor a 0")
        }
        
        if (priceDouble > 999999.99) {
            return ValidationResult(false, "El precio es demasiado alto")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Valida stock (número entero positivo)
     */
    fun validateStock(stock: String): ValidationResult {
        if (TextUtils.isEmpty(stock)) {
            return ValidationResult(false, "El stock es requerido")
        }
        
        if (containsInjection(stock)) {
            return ValidationResult(false, "El stock contiene caracteres no permitidos")
        }
        
        if (!POSITIVE_INTEGER_PATTERN.matcher(stock).matches()) {
            return ValidationResult(false, "El stock debe ser un número entero positivo")
        }
        
        val stockInt = stock.toIntOrNull()
        if (stockInt == null || stockInt < 0) {
            return ValidationResult(false, "El stock debe ser mayor o igual a 0")
        }
        
        if (stockInt > 999999) {
            return ValidationResult(false, "El stock es demasiado alto")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Valida nombre de usuario (validación estricta)
     */
    fun validateUsername(username: String): ValidationResult {
        if (TextUtils.isEmpty(username)) {
            return ValidationResult(false, "El nombre de usuario es requerido")
        }
        
        if (containsInjection(username)) {
            return ValidationResult(false, "El nombre de usuario contiene caracteres no permitidos")
        }
        
        if (username.length < 4) {
            return ValidationResult(false, "El nombre de usuario debe tener al menos 4 caracteres")
        }
        
        if (username.length > 30) {
            return ValidationResult(false, "El nombre de usuario debe tener máximo 30 caracteres")
        }
        
        // Solo letras y números (sin guiones ni guiones bajos)
        if (!Pattern.compile("^[a-zA-Z0-9]+$").matcher(username).matches()) {
            return ValidationResult(false, "El nombre de usuario solo puede contener letras y números")
        }
        
        // Verificar nombres de usuario reservados
        if (isReservedUsername(username)) {
            return ValidationResult(false, "Este nombre de usuario no está disponible")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Valida nombre completo
     */
    fun validateFullName(name: String): ValidationResult {
        if (TextUtils.isEmpty(name)) {
            return ValidationResult(false, "El nombre es requerido")
        }
        
        if (containsInjection(name)) {
            return ValidationResult(false, "El nombre contiene caracteres no permitidos")
        }
        
        if (!NAME_PATTERN.matcher(name).matches()) {
            return ValidationResult(false, "El nombre solo puede contener letras, espacios y algunos caracteres especiales")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Valida código de producto
     */
    fun validateProductCode(code: String): ValidationResult {
        if (TextUtils.isEmpty(code)) {
            return ValidationResult(false, "El código del producto es requerido")
        }
        
        if (containsInjection(code)) {
            return ValidationResult(false, "El código contiene caracteres no permitidos")
        }
        
        if (!PRODUCT_CODE_PATTERN.matcher(code).matches()) {
            return ValidationResult(false, "El código debe tener entre 3 y 20 caracteres (letras, números y guiones)")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Valida texto genérico y previene inyecciones
     */
    fun validateGenericText(text: String, fieldName: String, minLength: Int = 1, maxLength: Int = 255): ValidationResult {
        if (TextUtils.isEmpty(text)) {
            return ValidationResult(false, "$fieldName es requerido")
        }
        
        if (containsInjection(text)) {
            return ValidationResult(false, "$fieldName contiene caracteres no permitidos")
        }
        
        if (text.length < minLength) {
            return ValidationResult(false, "$fieldName debe tener al menos $minLength caracteres")
        }
        
        if (text.length > maxLength) {
            return ValidationResult(false, "$fieldName es demasiado largo")
        }
        
        return ValidationResult(true, "")
    }
    
    /**
     * Verifica si el texto contiene patrones de inyección
     */
    private fun containsInjection(text: String): Boolean {
        val lowerText = text.lowercase()
        return SQL_INJECTION_PATTERN.matcher(lowerText).matches() ||
               XSS_PATTERN.matcher(lowerText).matches() ||
               HTML_TAG_PATTERN.matcher(text).find()
    }
    
    /**
     * Sanitiza el texto removiendo caracteres peligrosos
     */
    fun sanitizeText(text: String): String {
        return text.replace(Regex("[<>\"'&]"), "")
    }
    
    /**
     * Verifica si la contraseña es común o débil
     */
    private fun isCommonPassword(password: String): Boolean {
        val commonPasswords = setOf(
            "password", "123456", "123456789", "qwerty", "abc123", "password123",
            "admin", "letmein", "welcome", "monkey", "dragon", "master", "user",
            "login", "princess", "qwerty123", "solo", "passw0rd", "starwars",
            "freedom", "whatever", "trustno1", "jordan", "harley", "ranger",
            "iwantu", "jennifer", "hunter", "buster", "soccer", "baseball",
            "tiger", "charlie", "andrew", "michelle", "love", "sunshine",
            "jordan23", "iloveyou", "fuckyou", "2000", "football", "jordan",
            "superman", "harley", "1234567", "fuckme", "121212", "buster",
            "dragon", "baseball", "donald", "harley", "hunter", "trustno1",
            "ranger", "buster", "jordan", "charlie", "michelle", "andrew",
            "love", "sunshine", "jordan23", "iloveyou", "2000", "football",
            "superman", "harley", "1234567", "121212", "buster", "dragon",
            "baseball", "donald", "harley", "hunter", "trustno1", "ranger",
            "buster", "jordan", "charlie", "michelle", "andrew", "love",
            "sunshine", "jordan23", "iloveyou", "2000", "football", "superman"
        )
        
        return commonPasswords.contains(password.lowercase())
    }
    
    /**
     * Verifica si la contraseña tiene patrones repetitivos
     */
    private fun hasRepeatingPatterns(password: String): Boolean {
        // Verificar secuencias numéricas
        if (password.matches(Regex(".*(123|234|345|456|567|678|789|012|111|222|333|444|555|666|777|888|999).*"))) {
            return true
        }
        
        // Verificar caracteres repetidos consecutivos
        if (password.matches(Regex(".*(.)\\1{2,}.*"))) {
            return true
        }
        
        // Verificar patrones de teclado
        val keyboardPatterns = listOf("qwerty", "asdfgh", "zxcvbn", "123456", "654321")
        val lowerPassword = password.lowercase()
        return keyboardPatterns.any { lowerPassword.contains(it) }
        
        return false
    }
    
    /**
     * Verifica si el nombre de usuario está reservado
     */
    private fun isReservedUsername(username: String): Boolean {
        val reservedUsernames = setOf(
            "admin", "administrator", "root", "system", "user", "guest", "test",
            "demo", "example", "sample", "info", "support", "help", "contact",
            "mail", "email", "webmaster", "noreply", "no-reply", "postmaster",
            "hostmaster", "usenet", "news", "uucp", "operator", "manager",
            "moderator", "staff", "team", "service", "api", "bot", "robot",
            "crawler", "spider", "anonymous", "unknown", "nobody", "everyone",
            "all", "public", "private", "internal", "external", "local",
            "remote", "server", "client", "database", "backup", "temp",
            "tmp", "cache", "log", "logs", "error", "debug", "dev", "development",
            "prod", "production", "staging", "test", "testing", "qa", "quality",
            "security", "auth", "authentication", "login", "logout", "register",
            "signup", "signin", "password", "reset", "forgot", "recover",
            "verify", "confirm", "activate", "deactivate", "enable", "disable",
            "lock", "unlock", "block", "unblock", "ban", "unban", "delete",
            "remove", "create", "update", "edit", "modify", "change", "set",
            "get", "post", "put", "delete", "head", "options", "trace",
            "connect", "patch", "search", "find", "list", "show", "view",
            "display", "print", "export", "import", "upload", "download",
            "save", "load", "store", "retrieve", "fetch", "send", "receive",
            "submit", "cancel", "close", "open", "start", "stop", "pause",
            "resume", "restart", "refresh", "reload", "back", "forward",
            "next", "previous", "first", "last", "home", "index", "main",
            "default", "custom", "personal", "private", "public", "shared",
            "common", "general", "specific", "special", "unique", "random",
            "auto", "automatic", "manual", "hand", "free", "paid", "premium",
            "basic", "standard", "advanced", "pro", "professional", "enterprise",
            "business", "commercial", "personal", "home", "family", "individual",
            "group", "team", "organization", "company", "corporation", "firm",
            "agency", "department", "division", "unit", "section", "branch",
            "office", "location", "place", "site", "area", "region", "zone",
            "country", "state", "city", "town", "village", "street", "road",
            "avenue", "boulevard", "drive", "lane", "way", "path", "route",
            "highway", "freeway", "expressway", "parkway", "bridge", "tunnel",
            "station", "terminal", "airport", "port", "harbor", "dock", "pier",
            "beach", "park", "garden", "forest", "mountain", "hill", "valley",
            "river", "lake", "ocean", "sea", "island", "peninsula", "bay",
            "gulf", "strait", "canal", "channel", "creek", "stream", "brook",
            "spring", "well", "fountain", "waterfall", "cascade", "rapids",
            "whirlpool", "eddy", "current", "tide", "wave", "surf", "swell",
            "breaker", "roller", "ripple", "splash", "spray", "mist", "fog",
            "cloud", "rain", "snow", "ice", "frost", "dew", "hail", "sleet",
            "storm", "thunder", "lightning", "wind", "breeze", "gust", "gale",
            "hurricane", "tornado", "cyclone", "typhoon", "blizzard", "avalanche",
            "earthquake", "volcano", "tsunami", "flood", "drought", "famine",
            "plague", "epidemic", "pandemic", "disease", "illness", "sickness",
            "health", "medical", "doctor", "nurse", "patient", "hospital",
            "clinic", "pharmacy", "drug", "medicine", "treatment", "therapy",
            "surgery", "operation", "procedure", "examination", "test", "check",
            "scan", "xray", "mri", "ct", "ultrasound", "ecg", "eeg", "blood",
            "urine", "stool", "tissue", "organ", "bone", "muscle", "nerve",
            "brain", "heart", "lung", "liver", "kidney", "stomach", "intestine",
            "skin", "hair", "eye", "ear", "nose", "mouth", "tongue", "tooth",
            "finger", "hand", "arm", "leg", "foot", "toe", "head", "neck",
            "chest", "back", "shoulder", "elbow", "wrist", "knee", "ankle",
            "hip", "waist", "belly", "butt", "ass", "penis", "vagina", "breast",
            "nipple", "clitoris", "testicle", "scrotum", "anus", "rectum",
            "prostate", "uterus", "ovary", "fallopian", "cervix", "placenta",
            "fetus", "baby", "child", "kid", "teen", "adult", "elder", "senior",
            "young", "old", "new", "fresh", "ripe", "mature", "grown", "big",
            "small", "large", "huge", "tiny", "giant", "dwarf", "tall", "short",
            "long", "wide", "narrow", "thick", "thin", "fat", "skinny", "slim",
            "heavy", "light", "strong", "weak", "hard", "soft", "firm", "loose",
            "tight", "loose", "free", "bound", "tied", "untied", "open", "closed",
            "locked", "unlocked", "secure", "safe", "dangerous", "risky", "safe",
            "unsafe", "protected", "unprotected", "guarded", "unguarded", "watched",
            "unwatched", "monitored", "unmonitored", "tracked", "untracked", "followed",
            "unfollowed", "liked", "unliked", "favorited", "unfavorited", "bookmarked",
            "unbookmarked", "pinned", "unpinned", "starred", "unstarred", "rated",
            "unrated", "reviewed", "unreviewed", "commented", "uncommented", "shared",
            "unshared", "published", "unpublished", "draft", "final", "complete",
            "incomplete", "finished", "unfinished", "done", "undone", "ready",
            "unready", "prepared", "unprepared", "set", "unset", "configured",
            "unconfigured", "installed", "uninstalled", "enabled", "disabled",
            "active", "inactive", "online", "offline", "connected", "disconnected",
            "available", "unavailable", "busy", "idle", "away", "present", "absent",
            "here", "there", "somewhere", "nowhere", "everywhere", "anywhere",
            "somewhere", "elsewhere", "nearby", "far", "close", "distant", "remote",
            "local", "global", "worldwide", "international", "national", "regional",
            "provincial", "state", "county", "city", "town", "village", "hamlet",
            "settlement", "community", "neighborhood", "district", "area", "zone",
            "region", "territory", "land", "ground", "soil", "earth", "world",
            "planet", "universe", "galaxy", "star", "sun", "moon", "planet",
            "asteroid", "comet", "meteor", "satellite", "space", "cosmos", "void",
            "empty", "full", "occupied", "vacant", "available", "taken", "free",
            "busy", "engaged", "unavailable", "reserved", "booked", "scheduled",
            "planned", "arranged", "organized", "structured", "ordered", "sorted",
            "arranged", "organized", "managed", "controlled", "directed", "guided",
            "led", "followed", "accompanied", "joined", "left", "stayed", "remained",
            "continued", "stopped", "paused", "resumed", "restarted", "repeated",
            "recycled", "reused", "renewed", "refreshed", "updated", "upgraded",
            "improved", "enhanced", "boosted", "increased", "decreased", "reduced",
            "minimized", "maximized", "optimized", "tuned", "adjusted", "modified",
            "changed", "altered", "transformed", "converted", "translated", "interpreted",
            "explained", "described", "defined", "specified", "detailed", "elaborated",
            "expanded", "extended", "stretched", "pulled", "pushed", "moved", "shifted",
            "transferred", "transported", "delivered", "sent", "received", "accepted",
            "rejected", "approved", "denied", "granted", "refused", "allowed", "forbidden",
            "permitted", "prohibited", "restricted", "limited", "unlimited", "boundless",
            "infinite", "finite", "definite", "indefinite", "certain", "uncertain",
            "sure", "unsure", "confident", "doubtful", "positive", "negative", "neutral",
            "balanced", "unbalanced", "stable", "unstable", "steady", "unsteady",
            "firm", "loose", "tight", "relaxed", "tense", "stressed", "calm", "excited",
            "bored", "interested", "curious", "indifferent", "careful", "careless",
            "attentive", "distracted", "focused", "scattered", "concentrated", "diluted",
            "pure", "impure", "clean", "dirty", "fresh", "stale", "new", "old",
            "modern", "ancient", "contemporary", "traditional", "classic", "vintage",
            "antique", "retro", "futuristic", "sci-fi", "fantasy", "realistic",
            "abstract", "concrete", "solid", "liquid", "gas", "plasma", "energy",
            "matter", "material", "substance", "element", "compound", "mixture",
            "solution", "suspension", "colloid", "emulsion", "foam", "gel", "paste",
            "powder", "granule", "particle", "atom", "molecule", "ion", "radical",
            "acid", "base", "salt", "oxide", "hydroxide", "carbonate", "sulfate",
            "nitrate", "phosphate", "chloride", "fluoride", "bromide", "iodide",
            "sulfide", "oxide", "nitride", "carbide", "boride", "silicide", "phosphide",
            "arsenide", "antimonide", "bismuthide", "selenide", "telluride", "polonide",
            "astatide", "radonide", "francium", "radium", "actinium", "thorium",
            "protactinium", "uranium", "neptunium", "plutonium", "americium", "curium",
            "berkelium", "californium", "einsteinium", "fermium", "mendelevium",
            "nobelium", "lawrencium", "rutherfordium", "dubnium", "seaborgium",
            "bohrium", "hassium", "meitnerium", "darmstadtium", "roentgenium",
            "copernicium", "nihonium", "flerovium", "moscovium", "livermorium",
            "tennessine", "oganesson", "hydrogen", "helium", "lithium", "beryllium",
            "boron", "carbon", "nitrogen", "oxygen", "fluorine", "neon", "sodium",
            "magnesium", "aluminum", "silicon", "phosphorus", "sulfur", "chlorine",
            "argon", "potassium", "calcium", "scandium", "titanium", "vanadium",
            "chromium", "manganese", "iron", "cobalt", "nickel", "copper", "zinc",
            "gallium", "germanium", "arsenic", "selenium", "bromine", "krypton",
            "rubidium", "strontium", "yttrium", "zirconium", "niobium", "molybdenum",
            "technetium", "ruthenium", "rhodium", "palladium", "silver", "cadmium",
            "indium", "tin", "antimony", "tellurium", "iodine", "xenon", "cesium",
            "barium", "lanthanum", "cerium", "praseodymium", "neodymium", "promethium",
            "samarium", "europium", "gadolinium", "terbium", "dysprosium", "holmium",
            "erbium", "thulium", "ytterbium", "lutetium", "hafnium", "tantalum",
            "tungsten", "rhenium", "osmium", "iridium", "platinum", "gold", "mercury",
            "thallium", "lead", "bismuth", "polonium", "astatine", "radon"
        )
        
        return reservedUsernames.contains(username.lowercase())
    }
    
    /**
     * Resultado de validación
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String
    )
}
