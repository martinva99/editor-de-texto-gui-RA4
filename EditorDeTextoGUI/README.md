# Editor de Texto GUI

<div style="color: blue"><strong>NOTA:</strong> Todo el contenido relacionado con las mejoras de <strong>Usabilidad y Accesibilidad (RA4)</strong> se muestra en este color.</div>

---

## Descripción del Proyecto
El **Editor de Texto GUI** es una aplicación de escritorio desarrollada en **Java (Swing)** que permite editar texto enriquecido con un enfoque en **preservar estilos**, **transformar texto sin perder formato**, cargar archivos grandes con **indicadores de progreso**, y control mediante interfaces naturales.

El programa soporta archivos **.txt** y **.rtf**, permite aplicar transformaciones avanzadas, gestionar estilos, realizar búsqueda y reemplazo respetando atributos y mostrar estados visuales mediante una clase personalizada `ProgressLabel`.

---

## <span style="color: blue">Mejoras de Interfaz (RA4)</span>

<div style="color: blue">
Para mejorar la usabilidad (Heurísticas de Nielsen) y la accesibilidad (WCAG), se ha reestructurado la interfaz principal:

- **Barra de Menús Estándar:** Se ha sustituido el panel de botones denso por una <code>JMenuBar</code> organizada en categorías lógicas (Archivo, Formato, Herramientas).
- **Navegación por Teclado:** Todas las funcionalidades son operables con teclado.
- **Limpieza Visual:** Se ha eliminado el ruido visual para un diseño más minimalista.
</div>

---

## Funcionalidades Principales

### Transformaciones de texto
Todas las transformaciones mantienen atributos como **negrita**, **cursiva**, **color**, **fuente**, etc.

- Convertir a **mayúsculas**
- Convertir a **minúsculas**
- **Invertir** el texto seleccionado
- Eliminar **espacios múltiples**

---

### Formato de texto
- **Negrita**
- **Cursiva**
- **Color del texto**
- Conservación de estilos al alternar opciones
- Uso de `StyledDocument` para modificar atributos

---

### Buscar y reemplazar (conservando formato)
Permite localizar y reemplazar contenido manteniendo negrita, cursiva, color y otros atributos.

---

### Cargar archivos (TXT / RTF) con progreso visual
- Lectura en **bloques de 8192 bytes**
- Progreso dividido por fases:
  - **0–10%** → preparación  
  - **10–50%** → lectura por bytes  
  - **50–95%** → parsing y formato  
  - **95–100%** → inserción final  
- Interfaz no bloqueada gracias a `SwingWorker`
- Indicadores visuales:
  - `JProgressBar`
  - `ProgressLabel`

---

### Guardar archivos
- Guardado en `.txt` y `.rtf`
- Conserva estilos y estructura del documento

---

## Reconocimiento de Voz (NUI)

El editor incluye un sistema de **control por comandos de voz** utilizando **Vosk** (motor de reconocimiento de voz offline).

### Características
- Reconocimiento de voz en **español** usando el modelo `vosk-model-small-es-0.42`  
- Procesamiento **offline** (no requiere conexión a Internet)  
- Muestra resultados parciales mientras se habla  
- Mensajes de error claros cuando el comando no es reconocido  

### Comandos de voz disponibles

| Comando | Acción |
|--------|--------|
| abrir | Abre el diálogo para cargar un archivo |
| guardar | Guarda el documento actual |
| negrita | Aplica o quita negrita |
| cursiva | Aplica o quita cursiva |
| color | Abre el selector de color |
| mayúsculas | Convierte el texto seleccionado a mayúsculas |
| minúsculas | Convierte el texto seleccionado a minúsculas |
| invertir | Invierte el orden de los caracteres |
| reemplazar | Abre el diálogo de buscar y reemplazar |
| espacios | Elimina espacios dobles |

### Uso
1. Ir al menú **Herramientas > Comandos de Voz** (o pulsar `Ctrl + Shift + V`).
2. Hablar claramente el comando al micrófono.
3. Esperar a que aparezca el texto reconocido.
4. Pulsar **"EJECUTAR"**.

### Requisitos técnicos
- Modelo Vosk en `src/models/vosk-model-small-es-0.42`  
- Biblioteca nativa `libvosk.dll` en `src/natives`  
- JARs de Vosk y JNA en `src/lib`  

---

## <span style="color: blue">Atajos de teclado</span>

<div style="color: blue">
Se han implementado atajos de teclado para todas las funciones principales:

| Categoría | Acción | Combinación |
|-----------|--------|-------------|
| **Archivo** | Abrir | `Ctrl + O` |
| | Guardar | `Ctrl + S` |
| **Formato** | Negrita | `Ctrl + N` |
| | Cursiva | `Ctrl + I` |
| | Mayúsculas | `Ctrl + Shift + M` |
| | Minúsculas | `Ctrl + M` |
| | Invertir | `Ctrl + R` |
| | Color | `Ctrl + Shift + C` |
| **Herramientas** | Buscar/Reemplazar | `Ctrl + F` |
| | Eliminar Espacios | `Ctrl + Shift + R` |
| | Voz (NUI) | `Ctrl + Shift + V` |
| **General** | Deshacer | `Ctrl + Z` |
</div>

---

## Menú contextual
- Cortar  
- Copiar  
- Pegar  

---

## Barra de estado
Muestra en tiempo real:
- Cantidad de caracteres  
- Cantidad de palabras  

---

## Estructura del Proyecto
EditorDeTextoGUI/
├── src/
│ ├── editorDeTextoGUI/
│ │ ├── VentanaPrincipal.java # Ventana principal (Refactorizada RA4)
│ │ ├── VentanaComandoVoz.java # Ventana de reconocimiento de voz
│ │ ├── NuiController.java # Controlador de comandos de voz
│ │ ├── NuiCommand.java # Enum con comandos disponibles
│ │ ├── NuiListener.java # Interface para escuchar comandos
│ │ └── ProgressLabel.java # Componente visual de progreso
│ ├── lib/ # JARs de Vosk y JNA
│ ├── models/ # Modelo de Vosk para español
│ └── natives/ # Biblioteca nativa libvosk.dll
└── README.md

---

## Ejecución del Programa

1. Importar el proyecto en Eclipse  
2. Abrir `VentanaPrincipal.java`  
3. Ejecutar como **Java Application**

---

## Pruebas Realizadas

### Pruebas generales
- Transformaciones de texto  
- Conservación de estilos  
- Buscar y reemplazar  
- Barra de progreso  
- Carga de archivos grandes  

---

### <span style="color: blue">Prueba 1 – Conversión a mayúsculas</span>

<div style="color: blue">
**Pasos:**
1. Escribir "hola mundo".
2. Seleccionar todo.
3. Ir a <strong>Menú Formato > Mayúsculas</strong> (o pulsar <code>Ctrl + Shift + M</code>).

**Resultado esperado:** "HOLA MUNDO" con el formato original intacto.
</div>

---

### <span style="color: blue">Prueba 2 – Alternar negrita y cursiva</span> 

<div style="color: blue">
**Pasos:** Seleccionar texto y usar atajos <code>Ctrl + N</code> / <code>Ctrl + I</code>.

**Resultado esperado:** Alternancia correcta sin pérdida de color u otros atributos.
</div>

---

### <span style="color: blue">Prueba 3 – Buscar y reemplazar</span>

<div style="color: blue">
**Pasos:** Pulsar <code>Ctrl + F</code>, buscar "hola" → reemplazar por "adiós".

**Resultado esperado:** Texto reemplazado correctamente.
</div>

---

### Prueba 4 – Menú contextual

**Pasos:** Cortar/copiar/pegar con clic derecho.

**Resultado esperado:** Funciona igual que los atajos de teclado.

---

### Prueba 5 – Barra de estado

**Resultado esperado:** Contadores correctos de caracteres y palabras.

---

### Prueba 6 – Carga de archivos grandes

**Pasos:** Abrir archivos de varios tamaños (1 KB – 10 MB). 

**Resultado esperado:**
- Interfaz fluida
- Progreso por fases
- Documento cargado correctamente
- ProgressLabel mostrando estados adecuados

---

### Pruebas NUI – Reconocimiento de voz

**Prueba – Comando válido**
1. Abrir ventana de voz (`Ctrl + Shift + V`).
2. Decir "negrita".
3. Pulsar "EJECUTAR".

**Resultado esperado:**
- Se aplica negrita al texto seleccionado.

**Prueba – Comando no válido**
1. Decir una frase sin comando.
2. Pulsar "EJECUTAR".

**Resultado esperado:**
- Mensaje de error.
- Lista de comandos disponibles.

---