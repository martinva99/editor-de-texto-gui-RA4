package editorDeTextoGUI;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * Componente personalizado para mostrar barra de progreso
 */
public class ProgressLabel extends JPanel {

	public enum Estado {
		IDLE, WORKING, DONE, ERROR
	}

	private JLabel statusLabel;
	private JLabel iconLabel;
	private JProgressBar progressBar;
	private Timer autoHideTimer;

	public ProgressLabel() {
		setLayout(new BorderLayout(5, 0));

		statusLabel = new JLabel("Listo");
		iconLabel = new JLabel();

		progressBar = new JProgressBar(0, 100);
		progressBar.setVisible(false);
		progressBar.setStringPainted(true);

		add(statusLabel, BorderLayout.WEST);
		add(iconLabel, BorderLayout.CENTER);
		add(progressBar, BorderLayout.EAST);
	}

	/**
	 * Cambia el texto de operación visible
	 */
	public void setOperation(String op) {
		statusLabel.setText(op);
	}

	/**
	 * Cambia el estado visual del componente
	 */
	public void setState(Estado estado) {

		// Evitar timers solapados: si existe uno en marcha, lo detenemos
		if (autoHideTimer != null && autoHideTimer.isRunning()) {
			autoHideTimer.stop();
		}

		switch (estado) {

		case IDLE:
			progressBar.setVisible(false);
			statusLabel.setText("Listo");
			statusLabel.setForeground(Color.GRAY);
			iconLabel.setIcon(null);
			break;

		case WORKING:
			progressBar.setVisible(true);
			statusLabel.setText("Procesando...");
			statusLabel.setForeground(Color.BLUE);
			iconLabel.setIcon(UIManager.getIcon("OptionPane.informationIcon"));
			break;

		case DONE:
			progressBar.setVisible(false);
			statusLabel.setText("Completado");
			statusLabel.setForeground(Color.GREEN);
			iconLabel.setIcon(UIManager.getIcon("FileView.fileIcon"));

			// Ocultar automáticamente después de 3 segundos
			autoHideTimer = new Timer(3000, e -> setState(Estado.IDLE));
			autoHideTimer.setRepeats(false);
			autoHideTimer.start();
			break;

		case ERROR:
			progressBar.setVisible(false);
			statusLabel.setText("Error");
			statusLabel.setForeground(Color.RED);
			iconLabel.setIcon(UIManager.getIcon("OptionPane.errorIcon"));
			break;
		}
	}

	/**
	 * Actualiza el valor de la barra de progreso
	 */
	public void setProgress(int value) {
		progressBar.setValue(value);
	}
}
