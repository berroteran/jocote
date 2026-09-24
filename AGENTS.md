# Jocote

- Java 21, JavaFX 21, Maven; cliente de escritorio compatible con Windows, macOS y Linux.
- Mantener `module-info.java` y la separación de paquetes: `model` (datos), `service` (lógica), `repository` (persistencia), `ui` (JavaFX).
- La lógica de HTTP, autenticación y cURL no debe depender de JavaFX. Toda petición y comando cURL debe pasar por `RequestPreparer`.
- No bloquear el hilo de JavaFX con operaciones de red. Cancelar peticiones al cerrar sus pestañas. Actualizar controles desde el hilo de JavaFX.
- No desactivar TLS, registrar credenciales ni incrustar rutas de un sistema operativo. Usar `Path`, UTF-8 y `user.home`.
- Evitar frameworks y dependencias innecesarias. Preferir servicios pequeños y modelos inmutables.
- Validar cambios con `mvn -B -ntp verify`. Para UI, ejecutar `mvn "-Djocote.uiTest=true" "-Dtest=JavaFxSmokeTest" test` en un escritorio o bajo Xvfb.
- Construir runtimes con `mvn javafx:jlink` en cada sistema y arquitectura destino; nunca afirmar validación de otra plataforma sin ejecutarla.
- No versionar `target/`, workspaces personales ni credenciales. No cambiar el archivo de workspace a un formato incompatible sin migración y pruebas.
