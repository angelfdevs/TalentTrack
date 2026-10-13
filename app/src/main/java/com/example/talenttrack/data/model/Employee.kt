package com.example.talenttrack.data.model

/**
 * [ADAPTAR] Modelo específico de TalentTrack: sus propiedades corresponden al JSON
 * de empleados y a las columnas SQLite. En otro caso, reemplaza entidad, campos y tipos.
 */
data class Employee (
    val id: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val profession: String,
    val position: String,
    val yearsExperience: Int,
    val email: String,
    val phone: String,
    val address: String,
    val district: String,
    val city: String,
    val country: String,
    val maritalStatus: String,
    val gender: String,
    val salary: Int,
    val department: String,
    val photoUrl: String

)
