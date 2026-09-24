# Jocote

Cliente REST de escritorio en **Java 21 y JavaFX**, con un único código fuente para **Windows, macOS y Linux**. Esta primera versión implementa el flujo esencial de trabajo solicitado, inspirado en Postman.

## Ejecutar

Requisitos de desarrollo: JDK 21 y Maven 3.9+. Maven descarga JavaFX y sus librerías nativas para el sistema actual; no necesitas instalar un SDK de JavaFX separado.

```sh
mvn javafx:run
```

También puedes compilar y usar los lanzadores:

```sh
mvn clean package
```

| Plataforma | Lanzador |
| --- | --- |
| Windows | Doble clic en `Jocote.bat` |
| macOS / Linux | `sh jocote.sh` |

Los lanzadores compilan si el JAR no existe. Después de modificar código, recompila con `mvn package` o usa `mvn javafx:run`. El lanzador Windows conserva visibles los errores.

El JAR requiere Java 21+ y la carpeta `target/lib` adyacente:

```sh
java -jar target/jocote-0.1.0-SNAPSHOT.jar
```

## Funciones

### Probar una API pública en un clic

1. Abre Jocote y pulsa **Probar GitHub** en el panel izquierdo.
2. Se guarda la colección **Demo GitHub** y se ejecuta `GET https://api.github.com/repos/berroteran/jocote`.
3. En **Respuesta** verás el estado HTTP, tiempo, tamaño y JSON real devuelto por GitHub; a la derecha aparece el cURL equivalente.
4. Abre con doble clic las otras peticiones de la colección y pulsa **Enviar**.

| Petición | Qué demuestra |
| --- | --- |
| Repositorio Jocote | Consulta de un recurso público y lectura de su JSON. |
| Perfil de berroteran | Consulta de un usuario público. |
| Repositorios públicos | Parámetros `per_page=5` y `sort=updated`; respuesta como lista. |
| Lenguajes de Jocote | Respuesta JSON pequeña con los lenguajes del repositorio. |

Las cuatro peticiones son GET y no requieren token. GitHub aplica límites a las peticiones sin autenticación; cualquier error o límite se muestra en la respuesta. Los endpoints públicos están documentados en [GitHub REST API](https://docs.github.com/en/rest/repos/repos#get-a-repository).

La colección está incluida en [demo-github.json](src/main/resources/dev/jocote/demo-github.json). Se agrega una sola vez y no reemplaza las colecciones existentes. Pulsar de nuevo el botón reutiliza la colección guardada, incluidas tus ediciones. No se realizan peticiones a GitHub al abrir normalmente la aplicación.

Para iniciar y ejecutar directamente la demo:

```sh
mvn javafx:run "-Djavafx.args=--demo"
```

También puedes usar `Jocote.bat --demo` en Windows o `sh jocote.sh --demo` en macOS/Linux, después de recompilar con `mvn package`.

### Cliente REST

- Panel izquierdo de colecciones, colapsable y redimensionable: crear, renombrar, eliminar, buscar y duplicar peticiones.
- Pestañas independientes para peticiones GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS y TRACE.
- Parámetros de query y encabezados editables, con activación por fila y valores repetidos.
- Autorización: ninguna, Bearer, Basic y API key en encabezado o query.
- Body: JSON, texto, XML y `application/x-www-form-urlencoded`. Content-Type automático, reemplazable por un encabezado explícito.
- Envío asíncrono, cancelación y timeout por petición de 1 a 600 segundos.
- Respuesta: código HTTP, tiempo, tamaño, encabezados, texto y JSON formateado; copia y guardado de los bytes originales.
- Panel derecho cURL colapsable, con generación en vivo para Bash/Zsh y PowerShell 7.3+.
- Guardado local de colecciones y aviso al cerrar pestañas con cambios pendientes.
- Atajos adaptados: Ctrl en Windows/Linux y ⌘ en macOS.

| Acción | Atajo |
| --- | --- |
| Nueva petición | Ctrl/⌘ + N |
| Enviar | Ctrl/⌘ + Enter |
| Guardar | Ctrl/⌘ + S |
| Cerrar pestaña | Ctrl/⌘ + W |

Doble clic sobre una celda para editar; Enter confirma el valor. Los parámetros del editor se agregan a los existentes en la URL. El cURL representa la pestaña activa, no necesariamente la última petición ejecutada.

## Datos locales y comportamiento HTTP

El workspace se guarda en `${user.home}/.jocote/workspace.json`. No se requiere una base de datos ni un backend. Para usar otro directorio:

```sh
java -Djocote.home=/ruta/al/workspace -jar target/jocote-0.1.0-SNAPSHOT.jar
```

En PowerShell, encierra `"-Djocote.home=C:\ruta\workspace"` entre comillas si contiene espacios. El guardado reemplaza el archivo de manera atómica cuando el sistema de archivos lo permite. Si el archivo existente está corrupto o tiene una versión incompatible, la aplicación muestra un error y lo conserva intacto.

**Las colecciones y las credenciales se guardan en texto plano.** cURL también incluye las credenciales configuradas. No subas tu workspace al repositorio. Esta versión no incorpora almacén seguro del sistema operativo ni coordinación de escrituras entre varias instancias; utiliza una instancia por workspace.

El cliente verifica certificados TLS y nombres de host. Los redirects se muestran como respuestas 3xx, sin seguirlos automáticamente. No se conserva una sesión de cookies automáticamente. Los encabezados `Host`, `Content-Length`, `Connection`, `Expect` y `Upgrade` están administrados/restringidos por el cliente HTTP de Java.

Las respuestas tienen un máximo de 10 MiB; la vista muestra hasta 300 000 caracteres. El guardado conserva todos los bytes recibidos dentro del límite. No hay vista específica de imágenes ni descompresión manual de respuestas comprimidas. La aplicación no solicita compresión por defecto.

## Organización

```text
src/main/java/
  module-info.java             Módulo JPMS dev.jocote
  dev/jocote/
    JocoteApplication.java     Composición y ciclo de vida
    Launcher.java              Entrada alternativa para distribución JAR
    model/                     Records y contratos de datos
    service/                   Preparación, HTTP, cURL y operaciones de workspace
    repository/                Serialización JSON y escritura a disco
    ui/                        Componentes y controladores JavaFX
src/main/resources/            Estilos compartidos
src/test/java/                 Pruebas de lógica, integración HTTP y UI
.github/workflows/build.yml    Matriz de construcción para los tres sistemas
```

La UI no construye encabezados ni codifica parámetros. `RequestPreparer` produce una petición normalizada que comparten el servicio HTTP y el generador cURL. Los servicios y modelos no dependen de JavaFX. El acceso a disco se encapsula en el repositorio; el servicio de workspace publica los cambios en memoria después de guardarlos correctamente.

Dependencias de producción: JavaFX Controls y Jackson. HTTP utiliza `java.net.http.HttpClient`. No hay Spring ni contenedor de inyección: las dependencias se conectan explícitamente al iniciar la aplicación.

## Validación

```sh
mvn -B -ntp verify
```

Las pruebas normales usan un servidor HTTP local y directorios temporales. No consumen APIs externas ni modifican colecciones personales. Cubren codificación, autorización, cURL, persistencia, errores HTTP, redirects, Unicode, timeout, cancelación y límite de respuesta.

La prueba gráfica es optativa porque requiere un escritorio:

```sh
mvn "-Djocote.uiTest=true" "-Dtest=JavaFxSmokeTest" test
```

En Linux sin escritorio:

```sh
xvfb-run -a mvn -Djocote.uiTest=true -Dtest=JavaFxSmokeTest test
```

Esta prueba abre JavaFX, verifica controles, ejecuta una petición contra un servidor local y guarda una captura en `target/jocote-preview.png`. Los tests se ejecutan en classpath; producción se ejecuta como módulo con Maven o el runtime generado.

Para verificar además las cuatro peticiones reales de la demo desde JavaFX (requiere internet y consume cuatro peticiones públicas a GitHub):

```sh
mvn "-Djocote.uiTest=true" "-Djocote.githubTest=true" "-Dtest=JavaFxSmokeTest" test
```

Esta verificación exige `200 OK` y el contenido esperado en cada respuesta, y genera `target/jocote-github-demo.png` y `target/github-demo-results.txt`. Es optativa: la suite normal y CI no dependen de GitHub.

## Distribución por plataforma

Para generar una imagen que incluya Java y JavaFX:

```sh
mvn clean verify javafx:jlink
```

El resultado está en `target/jocote-runtime/` y `target/jocote-runtime.zip`. Ejecuta `bin/Jocote.bat` en Windows o `bin/Jocote` en macOS/Linux. El usuario de esa distribución no necesita instalar Java ni Maven.

**La imagen debe construirse en el sistema y arquitectura destino.** El código es común; las librerías nativas de JavaFX y la JVM son específicas de la plataforma. La matriz de GitHub Actions construye, prueba la interfaz y publica un ZIP por runner. Windows ARM64, Linux ARM64 y otras arquitecturas requieren runners compatibles y validación adicional. La configuración CI no equivale a haber ejecutado esos sistemas localmente.

La imagen es portable; esta versión no genera instaladores MSI/DMG/DEB, firma de código ni notarización. En macOS, la distribución externa requerirá considerar los requisitos de firma de Apple.

## Alcance de esta versión

No es una réplica completa de Postman. Quedan fuera de esta primera entrega: importación/exportación Postman/OpenAPI, entornos y variables, historial, scripts y tests de colecciones, OAuth interactivo, multipart/archivos como body, WebSocket/gRPC, clientes TLS personalizados y colaboración/sincronización en nube.

## Referencias

- [OpenJFX: configuración con Maven](https://openjfx.io/openjfx-docs/maven)
- [Plugin oficial JavaFX: ejecución y jlink](https://github.com/openjfx/javafx-maven-plugin)
