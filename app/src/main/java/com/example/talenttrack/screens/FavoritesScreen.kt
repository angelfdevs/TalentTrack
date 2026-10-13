package com.example.talenttrack.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.talenttrack.data.local.EmployeeDatabaseHelper
import com.example.talenttrack.data.model.Employee
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** [ADAPTAR / OPCIONAL] Pantalla de registros guardados; el resumen salarial pertenece a TalentTrack. */
@Composable
fun FavoritesScreen(refreshKey: Int) {
    val appContext = LocalContext.current.applicationContext
    val databaseHelper = remember { EmployeeDatabaseHelper(appContext) }
    val preferences = remember {
        appContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    val coroutineScope = rememberCoroutineScope()

    var employees by remember { mutableStateOf(emptyList<Employee>()) }
    // [ADAPTAR] Este total y esta clave se reemplazan si el nuevo caso pide otro resumen.
    var totalSalary by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    var editError by remember { mutableStateOf<String?>(null) }

    // Al entrar a la pestaña recargamos SQLite y sincronizamos el resumen local.
    LaunchedEffect(refreshKey) {
        isLoading = true
        val savedEmployees = withContext(Dispatchers.IO) {
            databaseHelper.getFavorites()
        }
        employees = savedEmployees

        val calculatedTotal = savedEmployees.sumOf { it.salary }
        preferences.edit().putInt(PREFERENCE_TOTAL_SALARY, calculatedTotal).apply()
        totalSalary = preferences.getInt(PREFERENCE_TOTAL_SALARY, 0)
        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Employees Data",
            style = MaterialTheme.typography.headlineMedium
        )

        if (isLoading) {
            CircularProgressIndicator()
        } else if (employees.isEmpty()) {
            Text("No favorite employees saved.")
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = employees, key = { employee -> employee.id }) { employee ->
                    EmployeeCard(
                        employee = employee,
                        onEdit = {
                            editError = null
                            selectedEmployee = employee
                        }
                    )
                }

                // La rúbrica pide que el total aparezca después de todas las tarjetas.
                item {
                    Text(
                        text = "Total salary: $totalSalary",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }

    selectedEmployee?.let { employee ->
        EditEmployeeDialog(
            employee = employee,
            isSaving = isSaving,
            errorMessage = editError,
            onDismiss = {
                if (!isSaving) selectedEmployee = null
            },
            onSave = { updatedEmployee ->
                // Igual que en clase, las operaciones SQLite se ejecutan fuera del hilo UI.
                coroutineScope.launch {
                    isSaving = true
                    editError = null
                    try {
                        val wasUpdated = withContext(Dispatchers.IO) {
                            databaseHelper.updateEmployee(updatedEmployee)
                        }

                        if (wasUpdated) {
                            val refreshedEmployees = withContext(Dispatchers.IO) {
                                databaseHelper.getFavorites()
                            }
                            employees = refreshedEmployees

                            // El salario puede cambiar: recalculamos y persistimos el nuevo total.
                            totalSalary = refreshedEmployees.sumOf { it.salary }
                            preferences.edit()
                                .putInt(PREFERENCE_TOTAL_SALARY, totalSalary)
                                .apply()
                            selectedEmployee = null
                        } else {
                            editError = "No se pudo actualizar el empleado guardado."
                        }
                    } catch (exception: CancellationException) {
                        throw exception
                    } catch (exception: Exception) {
                        editError = "Ocurrió un error al actualizar. Inténtalo de nuevo."
                    } finally {
                        isSaving = false
                    }
                }
            }
        )
    }
}

/** Tarjeta de empleado con el botón que abre el formulario modal de edición. */
@Composable
private fun EmployeeCard(employee: Employee, onEdit: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // La foto es opcional en otros casos; requiere photoUrl y Coil.
                    AsyncImage(
                        model = employee.photoUrl,
                        contentDescription = "Photo of ${employee.firstName} ${employee.lastName}",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${employee.firstName} ${employee.lastName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text("ID: ${employee.id}")
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit employee"
                    )
                }
            }

            Text("Age: ${employee.age} · Profession: ${employee.profession}")
            Text("Position: ${employee.position} · Experience: ${employee.yearsExperience} years")
            Text("Email: ${employee.email}")
            Text("Phone: ${employee.phone}")
            Text("Address: ${employee.address}, ${employee.district}, ${employee.country}")
            Text("Salary: ${employee.salary} · Department: ${employee.department}")
        }
    }
}

/**
 * Formulario basado en AlertDialog, rememberSaveable y OutlinedTextField,
 * siguiendo la ventana modal trabajada en el proyecto de términos y condiciones.
 */
@Composable
private fun EditEmployeeDialog(
    employee: Employee,
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (Employee) -> Unit
) {
    // rememberSaveable conserva lo escrito si Android recrea la pantalla del diálogo.
    var firstName by rememberSaveable(employee.id) { mutableStateOf(employee.firstName) }
    var lastName by rememberSaveable(employee.id) { mutableStateOf(employee.lastName) }
    var age by rememberSaveable(employee.id) { mutableStateOf(employee.age.toString()) }
    var profession by rememberSaveable(employee.id) { mutableStateOf(employee.profession) }
    var position by rememberSaveable(employee.id) { mutableStateOf(employee.position) }
    var yearsExperience by rememberSaveable(employee.id) { mutableStateOf(employee.yearsExperience.toString()) }
    var email by rememberSaveable(employee.id) { mutableStateOf(employee.email) }
    var phone by rememberSaveable(employee.id) { mutableStateOf(employee.phone) }
    var address by rememberSaveable(employee.id) { mutableStateOf(employee.address) }
    var district by rememberSaveable(employee.id) { mutableStateOf(employee.district) }
    var city by rememberSaveable(employee.id) { mutableStateOf(employee.city) }
    var country by rememberSaveable(employee.id) { mutableStateOf(employee.country) }
    var maritalStatus by rememberSaveable(employee.id) { mutableStateOf(employee.maritalStatus) }
    var gender by rememberSaveable(employee.id) { mutableStateOf(employee.gender) }
    var salary by rememberSaveable(employee.id) { mutableStateOf(employee.salary.toString()) }
    var department by rememberSaveable(employee.id) { mutableStateOf(employee.department) }
    var photoUrl by rememberSaveable(employee.id) { mutableStateOf(employee.photoUrl) }
    var validationMessage by rememberSaveable(employee.id) { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit employee") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Employee ID: ${employee.id}") // El identificador no se edita.
                OutlinedTextField(firstName, { firstName = it }, label = { Text("First name") }, singleLine = true)
                OutlinedTextField(lastName, { lastName = it }, label = { Text("Last name") }, singleLine = true)
                OutlinedTextField(
                    age,
                    { age = it },
                    label = { Text("Age") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(profession, { profession = it }, label = { Text("Profession") }, singleLine = true)
                OutlinedTextField(position, { position = it }, label = { Text("Position") }, singleLine = true)
                OutlinedTextField(
                    yearsExperience,
                    { yearsExperience = it },
                    label = { Text("Years of experience") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(email, { email = it }, label = { Text("Email") }, singleLine = true)
                OutlinedTextField(phone, { phone = it }, label = { Text("Phone") }, singleLine = true)
                OutlinedTextField(address, { address = it }, label = { Text("Address") }, singleLine = true)
                OutlinedTextField(district, { district = it }, label = { Text("District") }, singleLine = true)
                OutlinedTextField(city, { city = it }, label = { Text("City") }, singleLine = true)
                OutlinedTextField(country, { country = it }, label = { Text("Country") }, singleLine = true)
                OutlinedTextField(maritalStatus, { maritalStatus = it }, label = { Text("Marital status") }, singleLine = true)
                OutlinedTextField(gender, { gender = it }, label = { Text("Gender") }, singleLine = true)
                OutlinedTextField(
                    salary,
                    { salary = it },
                    label = { Text("Salary") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(department, { department = it }, label = { Text("Department") }, singleLine = true)
                OutlinedTextField(photoUrl, { photoUrl = it }, label = { Text("Photo URL") }, singleLine = true)

                (validationMessage ?: errorMessage)?.let { message ->
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = !isSaving,
                onClick = {
                    val parsedAge = age.toIntOrNull()
                    val parsedExperience = yearsExperience.toIntOrNull()
                    val parsedSalary = salary.toIntOrNull()
                    validationMessage = when {
                        firstName.isBlank() || lastName.isBlank() -> "Name and last name are required."
                        parsedAge == null || parsedAge < 0 -> "Age must be a valid positive number."
                        parsedExperience == null || parsedExperience < 0 -> "Experience must be a valid positive number."
                        parsedSalary == null || parsedSalary < 0 -> "Salary must be a valid positive number."
                        else -> null
                    }

                    if (validationMessage == null) {
                        onSave(
                            employee.copy(
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                age = parsedAge!!,
                                profession = profession.trim(),
                                position = position.trim(),
                                yearsExperience = parsedExperience!!,
                                email = email.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                district = district.trim(),
                                city = city.trim(),
                                country = country.trim(),
                                maritalStatus = maritalStatus.trim(),
                                gender = gender.trim(),
                                salary = parsedSalary!!,
                                department = department.trim(),
                                photoUrl = photoUrl.trim()
                            )
                        )
                    }
                }
            ) {
                Text(if (isSaving) "Saving..." else "Save")
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private const val PREFERENCES_NAME = "talenttrack_preferences"
private const val PREFERENCE_TOTAL_SALARY = "total_salary"
