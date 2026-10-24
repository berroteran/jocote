# Jocote — Registro central de tareas y backlog

> **Alcance actual:** completar y demostrar las funciones básicas de un cliente REST/HTTP y sus preferencias locales. La sección «Tareas básicas actuales» define el trabajo inmediato. El catálogo original de 60 features y las ampliaciones de cuentas se conservan como referencia aplazada; sus prioridades P0/P1 no determinan el orden actual.

Fecha de revisión documental: 2026-10-24. Base inspeccionada: Jocote 0.1.0-SNAPSHOT, commit a0894db. Los resultados de pruebas de septiembre se conservan como evidencia histórica; no se volvieron a ejecutar en esta revisión documental.

## Recomendación

Ampliar primero la demostración con POST y formularios; la importación cURL ya está implementada. Incorporar después intercambio de colecciones con exclusión de secretos, envío de archivos y sincronización de URL/parámetros. Variables básicas por colección y archivos estables para Git quedan en el lote de prioridad Media. El motor avanzado de variables, entornos, scripting, runners y colaboración con backend siguen aplazados.

El catálogo original conserva sus **60 features propuestas**, con identificadores estables **JOC-001 a JOC-060**. Las demostraciones nuevas usan **JOC-061 a JOC-064**. Una tarea con sufijo, como **JOC-017.1**, representa una entrega acotada de la feature existente; completarla no implica completar toda la feature principal. Los códigos no cambian si cambia la prioridad.

Perfil y preferencias locales se incorporan con **JOC-065 a JOC-068**, más **JOC-032.1** (temas) y **JOC-032.2** (tamaño de texto), como entregas acotadas de accesibilidad y preferencias. **JOC-032.3** añade densidad visual configurable y está implementada. **JOC-069 a JOC-071** cubren cuentas remotas y quedan aplazadas. JOC-065 está implementada. JOC-032.1, JOC-066 y JOC-068 tienen entregas parciales; el resto continúa pendiente.

La revisión de Bruno incorpora **JOC-003.1** (variables básicas), **JOC-002.1** (exclusión de secretos al exportar) y **JOC-045.1** (archivos estables para Git externo), y amplía **JOC-008.1** sin duplicarla. Las cuatro entregas están pendientes; no completan sus features principales ni requieren cuentas, servicios en nube o integración Git dentro de la aplicación.

El perfil inicial funciona sin registro, contraseña ni conexión: nombre visible, alias opcional, correo opcional e identificación por iniciales. El correo local es solo un dato personal, no una identidad verificada. Idioma y tema se guardan en el equipo. Una cuenta remota será opcional y distinta de las credenciales que se usan para consumir APIs; no debe ser requisito para abrir Jocote o ejecutar peticiones locales.

No constituye una certificación exhaustiva de paridad con cada versión, plan o integración de Postman. La prioridad y complejidad son estimaciones de ingeniería para Jocote, no datos publicados por Postman.

## Organización por dominio

Toda tarea nueva se registra en su dominio principal dentro del alcance que corresponda: básico actual o avanzado aplazado. Conservar código, prioridad, importancia, complejidad y descripción; indicar otras áreas afectadas en **Áreas relacionadas**. Cambiar una tarea de dominio no cambia su código ni su prioridad.

- **Servicio:** preparación, transporte HTTP, configuración de peticiones y protocolos.
- **Seguridad:** autenticación, credenciales, certificados y controles de acceso.
- **Perfil y cuentas:** identidad del usuario de Jocote, datos personales y ciclo de vida de su cuenta; independiente de la autorización de peticiones REST.
- **UI:** controles, editores y presentación visual.
- **UIX (experiencia de usuario / UX):** navegación, edición, organización, accesibilidad y continuidad del flujo de trabajo.
- **Compatibilidad:** intercambio de formatos, herramientas externas, distribución e integración con sistemas operativos.
- **Datos y persistencia:** almacenamiento, recuperación, historial y ejemplos.
- **Log y diagnóstico:** trazas y mensajes útiles para investigar fallos, sin exponer secretos.
- **Pruebas y demostraciones:** ejemplos ejecutables y comprobaciones del comportamiento.
- **Automatización:** scripts, secuencias, ejecución programada y asistentes.
- **Colaboración y gobierno de APIs:** recursos compartidos, revisión y gestión de contratos.

**macOS, Windows y Linux** son áreas transversales de compatibilidad: todas las tareas deben conservar el comportamiento compartido en los tres sistemas. El índice por plataforma detalla verificaciones específicas sin duplicar las tareas. Si surge una implementación exclusiva de un sistema, crear una subtarea con código propio bajo Compatibilidad e indicar su plataforma.

Cada código tiene una única fila de definición. Los índices, dependencias y notas de entrega solo lo referencian. JOC-009 y JOC-010 se definen en el alcance básico; conservan como referencia sus prioridades originales P0. El alcance de streaming de JOC-010 también se conserva en esa definición. Las subtareas no cierran automáticamente su épica.

## Cómo usar este registro

Este archivo es la fuente central de tareas, features, deuda técnica y pendientes conocidos. Las especificaciones extensas pueden vivir en otros documentos, enlazadas desde su código. README describe capacidades disponibles; este registro gobierna el trabajo restante. No se ha realizado una auditoría exhaustiva de defectos: registrar nuevos hallazgos al identificarlos.

La petición del 2026-10-24 autoriza preparar este archivo para seguimiento en Git y sustituye su exclusión local anterior. Incorporarlo en un commit y publicarlo son operaciones posteriores; la restricción sobre AGENTS.md continúa vigente.

Cada fila conserva código estable, tarea, prioridad, importancia, complejidad, áreas y descripción; añade **estado, impacto, dependencias y comentarios**. La sección determina su dominio y alcance. La descripción expresa el resultado esperado; antes de iniciar una épica debe desglosarse en criterios de aceptación verificables.

| Campo adicional de seguimiento | Regla |
| --- | --- |
| Tipo | Feature por defecto; las filas que indiquen deuda, validación o defecto usan ese tipo. Una épica agrupa entregas con sufijo. |
| Responsable | Sin asignar salvo indicación explícita en Comentarios; nunca inferir una persona por haber editado código. |
| Hito o versión objetivo | Sin programar salvo indicación explícita; prioridad no implica fecha comprometida. |
| Bloqueo | Al marcar Bloqueada, indicar causa, dependencia concreta y condición para desbloquear. |
| Criterios de aceptación | Resultado observable, casos de error y preservación de datos; especificarlos antes de pasar a En curso. |
| Validación y evidencia | Al cerrar: pruebas/comandos y resultados, sistema/arquitectura cuando aplique, enlaces a código y commit/PR cuando existan. |
| Comentarios y actualización | Registrar decisiones, riesgos, alcance restante y fecha al cambiar el estado. Esta revisión documental es del 2026-10-24. |

**Estados:** Propuesta (requiere refinamiento), Pendiente (alcance definido), En curso, Bloqueada, Parcial (hay una entrega y queda alcance), Implementada (alcance documentado disponible), Cancelada (con motivo). **Aplazado es un alcance de planificación, no evidencia de bloqueo técnico.** No hay tareas declaradas En curso ni responsables asignados en esta revisión.

**Importancia** mide valor y criticidad. **Impacto** identifica qué comportamiento, datos o usuarios cambia la tarea. **Prioridad** determina orden dentro de su alcance. **Complejidad C1–C5** estima dificultad, no duración. Alta/Media del lote actual y P0–P3 del catálogo futuro son escalas de planificación distintas; no se deben ordenar juntas.

Para iniciar una tarea: asignar responsable e hito, confirmar dependencias, completar aceptación y actualizar estado. Para cerrarla: enlazar evidencia de validación, documentar límites y actualizar README cuando corresponda. No marcar completada una épica por haber terminado una subtarea. Los estados implementados de este registro se basan en código inspeccionado y entregas documentadas, sin certificar nuevamente el runtime.

## Índice por rubro y capa

| Rubro | Registro o referencias principales |
| --- | --- |
| Core y arquitectura | JOC-072; Servicio: preparación, transporte, respuestas, cURL y variables. |
| Interfaz gráfica | UI y UIX; JOC-019, JOC-032 y sus subtareas, JOC-047, JOC-066, JOC-067. |
| SSH y túneles | JOC-073; terminal remota en JOC-075. |
| Terminales | JOC-074 local, JOC-075 remota; no confundir con consola de diagnóstico JOC-021. |
| CLI sin escritorio | JOC-028 y JOC-028.1; no equivale a un emulador de terminal. |
| Seguridad y bloqueo | JOC-002, JOC-002.1, JOC-013, JOC-016 y JOC-076 (bloqueo local). |
| Datos y bloqueo de escritura | JOC-001 y JOC-001.1; recuperación JOC-011 y JOC-011.1. |
| Instaladores y actualizaciones | JOC-031, JOC-031.1 a JOC-031.3, JOC-050. |
| Perfil y cuentas | JOC-065 a JOC-071; identidad local separada de credenciales REST y SSH. |
| Logs y diagnóstico | JOC-021 y JOC-021.1. |
| Pruebas y compatibilidad | JOC-061 a JOC-064, JOC-077 y matriz por plataforma. |
| Automatización | JOC-023 a JOC-028, JOC-043, JOC-057 y JOC-059. |
| Colaboración | JOC-045, JOC-045.1 y JOC-051 a JOC-056. |

## Tareas básicas actuales

Estado: **JOC-009, JOC-065, JOC-032.3 y JOC-047.1 implementadas**. **JOC-047 implementada con el alcance de consultas y previsualización documentado en el README**. **JOC-032.1, JOC-066 y JOC-068 parcialmente implementadas**; las demás tareas básicas continúan **pendientes**. JOC-009 incorpora importación literal Bash/PowerShell en una pestaña editable, validación de opciones y pruebas de parser, HTTP local e interfaz JavaFX. No ejecuta comandos ni envía automáticamente; el alcance y las opciones soportadas se documentan en el README. La colección Demo GitHub ya comprueba cuatro peticiones GET reales; no se vuelve a registrar como trabajo pendiente. Los métodos HTTP, formularios, timeout y cancelación ya existen: las tareas JOC-061 a JOC-064 amplían la demostración y verifican su funcionamiento visible, sin crear un framework de pruebas ni un runner de colecciones.

En esta sección, **Alta** indica el siguiente lote de trabajo y **Media** el lote posterior. La importancia mide el valor para el uso básico. La complejidad usa la escala del documento: C2 baja, C3 media y C4 alta; no equivale a días de desarrollo.

### Servicio

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-010 | Enviar archivos | Pendiente | Alta | Alta | Ejecución y fidelidad de peticiones | C4 | Compatibilidad, UI | Sin prerrequisito identificado | Admitir multipart/form-data con campos y archivos, y archivo como body binario mediante streaming, sin cargar el archivo completo en memoria. Comprobar los bytes recibidos y actualizar el cURL generado. Reutiliza la feature existente. | Validar resultado y casos de error antes de cerrar. |
| JOC-017.1 | Seguir redirecciones | Pendiente | Alta | Alta | Ejecución y fidelidad de peticiones | C3 | Seguridad, Compatibilidad | Sin prerrequisito identificado | Agregar un control para seguir o no 301/302/303/307/308 y limitar los saltos. Respetar las reglas de método/body y no reenviar credenciales a otro host. Mostrar la URL final y reflejar la opción en cURL. | Validar resultado y casos de error antes de cerrar. |
| JOC-014.1 | Reutilizar cookies de sesión | Pendiente | Media | Alta | Ejecución y fidelidad de peticiones | C3 | Seguridad, UI | Sin prerrequisito identificado | Recibir Set-Cookie y enviar las cookies aplicables a las siguientes peticiones, respetando dominio, path, Secure y expiración. Permitir verlas y limpiarlas; inicialmente basta mantenerlas durante la sesión. | Validar resultado y casos de error antes de cerrar. |
| JOC-018.1 | Leer respuestas comprimidas | Pendiente | Media | Alta | Ejecución y fidelidad de peticiones | C3 | Seguridad, UI | Sin prerrequisito identificado | Solicitar y decodificar gzip/deflate para mostrar correctamente el body. Conservar límites de tamaño, también después de descomprimir, e indicar errores de contenido inválido. No requiere ampliar ahora el límite de descarga. | Validar resultado y casos de error antes de cerrar. |
| JOC-003.1 | Variables básicas por colección | Pendiente | Media | Alta | Ejecución y fidelidad de peticiones | C3 | UIX, Datos, Seguridad | Sin prerrequisito identificado | Definir un único mapa de variables por colección y sustituir {{baseUrl}}, {{token}} u otros nombres en URL, autorización, headers y body mediante RequestPreparer. Mantener las plantillas guardadas, usar la misma resolución para envío y cURL e impedir el envío si falta un valor, indicando solo el nombre. Validar nombres únicos; resolver una sola vez, sin expresiones ni referencias recursivas. Persistir solo valores no sensibles; los marcados como secretos se introducen para la sesión, se enmascaran en la UI y no se guardan ni exportan. Conservar compatibilidad con colecciones anteriores. Sin ámbitos globales, entornos, herencia ni scripts. | Validar resultado y casos de error antes de cerrar. |

### Seguridad

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-002.1 | Excluir secretos al exportar colecciones | Pendiente | Alta | Crítica | Protección de secretos y acceso | C3 | Compatibilidad, Datos, UIX | Entrega conjunta con JOC-008.1 | Entregar junto con JOC-008.1 una copia exportada que omita valores de credenciales de AuthConfig, Authorization, Proxy-Authorization, Cookie y variables marcadas como sensibles. Permitir señalar otros valores sensibles de headers, query, formulario o body; mostrar una vista previa editable de la copia antes de exportar. Conservar nombres y referencias cuando corresponda, sin modificar la colección original. No ofrecer inclusión automática de secretos; advertir que no se detectan todos los secretos arbitrarios en texto libre. Probar exclusión con valores ficticios y evitar registrarlos en logs. No requiere cifrado, keychain ni el almacén completo JOC-002. | Validar resultado y casos de error antes de cerrar. |

### UI — Interfaz visual

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-047.1 | Buscar y leer mejor la respuesta | Implementada | Media | Media | Lectura y edición en escritorio | C2 | UIX | Sin prerrequisito identificado | Implementada dentro de JOC-047: búsqueda literal con distinción de mayúsculas, navegación con anterior/siguiente y ajuste de líneas en Body y resultados de consulta. Conservar texto original y formato JSON/XML. Las vistas avanzadas pertenecen a JOC-047, sin duplicar esta entrega. | Incluida en JOC-047; no contar como otra feature completa. |
| JOC-032.1 | Temas claro, oscuro y del sistema | Parcial | Media | Alta | Lectura y edición en escritorio | C3 | UIX, Compatibilidad, Perfil | JOC-068 (persistencia disponible) | Permitir elegir Claro, Oscuro o Sistema. Aplicar una paleta coherente a controles, editores, diálogos y estados, con contraste legible. Recordar la selección mediante JOC-068. Verificar la detección del sistema en JavaFX 21 por plataforma; si no está disponible, usar un tema de respaldo documentado y permitir selección manual. No depende de una cuenta. | Claro/oscuro y temas nativos disponibles; falta modo Sistema. |

### UIX — Experiencia de usuario

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-020.1 | Sincronizar URL y parámetros | Pendiente | Alta | Alta | Productividad y continuidad del trabajo | C3 | Servicio, UI | Sin prerrequisito identificado | Al pegar una URL con query, reflejarla en la tabla; las ediciones deben actualizar la URL sin duplicar valores ni perder parámetros repetidos, vacíos o codificados. La edición masiva y las plantillas de ruta quedan fuera de esta entrega. | Validar resultado y casos de error antes de cerrar. |
| JOC-066 | Pantalla de perfil y preferencias | Parcial | Media | Alta | Productividad y continuidad del trabajo | C2 | UI, Perfil, Datos | Sin prerrequisito identificado | Agregar una entrada visible a Configuración con secciones Perfil, Idioma y Apariencia. Permitir editar, guardar o cancelar cambios y restaurar preferencias predeterminadas sin borrar colecciones, peticiones ni credenciales. Integrar las entregas disponibles sin exigir implementar todas a la vez. | Perfil y apariencia disponibles; faltan idioma y restauración integral. |
| JOC-067 | Idioma de la interfaz | Pendiente | Media | Alta | Productividad y continuidad del trabajo | C3 | UI, Compatibilidad, Perfil | JOC-068 (extender preferencias) | Ofrecer Español e Inglés inicialmente. Extraer los textos de interfaz, validaciones y mensajes propios a recursos traducibles con un idioma de respaldo. Guardar la elección y documentar si requiere reinicio. No traducir ni modificar URL, headers, bodies, respuestas o nombres escritos por el usuario. | Validar resultado y casos de error antes de cerrar. |
| JOC-032.2 | Tamaño del texto y del editor | Pendiente | Media | Media | Productividad y continuidad del trabajo | C2 | UI, Compatibilidad, Perfil | JOC-068 (extender preferencias) | Permitir ajustar por separado el tamaño del texto de interfaz y de los editores dentro de un rango legible, recordar la selección y restablecer valores predeterminados. Comprobar que controles y diálogos no se recorten con escalado HiDPI. No requiere temas personalizados ni fuentes descargables. | Validar resultado y casos de error antes de cerrar. |
| JOC-032.3 | Densidad visual configurable | Implementada | Media | Alta | Productividad y continuidad del trabajo | C3 | UI, Compatibilidad, Perfil, Datos | JOC-068 (persistencia disponible) | Implementada: elegir Compacta, Normal o Amplia desde el menú del avatar. Ajustar márgenes, separaciones, controles y filas en tiempo real, sin cambiar fuente ni perder peticiones/respuestas. Aplicar a los cuatro temas, nuevas pestañas y diálogos; persistir la elección y cargar Normal en preferencias anteriores. | Validar resultado y casos de error antes de cerrar. |

### Perfil y cuentas

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-065 | Perfil local básico | Implementada | Media | Media | Identidad y preferencias del usuario | C2 | UIX, Datos | JOC-068 (perfil disponible) | Crear un único perfil local con nombre visible, alias y correo opcionales, e iniciales como avatar predeterminado. Permitir consultar y editar esos datos; usar un nombre predeterminado al omitirlos. Validar longitud y formato del correo cuando se indique. Guardar mediante JOC-068; no solicitar contraseña ni enviar estos datos a las APIs consumidas. Fotos, múltiples perfiles y sincronización quedan fuera de esta entrega. | Validar resultado y casos de error antes de cerrar. |

### Compatibilidad e interoperabilidad

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-009 | Importar cURL | Implementada | Alta | Alta | Intercambio y operación entre herramientas/sistemas | C3 | Servicio, Seguridad, UIX | Sin prerrequisito identificado | Pegar un comando y reconstruir URL, método, headers y body editables. Empezar con las opciones habituales, informar las no soportadas y nunca ejecutar el texto como comando del sistema. Reutiliza la feature existente. | Ver README e importador/pruebas cURL; entrega histórica 2026-09-24. |
| JOC-008.1 | Importar y exportar una colección Jocote | Pendiente | Alta | Alta | Intercambio y operación entre herramientas/sistemas | C3 | Datos, UIX, Seguridad | Entrega conjunta con JOC-002.1 | Exportar una colección a un archivo JSON UTF-8 con versión de formato e importarlo desde la UI. Conservar peticiones, orden, nombres y configuración no sensible; excluir perfil, preferencias, historial y workspace completo. Entregar con JOC-002.1. Validar tamaño, esquema y versión antes de escribir; gestionar IDs duplicados sin sobrescribir silenciosamente. Comprobar ida y vuelta, archivos inválidos y cancelación sin cambios. Incorporar variables de JOC-003.1 cuando existan. La compatibilidad Postman y Bruno queda aplazada. | Validar resultado y casos de error antes de cerrar. |

### Datos y persistencia

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-011.1 | Restaurar pestañas y borradores | Pendiente | Media | Alta | Integridad y recuperación de datos locales | C3 | UIX | Sin prerrequisito identificado | Guardar y recuperar las pestañas abiertas y su contenido sin perder cambios. Distinguir el borrador de la petición guardada; no requiere aún backups, migraciones generales ni sincronización. | Validar resultado y casos de error antes de cerrar. |
| JOC-012.1 | Historial sencillo de peticiones | Pendiente | Media | Media | Integridad y recuperación de datos locales | C3 | UIX, Seguridad | Sin prerrequisito identificado | Mantener una lista local acotada de ejecuciones recientes con método, URL, fecha y estado. Permitir reabrir la petición y limpiar el historial; no almacenar respuestas completas ni credenciales por defecto. | Validar resultado y casos de error antes de cerrar. |
| JOC-068 | Persistencia de perfil y preferencias | Parcial | Media | Alta | Integridad y recuperación de datos locales | C2 | Perfil, UIX, Compatibilidad | Sin prerrequisito identificado | Guardar nombre, idioma, tema y tamaños de texto en configuración local separada de colecciones y secretos. Cargar valores predeterminados en el primer inicio, conservar valores al reiniciar y manejar un archivo inválido sin sobrescribirlo silenciosamente. Usar rutas portables y el guardado atómico existente; no requiere una base de datos ni el sistema completo de migraciones JOC-001. | Perfil, tema y densidad persisten; faltan idioma y tamaños. |

### Log y diagnóstico

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-021.1 | Explicar errores de conexión | Pendiente | Media | Alta | Diagnóstico de fallos sin exponer secretos | C2 | Servicio, UIX | Sin prerrequisito identificado | Distinguir URL inválida, DNS, conexión rechazada, timeout y certificado TLS rechazado. Mostrar un mensaje útil sin confundir un fallo de transporte con una respuesta HTTP 4xx/5xx. No requiere una consola completa. | Validar resultado y casos de error antes de cerrar. |

### Pruebas y demostraciones

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-061 | Demostrar POST con JSON | Pendiente | Alta | Alta | Confianza y reproducción del comportamiento | C2 | Servicio, UI | Sin prerrequisito identificado | Agregar una petición de ejemplo a un servicio de eco público o servidor local controlado. Mostrar la respuesta y comprobar que el método, los headers y el JSON recibidos coinciden con lo enviado, incluidos caracteres Unicode. | Hay pruebas HTTP locales; falta demostración visible incluida. |
| JOC-062 | Demostrar POST con formulario | Pendiente | Alta | Alta | Confianza y reproducción del comportamiento | C2 | Servicio, Compatibilidad | Sin prerrequisito identificado | Agregar un ejemplo application/x-www-form-urlencoded. Comprobar campos vacíos, espacios, acentos y caracteres como +, & y = mediante la respuesta del servidor. | FORM existe; falta demostración visible incluida. |
| JOC-063 | Demostrar PUT, PATCH y DELETE | Pendiente | Alta | Alta | Confianza y reproducción del comportamiento | C2 | Servicio, UI | Sin prerrequisito identificado | Agregar ejemplos sobre recursos de prueba; comprobar método, body cuando corresponda y respuesta visible, incluido un caso sin contenido. No operar sobre datos reales del usuario. | Métodos HTTP existentes; falta demostración visible incluida. |
| JOC-064 | Demostrar errores, timeout y cancelación | Pendiente | Alta | Alta | Confianza y reproducción del comportamiento | C2 | Servicio, UIX, Log | Sin prerrequisito identificado | Incluir un 404 y una respuesta lenta reproducible. Verificar que se muestre el body del error HTTP, que expire el timeout configurado y que Cancelar libere la UI. Reutilizar el transporte actual. | Timeout/cancelación y 404 probados en servicio; falta demo visible reproducible. |

### Colaboración mediante archivos locales

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-045.1 | Exportaciones estables para Git externo | Pendiente | Media | Alta | Compartir cambios revisables sin secretos | C2 | Compatibilidad, Datos, Seguridad | JOC-008.1, JOC-002.1 | Sobre el JSON de JOC-008.1, fijar indentación, UTF-8, saltos LF y orden estable de propiedades, preservando el orden funcional de peticiones y valores repetidos. Exportar dos veces sin cambios debe producir los mismos bytes, sin timestamps ni IDs regenerados. Documentar un archivo por colección en una carpeta elegida y el flujo exportar, revisar diff, versionar con Git externo e importar. Proporcionar un ejemplo de .gitignore para datos locales y secretos; no agregar automáticamente archivos a Git. Depende de JOC-008.1 y JOC-002.1. Sin comandos Git desde Jocote, vigilancia de archivos, resolución de conflictos ni cambio del workspace actual. | Validar resultado y casos de error antes de cerrar. |

**Orden recomendado:** JOC-061 → JOC-062 → JOC-063 → JOC-064 → (JOC-008.1 + JOC-002.1) → JOC-010 → JOC-020.1 → JOC-017.1. Después, atender las tareas de prioridad Media según el uso real, incluidas JOC-003.1 y JOC-045.1. JOC-045.1 depende del intercambio de colecciones; las variables básicas no bloquean esa primera entrega.

**Entrega JOC-047 / JOC-047.1 (2026-09-24):** visor estructurado y pasivo, búsqueda y ajuste de líneas, consultas en segundo plano y cancelación al reemplazar/cerrar respuesta. Sin nuevas dependencias Maven; módulos JDK java.xml y java.desktop declarados. Validación en Windows: 115 pruebas aprobadas, incluida JavaFX; capturas de árbol, código coloreado, XPath y HTML revisadas. Runtime jlink generado y resolución modular comprobada en Windows. XML bloquea DTD/entidades externas; pruebas de recursos HTML/XML verifican cero llamadas externas. Límites explícitos de texto, profundidad, nodos, resultados e imágenes. macOS/Linux pendientes de ejecución.

**Entrega de densidad visual (2026-09-24):** JOC-032.3 implementada con ajustes centralizados en CSS, preferencias compatibles con el formato anterior y selector en el menú del avatar. Validación en Windows: 93 pruebas aprobadas; la prueba gráfica recorre las 12 combinaciones de tema/densidad, comprueba el ancho mínimo y conserva petición, respuesta y fuente. macOS/Linux pendientes de ejecución.

**Entrega de cabecera y perfil (2026-09-24):** botón de ciclo después de Nueva petición y avatar de iniciales con menú al extremo derecho. JOC-065 permite editar nombre, alias y correo opcionales. JOC-032.1 entrega Claro/Oscuro de Jocote y los temas nativos Modena/Caspian; queda pendiente el modo Sistema. JOC-066 entrega Mi perfil y el menú Apariencia; quedan pendientes Idioma y restauración de preferencias. JOC-068 guarda perfil y tema en preferences.json separado, con escritura atómica fuera del hilo gráfico, restauración al iniciar y protección del archivo inválido; idioma y tamaños de texto se integrarán con sus tareas. Validación en Windows: 92 pruebas aprobadas, incluida JavaFX y capturas de los cuatro temas; macOS/Linux pendientes de ejecución. No se incorpora carga de fotografías ni cuentas remotas.

Dentro del lote de perfil y preferencias: **JOC-068 + JOC-066 → JOC-065 → JOC-067 / JOC-032.1 → JOC-032.2**. Este lote es local y no depende de JOC-069 a JOC-071, del backend de sincronización ni de equipos/permisos.

JOC-015 (proxy) y JOC-016 (certificados privados/mTLS) se adelantan únicamente cuando una API objetivo los requiera. Las tareas básicas no dependen de completar antes el almacén cifrado de secretos, el motor avanzado de variables, OAuth2 ni el resto del catálogo avanzado. JOC-003.1 mantiene los secretos solo durante la sesión; JOC-002.1 protege la copia exportada, sin prometer resolver la protección de todas las credenciales del workspace existente.

Los ejemplos deben documentar URL, datos de prueba, pasos y respuesta esperada. Cualquier comprobación contra servicios públicos debe ser optativa; usar un servicio local controlado cuando se necesite reproducir una condición de forma determinista. Cada cambio conserva el mismo comportamiento en Windows, macOS y Linux y mantiene coherentes la petición enviada y su cURL.

**Revisión Bruno (2026-09-24):** se adopta portabilidad local y reutilización sencilla de peticiones. El uso sin cuenta, las colecciones locales y el envío desde el equipo ya existen y se conservan como requisitos, no como tareas nuevas. No se incorporan ahora cuentas obligatorias, nube, un formato propio similar a Bru, runners, scripting ni integración Git embebida. La exportación JSON inicial no convierte el workspace en un repositorio Git.

## Qué ya existe

- Java 21, JavaFX, Maven y un módulo JPMS con paquetes separados para modelo, servicios, repositorio y UI.
- Peticiones HTTP, parámetros, headers, body JSON/texto/XML/form-urlencoded y autorización Basic/Bearer/API key.
- Colecciones locales con operaciones básicas, pestañas y paneles laterales colapsables.
- Envío asíncrono, cancelación, timeout, datos de respuesta, formato JSON y generación cURL.
- Colección Demo GitHub, botón Probar GitHub y lanzamiento con --demo; cuatro peticiones GET públicas verificadas con respuesta 200 OK.
- Persistencia JSON con reemplazo atómico cuando está disponible, pruebas y configuración CI para tres sistemas.
- Runtimes jlink y lanzadores; ampliar distribución no significa volver a implementar estos mecanismos.

La configuración CI existente no se cuenta como ausencia de soporte multiplataforma. La compatibilidad debe verificarse por sistema y arquitectura, y la distribución nativa necesita trabajo adicional.

## Evidencia de las brechas

| Código local | Estado observado | Backlog relacionado |
| --- | --- | --- |
| [AuthConfig](../src/main/java/dev/jocote/model/AuthConfig.java) | Solo NONE, BASIC, BEARER y API_KEY; credencial dentro del modelo persistido. | JOC-002, JOC-013, JOC-033 |
| [RequestDefinition](../src/main/java/dev/jocote/model/RequestDefinition.java) | Body NONE, JSON, TEXT, XML y FORM; sin archivos, scripts ni configuración de entorno. | JOC-003, JOC-004, JOC-010, JOC-024 |
| [RequestPreparer](../src/main/java/dev/jocote/service/RequestPreparer.java) | Prepara una petición individual; no resuelve variables ni herencia. | JOC-003, JOC-006, JOC-020 |
| [HttpRequestService](../src/main/java/dev/jocote/service/HttpRequestService.java) | Redirects desactivados; límite fijo de 10 MiB; sin gestor explícito de cookies, proxy ni certificados por host. | JOC-014 a JOC-018 |
| [WorkspaceRepository](../src/main/java/dev/jocote/repository/WorkspaceRepository.java) | JSON versión 1; rechaza otras versiones; sin migración ni coordinación entre instancias. | JOC-001, JOC-002 |
| [WorkspaceService](../src/main/java/dev/jocote/service/WorkspaceService.java) | Colecciones y peticiones; no carpetas anidadas ni runner. | JOC-005, JOC-027 |
| [ResponsePane](../src/main/java/dev/jocote/ui/ResponsePane.java) | Texto original, búsqueda, formato JSON/XML, árbol JSON y resaltado de lectura, imágenes, HTML estático y consultas JSONPath/XPath acotadas. Guardado de bytes; vista limitada a 300 000 caracteres. JOC-047 implementada; compresión y comparación siguen pendientes. | JOC-018, JOC-047, JOC-048 |
| [MainView](../src/main/java/dev/jocote/ui/MainView.java), [Workspace](../src/main/java/dev/jocote/model/Workspace.java) y [jocote.css](../src/main/resources/dev/jocote/jocote.css) | Perfil local y selector de cuatro temas implementados en MainView, ProfileDialog y ThemeManager; preferencias separadas del Workspace. Idioma, tema automático del sistema y cuentas remotas pendientes. La autorización REST sigue siendo independiente del perfil. | JOC-065 a JOC-071, JOC-032.1, JOC-032.2 |

## Criterios de clasificación

Las prioridades P0 a P3 que siguen corresponden al **catálogo avanzado aplazado**. Para el trabajo inmediato rigen exclusivamente las prioridades Alta/Media y el orden de la sección «Tareas básicas actuales».

**Prioridad** indica secuencia de entrega, considerando dependencias y utilidad para un cliente REST de escritorio:

- **P0 — Primero:** base necesaria para el uso diario, recuperación de trabajo y migración desde otras herramientas.
- **P1 — Siguiente:** completar el flujo REST profesional y las pruebas automatizadas.
- **P2 — Posterior:** ampliar protocolos, diagnóstico y ejecución operativa.
- **P3 — Largo plazo:** funciones de plataforma, servicios compartidos y extensibilidad avanzada.

P0 no significa que todo deba desarrollarse simultáneamente. Una capacidad de importancia alta puede quedar en P3 si depende de una plataforma que aún no existe.

**Importancia** indica valor y criticidad: **Crítica** bloquea flujos centrales o protege datos esenciales; **Alta** aporta valor frecuente o resuelve una necesidad relevante de un segmento; **Media** amplía casos de uso; **Baja** es opcional respecto al objetivo REST.

**Complejidad**, relativa al código actual e incluyendo UI, persistencia, pruebas y portabilidad:

| Nivel | Significado |
| --- | --- |
| C1 — Muy baja | Cambio localizado, sin contratos ni dependencias nuevos. |
| C2 — Baja | Componente acotado con reglas y pruebas sencillas. |
| C3 — Media | Afecta varias capas o incorpora un modelo nuevo. |
| C4 — Alta | Integración/protocolo importante, migraciones o diferencias entre plataformas. |
| C5 — Muy alta | Subsistema, runtime aislado, backend distribuido o área con incertidumbre elevada. |

No son estimaciones de días. Cada C4/C5 necesita refinamiento técnico, criterios de aceptación y validación de dependencias.

## Catálogo avanzado aplazado por dominio

Las prioridades originales P0–P3 se conservan como columna; agrupar por dominio no adelanta estas features. JOC-009 y JOC-047 ya fueron implementadas con los alcances documentados; sus entradas se mantienen como referencia. Las demás features conservan su alcance aplazado; Parcial en JOC-001, JOC-031 y JOC-032 reconoce mecanismos ya presentes sin cerrar la épica. JOC-009 y JOC-010 tienen su definición única en el lote básico (prioridad histórica P0).

### Servicio

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-003 | Motor de variables | Pendiente | P0 | Crítica | Ejecución y fidelidad de peticiones | C4 | Configuración, UIX | Sin prerrequisito identificado | Resolver {{variable}} en URL, autorización, headers y body, con ámbitos, precedencia explícita y errores por valores ausentes. | Refinar aceptación antes de iniciar. |
| JOC-004 | Gestión de entornos | Pendiente | P0 | Crítica | Ejecución y fidelidad de peticiones | C3 | Configuración, Seguridad, UIX | JOC-003 | Crear y seleccionar DEV, QA y PRD, reutilizando las mismas peticiones con distintos valores y secretos. | Refinar aceptación antes de iniciar. |
| JOC-006 | Configuración heredada | Pendiente | P0 | Alta | Ejecución y fidelidad de peticiones | C3 | Configuración, Seguridad | JOC-003, JOC-005 | Heredar autorización, variables y encabezados desde colección o carpeta, permitiendo reemplazos y desactivación por petición. | Refinar aceptación antes de iniciar. |
| JOC-014 | Gestor de cookies | Pendiente | P1 | Alta | Ejecución y fidelidad de peticiones | C3 | Seguridad, UI | Sin prerrequisito identificado | Mantener cookies por workspace y dominio, respetar path, Secure y expiración, y permitir inspección, edición y limpieza. | Refinar aceptación antes de iniciar. |
| JOC-015 | Proxy configurable | Pendiente | P1 | Alta | Ejecución y fidelidad de peticiones | C4 | Seguridad, Compatibilidad | Sin prerrequisito identificado | Configurar proxy HTTP/HTTPS, autenticación y exclusiones por destino, con selección explícita entre configuración propia y del sistema. | Refinar aceptación antes de iniciar. |
| JOC-017 | Opciones HTTP avanzadas | Pendiente | P1 | Alta | Ejecución y fidelidad de peticiones | C3 | Seguridad, Compatibilidad | Sin prerrequisito identificado | Configurar redirects, máximo de saltos, HTTP/1.1 o HTTP/2 y timeouts; impedir filtrado de credenciales al cambiar de host. | Refinar aceptación antes de iniciar. |
| JOC-018 | Compresión y respuestas grandes | Pendiente | P1 | Alta | Ejecución y fidelidad de peticiones | C4 | Seguridad, UI | Sin prerrequisito identificado | Decodificar gzip/deflate y descargar por streaming, con límites configurables y protección frente a respuestas descomprimidas excesivas. | Refinar aceptación antes de iniciar. |
| JOC-034 | Cliente GraphQL | Pendiente | P2 | Alta | Ejecución y fidelidad de peticiones | C4 | UI, Compatibilidad | Sin prerrequisito identificado | Agregar introspección, explorador de esquema, autocompletado, variables y selección de operaciones; hoy solo puede enviarse manualmente por HTTP. | Refinar aceptación antes de iniciar. |
| JOC-035 | Cliente WebSocket | Pendiente | P2 | Alta | Ejecución y fidelidad de peticiones | C4 | UI, Seguridad | Sin prerrequisito identificado | Abrir conexiones ws/wss, enviar mensajes de texto/binarios, inspeccionar frames y gestionar ping, cierre y reconexión. | Refinar aceptación antes de iniciar. |
| JOC-036 | Cliente Socket.IO | Pendiente | P2 | Media | Ejecución y fidelidad de peticiones | C4 | UI, Compatibilidad | Sin prerrequisito identificado | Gestionar namespaces, eventos y acknowledgements mediante un adaptador específico; no tratar Socket.IO como WebSocket simple. | Refinar aceptación antes de iniciar. |
| JOC-037 | Cliente gRPC | Pendiente | P2 | Alta | Ejecución y fidelidad de peticiones | C5 | UI, Compatibilidad | Sin prerrequisito identificado | Importar protobuf o usar reflection, configurar metadata y ejecutar llamadas unarias y streaming con inspección de mensajes. | Refinar aceptación antes de iniciar. |
| JOC-038 | Server-Sent Events | Pendiente | P2 | Media | Ejecución y fidelidad de peticiones | C3 | UI, UIX | Sin prerrequisito identificado | Visualizar eventos SSE incrementalmente, con identificadores, reconexión y controles de sesión independientes del timeout HTTP convencional. | Refinar aceptación antes de iniciar. |
| JOC-039 | Asistencia SOAP/WSDL | Pendiente | P2 | Alta | Ejecución y fidelidad de peticiones | C4 | UI, Compatibilidad | Sin prerrequisito identificado | Complementar el body XML existente con importación WSDL, selección de operaciones, envelopes, SOAPAction e inspección de faults. | Refinar aceptación antes de iniciar. |
| JOC-040 | Cliente MQTT | Pendiente | P2 | Media | Ejecución y fidelidad de peticiones | C4 | UI, Seguridad | Sin prerrequisito identificado | Conectar a brokers, publicar y suscribirse a topics, configurar QoS y mensajes retenidos e inspeccionar sesiones. | Refinar aceptación antes de iniciar. |
| JOC-041 | Cliente MCP | Pendiente | P2 | Media | Ejecución y fidelidad de peticiones | C4 | Seguridad, Compatibilidad | Sin prerrequisito identificado | Conectar a servidores MCP, descubrir tools/resources/prompts y probar intercambios; controlar explícitamente el lanzamiento de procesos locales. | Refinar aceptación antes de iniciar. |

### Seguridad

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-002 | Almacén de secretos | Pendiente | P0 | Crítica | Protección de secretos y acceso | C4 | Datos, Log, Compatibilidad | Sin prerrequisito identificado | Separar credenciales de las colecciones, cifrar su almacenamiento y ocultarlas en historial, consola y exportaciones por defecto. | Refinar aceptación antes de iniciar. |
| JOC-013 | OAuth 2.0 | Pendiente | P1 | Crítica | Protección de secretos y acceso | C4 | Servicio, UIX | Sin prerrequisito identificado | Obtener y renovar tokens con Authorization Code + PKCE y Client Credentials; gestionar scopes, expiración y perfiles reutilizables. | Refinar aceptación antes de iniciar. |
| JOC-016 | Certificados y mTLS | Pendiente | P1 | Alta | Protección de secretos y acceso | C4 | Servicio, Compatibilidad | Sin prerrequisito identificado | Administrar CA privadas y certificados de cliente por host, con formatos comunes, validación de vigencia y claves protegidas. | Refinar aceptación antes de iniciar. |
| JOC-033 | Autenticaciones adicionales | Pendiente | P2 | Alta | Protección de secretos y acceso | C5 | Servicio, Compatibilidad | Sin prerrequisito identificado | Implementar proveedores separados para Digest, OAuth 1.0, AWS SigV4, NTLM y otros esquemas exigidos por las APIs objetivo. | Refinar aceptación antes de iniciar. |
| JOC-054 | Administración empresarial | Pendiente | P3 | Alta | Protección de secretos y acceso | C5 | Colaboración, Log | JOC-052 | Incorporar SSO, políticas organizacionales, auditoría y gestión de acceso, evitando registrar valores sensibles. | Refinar aceptación antes de iniciar. |

### UI — Interfaz visual

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-019 | Editor de código | Pendiente | P1 | Alta | Lectura y edición en escritorio | C3 | UIX, Servicio | Sin prerrequisito identificado | Agregar resaltado, autocompletado, números de línea, buscar/reemplazar y validación JSON/XML, conservando la edición de contenido inválido. | Refinar aceptación antes de iniciar. |
| JOC-047 | Visualización avanzada de respuestas | Implementada | P2 | Media | Lectura y edición en escritorio | C3 | UIX, Seguridad | Sin prerrequisito identificado | Implementada: árbol JSON colapsable, JSON/XML con resaltado de lectura, imágenes PNG/JPEG/GIF/BMP estáticas, consultas JSONPath literales (propiedades, índices, comodines y descendientes) y XPath 1.0. HTML como texto enriquecido JavaFX sin scripts, CSS, navegación ni recursos externos. Conservar Body y bytes originales. Límites y sintaxis en README; filtros/slices/uniones/funciones JSONPath, renderizado web completo y edición con resaltado quedan fuera de esta entrega. JOC-019 sigue pendiente para el editor del request. | Ver README y ResponseInspectorTest/ResponseViewerChecks; entrega histórica 2026-09-24. |
| JOC-048 | Comparación de respuestas | Pendiente | P2 | Media | Lectura y edición en escritorio | C3 | UIX, Datos | Sin prerrequisito identificado | Comparar ejecuciones o snapshots, resaltando cambios estructurales y permitiendo ignorar campos volátiles. | Refinar aceptación antes de iniciar. |

### UIX — Experiencia de usuario

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-005 | Carpetas y organización | Pendiente | P0 | Alta | Productividad y continuidad del trabajo | C3 | UI, Datos | Sin prerrequisito identificado | Agregar carpetas anidadas, arrastrar y soltar, reordenar y mover peticiones, además del CRUD de colecciones ya disponible. | Refinar aceptación antes de iniciar. |
| JOC-020 | Edición avanzada de parámetros | Pendiente | P1 | Alta | Productividad y continuidad del trabajo | C3 | Servicio, UI | Sin prerrequisito identificado | Sincronizar URL y tabla, admitir parámetros de ruta, edición masiva de headers/query y control explícito de codificación. | Refinar aceptación antes de iniciar. |
| JOC-030 | Workspaces locales | Pendiente | P1 | Alta | Productividad y continuidad del trabajo | C3 | Datos, Seguridad, UI | Sin prerrequisito identificado | Administrar proyectos aislados desde la UI, con búsqueda global, etiquetas y favoritos, sin mezclar entornos, cookies o secretos. | Refinar aceptación antes de iniciar. |
| JOC-032 | Accesibilidad y preferencias | Parcial | P1 | Alta | Productividad y continuidad del trabajo | C3 | UI, Compatibilidad | Sin prerrequisito identificado | Mejorar navegación por teclado, foco, lectores de pantalla, escalado HiDPI, tamaño de fuente y temas claro/oscuro. JOC-032.1 entrega temas, JOC-032.2 tamaños de texto y JOC-032.3 densidad visual; completarlas no cierra los requisitos restantes de accesibilidad. | Temas y densidad disponibles; accesibilidad integral pendiente. |

### Compatibilidad e interoperabilidad

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-007 | Importación Postman | Pendiente | P0 | Crítica | Intercambio y operación entre herramientas/sistemas | C4 | Datos, UIX | JOC-002.1; JOC-004 para entornos | Importar Collection v2.1 y entornos; preservar datos y reportar explícitamente las propiedades o scripts todavía no soportados. | Refinar aceptación antes de iniciar. |
| JOC-008 | Exportación de colecciones | Pendiente | P0 | Alta | Intercambio y operación entre herramientas/sistemas | C3 | Datos, Seguridad | JOC-008.1, JOC-002.1 | Exportar formato Jocote y Postman compatible, con selección de recursos, referencias a secretos y pruebas de ida y vuelta. | Refinar aceptación antes de iniciar. |
| JOC-022 | Importación OpenAPI | Pendiente | P1 | Alta | Intercambio y operación entre herramientas/sistemas | C4 | Servicio, Seguridad | Sin prerrequisito identificado | Importar OpenAPI 3.x en JSON/YAML, resolver referencias controladamente y generar peticiones, parámetros, esquemas y ejemplos. | Refinar aceptación antes de iniciar. |
| JOC-028 | CLI y reportes CI/CD | Pendiente | P1 | Alta | Intercambio y operación entre herramientas/sistemas | C4 | Automatización, Log | JOC-028.1; JOC-027 para colecciones | Ejecutar colecciones sin JavaFX, cargar entornos y secretos externos y generar códigos de salida y reportes JUnit/JSON/HTML. | Refinar aceptación antes de iniciar. |
| JOC-031 | Distribución nativa multiplataforma | Parcial | P1 | Alta | Intercambio y operación entre herramientas/sistemas | C4 | Seguridad, UIX | Sin prerrequisito identificado | Ampliar los runtimes existentes con instaladores, firma/notarización y pruebas reales de instalación y actualización por sistema y arquitectura soportados. | jlink y CI existentes; faltan instaladores y distribución firmada. |
| JOC-046 | Generación de código cliente | Pendiente | P2 | Media | Intercambio y operación entre herramientas/sistemas | C3 | Servicio, UI | Sin prerrequisito identificado | Ampliar cURL con ejemplos Java HttpClient, JavaScript y Python, manteniendo la misma semántica de autenticación y body. | Refinar aceptación antes de iniciar. |
| JOC-049 | Documentación exportable | Pendiente | P2 | Media | Intercambio y operación entre herramientas/sistemas | C3 | Seguridad, Datos | JOC-029, JOC-002.1 | Generar Markdown/HTML navegable con endpoints, parámetros, autorización y ejemplos; producir una salida segura para compartir. | Refinar aceptación antes de iniciar. |
| JOC-050 | Actualizaciones de la aplicación | Pendiente | P2 | Alta | Intercambio y operación entre herramientas/sistemas | C4 | Seguridad, Datos | JOC-031; estrategia de firma y recuperación | Detectar versiones, verificar integridad y firma, actualizar por plataforma y recuperar la versión anterior si falla la instalación. | Refinar aceptación antes de iniciar. |
| JOC-058 | Captura e importación de tráfico | Pendiente | P3 | Media | Intercambio y operación entre herramientas/sistemas | C5 | Servicio, Seguridad | Sin prerrequisito identificado | Capturar peticiones mediante proxy local o integración de navegador; para HTTPS, gestionar certificados y consentimiento explícito. | Refinar aceptación antes de iniciar. |
| JOC-060 | Extensiones controladas | Pendiente | P3 | Media | Intercambio y operación entre herramientas/sistemas | C5 | Seguridad, Servicio | Sin prerrequisito identificado | Definir puntos de extensión para importadores, autenticación y protocolos, con permisos y compatibilidad versionada; propuesta arquitectónica para Jocote. | Refinar aceptación antes de iniciar. |

### Datos y persistencia

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-001 | Persistencia robusta | Parcial | P0 | Crítica | Integridad y recuperación de datos locales | C4 | Compatibilidad | Sin prerrequisito identificado | Añadir migraciones del workspace, backups recuperables y bloqueo de escritura entre instancias; conservar el guardado atómico existente. | Guardado atómico existente; faltan migraciones, backups y coordinación. |
| JOC-011 | Autoguardado y recuperación | Pendiente | P0 | Alta | Integridad y recuperación de datos locales | C3 | UIX, Datos | JOC-011.1 | Recuperar borradores, pestañas abiertas y distribución de paneles tras reiniciar, diferenciando borradores de peticiones guardadas. | Refinar aceptación antes de iniciar. |
| JOC-012 | Historial de ejecuciones | Pendiente | P0 | Alta | Integridad y recuperación de datos locales | C3 | UIX, Seguridad | JOC-012.1, JOC-002 | Buscar y repetir ejecuciones anteriores, con snapshots de petición y respuesta, límites de retención y protección de datos sensibles. | Refinar aceptación antes de iniciar. |
| JOC-029 | Ejemplos guardados | Pendiente | P1 | Media | Integridad y recuperación de datos locales | C2 | Servicio, UIX | Sin prerrequisito identificado | Asociar múltiples ejemplos de petición/respuesta y descripciones a cada endpoint, reutilizables en documentación y mocks. | Refinar aceptación antes de iniciar. |

### Log y diagnóstico

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-021 | Consola de diagnóstico | Pendiente | P1 | Alta | Diagnóstico de fallos sin exponer secretos | C3 | Seguridad, UI | Sin prerrequisito identificado | Mostrar la petición efectivamente enviada, redirects, errores de conexión/TLS y tiempos disponibles, enmascarando secretos. | Refinar aceptación antes de iniciar. |

### Pruebas y demostraciones

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-025 | Aserciones y validación de contratos | Pendiente | P1 | Crítica | Confianza y reproducción del comportamiento | C4 | Servicio, UI | Definir motor de aserciones; JOC-023 si usa scripts | Validar status, headers, tiempos, contenido y JSON Schema; mostrar resultados por aserción y errores que se puedan diagnosticar. | Refinar aceptación antes de iniciar. |
| JOC-042 | Mock server local | Pendiente | P2 | Alta | Confianza y reproducción del comportamiento | C4 | Servicio, Automatización | JOC-029 | Servir respuestas desde ejemplos, con reglas de matching, latencias y escenarios de error; ejecución independiente de la ventana. | Refinar aceptación antes de iniciar. |
| JOC-044 | Pruebas de rendimiento | Pendiente | P2 | Media | Confianza y reproducción del comportamiento | C5 | Servicio, Log | JOC-027, JOC-028 | Configurar concurrencia, rampas y duración; medir throughput, percentiles y errores con controles de carga y consumo de recursos. | Refinar aceptación antes de iniciar. |

### Automatización

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-023 | Runtime JavaScript aislado | Pendiente | P1 | Crítica | Ejecución repetible sin intervención | C5 | Seguridad, Compatibilidad | Definir aislamiento y límites antes de implementar | Ejecutar scripts con límites de tiempo y memoria, cancelación y acceso restringido a archivos, procesos, red y clases Java. | Refinar aceptación antes de iniciar. |
| JOC-024 | Scripts pre-request y post-response | Pendiente | P1 | Crítica | Ejecución repetible sin intervención | C4 | Servicio, Compatibilidad | JOC-023, JOC-003 | Ejecutar hooks por colección, carpeta y petición, con variables y una API de scripting cuya compatibilidad Postman esté documentada. | Refinar aceptación antes de iniciar. |
| JOC-026 | Extracción y encadenamiento | Pendiente | P1 | Alta | Ejecución repetible sin intervención | C4 | Servicio, Seguridad | JOC-003; JOC-024 si usa hooks | Extraer tokens e identificadores de respuestas y usarlos en pasos posteriores, con condiciones y orden de ejecución definido. | Refinar aceptación antes de iniciar. |
| JOC-027 | Collection Runner | Pendiente | P1 | Crítica | Ejecución repetible sin intervención | C4 | Servicio, UIX | JOC-025, JOC-026; JOC-024 si ejecuta scripts | Ejecutar colecciones con orden, iteraciones, datos CSV/JSON, cancelación y políticas ante fallos, mostrando resultados por petición. | Refinar aceptación antes de iniciar. |
| JOC-043 | Monitores programados | Pendiente | P2 | Alta | Ejecución repetible sin intervención | C4 | Log, Datos | JOC-027, JOC-028; host persistente | Ejecutar verificaciones periódicas, almacenar resultados y emitir alertas mediante un runner persistente, sin depender de que la laptop esté encendida. | Refinar aceptación antes de iniciar. |
| JOC-057 | Flujos visuales | Pendiente | P3 | Media | Ejecución repetible sin intervención | C5 | UI, Servicio | JOC-027 | Construir workflows mediante nodos de petición, transformación, condición e iteración, reutilizando el runner y su modelo de ejecución. | Refinar aceptación antes de iniciar. |
| JOC-059 | Asistente IA opcional | Pendiente | P3 | Baja | Ejecución repetible sin intervención | C5 | Seguridad, UIX | Sin prerrequisito identificado | Proponer peticiones, pruebas y explicaciones de errores; controlar proveedor, costos y datos enviados, sin ejecutar acciones automáticamente. | Refinar aceptación antes de iniciar. |

### Colaboración y gobierno de APIs

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-045 | Integración Git de colecciones | Pendiente | P2 | Alta | Trabajo compartido y control de contratos | C4 | Datos, Compatibilidad | JOC-045.1 | Versionar recursos en archivos estables, revisar diffs y resolver conflictos, excluyendo secretos, historial y datos locales de ejecución. | Refinar aceptación antes de iniciar. |
| JOC-051 | Sincronización entre dispositivos | Pendiente | P3 | Alta | Trabajo compartido y control de contratos | C5 | Datos, Seguridad | Sin prerrequisito identificado | Agregar backend opcional con sincronización offline, revisiones, resolución de conflictos y separación de secretos personales. | Refinar aceptación antes de iniciar. |
| JOC-052 | Equipos y permisos | Pendiente | P3 | Alta | Trabajo compartido y control de contratos | C5 | Seguridad, UIX | JOC-051, JOC-069 | Compartir workspaces y colecciones con invitaciones y roles de lectura, edición y administración mediante autorización en el servidor. | Refinar aceptación antes de iniciar. |
| JOC-053 | Revisión colaborativa | Pendiente | P3 | Media | Trabajo compartido y control de contratos | C4 | UIX, Datos | JOC-052 | Añadir comentarios, forks, propuestas de cambio y aprobaciones sobre versiones identificables de colecciones y contratos. | Refinar aceptación antes de iniciar. |
| JOC-055 | Diseño y evolución de contratos | Pendiente | P3 | Alta | Trabajo compartido y control de contratos | C5 | Compatibilidad, Pruebas | Sin prerrequisito identificado | Editar y versionar OpenAPI, aplicar lint, detectar cambios incompatibles y reconciliar contratos con colecciones y documentación. | Refinar aceptación antes de iniciar. |
| JOC-056 | Catálogo y gobierno de APIs | Pendiente | P3 | Media | Trabajo compartido y control de contratos | C5 | Servicio, UIX | Sin prerrequisito identificado | Catalogar APIs por propietario y versión, buscar servicios y evaluar reglas de diseño, documentación y ciclo de vida. | Refinar aceptación antes de iniciar. |

### Perfil y cuentas

Estas tareas son **futuras y pendientes**. Requieren definir un backend o proveedor de identidad y su operación; no son necesarias para el perfil local. La identidad de Jocote nunca sustituye automáticamente la autorización configurada para una API externa.

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-069 | Registro e inicio de sesión opcionales | Pendiente | P3 | Media | Identidad y preferencias del usuario | C4 | Seguridad, Servicio, UIX | Definir backend o proveedor de identidad | Permitir crear una cuenta de Jocote, verificar el correo e iniciar sesión mediante un backend o proveedor de identidad definido. Mantener disponible el uso local sin cuenta. No almacenar contraseñas en colecciones o preferencias; la elección del mecanismo de autenticación se resuelve al diseñar esta entrega. No incluye sincronización ni roles de equipo. | Refinar aceptación antes de iniciar. |
| JOC-070 | Sesión de cuenta y cierre de sesión | Pendiente | P3 | Alta | Identidad y preferencias del usuario | C3 | Seguridad, Datos, UIX | JOC-069; JOC-002 si persiste tokens | Mostrar qué cuenta está conectada, gestionar expiración y restauración de sesión, y permitir cerrar sesión eliminando sus credenciales locales. Si se ofrece recordar sesión, proteger los tokens mediante JOC-002. Cerrar sesión o perder conexión conserva las colecciones locales y permite continuar trabajando. Depende de JOC-069. | Refinar aceptación antes de iniciar. |
| JOC-071 | Gestión y recuperación de cuenta | Pendiente | P3 | Media | Identidad y preferencias del usuario | C4 | Seguridad, Servicio, UIX | JOC-069, JOC-070 | Editar nombre y correo de la cuenta, verificar cambios de correo y permitir recuperar acceso o cambiar contraseña cuando el mecanismo elegido la utilice. Incluir eliminación de cuenta con confirmación y explicación de los datos afectados; conservar los archivos locales salvo solicitud expresa de borrado. Depende de JOC-069 y JOC-070; usar los flujos del proveedor si corresponde. | Refinar aceptación antes de iniciar. |

## Ampliaciones y deuda técnica por rubro

Estas tareas registran rubros faltantes solicitados el 2026-10-24. Son propuestas o entregas por refinar; no amplían automáticamente el lote básico ni implican que exista implementación. La prioridad P1/P2 es orientativa dentro de este alcance. SSH, terminal local, terminal remota y bloqueo de aplicación necesitan confirmar su producto y diseño antes de programar una entrega.

### Core y arquitectura

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-072 | Separación incremental de núcleo y adaptadores | Propuesta | P1 | Alta | Reutilización del núcleo y mantenimiento | C4 | Servicio, Datos, UI, CLI | Inventariar contratos y dependencias por componente | Migrar por componente a dominios core, adaptadores y bootstrap; conservar RequestPreparer como contrato compartido. Aceptación: pruebas del núcleo sin toolkit, misma semántica HTTP/cURL, recursos e imports JPMS coherentes y sin ciclos. | Deuda técnica; paquetes generales actuales no equivalen a arquitectura objetivo. Separar cada refactor de features funcionales. |

### SSH y túneles

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-073 | Conexiones y túneles SSH para HTTP | Propuesta | P2 | Alta | Acceso a APIs de redes privadas | C5 | Seguridad, Servicio, UI, Compatibilidad | JOC-002 si persiste credenciales; definir adaptador SSH y JPMS | Definir perfiles host/puerto/usuario, claves o agente, verificación estricta de host y túneles locales. Aceptación: API de prueba accesible por túnel, rechazo de host key cambiada, cancelación y cierre sin puertos/procesos huérfanos. Conservar TLS y semántica Host/SNI del destino HTTP. | No existe adaptador SSH; definir primero alcance de túneles. Nunca aceptar automáticamente claves desconocidas ni guardar contraseñas en colecciones. |

### Terminales

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-074 | Terminal local integrada | Propuesta | P2 | Media | Operación local desde escritorio | C5 | UI, Seguridad, Compatibilidad | Seleccionar emulador y adaptador PTY compatible con JPMS/jlink | Definir shells soportados por SO y terminal con PTY, entrada/salida interactiva, Unicode, resize, scrollback acotado y cancelación. Aceptación: sesión interactiva, cierre de árbol de procesos y pruebas por plataforma. Lanzar solo por acción explícita, pasando argumentos sin concatenar comandos. | No existe terminal; un TextArea con stdout no satisface emulación de terminal. No ejecutar el cURL importado automáticamente. |
| JOC-075 | Terminal remota SSH | Propuesta | P2 | Media | Operación interactiva remota | C5 | SSH, UI, Seguridad, Compatibilidad | JOC-073 (conexión/verificación); componente de terminal de JOC-074 | Reutilizar conexión SSH con canal shell y PTY remoto, manteniendo ciclo de vida separado del túnel HTTP. Aceptación: interacción, resize, desconexión y cierre; cancelar la terminal no cierra un túnel aún utilizado por una petición. | No existe terminal remota. No registrar pulsaciones ni salida potencialmente sensible por defecto. |

### Seguridad y bloqueo de aplicación

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-076 | Bloqueo local de aplicación | Propuesta | P2 | Alta | Privacidad de vistas y sesiones locales | C4 | Perfil, UI, Datos, SSH | Definir amenaza, desbloqueo y política de secretos; JOC-002 si usa almacén protegido | Definir bloqueo manual y por inactividad, ocultamiento de información y política explícita para peticiones, terminales y túneles activos. Aceptación: desbloqueo verificable, sin pérdida de borradores, sin acceso alternativo a vistas y sin secretos persistidos como efecto del bloqueo. | No implementado; el bloqueo visual por sí solo no cifra datos en disco. Distinguir de login remoto y bloqueo de escritura JOC-001.1. |

### Datos y bloqueo de escritura

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-001.1 | Coordinar instancias sobre el mismo workspace | Pendiente | P1 | Crítica | Evitar sobrescritura y pérdida de colecciones | C3 | Datos, Compatibilidad, UIX | Diseñar coordinación para workspace y preferences del mismo directorio | Añadir bloqueo por directorio de datos con política de segunda instancia explícita. Aceptación: dos procesos no sobrescriben cambios; cierre normal o abrupto libera el bloqueo sin borrar datos. Mostrar ruta y recuperación segura sin forzar la apertura. | Subtarea de JOC-001; guardado atómico actual no evita actualizaciones perdidas. Validar en Windows/macOS/Linux y documentar límites de sistemas de archivos. |

### CLI sin escritorio

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-028.1 | Ejecutar una petición desde CLI | Propuesta | P1 | Alta | Uso en scripts y servidores sin pantalla | C4 | Core, Automatización, Seguridad, Log | JOC-072 para separación del runtime; definir contrato de argumentos | Entrada sin toolkit que reutilice preparación/HTTP/cURL. Aceptación: help/version, rechazo de opciones desconocidas, fin de opciones, stdout/stderr separados, códigos estables, stdin cuando se documente, timeout y cancelación. Probar en proceso sin display. | Entrega acotada de JOC-028; no requiere primero un runner de colecciones ni scripts. Separar módulos/runtimes si es necesario. |

### Instaladores y distribución

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-031.1 | Instalador para Windows | Propuesta | P1 | Alta | Instalación y ciclo de vida en Windows | C4 | Compatibilidad, Seguridad, UIX | Runtime jlink por SO/arquitectura; definir canal y firma | Definir MSI o EXE y alcance por usuario/equipo. Aceptación: instalar sin JDK, abrir, actualizar y desinstalar sin eliminar datos personales; validar firma según canal de distribución. | Subtarea de JOC-031; no confundir ZIP portable con instalador. Automatización de actualización en JOC-050. |
| JOC-031.2 | Distribución instalable para macOS | Propuesta | P1 | Alta | Instalación y ciclo de vida en macOS | C4 | Compatibilidad, Seguridad, UIX | Runtime jlink por SO/arquitectura; definir canal y firma | Definir DMG/PKG, bundle y arquitecturas. Aceptación: instalar sin JDK, lanzar, actualizar y desinstalar preservando datos; para distribución externa validar firma/notarización y arranque en sistema limpio. | Subtarea de JOC-031; no confundir ZIP portable con instalador. Automatización de actualización en JOC-050. |
| JOC-031.3 | Distribución instalable para Linux | Propuesta | P1 | Alta | Instalación y ciclo de vida en Linux | C4 | Compatibilidad, Seguridad, UIX | Runtime jlink por SO/arquitectura; definir canal y firma | Definir inicialmente formato y distribuciones soportadas antes de ampliar DEB/RPM u otro canal. Aceptación: instalar sin JDK, integrar lanzador, actualizar y desinstalar conservando datos; validar dependencias gráficas en sistema limpio. | Subtarea de JOC-031; no confundir ZIP portable con instalador. Automatización de actualización en JOC-050. |

### Pruebas y validación transversal

| Código | Tarea | Estado | Prioridad | Importancia | Impacto | Complejidad | Áreas relacionadas | Dependencias | Descripción / resultado esperado | Comentarios |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| JOC-077 | Matriz verificable de escritorio y distribución | Pendiente | P1 | Alta | Confianza en soporte por SO y arquitectura | C3 | Pruebas, Compatibilidad, UI, Seguridad | CI existente; JOC-031.1 a JOC-031.3 para pruebas de instaladores | Registrar SO, arquitectura, versión JDK/JavaFX, comandos, resultado y evidencia. Cubrir verify, smoke JavaFX, temas/densidad, teclado/HiDPI, rutas Unicode, jlink y arranque en equipo sin JDK; añadir instalación cuando exista. Aceptación: cada combinación declarada soportada tiene evidencia o limitación explícita. | Validación; CI configurada no equivale a runs aprobados ni verificación manual. Reutilizar tareas funcionales sin duplicarlas por plataforma. |

## Compatibilidad por plataforma

Este índice referencia tareas existentes; no agrega entregas al lote básico ni afirma que estas verificaciones ya se hayan ejecutado. Las diferencias de sistema se registran en la tarea correspondiente; sistema operativo y arquitectura se verifican por separado.

### macOS

- **JOC-031 / JOC-050:** instalación, firma, notarización, actualización y recuperación por arquitectura soportada.
- **JOC-032:** atajos, foco, accesibilidad y escalado de JavaFX.
- **JOC-002 / JOC-015 / JOC-016:** comprobar almacenamiento protegido y adaptadores de proxy/certificados cuando se implementen.
- **JOC-009 / JOC-010:** comprobar cURL y rutas de archivos con espacios y Unicode en los shells documentados para macOS.

### Windows

- **JOC-031 / JOC-050:** instalador, firma, permisos, actualización y recuperación por arquitectura soportada.
- **JOC-032:** teclado, foco, accesibilidad y escalado de JavaFX.
- **JOC-002 / JOC-015 / JOC-016:** comprobar almacenamiento protegido y adaptadores de proxy/certificados cuando se implementen.
- **JOC-009 / JOC-010:** comprobar cURL en PowerShell, selección del ejecutable y quoting de rutas con espacios y Unicode; documentar el shell soportado.

### Linux

- **JOC-031 / JOC-050:** paquetes o distribución elegida, permisos de ejecución, dependencias gráficas, actualización y recuperación.
- **JOC-032:** teclado, foco, accesibilidad y escalado en los entornos gráficos soportados.
- **JOC-002 / JOC-015 / JOC-016:** comprobar almacenamiento protegido y adaptadores de proxy/certificados cuando se implementen.
- **JOC-009 / JOC-010:** comprobar cURL en los shells documentados y rutas de archivos con espacios y Unicode.

En las tres plataformas, JOC-061 a JOC-064 comprueban el flujo básico visible de petición y respuesta. Los cambios de persistencia (JOC-001, JOC-011 y JOC-012, incluidas sus entregas acotadas) deben conservar rutas portables y recuperación consistente.

Para JOC-008.1, JOC-002.1, JOC-003.1 y JOC-045.1, verificar en los tres sistemas rutas con espacios y Unicode, JSON UTF-8, conservación del orden, ida y vuelta y exclusión de secretos. El exportador estable debe generar los mismos bytes ante los mismos datos con independencia del sistema operativo. Estas verificaciones están pendientes; no implican implementación.

Para perfil y preferencias, verificar por separado en macOS, Windows y Linux: restauración de configuración (JOC-068), acentos y expansión de textos traducidos (JOC-067), tema elegido y detección del sistema con respaldo (JOC-032.1), tamaños de fuente y escalado (JOC-032.2). Los datos del perfil (JOC-065) no se agregan a peticiones ni exportaciones de colecciones. Estos requisitos no duplican tareas por sistema operativo.

## Dependencias del catálogo avanzado aplazado

Estas relaciones se conservan para cuando se retome el alcance avanzado; no son requisitos previos para las tareas básicas actuales.

1. **JOC-001** establece cómo evolucionar el formato sin perder datos. Es requisito transversal para cambiar los modelos persistidos.
2. **JOC-002 → JOC-003 → JOC-004** establece secretos, resolución de variables y entornos. Historial, exportaciones y scripts deben reutilizar este contrato.
3. **JOC-005 + JOC-006 → JOC-007 + JOC-008** permite trasladar colecciones con su organización y configuración. La importación inicial puede preservar scripts sin ejecutarlos; debe informar claramente ese límite.
4. **JOC-009 y JOC-010** completan interoperabilidad por cURL y envío de archivos. El generador cURL existente debe actualizarse junto con el transporte.
5. **JOC-011 + JOC-012** añaden continuidad del trabajo y trazabilidad usando persistencia y redacción de secretos.
6. **JOC-013 a JOC-022** completan autenticación, red y edición. Proxy y mTLS se pueden adelantar si son requisitos de las APIs que se usarán primero.
7. **JOC-023 → JOC-024 + JOC-025 → JOC-026 + JOC-027 → JOC-028** establece scripting, pruebas, flujos, runner y CLI. Se debe definir el subconjunto de API Postman soportado; importar JSON no equivale a ejecutar cualquier script de Postman.
8. **JOC-029 → JOC-042 + JOC-049** reutiliza los ejemplos como fuente de mocks y documentación.
9. **JOC-027 + JOC-028 → JOC-043 + JOC-044** reutiliza el motor de ejecución para monitores y rendimiento. La alta disponibilidad exige un proceso/host persistente.
10. **JOC-051 → JOC-052 → JOC-053 + JOC-054** requiere diseñar un backend de colaboración. No basta con agregar controles a JavaFX.
11. **JOC-031** aprovecha el empaquetado actual; **JOC-050** depende de una estrategia de distribución y firma.
12. **JOC-055/JOC-056/JOC-057/JOC-059/JOC-060** deben refinarse como épicas independientes, después de validar su demanda.
13. **JOC-069 → JOC-070 → JOC-071** cubre el ciclo de cuenta remota; requiere un servicio de identidad y almacenamiento protegido para sesiones persistentes (JOC-002). JOC-051 añade sincronización y JOC-052 permisos compartidos por separado. Ninguna de estas features bloquea JOC-065 a JOC-068 ni las preferencias JOC-032.1/JOC-032.2.

La secuencia original de este catálogo queda aplazada. El siguiente trabajo se selecciona de la sección «Tareas básicas actuales».

## Reglas transversales para implementar cada feature

- Mantener un núcleo compartido Java sin dependencia de JavaFX para que UI, CLI, runner y monitores ejecuten las mismas reglas.
- Usar adaptadores pequeños para almacén de secretos, integración del sistema e instaladores; no duplicar la aplicación por plataforma.
- Mantener alineados transporte, cURL, importación/exportación y snapshots de ejecución. Documentar las diferencias inevitables.
- Evitar acceso de red y disco pesado en el hilo de JavaFX; prever cancelación y límites de recursos.
- Migrar datos con compatibilidad explícita, backups y pruebas de restauración.
- Incorporar pruebas relevantes de HTTP, TLS, proxy y protocolos mediante servicios controlados.
- Verificar comportamientos gráficos y distribución en Windows, macOS y Linux. No confundir sistema operativo con arquitectura.
- No prometer compatibilidad total con scripts Postman: su runtime y API requieren un contrato y una batería de compatibilidad propios.
- Descomponer las épicas C5 en entregas verificables antes de estimar esfuerzo o fechas.

## Fuentes de contraste

La [comparación de Bruno con Postman](https://www.usebruno.com/compare/bruno-vs-postman) inspira JOC-008.1 y JOC-045.1 por su enfoque de archivos locales y versionado externo. JOC-003.1 y JOC-002.1 son recortes de necesidades ya presentes en Jocote, no una copia de su arquitectura. La página es material comercial de Bruno; no se adoptan sus afirmaciones generales sobre precios, privacidad o limitaciones de Postman como criterios de aceptación. Prioridades y alcances son decisiones de este proyecto.

El referente para variables y entornos es la reutilización de valores entre peticiones y sus ámbitos, documentada en [Variables de Postman](https://learning.postman.com/docs/use/send-requests/variables/variables/). La migración de recursos y comandos está descrita en [Importación de datos](https://learning.postman.com/docs/getting-started/importing-and-exporting/importing-data).

Postman contempla adquisición y renovación de tokens mediante [OAuth 2.0](https://learning.postman.com/docs/use/send-requests/authorization/oauth-20), además de un almacén separado de secretos como [Postman Vault](https://learning.postman.com/docs/use/postman-vault/postman-vault-key). Los mecanismos exactos propuestos para Jocote requieren diseño propio.

La automatización de referencia usa [scripts pre-request y post-response](https://learning.postman.com/docs/tests-and-scripts/write-scripts/intro-to-scripts/) y [Collection Runner](https://learning.postman.com/docs/tests-and-scripts/running-collections/intro-to-collection-runs/). Los requisitos de aislamiento y límites aquí propuestos son decisiones para Jocote.

La cobertura de protocolos y funciones avanzadas se contrastó con [Postman API Client](https://www.postman.com/product/api-client/) y el índice de [documentación de Postman](https://learning.postman.com/docs/tests-and-scripts/write-scripts/intro-to-scripts/). Las funciones de equipo se contrastaron con [colaboración](https://learning.postman.com/docs/collaborating-in-postman/api-collaboration-features).

Persistencia, recuperación, distribución, actualizaciones y extensibilidad se incluyen por necesidades propias de Jocote; no implican que Postman implemente los mismos mecanismos.
