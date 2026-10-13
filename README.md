# TalentTrack: guía reutilizable para practicar aplicaciones móviles

Este repositorio sirve como **proyecto de referencia** para practicar aplicaciones Android con Kotlin y Jetpack Compose. No copies TalentTrack sin revisar el nuevo enunciado: nombres, campos, endpoint, tabla local y pantallas describen específicamente el caso de empleados.

## Cómo adaptar esta estructura a otro caso

Antes de programar, convierte el caso y su rúbrica en una tabla breve:

| Pregunta | Qué anotar |
|---|---|
| ¿Qué información administra la app? | Entidad principal y campos requeridos |
| ¿De dónde vienen los datos? | Endpoint/JSON, entrada manual o ambos |
| ¿Qué pantallas y acciones se piden? | Buscar, listar, ver detalle, guardar, editar |
| ¿Qué debe persistir localmente? | Registros SQLite y preferencias/resúmenes |
| ¿Qué exige la presentación? | Navegación, branding, iconos, imágenes y Material 3 |

Después adapta los archivos en este orden. Si una función no aparece en el caso ni en la rúbrica, no la agregues solo porque exista en TalentTrack.

## Orden de trabajo, archivo por archivo

1. **Crear el proyecto en Android Studio.** Define el nombre y el paquete que correspondan al examen. Configura el SDK mínimo solicitado; en el parcial de referencia era API 24.

2. **Revisar las versiones y los plugins.** Consulta `gradle/libs.versions.toml` y el `build.gradle.kts` raíz. Si cambias una versión, verifica que sea compatible con el Kotlin/Android Gradle Plugin del proyecto.

3. **Configurar el módulo Android.** En `app/build.gradle.kts`, establece `minSdk`, `applicationId` y las dependencias que realmente necesita el caso. Esta referencia usa Material 3, Navigation Compose, iconos extendidos y Coil 3.3.0 para fotos. Sincroniza Gradle antes de continuar.

4. **Configurar permisos y nombre de la app.** En `app/src/main/AndroidManifest.xml`, conserva `INTERNET` si la app consume un endpoint o carga imágenes remotas. Cambia o quita ese permiso si el caso no usa red. Ajusta el nombre en `app/src/main/res/values/strings.xml`.

5. **Preparar el punto de entrada.** Revisa `app/src/main/java/com/example/talenttrack/MainActivity.kt`. `MainActivity` inicia el tema Compose y llama a `AppNavigation()`. Normalmente se conserva este patrón; cambia el tema y la función raíz si el nuevo proyecto usa otros nombres.

6. **Definir las pantallas y rutas.** En `navigation/AppNavigation.kt`, crea una ruta por pantalla solicitada y configura `NavHost`. Ajusta los destinos, las etiquetas e iconos de la barra inferior. Quita una pestaña si el caso no la requiere.

7. **Crear el modelo del caso.** En `data/model/Employee.kt`, reemplaza `Employee` por la entidad del nuevo caso y define sus propiedades con los nombres y tipos correctos. Mantén coherente el mapeo entre JSON, SQLite y UI.

8. **Conectar el servicio remoto, si corresponde.** En `data/remote/EmployeeApi.kt`, adapta URL, parámetros y nombres de las claves JSON. El patrón de corrutina, ejecución en `Dispatchers.IO`, tiempos de espera, lectura del estado HTTP y cierre de conexión se puede reutilizar. El endpoint y el mapeo de campos son propios de TalentTrack.

9. **Preparar la persistencia, si corresponde.** En `data/local/EmployeeDatabaseHelper.kt`, adapta nombre de base de datos, tabla, columnas y conversiones al modelo nuevo. Conserva operaciones como crear tabla, insertar y consultar solo si el enunciado requiere guardado local. Implementa una migración si cambias el esquema de una base ya existente.

10. **Construir la pantalla de consulta.** En `screens/SearchScreen.kt`, adapta el campo de búsqueda, resultados, estados de carga/error y acciones. Añade una ventana modal solamente si se solicita ver detalle. Conserva solo las variables y bloques que las funciones del caso necesiten.

11. **Construir la pantalla de datos guardados.** En `screens/FavoritesScreen.kt`, reemplaza “favoritos” por la lista local correspondiente. Adapta el resumen guardado en `SharedPreferences`; `totalSalary` y la clave `total_salary` existen por el requisito específico de sumar salarios en TalentTrack.

12. **Añadir edición, branding e icono.** Si se solicitan, conecta el icono de edición con un formulario, actualiza SQLite y vuelve a calcular los resúmenes. Personaliza la imagen dentro de la app y el icono launcher desde Android Studio.

13. **Verificar y entregar.** Compila y ejecuta la app en el emulador. Prueba cada flujo del caso: carga, resultados, modal, guardado, edición y persistencia después de cerrar/abrir. Exporta el ZIP con el nombre exacto solicitado por el examen.

## Qué partes reutilizar, adaptar o quitar

Usa estas etiquetas como comentarios al leer los archivos:

- **[REUTILIZABLE]** patrón de navegación, estados de carga/error, corrutinas, modal Compose y operaciones SQLite.
- **[ADAPTAR]** nombre de entidad, propiedades, endpoint, claves JSON, rutas, labels, columnas SQLite y claves de preferencias.
- **[OPCIONAL]** bloque que solo se conserva si el caso lo pide: red, fotos, modal, guardado local, suma/resumen o pestaña extra.
- **[ELIMINAR SI NO APLICA]** bloque y dependencia que quedarían sin uso en el nuevo caso. Elimina también sus imports y permisos asociados.

| Archivo o símbolo actual | Función | Qué se puede quitar o cambiar |
|---|---|---|
| `MainActivity` | Inicia tema y navegación | Conserva normalmente; cambia nombres del tema/raíz si cambiaste el proyecto |
| `SEARCH_ROUTE`, `FAVORITES_ROUTE` | Identificadores de navegación | Renombra según pantallas; elimina rutas y botones que el caso no requiere |
| `profession` | Texto de búsqueda de empleados | Cambia el nombre y criterio al caso; elimina si no hay búsqueda |
| `isLoading`, `errorMessage`, `hasSearched` | Estados de una consulta remota | Patrón reutilizable; elimina estados que no tenga la pantalla |
| `selectedEmployee` y `AlertDialog` | Empleado seleccionado y modal de detalle | Cambia la entidad/campos o elimina ambos si no hay detalle modal |
| `EmployeeApi.searchByProfession` | Petición GET y parseo de la respuesta | Cambia endpoint, query y JSON; elimina `EmployeeApi`, permiso de Internet y dependencias de red si no existe backend |
| `photoUrl`, `AsyncImage`, Coil | Carga de imagen remota | Cambia URL/campo o elimina llamadas, imports y ambas dependencias Coil si no hay imágenes |
| `EmployeeDatabaseHelper.saveFavorite/getFavorites/updateEmployee` | Guardar, listar y editar registros locales | Adapta tabla y campos; elimina helper y acciones locales si no hay persistencia |
| `favoritesRefreshKey` | Ordena recargar al entrar en “Employees Data” | Reutiliza el patrón para refrescar una pantalla; quítalo si no existe una pantalla que deba recargarse |
| `totalSalary`, `total_salary` | Suma de salarios y clave de SharedPreferences | Cambia al resumen real del caso o elimina ambos si no se pide guardar un resumen |
| `EmployeeCard` | Presentación de una tarjeta de empleado | Renombra y adapta campos, foto, botones y acciones |

### Ejemplos de comentarios junto al código

```kotlin
// [REUTILIZABLE] Estado para mostrar progreso durante una petición asíncrona.
var isLoading by remember { mutableStateOf(false) }

// [ADAPTAR] Este término coincide con la búsqueda por profesión de TalentTrack.
// Cámbialo por el criterio solicitado o quítalo si el caso no tiene búsqueda.
var profession by rememberSaveable { mutableStateOf("") }

// [OPCIONAL] Solo se necesita si un resultado abre una ventana de detalle.
var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
```

No borres una función solo porque sea específica. Comprueba primero sus llamadas, imports, dependencias Gradle, permisos y referencias desde navegación. En una app sin backend, por ejemplo, se elimina el flujo remoto completo; en una app sin fotos se quita Coil y los `AsyncImage`.

## Estado de esta referencia

TalentTrack incluye búsqueda por endpoint, fotos, modal de detalle, guardado/consulta/edición en SQLite, navegación con iconos, logo visible en Home, icono launcher distintivo y suma salarial sincronizada con `SharedPreferences`. Antes de usarlo como referencia, ejecuta la app y recorre búsqueda, detalle, guardado, edición y actualización del total. En el examen, adapta cada elemento al caso y su rúbrica.
