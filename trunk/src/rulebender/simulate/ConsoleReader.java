package rulebender.simulate;

import java.io.InputStream;

public class ConsoleReader extends StreamDisplayThread {

	private String errors = "";
	private boolean errorLast = false;
	private String warnings = "";
	private boolean warningLast = false;

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
		if (line.startsWith("ERROR")
		    || (line.startsWith("WARNING: Some problem processing"))) {
			errors += line + "\n";
			errorLast = true;
		} else if (errorLast) {
			errors += line + "\n";
			errorLast = false;
		} else if (line
		    .startsWith("WARNING: Attempt to execute action without loading model.")) {
			errors += line + "\n";
		} else if (line.startsWith("WARNING")
		    && !line.startsWith("WARNING: Attempt to ")) {
			warnings += line + "\n";
			warningLast = true;
		} else if (warningLast) {
			warnings += line + "\n";
			warningLast = false;
		}
	}

	/**
	 * Error occurred in the console.
	 * 
	 * @return true, if error occurred
	 */
	public boolean hadError() {
		return !errors.equals("");
	}

	/**
	 * Gets the error.
	 * 
	 * @return the error
	 */
	public void reportError() {
		super.processLine("ERRORS occurred during the processing in BioNetGen\n"
		    + errors);
		errors = "";
	}

	public String getError() {
		return errors;
	}

	/**
	 * warnings occurred in the console.
	 * 
	 * @return true, if warnings occurred
	 */
	public boolean hadWarnings() {
		return !warnings.equals("");
	}

	/**
	 * Gets the warnings.
	 * 
	 * @return the warnings
	 */
	public void reportWarnings() {
		super.processLine("WARNINGS occurred during the processing in BioNetGen\n"
		    + warnings);
		warnings = "";
	}

	public String getWarnings() {
		return warnings;
	}
}
