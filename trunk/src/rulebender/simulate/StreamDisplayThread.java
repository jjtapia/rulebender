package rulebender.simulate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import rulebender.core.utility.Console;

public class StreamDisplayThread extends Thread 
{
	private InputStream m_stream;
	private boolean m_printNow;
	private String m_log="";
	private String m_name;
	
	public StreamDisplayThread(String name, InputStream stream, boolean printNow)
	{
		m_stream = stream;
		m_printNow = printNow;
		m_name = name;
	}
	
	public void run()
	{
		String line = "";
		
		BufferedReader buffer = new BufferedReader(new InputStreamReader(m_stream));
		
		try 
		{
			while ((line = buffer.readLine()) != null) 
			{
				if(!m_printNow)
					Console.displayOutput(m_name, line);
				
				m_log += line + Console.getConsoleLineDelimeter();
			}
		} 
		catch (IOException e) 
		{		
			// This happens if the simulation/scan is cancelled.  
			//e.printStackTrace();
		}
	}	

	public String getLog()
	{
		return m_log;
	}
}

