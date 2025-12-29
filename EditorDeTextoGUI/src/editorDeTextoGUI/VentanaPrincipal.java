package editorDeTextoGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import javax.swing.undo.UndoManager;

/**
 * Ventana principal del editor de texto con interfaz gráfica
 */
public class VentanaPrincipal extends JFrame {

	private static JTextPane textPane;
	private static JLabel lblEstado;
	private UndoManager undoManager = new UndoManager();
	private static ProgressLabel progressLabel;
	private NuiController controller;

	public VentanaPrincipal() {
		// Inicialización de la ventana
		super("Editor de texto GUI");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(800, 600);
		setLocationRelativeTo(null);
		getContentPane().setLayout(new BorderLayout());

		JMenuBar menuBar = new JMenuBar();

		// Menú desplegable para acciones de archivo (y sus items con cada acción) 
		JMenu menuArchivo = new JMenu("Archivo");

		JMenuItem itemAbrir = new JMenuItem("Abrir (Ctrl + O)");
		itemAbrir.addActionListener(e -> abrirArchivo());
		itemAbrir.setToolTipText("Abrir un archivo de texto existente");

		JMenuItem itemGuardar = new JMenuItem("Guardar (Ctrl + S)");
		itemGuardar.addActionListener(e -> guardarArchivo());
		itemGuardar.setToolTipText("Guardar el texto en un archivo (.rtf)");

		menuArchivo.add(itemAbrir);
		menuArchivo.add(itemGuardar);

		// Menú desplegable para acciones de formato (y sus items con cada acción)
		JMenu menuFormato = new JMenu("Formato");

		JMenuItem itemNegrita = new JMenuItem("Negrita (Ctrl + B)");
		itemNegrita.addActionListener(e -> alternarEstilo("negrita"));
		itemNegrita.setToolTipText("Transformar texto a negrita");

		JMenuItem itemCursiva = new JMenuItem("Cursiva (Ctrl + I)");
		itemCursiva.addActionListener(e -> alternarEstilo("cursiva"));
		itemCursiva.setToolTipText("Transformar texto a cursiva");

		JMenuItem itemMayus = new JMenuItem("MAYÚSCULAS (Ctrl + Shift + M)");
		itemMayus.addActionListener(e -> transformarTexto("mayus"));
		itemMayus.setToolTipText("Transformar texto a mayúsculas");

		JMenuItem itemMinus = new JMenuItem("minúsculas (Ctrl + M)");
		itemMinus.addActionListener(e -> transformarTexto("minus"));
		itemMinus.setToolTipText("Transformar texto a minúsulas");

		JMenuItem itemInvertir = new JMenuItem("Invertir (Ctrl + R)");
		itemInvertir.addActionListener(e -> transformarTexto("invertir"));
		itemInvertir.setToolTipText("Invertir el orden de todos los caracteres del texto seleccionado");

		JMenuItem itemColor = new JMenuItem("Color (Ctrl + Shift + C)");
		itemColor.addActionListener(e -> aplicarColor());
		itemColor.setToolTipText("Cambiar el color del texto");

		menuFormato.add(itemNegrita);
		menuFormato.add(itemCursiva);
		menuFormato.add(itemMayus);
		menuFormato.add(itemMinus);
		menuFormato.add(itemInvertir);
		menuFormato.add(itemColor);

		// Menú desplegable para otras acciones (y sus items con cada acción)
		JMenu menuHerramientas = new JMenu("Herramientas");

		JMenuItem itemBuscar = new JMenuItem("Buscar/Reemplazar (Ctrl + F)");
		itemBuscar.addActionListener(e -> abrirDialogoBuscar());
		itemBuscar.setToolTipText("Buscar una palabra y reemplazarla por otra");

		JMenuItem itemEspacios = new JMenuItem("Eliminar espacios dobles (Ctrl + Shift + R)");
		itemEspacios.addActionListener(e -> transformarTexto("espacios"));
		itemEspacios.setToolTipText("Eliminar todos los espacios dobles del texto seleccionado");

		JMenuItem itemVoz = new JMenuItem("Voz (Ctrl + Shift + V)");
		itemVoz.addActionListener(e -> new VentanaComandoVoz());
		itemVoz.setToolTipText("Realizar una acción mediante un comando por voz");

		menuHerramientas.add(itemBuscar);
		menuHerramientas.add(itemEspacios);
		menuHerramientas.add(itemVoz);

		menuBar.add(menuArchivo);
		menuBar.add(menuFormato);
		menuBar.add(menuHerramientas);

		setJMenuBar(menuBar);

		// AREA DE TEXTO PRINCIPAL
		textPane = new JTextPane();
		JScrollPane scrollPane = new JScrollPane(textPane);
		getContentPane().add(scrollPane, BorderLayout.CENTER);

		// PANEL INFERIOR: estado y progreso
		JPanel panelSur = new JPanel(new BorderLayout());
		lblEstado = new JLabel("Caracteres: 0 | Palabras: 0");
		lblEstado.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		progressLabel = new ProgressLabel();

		panelSur.add(lblEstado, BorderLayout.WEST);
		panelSur.add(progressLabel, BorderLayout.EAST);

		getContentPane().add(panelSur, BorderLayout.SOUTH);

		// MENU CONTEXTUAL (clic derecho) - cortar/copiar/pegar
		JPopupMenu menu = new JPopupMenu();
		JMenuItem cortar = new JMenuItem("Cortar");
		JMenuItem copiar = new JMenuItem("Copiar");
		JMenuItem pegar = new JMenuItem("Pegar");

		menu.add(cortar);
		menu.add(copiar);
		menu.add(pegar);

		textPane.setComponentPopupMenu(menu);

		cortar.addActionListener(e -> textPane.cut());
		copiar.addActionListener(e -> textPane.copy());
		pegar.addActionListener(e -> textPane.paste());

		// Deshacer/rehacer: se añade un UndoableEditListener al documento
		textPane.getDocument().addUndoableEditListener(undoManager);

		// Atajos de teclado
		configurarAtajos();

		// KeyListener para actualizar contadores al escribir
		textPane.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				actualizarEstado(); // recalcula caracteres y palabras
			}
		});
	}

	/**
	 * Aplica transformaciones al texto seleccionado manteniendo el formato
	 */
	public static void transformarTexto(String tipo) {
		try {
			StyledDocument doc = textPane.getStyledDocument();
			int start = textPane.getSelectionStart();
			int end = textPane.getSelectionEnd();

			if (start == end)
				return; // nada seleccionado

			String textoOriginal = doc.getText(start, end - start);
			String textoTransformado = aplicarTransformacion(textoOriginal, tipo);

			// Si la longitud no cambia: conservar estilo carácter a carácter
			if (textoOriginal.length() == textoTransformado.length()) {
				transformarConservandoEstilos(doc, start, end, textoOriginal, textoTransformado, tipo);
			} else {
				// Si cambia la longitud: sin estilos
				doc.remove(start, end - start);
				doc.insertString(start, textoTransformado, null);
			}

			actualizarEstado();

		} catch (BadLocationException ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error al transformar el texto", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Aplica la transformación específica al texto (mayus/minus/invertir/espacios)
	 */
	public static String aplicarTransformacion(String texto, String tipo) {
		switch (tipo) {
		case "mayus":
			return texto.toUpperCase();
		case "minus":
			return texto.toLowerCase();
		case "invertir":
			return new StringBuilder(texto).reverse().toString();
		case "espacios":
			return texto.replaceAll("\\s+", " ");
		default:
			return texto;
		}
	}

	/**
	 * Transforma el texto conservando los estilos originales carácter por carácter
	 */
	public static void transformarConservandoEstilos(StyledDocument doc, int start, int end, String textoOriginal,
			String textoTransformado, String tipo) throws BadLocationException {

		int length = textoTransformado.length();

		// Capturar los AttributeSet de cada carácter
		AttributeSet[] atributosArr = new AttributeSet[length];

		for (int i = 0; i < length; i++) {
			int posOriginal;

			if ("invertir".equals(tipo)) {
				// Si invertimos, la posición original corresponde al final
				posOriginal = textoOriginal.length() - 1 - i;
			} else {
				posOriginal = i;
			}

			Element elem = doc.getCharacterElement(start + posOriginal);
			atributosArr[i] = elem.getAttributes();
		}

		// Reemplazar usando los atributos guardados
		for (int i = 0; i < length; i++) {
			doc.remove(start + i, 1);
			doc.insertString(start + i, String.valueOf(textoTransformado.charAt(i)), atributosArr[i]);
		}
	}

	/**
	 * Alterna el estilo de negrita o cursiva en el texto seleccionado
	 */
	public static void alternarEstilo(String tipo) {
		StyledDocument doc = textPane.getStyledDocument();
		int start = textPane.getSelectionStart();
		int end = textPane.getSelectionEnd();
		if (start == end)
			return; // nada seleccionado

		Element elem = doc.getCharacterElement(start);
		AttributeSet attrs = elem.getAttributes();

		boolean actualNegrita = StyleConstants.isBold(attrs);
		boolean actualCursiva = StyleConstants.isItalic(attrs);

		// Crear un estilo temporal con las nuevas propiedades
		Style estilo = textPane.addStyle("estiloTemp", null);

		switch (tipo) {
		case "negrita":
			StyleConstants.setBold(estilo, !actualNegrita);
			StyleConstants.setItalic(estilo, actualCursiva);
			break;
		case "cursiva":
			StyleConstants.setItalic(estilo, !actualCursiva);
			StyleConstants.setBold(estilo, actualNegrita);
			break;
		}

		doc.setCharacterAttributes(start, end - start, estilo, false);
	}

	/**
	 * Aplica color al texto seleccionado mediante un selector de color
	 */
	public static void aplicarColor() {
		int start = textPane.getSelectionStart();
		int end = textPane.getSelectionEnd();
		if (start == end)
			return;

		Color nuevoColor = JColorChooser.showDialog(null, "Elige un color", Color.BLACK);
		if (nuevoColor == null)
			return;

		StyledDocument doc = textPane.getStyledDocument();
		Element elem = doc.getCharacterElement(start);
		AttributeSet attrs = elem.getAttributes();

		Style estilo = textPane.addStyle("colorTemp", null);
		// mantener negrita/cursiva existentes
		StyleConstants.setBold(estilo, StyleConstants.isBold(attrs));
		StyleConstants.setItalic(estilo, StyleConstants.isItalic(attrs));
		StyleConstants.setForeground(estilo, nuevoColor);

		doc.setCharacterAttributes(start, end - start, estilo, false);
	}

	/**
	 * Actualiza los contadores de caracteres y palabras en la barra de estado
	 */
	public static void actualizarEstado() {
		String texto = textPane.getText();
		int caracteres = texto.length();
		int palabras = texto.isEmpty() ? 0 : texto.trim().split("\\s+").length;
		lblEstado.setText("Caracteres: " + caracteres + " | Palabras: " + palabras);
	}

	/**
	 * Configura atajos de teclado. Se usan InputMap/ActionMap para atajos globales.
	 */
	public void configurarAtajos() {

		InputMap map = textPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		ActionMap act = textPane.getActionMap();

		// Atajo para deshacer (Ctrl+Z)
		map.put(KeyStroke.getKeyStroke("control Z"), "deshacer");
		act.put("deshacer", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				if (undoManager.canUndo()) {
					undoManager.undo();
				}
			}
		});

		// Atajo para negrita (Ctrl+B)
		map.put(KeyStroke.getKeyStroke("control B"), "negrita");
		act.put("negrita", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				alternarEstilo("negrita");
			}
		});

		// Atajo para cursiva (Ctrl+I)
		map.put(KeyStroke.getKeyStroke("control I"), "cursiva");
		act.put("cursiva", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				alternarEstilo("cursiva");
			}
		});

		// Atajo para minúsculas (Ctrl+M)
		map.put(KeyStroke.getKeyStroke("control M"), "minus");
		act.put("minus", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				transformarTexto("minus");
			}
		});

		// Atajo para mayúsculas (Ctrl+Shift+M)
		map.put(KeyStroke.getKeyStroke("control shift M"), "mayus");
		act.put("mayus", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				transformarTexto("mayus");
			}
		});

		// Atajo para invertir (Ctrl+R)
		map.put(KeyStroke.getKeyStroke("control R"), "invertir");
		act.put("invertir", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				transformarTexto("invertir");
			}
		});

		// Atajo para eliminar espacios dobles (Ctrl+Shift+R)
		map.put(KeyStroke.getKeyStroke("control shift R"), "espacios");
		act.put("espacios", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				transformarTexto("espacios");
			}
		});

		// Atajo para abrir un archivo (Ctrl+O)
		map.put(KeyStroke.getKeyStroke("control O"), "abrir");
		act.put("abrir", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				abrirArchivo();
			}
		});

		// Atajo para guardar el archivo (Ctrl+S)
		map.put(KeyStroke.getKeyStroke("control S"), "guardar");
		act.put("guardar", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				guardarArchivo();
			}
		});

		// Atajo para aplicar color (Ctrl+Shift+C)
		map.put(KeyStroke.getKeyStroke("control shift C"), "color");
		act.put("color", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				aplicarColor();
			}
		});

		// Atajo para comandos por voz (Ctrl+Shift+V)
		map.put(KeyStroke.getKeyStroke("control shift V"), "voz");
		act.put("voz", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				new VentanaComandoVoz();
			}
		});

		// Atajo para buscar/reemplazar (Ctrl+F)
		map.put(KeyStroke.getKeyStroke("control F"), "buscar");
		act.put("buscar", new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				abrirDialogoBuscar();
			}
		});

	}

	/**
	 * Ventana modal para buscar y reemplazar
	 */
	public static void abrirDialogoBuscar() {
		JDialog dialogo = new JDialog(new JFrame(), "Buscar y Reemplazar", true);
		dialogo.setLayout(new GridLayout(3, 2, 5, 5));
		dialogo.setSize(400, 150);
		dialogo.setLocationRelativeTo(null);

		JLabel lblBuscar = new JLabel("Buscar:");
		JLabel lblReemplazar = new JLabel("Reemplazar con:");
		JTextField txtBuscar = new JTextField();
		JTextField txtReemplazar = new JTextField();
		JButton btnReemplazar = new JButton("Reemplazar Todo");

		dialogo.add(lblBuscar);
		dialogo.add(txtBuscar);
		dialogo.add(lblReemplazar);
		dialogo.add(txtReemplazar);
		dialogo.add(new JLabel(""));
		dialogo.add(btnReemplazar);

		btnReemplazar.addActionListener(e -> {
			try {
				String buscar = txtBuscar.getText();
				String reemplazar = txtReemplazar.getText();
				StyledDocument doc = textPane.getStyledDocument();
				String texto = textPane.getText();

				// Reemplazo simple: busca la siguiente ocurrencia y la sustituye manteniendo
				// atributos
				int index = texto.indexOf(buscar);
				while (index >= 0) {
					Element elem = doc.getCharacterElement(index);
					AttributeSet attrs = elem.getAttributes();

					// Reemplaza manteniendo el formato del primer carácter encontrado
					doc.remove(index, buscar.length());
					doc.insertString(index, reemplazar, attrs);

					texto = textPane.getText();
					index = texto.indexOf(buscar, index + reemplazar.length());
				}

				actualizarEstado();
				dialogo.dispose();
			} catch (BadLocationException ex) {
				ex.printStackTrace();
			}
		});
		btnReemplazar.setToolTipText("Reemplaza un texto por otro.");

		dialogo.setVisible(true);
	}

	/**
	 * Guarda el contenido del editor en un archivo RTF con formato Usa SwingWorker
	 * para no bloquear
	 */
	public static void guardarArchivo() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Guardar archivo");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Archivos RTF (*.rtf)", "rtf");
		fileChooser.setFileFilter(filter);

		int seleccion = fileChooser.showSaveDialog(null);
		if (seleccion != JFileChooser.APPROVE_OPTION)
			return;

		File archivo = fileChooser.getSelectedFile();
		if (!archivo.getName().toLowerCase().endsWith(".rtf")) {
			archivo = new File(archivo.getAbsolutePath() + ".rtf");
		}

		progressLabel.setOperation("Guardando");
		progressLabel.setState(ProgressLabel.Estado.WORKING);
		progressLabel.setProgress(0);

		File destino = archivo;

		// SwingWorker: doInBackground -> proceso en segundo plano
		SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
			@Override
			protected Void doInBackground() {
				RTFEditorKit rtfKit = new RTFEditorKit();
				try (FileOutputStream fos = new FileOutputStream(destino)) {
					// Publica progreso intermedio (se recibe en process())
					publish(30);
					rtfKit.write(fos, textPane.getDocument(), 0, textPane.getDocument().getLength());
					publish(100);
				} catch (Exception ex) {
					SwingUtilities.invokeLater(() -> {
						progressLabel.setState(ProgressLabel.Estado.ERROR);
						JOptionPane.showMessageDialog(null, "Error al guardar el archivo:\n" + ex.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					});
				}
				return null;
			}

			@Override
			protected void process(java.util.List<Integer> chunks) {
				// process se ejecuta en EDT y actualiza la barra con el último valor publicado
				if (!chunks.isEmpty()) {
					progressLabel.setProgress(chunks.get(chunks.size() - 1));
				}
			}

			@Override
			protected void done() {
				try {
					get(); // propaga excepción si existió
					progressLabel.setProgress(100);
					progressLabel.setState(ProgressLabel.Estado.DONE);
					JOptionPane.showMessageDialog(null,
							"Archivo guardado correctamente con formato:\n" + destino.getName(), "Éxito",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
				}
			}
		};

		worker.execute(); // inicia el SwingWorker
	}

	/**
	 * Abre un archivo de texto o RTF en el editor
	 */
	public static void abrirArchivo() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("Abrir archivo");

		FileNameExtensionFilter rtfFilter = new FileNameExtensionFilter("Archivos RTF (*.rtf)", "rtf");
		FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt");
		fileChooser.addChoosableFileFilter(rtfFilter);
		fileChooser.addChoosableFileFilter(txtFilter);
		fileChooser.setFileFilter(rtfFilter);

		int seleccion = fileChooser.showOpenDialog(null);

		if (seleccion == JFileChooser.APPROVE_OPTION) {
			File archivo = fileChooser.getSelectedFile();

			progressLabel.setOperation("Cargando");
			progressLabel.setState(ProgressLabel.Estado.WORKING);
			progressLabel.setProgress(0);

			boolean esRTF = archivo.getName().toLowerCase().endsWith(".rtf")
					|| (fileChooser.getFileFilter() == rtfFilter);

			if (esRTF) {
				cargarArchivoRTF(archivo);
			} else {
				cargarArchivoTexto(archivo);
			}
		}
	}

	/**
	 * Carga un archivo RTF conservando el formato usando SwingWorker
	 */
	public static void cargarArchivoRTF(File archivo) {
		progressLabel.setProgress(5);

		SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
			@Override
			protected Void doInBackground() throws Exception {
				RTFEditorKit rtfKit = new RTFEditorKit();

				try (FileInputStream fis = new FileInputStream(archivo)) {
					publish(10);

					Document docTemp = rtfKit.createDefaultDocument();

					// Leer en bloques para poder publicar progreso por bytes
					long fileSize = archivo.length();
					byte[] buffer = new byte[8192];
					int bytesRead;
					long totalBytesRead = 0;

					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					while ((bytesRead = fis.read(buffer)) != -1) {
						baos.write(buffer, 0, bytesRead);
						totalBytesRead += bytesRead;

						// Se calcula un progreso parcial entre 10 y 50 (por eso *40)
						int progreso = 10 + (int) ((totalBytesRead * 40.0) / fileSize);
						publish(progreso);
					}

					byte[] fileContent = baos.toByteArray();

					publish(50);

					// Procesar contenido RTF en memoria
					try (ByteArrayInputStream bais = new ByteArrayInputStream(fileContent)) {
						rtfKit.read(bais, docTemp, 0);
					}

					publish(95);

					// Actualizamos el textPane en el EDT
					SwingUtilities.invokeAndWait(() -> {
						textPane.setDocument(docTemp);
						actualizarEstado();
					});

					publish(100);

				} catch (Exception ex) {
					SwingUtilities.invokeLater(() -> {
						progressLabel.setState(ProgressLabel.Estado.ERROR);
						JOptionPane.showMessageDialog(null, "Error al abrir el archivo RTF:\n" + ex.getMessage(),
								"Error", JOptionPane.ERROR_MESSAGE);
					});
				}
				return null;
			}

			@Override
			protected void process(java.util.List<Integer> chunks) {
				if (!chunks.isEmpty()) {
					progressLabel.setProgress(chunks.get(chunks.size() - 1));
				}
			}

			@Override
			protected void done() {
				try {
					get();
					progressLabel.setProgress(100);
					progressLabel.setState(ProgressLabel.Estado.DONE);
					JOptionPane.showMessageDialog(null, "Archivo RTF cargado correctamente:\n" + archivo.getName(),
							"Éxito", JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
				}
			}
		};

		worker.execute();
	}

	/**
	 * Carga un archivo de texto plano usando SwingWorker para no bloquear la
	 * interfaz
	 */
	public static void cargarArchivoTexto(File archivo) {
		progressLabel.setProgress(5);

		SwingWorker<Void, Integer> worker = new SwingWorker<Void, Integer>() {
			@Override
			protected Void doInBackground() throws Exception {
				try (BufferedReader reader = new BufferedReader(new FileReader(archivo))) {
					StringBuilder contenido = new StringBuilder();
					String linea;
					int lineCount = 0;

					publish(10);

					// Contamos líneas para poder calcular el progreso
					try (BufferedReader counter = new BufferedReader(new FileReader(archivo))) {
						while (counter.readLine() != null) {
							lineCount++;
						}
					}

					publish(20);

					// Leer línea a línea y actualizar progreso
					try (BufferedReader contentReader = new BufferedReader(new FileReader(archivo))) {
						int currentLine = 0;
						while ((linea = contentReader.readLine()) != null) {
							contenido.append(linea).append("\n");
							currentLine++;
							int progreso = 20 + (int) ((currentLine * 70.0) / lineCount);
							publish(Math.min(progreso, 90));
						}
					}

					final String textoFinal = contenido.toString();
					// Actualizar el textPane en el EDT y esperar a que se haga (invokeAndWait)
					SwingUtilities.invokeAndWait(() -> {
						textPane.setText(textoFinal);
						actualizarEstado();
					});

					publish(100);

				} catch (Exception ex) {
					SwingUtilities.invokeLater(() -> {
						progressLabel.setState(ProgressLabel.Estado.ERROR);
						JOptionPane.showMessageDialog(null, "Error al abrir el archivo:\n" + ex.getMessage(), "Error",
								JOptionPane.ERROR_MESSAGE);
					});
				}
				return null;
			}

			@Override
			protected void process(java.util.List<Integer> chunks) {
				if (!chunks.isEmpty()) {
					progressLabel.setProgress(chunks.get(chunks.size() - 1));
				}
			}

			@Override
			protected void done() {
				try {
					get();
					progressLabel.setProgress(100);
					progressLabel.setState(ProgressLabel.Estado.DONE);
					JOptionPane.showMessageDialog(null, "Archivo cargado correctamente:\n" + archivo.getName(), "Éxito",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
				}
			}
		};

		worker.execute();
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			VentanaPrincipal vp = new VentanaPrincipal();
			vp.setVisible(true);
		});
	}
}
