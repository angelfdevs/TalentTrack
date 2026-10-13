package com.example.talenttrack.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.talenttrack.data.model.Employee

/**
 * [ADAPTAR / OPCIONAL] SQLiteOpenHelper es un patrón reutilizable.
 * El nombre de la base, tabla y columnas describen los favoritos de empleados.
 * Si el caso no pide persistencia local, se puede quitar el helper y sus llamadas.
 */
class EmployeeDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, "talenttrack.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val query = """
            CREATE TABLE $TABLE_NAME (
                id TEXT PRIMARY KEY NOT NULL,
                first_name TEXT NOT NULL,
                last_name TEXT NOT NULL,
                age INTEGER NOT NULL,
                profession TEXT NOT NULL,
                position TEXT NOT NULL,
                years_experience INTEGER NOT NULL,
                email TEXT NOT NULL,
                phone TEXT NOT NULL,
                address TEXT NOT NULL,
                district TEXT NOT NULL,
                city TEXT NOT NULL,
                country TEXT NOT NULL,
                marital_status TEXT NOT NULL,
                gender TEXT NOT NULL,
                salary INTEGER NOT NULL,
                department TEXT NOT NULL,
                photo_url TEXT NOT NULL
            )
        """.trimIndent()

        db.execSQL(query)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        // Aquí agregaremos una migración si cambia la estructura de la tabla.
    }

    /** [ADAPTAR / OPCIONAL] Inserta un favorito; cambia entidad y columnas al caso. */
    fun saveFavorite(employee: Employee): Boolean {
        val values = ContentValues().apply {
            put("id", employee.id)
            put("first_name", employee.firstName)
            put("last_name", employee.lastName)
            put("age", employee.age)
            put("profession", employee.profession)
            put("position", employee.position)
            put("years_experience", employee.yearsExperience)
            put("email", employee.email)
            put("phone", employee.phone)
            put("address", employee.address)
            put("district", employee.district)
            put("city", employee.city)
            put("country", employee.country)
            put("marital_status", employee.maritalStatus)
            put("gender", employee.gender)
            put("salary", employee.salary)
            put("department", employee.department)
            put("photo_url", employee.photoUrl)
        }

        val result = writableDatabase.insertWithOnConflict(
            TABLE_NAME,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )

        return result != -1L
    }

    /**
     * Actualiza un empleado ya guardado usando ContentValues, igual que el patrón
     * de inserción visto en clase. El id identifica el registro y no se modifica.
     */
    fun updateEmployee(employee: Employee): Boolean {
        val values = ContentValues().apply {
            put("first_name", employee.firstName)
            put("last_name", employee.lastName)
            put("age", employee.age)
            put("profession", employee.profession)
            put("position", employee.position)
            put("years_experience", employee.yearsExperience)
            put("email", employee.email)
            put("phone", employee.phone)
            put("address", employee.address)
            put("district", employee.district)
            put("city", employee.city)
            put("country", employee.country)
            put("marital_status", employee.maritalStatus)
            put("gender", employee.gender)
            put("salary", employee.salary)
            put("department", employee.department)
            put("photo_url", employee.photoUrl)
        }

        // El tercer argumento limita la edición al empleado seleccionado.
        val updatedRows = writableDatabase.update(
            TABLE_NAME,
            values,
            "id = ?",
            arrayOf(employee.id)
        )

        return updatedRows > 0
    }

    /** [ADAPTAR / OPCIONAL] Lee los registros locales ordenados por nombre. */
    fun getFavorites(): List<Employee> {
        val employees = mutableListOf<Employee>()

        readableDatabase.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "first_name ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                employees.add(
                    Employee(
                        id = cursor.getString(cursor.getColumnIndexOrThrow("id")),
                        firstName = cursor.getString(cursor.getColumnIndexOrThrow("first_name")),
                        lastName = cursor.getString(cursor.getColumnIndexOrThrow("last_name")),
                        age = cursor.getInt(cursor.getColumnIndexOrThrow("age")),
                        profession = cursor.getString(cursor.getColumnIndexOrThrow("profession")),
                        position = cursor.getString(cursor.getColumnIndexOrThrow("position")),
                        yearsExperience = cursor.getInt(
                            cursor.getColumnIndexOrThrow("years_experience")
                        ),
                        email = cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        phone = cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        address = cursor.getString(cursor.getColumnIndexOrThrow("address")),
                        district = cursor.getString(cursor.getColumnIndexOrThrow("district")),
                        city = cursor.getString(cursor.getColumnIndexOrThrow("city")),
                        country = cursor.getString(cursor.getColumnIndexOrThrow("country")),
                        maritalStatus = cursor.getString(
                            cursor.getColumnIndexOrThrow("marital_status")
                        ),
                        gender = cursor.getString(cursor.getColumnIndexOrThrow("gender")),
                        salary = cursor.getInt(cursor.getColumnIndexOrThrow("salary")),
                        department = cursor.getString(cursor.getColumnIndexOrThrow("department")),
                        photoUrl = cursor.getString(cursor.getColumnIndexOrThrow("photo_url"))
                    )
                )
            }
        }

        return employees
    }

    companion object {
        private const val TABLE_NAME = "favorite_employees"
    }
}
