package com.example.talenttrack.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.example.talenttrack.data.model.Employee
import com.example.talenttrack.data.remote.EmployeeApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton

import android.widget.Toast
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.ui.platform.LocalContext
import com.example.talenttrack.data.local.EmployeeDatabaseHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.res.painterResource
import com.example.talenttrack.R

@Composable
fun SearchScreen() {
    // [ADAPTAR] Entrada específica para la búsqueda de TalentTrack; reemplaza criterio y nombre en otro caso.
    var profession by rememberSaveable { mutableStateOf("") }
    var employees by remember { mutableStateOf(emptyList<Employee>()) }
    // [REUTILIZABLE] Estados usuales de una consulta remota; conserva los que necesite la pantalla.
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasSearched by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    // [OPCIONAL] Este estado solo se necesita porque el resultado abre un modal de detalle.
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    val appContext = LocalContext.current.applicationContext
    // [OPCIONAL] Se usa en esta pantalla para registrar empleados en SQLite.
    val databaseHelper = remember { EmployeeDatabaseHelper(appContext) }

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Branding visible en Home, como solicita el caso; el vector también identifica el launcher.
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_talenttrack_logo),
                contentDescription = "TalentTrack brand logo",
                modifier = Modifier.size(56.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "TalentTrack",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Conectando talento con oportunidades",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            "Search employees by profession"
        )

        OutlinedTextField(
            value = profession,
            onValueChange = { profession = it },
            label = { Text("Profession") },
            placeholder = { Text("e.g. abogado") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading = true
                    errorMessage = null
                    employees = emptyList()
                    hasSearched = true

                    try {
                        employees = EmployeeApi.searchByProfession(profession)
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        errorMessage = "Could not load employees. Check your connection and try again."
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = profession.isNotBlank() && !isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isLoading) "Searching..." else "Search")

        }

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (hasSearched && !isLoading && errorMessage == null) {
            if (employees.isEmpty()) {
                Text("No employees found.")
            } else {
                Text("Employees found: ${employees.size}")

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = employees,
                        key = { employee -> employee.id }
                    ) { employee ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // [OPCIONAL] Quita imagen, campo photoUrl y dependencias Coil si el caso no pide fotos.
                                AsyncImage(
                                    model = employee.photoUrl,
                                    contentDescription = "Photo of ${employee.firstName} ${employee.lastName}",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "${employee.firstName} ${employee.lastName}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text("ID: ${employee.id}")
                                    Text("Profession: ${employee.profession}")
                                }

                                IconButton(
                                    onClick = { selectedEmployee = employee }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "View employee details"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            val saved = withContext(Dispatchers.IO) {
                                                databaseHelper.saveFavorite(employee)
                                            }

                                            val message = if (saved) {
                                                "Employee added to Employees Data"
                                            } else {
                                                "Could not save employee"
                                            }

                                            Toast.makeText(
                                                appContext,
                                                message,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FavoriteBorder,
                                        contentDescription = "Save employee"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        selectedEmployee?.let { employee ->
            AlertDialog(
                onDismissRequest = { selectedEmployee = null },
                title = {
                    Text("${employee.firstName} ${employee.lastName}")
                },
                text = {
                    Column(
                        modifier = Modifier
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("ID: ${employee.id}")
                        Text("Nombre: ${employee.firstName}")
                        Text("Apellidos: ${employee.lastName}")
                        Text("Edad: ${employee.age}")
                        Text("Profesión: ${employee.profession}")
                        Text("Cargo: ${employee.position}")
                        Text("Experiencia: ${employee.yearsExperience} años")
                        Text("Email: ${employee.email}")
                        Text("Teléfono: ${employee.phone}")
                        Text("Dirección: ${employee.address}")
                        Text("Distrito: ${employee.district}")
                        Text("Ciudad: ${employee.city}")
                        Text("País: ${employee.country}")
                        Text("Estado civil: ${employee.maritalStatus}")
                        Text("Género: ${employee.gender}")
                        Text("Salario: ${employee.salary}")
                        Text("Departamento: ${employee.department}")
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { selectedEmployee = null }
                    ) {
                        Text("Cerrar")
                    }
                }
            )
        }

    }
}
