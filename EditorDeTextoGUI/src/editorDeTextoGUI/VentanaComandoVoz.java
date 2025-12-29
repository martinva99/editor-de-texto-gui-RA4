package editorDeTextoGUI;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.vosk.Model;
import org.vosk.Recognizer;

/**
 * Ventana de reconocimiento de voz mediante Vosk.
 * Captura audio del micrófono, lo procesa con Vosk y muestra el texto
 * reconocido.
 */
public class VentanaComandoVoz {

	private static JFrame frame;
	private static JTextPane panelTexto;
	private static Model model;
	private static Recognizer recognizer;
	private static TargetDataLine microphoneLine;
	private static SwingWorker<Void, String> worker;

	// Ruta al modelo de Vosk para español
	private static final String MODEL_PATH = "C:\\Users\\super\\Espacio de trabajo local\\EditorDeTextoGUI\\src\\models\\vosk-model-small-es-0.42";

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				VentanaComandoVoz window = new VentanaComandoVoz();
				window.frame.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	public VentanaComandoVoz() {
		initialize();
		startRecognition();
	}

	/**
	 * Inicializa los componentes de la interfaz gráfica
	 */
	private void initialize() {
		frame = new JFrame("Comando de Voz");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Liberar recursos al cerrar la ventana
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				stopRecognition();
			}
		});

		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		// Panel de texto donde se muestra el comando reconocido
		panelTexto = new JTextPane();
		panelTexto.setToolTipText("Aquí aparecerá el texto reconocido");
		frame.getContentPane().add(panelTexto, BorderLayout.CENTER);

		// Panel de botones
		JPanel panelBotones = new JPanel();
		panelBotones.setLayout(new GridLayout(1, 2, 50, 50));
		panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
		frame.getContentPane().add(panelBotones, BorderLayout.SOUTH);

		// Botón para ejecutar el comando reconocido
		JButton botonEjecutar = new JButton("EJECUTAR");
		botonEjecutar.setToolTipText("Ejecutar el comando de voz reconocido");
		botonEjecutar.addActionListener(e -> ejecutarComando());

		// Botón para volver sin ejecutar
		JButton botonVolver = new JButton("VOLVER");
		botonVolver.setToolTipText("Cerrar sin ejecutar comando");
		botonVolver.addActionListener(e -> {
			stopRecognition();
			frame.dispose();
		});

		panelBotones.add(botonVolver);
		panelBotones.add(botonEjecutar);

		frame.setVisible(true);
	}

	/**
	 * Ejecuta el comando de voz reconocido y muestra errores si no es válido
	 */
	private void ejecutarComando() {
		String texto = panelTexto.getText();

		// Validar texto vacío
		if (texto == null || texto.trim().isEmpty()) {
			JOptionPane.showMessageDialog(frame,
					"No se ha reconocido ningún comando.\n\n" +
							"Habla claramente al micrófono y espera a que aparezca " +
							"el texto antes de pulsar EJECUTAR.",
					"Sin comando",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		// Intentar ejecutar el comando
		boolean exito = NuiController.textoComando(texto);

		if (!exito) {
			// El comando no fue reconocido, mostrar error con lista de comandos válidos
			JOptionPane.showMessageDialog(frame,
					"Comando no reconocido: \"" + texto.trim() + "\"\n\n" +
							"Comandos disponibles:\n" +
							"• abrir - Abrir un archivo\n" +
							"• guardar - Guardar el archivo\n" +
							"• negrita - Aplicar negrita\n" +
							"• cursiva - Aplicar cursiva\n" +
							"• color - Cambiar color del texto\n" +
							"• mayúsculas - Convertir a mayúsculas\n" +
							"• minúsculas - Convertir a minúsculas\n" +
							"• invertir - Invertir el texto\n" +
							"• reemplazar - Buscar y reemplazar\n" +
							"• espacios - Eliminar espacios dobles",
					"Comando no válido",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Ejecuta la acción correspondiente al comando en VentanaPrincipal
	 */
	public static void onCommand(NuiCommand cmd) {
		switch (cmd) {
			case ABRIR_DOCUMENTO:
				VentanaPrincipal.abrirArchivo();
				break;
			case GUARDAR_DOCUMENTO:
				VentanaPrincipal.guardarArchivo();
				break;
			case APLICAR_NEGRITA:
				VentanaPrincipal.alternarEstilo("negrita");
				break;
			case APLICAR_CURSIVA:
				VentanaPrincipal.alternarEstilo("cursiva");
				break;
			case CAMBIAR_COLOR:
				VentanaPrincipal.aplicarColor();
				break;
			case INVERTIR:
				VentanaPrincipal.transformarTexto("invertir");
				break;
			case MAYUSCULAS:
				VentanaPrincipal.transformarTexto("mayus");
				break;
			case MINUSCULAS:
				VentanaPrincipal.transformarTexto("minus");
				break;
			case BUSCAR_REEMPLAZAR:
				VentanaPrincipal.abrirDialogoBuscar();
				break;
			case ELIMINAR_ESPACIOS:
				VentanaPrincipal.transformarTexto("espacios");
				break;
			default:
				break;
		}

		// Cerrar la ventana de comandos después de ejecutar
		if (frame != null) {
			stopRecognition();
			frame.dispose();
		}
	}

	/**
	 * Inicia el reconocimiento de voz usando Vosk
	 */
	public void startRecognition() {
		// Evitar múltiples workers activos
		if (worker != null && !worker.isDone()) {
			JOptionPane.showMessageDialog(frame,
					"El reconocimiento de voz ya está activo.",
					"Información",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		// Limpiar recursos residuales de sesiones anteriores
		if (recognizer != null || microphoneLine != null) {
			stopRecognition();
		}

		worker = new SwingWorker<Void, String>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					// Cargar el modelo solo la primera vez (se reutiliza)
					if (model == null) {
						File modelDir = new File(MODEL_PATH);

						if (!modelDir.exists() || !modelDir.isDirectory()) {
							throw new Exception("No se encontró el modelo de voz en:\n" + MODEL_PATH);
						}

						model = new Model(MODEL_PATH);
					}

					// Crear nuevo recognizer para esta sesión
					recognizer = new Recognizer(model, 16000);

					// Configurar audio (16 kHz, 16 bits, mono)
					AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
					DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

					// Verificar soporte del formato de audio
					if (!AudioSystem.isLineSupported(info)) {
						throw new LineUnavailableException(
								"El sistema no soporta el formato de audio requerido " +
										"(16kHz, 16 bits, mono)");
					}

					// Abrir micrófono
					microphoneLine = (TargetDataLine) AudioSystem.getLine(info);
					microphoneLine.open(format);
					microphoneLine.start();

					// Bucle de captura de audio
					byte[] buffer = new byte[4096];
					int bytesRead;

					while (!isCancelled()) {
						bytesRead = microphoneLine.read(buffer, 0, buffer.length);
						if (bytesRead <= 0)
							continue;

						boolean isFinal = recognizer.acceptWaveForm(buffer, bytesRead);

						if (isFinal) {
							publish(recognizer.getResult());
						} else {
							publish(recognizer.getPartialResult());
						}
					}

				} catch (LineUnavailableException lue) {
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(frame,
								"No se pudo acceder al micrófono.\n\n" +
										"Verifica que:\n" +
										"• El micrófono está conectado\n" +
										"• Ninguna otra aplicación lo está usando\n" +
										"• Los permisos de micrófono están habilitados\n\n" +
										"Error: " + lue.getMessage(),
								"Error de micrófono",
								JOptionPane.ERROR_MESSAGE);
						if (worker != null)
							worker.cancel(true);
					});
				} catch (Exception ex) {
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(frame,
								"Error al iniciar el reconocimiento de voz.\n\n" +
										"Error: " + ex.getMessage(),
								"Error",
								JOptionPane.ERROR_MESSAGE);
						if (worker != null)
							worker.cancel(true);
					});
				}
				return null;
			}

			@Override
			protected void process(List<String> texts) {
				if (texts == null || texts.isEmpty())
					return;

				String json = texts.get(texts.size() - 1);
				String text = extractText(json);

				// Solo actualizar si hay texto reconocido
				if (!text.isEmpty()) {
					panelTexto.setText(text);
				}
			}

			@Override
			protected void done() {
				try {
					get();
				} catch (Exception e) {
					// Excepciones ya manejadas en doInBackground
				}
			}
		};

		worker.execute();
	}

	/**
	 * Detiene el reconocimiento y libera recursos (excepto el modelo)
	 */
	public static void stopRecognition() {
		// Cancelar el worker
		if (worker != null && !worker.isDone()) {
			worker.cancel(true);
			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		worker = null;

		// Cerrar micrófono
		try {
			if (microphoneLine != null) {
				microphoneLine.stop();
				microphoneLine.close();
				microphoneLine = null;
			}
		} catch (Exception e) {
			// Ignorar errores al cerrar
		}

		// Cerrar recognizer (el modelo se reutiliza)
		try {
			if (recognizer != null) {
				recognizer.close();
				recognizer = null;
			}
		} catch (Exception e) {
			// Ignorar errores al cerrar
		}

		// Pausa para liberar recursos nativos
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Extrae el texto desde el JSON que devuelve Vosk
	 */
	public static String extractText(String json) {
		if (json == null || json.isEmpty())
			return "";

		// Corregir codificación UTF-8
		String jsonUtf8;
		try {
			jsonUtf8 = new String(json.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
		} catch (Exception e) {
			jsonUtf8 = json;
		}

		// Normalizar JSON (Vosk usa espacios alrededor de ":")
		String normalizedJson = jsonUtf8.replaceAll("\"\\s*:\\s*\"", "\":\"");

		// Buscar campo "text" (resultado final)
		int idx = normalizedJson.indexOf("\"text\":");
		if (idx >= 0) {
			int a = normalizedJson.indexOf('"', idx + 7);
			int b = normalizedJson.indexOf('"', a + 1);
			if (a >= 0 && b > a)
				return normalizedJson.substring(a + 1, b);
		}

		// Buscar campo "partial" (resultado parcial)
		idx = normalizedJson.indexOf("\"partial\":");
		if (idx >= 0) {
			int a = normalizedJson.indexOf('"', idx + 10);
			int b = normalizedJson.indexOf('"', a + 1);
			if (a >= 0 && b > a)
				return normalizedJson.substring(a + 1, b);
		}

		return "";
	}
}
