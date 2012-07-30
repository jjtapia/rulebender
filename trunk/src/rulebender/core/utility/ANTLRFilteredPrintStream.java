package rulebender.core.utility;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import rulebender.errorview.model.BNGLError;
import rulebender.logging.Logger;
import rulebender.logging.Logger.LOG_LEVELS;

public class ANTLRFilteredPrintStream extends PrintStream
{
	private PrintStream m_old;
	private String m_consoleName;
	private ArrayList<BNGLError> m_errors;
	private String m_file;
	
	public ANTLRFilteredPrintStream(OutputStream arg0, 
	    String consoleName, 
	    PrintStream oldStream, 
	    String file) 
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
	    Logger.log(LOG_LEVELS.INFO, this.getClass(), "Potential Error: " + s);

	    m_errors.add(parseError(s));
		}
		else if(s.subSequence(5,9).equals("line"))
		{
		  s = s.replaceAll("null\\s+", "");
		  //FIXME The second condition is for some strange errors being reported
	    // of the form "null line xx:xx message" instead of just 
	    // "line xx:xx message"
		  Logger.log(LOG_LEVELS.INFO, this.getClass(), "Potential Error: " + s);

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
	  // Example...
		//line 13:3 no viable alternative at input 'version' in model
	  
		// chomp "line "
		s = s.substring(s.indexOf(" ")).trim();
		
		// get the line number
		int line = Integer.parseInt(s.substring(0, s.indexOf(":")));
		
		// chomp "12:2 "
		s = s.substring(s.indexOf(" ")).trim();
		
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

