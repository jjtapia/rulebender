package rulebender.core.utility;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import rulebender.errorview.model.BNGLError;

public class ANTLRFilteredPrintStream extends PrintStream
{
	private PrintStream m_old;
	private String m_consoleName;
	private ArrayList<BNGLError> m_errors;
	private String m_file;
	
	public ANTLRFilteredPrintStream(OutputStream arg0, String consoleName, PrintStream oldStream, String file) 
	{
		super(arg0);
		setOld(oldStream);
		setConsoleName(consoleName);		
		setFile(file);
		
		m_errors = new ArrayList<BNGLError>();
	}
	
	@Override
	public void println(String s)
	{		
		if(s.subSequence(0, 4).equals("line"))
		{
			//super.println(s);
			//Console.displayOutput(m_consoleName, s);
			m_errors.add(parseError(s));
		}
		else
		{
			m_old.println(s);
		}
	}
		
	private void setOld(PrintStream old)
	{
		m_old = old;
	}
	
	private void setConsoleName(String name)
	{
		m_consoleName = name;
	}
	

	private BNGLError parseError(String s)
	{
		//line 13:3 no viable alternative at input 'version' in model
		
		// chomp "line "
		s = s.substring(s.indexOf(" ")).trim();
		
		// get the line number
		int line = Integer.parseInt(s.substring(0, s.indexOf(":")));
		
		// chomp "12:2 "
		s = s.substring(s.indexOf(" ")).trim();
		
		//DEBUG
		System.out.println("Error: \n\tFile" + m_file + "\n\tLine: " + line + "\n\tMessage: " + s);
		
		
		return new BNGLError(m_file, line , s);
	}
	
	public ArrayList<BNGLError> getErrorList()
	{
		return m_errors;
	}
	private void setFile(String file)
	{
		m_file = file;
	}
	
}

