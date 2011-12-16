package rulebender.core.utility;

import java.io.OutputStream;
import java.io.PrintStream;

public class ANTLRFilteredPrintStream extends PrintStream
{
	private PrintStream m_old;
	private String m_consoleName;
	
	public ANTLRFilteredPrintStream(OutputStream arg0, String consoleName, PrintStream oldStream) 
	{
		super(arg0);
		setOld(oldStream);
		setConsoleName(consoleName);		
	}
	
	@Override
	public void println(String s)
	{
		System.out.println("************* received in the filter println: " + s);
				
		if(s.subSequence(0, 4).equals("line"))
		{
			//super.println(s);
			Console.displayOutput(m_consoleName, s);
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
}

