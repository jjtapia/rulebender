package rulebender.prereq;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

public class PreReqChecker 
{
	
	public static boolean isPerlInPath()
	{
		String instruction = "perl --version";
		
		// run the process
		try 
		{
			Runtime.getRuntime().exec(instruction);
			return true;
		}
		catch (IOException e) 
		{
			return false;
		}	
	}
}

