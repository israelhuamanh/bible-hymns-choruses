# Política de Privacidad y Declaración de Permisos — Biblia Himnos y Coros

**Última actualización:** 26 de noviembre de 2026  
**Aplicación:** Biblia Himnos y Coros (`com.huaman.bible_hymns_choruses`)

---

## 1. Resumen General (Funcionamiento 100 % Offline y Privado)
**Biblia Himnos y Coros** es una aplicación diseñada para funcionar **100 % sin conexión a Internet (offline)**.  
La aplicación **no recopila, no transmite, no vende ni comparte ningún dato personal, grabación de audio, fotografía ni texto** con servidores externos ni con terceros.

---

## 2. Datos que el Usuario Crea en la Aplicación
Toda la información creada por el usuario se almacena **exclusivamente de forma local** dentro del almacenamiento interno privado del dispositivo (`user_bookmarks.db` y `filesDir/note_media/`):
- **Guardados y Notas de Versículos:** Versículos, himnos, coros y las notas personales asociadas a ellos.
- **Apuntes:** Títulos, textos de estudio, fotografías adjuntas y grabaciones de voz realizadas por el usuario.
- **Preferencias de Lectura:** Tamaño de fuente, grosor tipográfico, tema visual (claro/oscuro/sistema) y última posición de lectura (almacenados localmente mediante Jetpack DataStore).

---

## 3. Declaración de Permisos para Google Play Console

### `android.permission.RECORD_AUDIO` (Micrófono)
- **Propósito único:** Permitir al usuario grabar notas de voz dentro de la sección **Apuntes** cuando pulsa explícitamente el botón **"Grabar audio"**.
- **Comportamiento y privacidad:**
  - El micrófono **solo se activa** mediante acción directa del usuario tras otorgar el permiso en tiempo de ejecución y se detiene inmediatamente al pulsar **"Detener"** o al salir del editor.
  - Los archivos de audio (`.m4a`) se guardan únicamente en la carpeta privada interna de la aplicación (`filesDir/note_media/`) y se eliminan permanentemente del dispositivo cuando el usuario borra el audio o el apunte.
  - Ninguna grabación de voz se envía por Internet ni sale del dispositivo.

### Selección de Imágenes (Android Photo Picker)
- La aplicación utiliza el selector oficial del sistema (**Android Photo Picker / `PickVisualMedia`**), por lo que **no solicita permisos invasivos de lectura sobre toda la galería** (`READ_MEDIA_IMAGES` ni `READ_EXTERNAL_STORAGE`).
- La aplicación solo accede a las imágenes puntuales que el usuario selecciona voluntariamente para adjuntar a un apunte, guardando una copia comprimida local que se elimina cuando el usuario borra la imagen o el apunte.

---

## 4. Copias de Seguridad del Sistema (Android Auto Backup)
Si el usuario tiene activada la copia de seguridad estándar de su cuenta de Google en su dispositivo Android, el sistema operativo puede respaldar de manera cifrada únicamente la base de datos de usuario (`user_bookmarks.db`), sus archivos multimedia de apuntes (`note_media/`) y sus preferencias (`datastore/`) para restaurarlos si cambia de teléfono. El desarrollador no tiene acceso a dichas copias de seguridad.

---

## 5. Eliminación de Datos
El usuario tiene control total sobre su información en todo momento:
- Puede editar o eliminar individualmente cualquier **Guardado**, **Apunte**, **Imagen** o **Grabación de voz** desde la propia interfaz de la aplicación (con opción de confirmación/deshacer).
- Al desinstalar la aplicación o borrar sus datos desde los ajustes de Android, todos los registros y archivos locales se eliminan por completo del dispositivo.

---

## 6. Contacto
Si tienes preguntas sobre esta Política de Privacidad, puedes contactar al desarrollador a través del repositorio oficial del proyecto:  
https://github.com/israelhuamanh/bible-hymns-choruses
