package editorDeTextoGUI;

/**
 * Controlador que interpreta los comandos de voz reconocidos
 * y los convierte en acciones del editor
 */
public class NuiController {

	// Lista de comandos válidos para mostrar en mensajes de error
	public static final String[] COMANDOS_VALIDOS = {
			"abrir", "guardar", "negrita", "cursiva", "color",
			"mayúsculas", "minúsculas", "invertir", "reemplazar", "espacios"
	};

	/**
	 * Procesa el texto reconocido y ejecuta el comando correspondiente
	 * 
	 * @param texto El texto reconocido por Vosk
	 * @return true si el comando fue reconocido y ejecutado, false en caso
	 *         contrario
	 */
	public static boolean textoComando(String texto) {
		// Validar texto vacío
		if (texto == null || texto.trim().isEmpty()) {
			return false;
		}

		texto = texto.trim().toLowerCase();
		NuiCommand comando;

		switch (texto) {
			case "color":
				comando = NuiCommand.CAMBIAR_COLOR;
				break;
			case "negrita":
				comando = NuiCommand.APLICAR_NEGRITA;
				break;
			case "cursiva":
				comando = NuiCommand.APLICAR_CURSIVA;
				break;
			case "mayúsculas":
				comando = NuiCommand.MAYUSCULAS;
				break;
			case "minúsculas":
				comando = NuiCommand.MINUSCULAS;
				break;
			case "invertir":
				comando = NuiCommand.INVERTIR;
				break;
			case "reemplazar":
				comando = NuiCommand.BUSCAR_REEMPLAZAR;
				break;
			case "espacios":
				comando = NuiCommand.ELIMINAR_ESPACIOS;
				break;
			case "abrir":
				comando = NuiCommand.ABRIR_DOCUMENTO;
				break;
			case "guardar":
				comando = NuiCommand.GUARDAR_DOCUMENTO;
				break;
			default:
				return false; // Comando no reconocido
		}

		VentanaComandoVoz.onCommand(comando);
		return true;
	}

	/**
	 * Devuelve una cadena con los comandos válidos formateados para mostrar
	 */
	public static String getComandosDisponibles() {
		return String.join(", ", COMANDOS_VALIDOS);
	}
}
