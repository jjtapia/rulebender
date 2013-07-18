package rulebender.simulate;

import java.io.InputStream;

public class ConsoleReader extends StreamDisplayThread {

	private String error = "";

	/**
	 * Instantiates a new stream handler. The output is given to the SimSystem
	 * with the appropriate Level.
	 * 
	 * @param input
	 *          the input stream
	 * @param reportLevel
	 *          the report level
	 */
	public ConsoleReader(InputStream input) {
		super("BNGConsole", input, true);
	}

	/**
	 * Processes the line which was written on the stream. In the default
	 * implementation it is just passed to the SimSystem.
	 * 
	 * @param line
	 *          the line
	 */
	@Override
	protected void processLine(String line) {
		super.processLine(line);
		if (line.startsWith("ERROR")) {
			error += line;
		}
		if (hadError() && line.trim().startsWith("at line")) {
			error += line;
		}
	}

	/**
	 * Error occurred in the console.
	 * 
	 * @return true, if error occurred
	 */
	public boolean hadError() {
		return !error.equals("");
	}

	/**
	 * Gets the error.
	 * 
	 * @return the error
	 */
	public void reportError() {
		processLine("An ERROR occuered during the processing in BioNetGen\n"
		    + error);
		error = "";
	}

	public String getError() {
		return error;
	}
}
