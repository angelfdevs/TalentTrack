package com.example.talenttrack.data.remote

import com.example.talenttrack.data.model.Employee
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object EmployeeApi {
    /**
     * [ADAPTAR / ELIMINAR] El patrón de corrutina, IO, tiempos de espera y parseo es reutilizable.
     * La URL, el parámetro "profesion" y las claves JSON son propios de TalentTrack.
     * Si el caso no consume un servicio remoto, elimina este objeto, sus llamadas y lo asociado a red.
     */
    suspend fun searchByProfession(profession: String): List<Employee> =
        withContext(Dispatchers.IO) {
            val encodedProfession =
                URLEncoder.encode(profession.trim(), "UTF-8")

            val url = URL(
                "https://dev.codemcy.com/employees.php?profesion=$encodedProfession"
            )

            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 15_000
                connection.readTimeout = 15_000

                val statusCode = connection.responseCode

                if (statusCode !in 200..299) {
                    throw IOException("Server returned HTTP $statusCode")
                }

                val responseBody = connection.inputStream
                    .bufferedReader()
                    .use { it.readText() }

                val jsonArray = JSONArray(responseBody)

                List(jsonArray.length()) { index ->
                    val item = jsonArray.getJSONObject(index)

                    Employee(
                        id = item.getString("id"),
                        firstName = item.getString("nombre"),
                        lastName = item.getString("apellidos"),
                        age = item.getInt("edad"),
                        profession = item.getString("profesion"),
                        position = item.getString("cargo"),
                        yearsExperience = item.getInt("experiencia"),
                        email = item.getString("email"),
                        phone = item.getString("telefono"),
                        address = item.getString("direccion"),
                        district = item.getString("distrito"),
                        city = item.getString("ciudad"),
                        country = item.getString("pais"),
                        maritalStatus = item.getString("estado_civil"),
                        gender = item.getString("genero"),
                        salary = item.getInt("salario"),
                        department = item.getString("departamento"),
                        photoUrl = item.getString("foto")
                    )
                }
            } finally {
                connection.disconnect()
            }
        }
}
