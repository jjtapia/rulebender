package rulebender.simulate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import rulebender.core.utility.Console;

public class StreamDisplayThread extends Thread {
	private final InputStream m_stream;
	private final boolean m_printNow;
	private String m_log = "";
	private final String m_name;
	private int m_pid = -1;

	public StreamDisplayThread(String name, InputStream stream, boolean printNow) {
		m_stream = stream;
		m_printNow = printNow;
		m_name = name;
	}

	@Override
	public void run() {
		String line = "";

		BufferedReader buffer = new BufferedReader(new InputStreamReader(m_stream));

		try {
			while ((line = buffer.readLine()) != null) {
				processLine(line);

			}
		} catch (IOException e) {
			// This happens if the simulation/scan is cancelled.
			// e.printStackTrace();
		}
	}

	protected void processLine(String line) {
		if (m_printNow) {
			Console.displayOutput(m_name, line);
		}

		if (line.startsWith("[simulation PID is:")) {
			String[] split = line.split("\\s+");
			System.out.println("split[3]: " + split[3]);
			m_pid = Integer.parseInt(split[3].substring(0, split[3].indexOf("]"))
			    .trim());
		}
		m_log += line + Console.getConsoleLineDelimeter();
	}

	public String getLog() {
		return m_log;
	}

	public int getPID() {
		return m_pid;
	}
}
