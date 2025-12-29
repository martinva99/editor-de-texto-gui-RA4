---
trigger: always_on
---

# AGENTS.md

## Objetivo general
Este proyecto utiliza **Vosk** para **reconocimiento de voz**, y todas las contribuciones del agente deben centrarse **exclusivamente** en este ámbito.  
Cualquier código, documentación o prueba que no esté directamente relacionado con Vosk—modelos, reconocimiento, audio, decodificación, utilidades del motor, etc.—**no debe modificarse bajo ninguna circunstancia**.

---

## Regla principal — **Muy importante**
### ❗ No modifiques nada sin consultármelo antes  
El agente debe **pedir confirmación explícita** antes de:
- Editar código existente
- Añadir funciones nuevas
- Eliminar cualquier cosa
- Refactorizar
- Cambiar dependencias o versiones de Vosk
- Modificar scripts, pruebas o archivos auxiliares

Sin confirmación directa del usuario, **el agente no debe hacer ningún cambio**.

---

## Alcance permitido
El agente solo puede trabajar en código relacionado con:
- Integración con Vosk
- Gestión de modelos (`model` directory, rutas, carga, fallback…)
- Captura de audio
- Procesamiento de audio
- Conversión a texto
- Manejo de resultados parciales y finales
- Optimización segura del pipeline de reconocimiento

Todo lo que quede fuera de esa lista requiere aprobación explícita.

---

## Metodología de trabajo
Para cada tarea el agente debe:

1. **Leer cuidadosamente las instrucciones del usuario.**
2. Si falta información o existe duda → **preguntar y detenerse**.
3. Antes de modificar cualquier archivo → **preguntar si puede hacerlo**.
4. Al añadir o modificar código:
   - **Cada línea nueva debe incluir un comentario explicativo.**
   - **Cada línea modificada debe incluir un comentario explicando por qué se cambió.**
5. Entregar el código final solo tras confirmación del usuario.

---

## Buenas prácticas
- No asumir comportamientos no especificados.
- No introducir dependencias nuevas sin permiso.
- No cambiar parámetros de modelos Vosk sin autorización explícita.
- Evitar lógica difícil de leer.
- Mantener funciones pequeñas, claras y documentadas.
- Verificar siempre que el manejo de audio no cause bloqueos o fugas de recursos.

---

## Estilo de código para este proyecto
- Comentarios obligatorios por línea modificada/añadida.
- Evitar transformar o filtrar audio sin justificarlo.
- Manejar errores con claridad: si el modelo no está disponible, si el audio llega corrupto, etc.

