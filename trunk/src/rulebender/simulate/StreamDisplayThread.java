package rulebender.simulate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import editor.BNGEditor;

public class StreamDisplayThread extends Thread 
{
	InputStream m_stream;
	boolean m_printNow;
	String m_log="";
	
	public StreamDisplayThread(InputStream stream, boolean printNow)
	{
		m_stream = stream;
		m_printNow = printNow;
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
					BNGEditor.displayOutput(line + BNGEditor.getConsoleLineDelimeter());
				
				m_log += line + BNGEditor.getConsoleLineDelimeter();
			}
		} 
		catch (IOException e) 
		{			
			e.printStackTrace();
		}
	}	

	public String getLog()
	{
		return m_log;
	}
}
