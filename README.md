# Chef Restaurant PDA - Aplicación de Gestión de Comandas

# CONTRASEÑAS PERFILES APP

1.  Camareros
     * Usuario: adrianp // Contraseña: adrian1998
     * Usuario: danielf // Contraeña: daniel1998

2.  Jefe
     * Usuario: antoniop // Constraseña: antonio1998

### 1. Configurar la Base de Datos
1.  Abre phpMyAdmin desde el panel de control de XAMPP (normalmente accesible vía `http://localhost/phpmyadmin`).
2.  Crea una nueva base de datos llamada `tfg`.
3.  Selecciona la base de datos `tfg` y ve a la pestaña "Importar".
4.  Selecciona el archivo `tfg.sql`  y ejecútalo. 

### 2. Configurar los Archivos de la API PHP
1.  Copia **toda la carpeta** que contiene los scripts PHP al directorio `htdocs` de tu instalación de XAMPP.
    * La ruta típica es `C:\xampp\htdocs\`.
    * Después de copiar, debes tener una estructura como `C:\xampp\htdocs\tfg_api\` y dentro de ella todos los archivos
2.  **Verificar la conexión a la BD en los scripts PHP:** Abre cualquiera de los archivos PHP (ej. `login.php`) y verifica que los parámetros de conexión a la base de datos sean correctos para tu entorno XAMPP:
    ```php
    $servername = "localhost";
    $username_db = "root"; // Usuario por defecto de MySQL en XAMPP
    $password_db = "";     // Contraseña por defecto de MySQL en XAMPP (vacía)
    $dbname = "tfg";
    ```
    Estos son los valores por defecto para XAMPP y deberían funcionar.

## 3. Configuración del Frontend (Aplicación Android)

### 3.1. Abrir el Proyecto en Android Studio
1.  Abre Android Studio.
2.  Selecciona "Open" o "Open an existing Android Studio project".
3.  Navega hasta la carpeta raíz de tu proyecto Android (la que contiene el archivo `build.gradle.kts` a nivel de proyecto y la carpeta `app`) y selecciónala.
4.  Espera a que Android Studio sincronice el proyecto y descargue las dependencias necesarias.

### 3.2. Configurar la URL Base de la API
1.  En Android Studio, abre el archivo `ApiClient.java`.
2.  Modifica la constante `BASE_URL` para que apunte a la dirección IP de la máquina donde está corriendo XAMPP y la carpeta de tu API.
    * **Si pruebas en un dispositivo físico conectado a la misma red Wi-Fi que tu PC con XAMPP:** Debes usar la dirección IP de tu PC en la red local. Puedes encontrarla ejecutando `ipconfig` (en Windows) o `ifconfig`/`ip addr` (en Linux/macOS) en la terminal de tu PC. Por ejemplo, si la IP de tu PC es `192.168.1.100`:
        ```java
        private static final String BASE_URL = "[http://192.168.1.100/](http://192.168.1.100/)"; // Ejemplo para dispositivo físico
        ```

### 3.3. Verificar `AndroidManifest.xml`
1.  Asegúrate de que el permiso de Internet está presente:
    `<uses-permission android:name="android.permission.INTERNET" />`
2.  Asegúrate de que `android:usesCleartextTraffic="true"` está en la etiqueta `<application>` si tu `BASE_URL` usa `http` y no `https`.
    ```xml
    <application
        ...
        android:usesCleartextTraffic="true"
        ...>
    ```

## 4. Ejecución y Pruebas de la Aplicación

### 4.1. Iniciar el Servidor Backend
* Asegúrate de que los módulos Apache y MySQL estén corriendo en tu panel de control de XAMPP.

### 4.2. Ejecutar la Aplicación Android
1.  Conecta un dispositivo Android (con depuración USB habilitada) a tu PC o inicia un Emulador de Android desde Android Studio.
2.  Asegúrate de que el dispositivo/emulador esté en la misma red que tu servidor XAMPP si estás usando la IP local de tu PC en `BASE_URL`.
3.  En Android Studio, selecciona el dispositivo/emulador de destino y pulsa el botón "Run" (o "Debug").

### 4.3. Usuarios de Prueba
La base de datos `tfg.sql` incluye los siguientes usuarios de prueba. Las contraseñas originales se hashearon usando el script `generar_hash.php` (por ejemplo, si la contraseña para todos es "1234", el hash almacenado sería el resultado de `password_hash("1234", PASSWORD_DEFAULT)`).

* **Camareros:**
    * Usuario: `adrianp`, Nombre: "Adrian Paul", Rol: "camarero"
    * Usuario: `danielf`, Nombre: "Daniel Fernández", Rol: "camarero"
    * (Asume una contraseña común para pruebas, ej. `1234`, o la que hayas usado al generar los hashes).
* **Jefe:**
    * Usuario: `antoniop`, Nombre: "Antonio Paqui", Rol: "jefe"
    * (Asume una contraseña común para pruebas, ej. `1234`, o la que hayas usado).

### 4.4. Flujos de Prueba Recomendados

1.  **Login:**
    * Intenta iniciar sesión con credenciales válidas de camarero. Deberías ser redirigido a `MesaActivity`.
    * Cierra sesión y vuelve a iniciar. Comprueba la persistencia de sesión (cierra la app completamente y ábrela de nuevo; deberías ir directamente a `MesaActivity`).
    * Intenta iniciar sesión con credenciales válidas de jefe. Deberías ser redirigido a `JefeActivity`.
    * Prueba credenciales incorrectas para ver los mensajes de error.

2.  **Flujo del Camarero (`MesaActivity` -> `CamareroActivity`):**
    * **Seleccionar Mesa Libre:** Pulsa una mesa libre, introduce número de comensales, acepta. Deberías ir a `CamareroActivity`.
    * **Añadir Productos:** En `CamareroActivity`, usa el botón de menú para añadir comidas y bebidas a la comanda. Verifica que se añaden a la lista.
    * **Borrar Productos (Local):** Selecciona uno o varios ítems y usa el botón de borrar. Verifica que se eliminan de la lista local.
    * **Enviar Comanda:** Pulsa el botón de enviar. Verifica que los ítems se marcan como enviados (fondo verde) y se muestra un mensaje de éxito.
    * **Añadir Más Productos:** Añade más productos a la misma comanda (aparecerán sin fondo verde). Envía de nuevo. Verifica que los nuevos ítems se marcan en verde y los anteriores permanecen.
    * **Borrar Productos (y Sincronizar):** Borra un ítem ya enviado (verde) de la lista local. Pulsa enviar comanda. Verifica que el ítem borrado ya no está.
    * **Volver a Mesa y Reabrir Comanda:** Desde `CamareroActivity`, usa el botón para volver a `MesaActivity`. La mesa usada debería aparecer como "ocupada". Pulsa de nuevo sobre ella. Deberías volver a `CamareroActivity` y ver todos los ítems enviados previamente.
    * **"Imprimir Comanda" / Cerrar Mesa:** Desde el menú de `CamareroActivity`, selecciona "Imprimir Comanda". Debería aparecer el diálogo de cuenta. Al aceptar y cerrar, deberías volver a `MesaActivity` y la mesa debería aparecer como "libre". Verifica en la base de datos que la comanda y sus ítems hayan sido eliminados y `mesa_activa` tenga `activa=0`.

3.  **Flujo del Jefe (`JefeActivity` -> `GestionProductosActivity` / `GestionMesasActivity`):**
    * **Gestión de Productos:**
        * Navega a "Gestiona Comida" y "Gestiona Bebida". Verifica que se listen los productos.
        * Añade un nuevo producto. Verifica que aparece en la lista.
        * Edita un producto existente. Verifica que los cambios se reflejan.
        * Elimina un producto (primero uno que no esté en ninguna comanda, luego intenta uno que sí lo esté para ver el mensaje de error de FK).
    * **Gestión de Mesas Físicas:**
        * Navega a "Gestiona Mesas". Verifica que se listen las mesas.
        * Añade una nueva mesa física con un número de mesa (ID) único.
        * Edita la capacidad de una mesa.
        * Elimina una mesa (primero una que no esté en `mesa_activa`, luego intenta una que sí para ver el mensaje de error).
    * **Cerrar Sesión (Jefe):** Verifica que te devuelve a `LoginActivity`.

## 5. Solución de Problemas Comunes

* **Error "unexpected end of stream" o "MalformedJsonException" en Android:** Generalmente indica un error PHP en el servidor que está imprimiendo texto HTML/error antes de la respuesta JSON. Revisa los logs de error de PHP en XAMPP (`C:\xampp\php\logs\php_error_log` o similar) o habilita `display_errors` temporalmente en el script PHP problemático.
* **Error 404 Not Found:** Verifica que el nombre y la ruta del script PHP en la anotación de Retrofit (`@GET`/`@POST` en `ApiService.java`) coincidan exactamente con el nombre y ubicación del archivo en tu carpeta `htdocs/tfg_api/`.
* **Error de Conexión (Connection Refused, Timeout):**
    * Asegúrate de que XAMPP (Apache, MySQL) esté corriendo.
    * Verifica que la `BASE_URL` en `ApiClient.java` sea correcta (IP y puerto).
    * Asegúrate de que tu dispositivo Android/emulador esté en la misma red que el PC con XAMPP y que no haya firewalls bloqueando la conexión.
    * Confirma que `android:usesCleartextTraffic="true"` está en el Manifest si usas HTTP.
* **Errores SQL (ej. "Unknown column", "FOREIGN KEY constraint fails"):** Indican un desajuste entre las sentencias SQL en tus scripts PHP y el esquema real de tu base de datos, o una violación de las restricciones de integridad. Revisa los nombres de las columnas, las tablas y las relaciones.

