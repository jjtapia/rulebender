package editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class VersionTracker 
{
	public static int v_major = 0, v_minor = 3, v_revision = 210;
	
	String versionString;
	String changesString;
	
	int major, minor, revision;
	
	public VersionTracker(String version, String changes)
	{
		this.versionString = version;
		this.changesString = changes;
		
		changesString = changesString.replace("-", "\n\t-");		
		
		// Split up the line.
		String[] lineSplit = versionString.split("\\.");
		
		// Get ints for the version numbers.
		major = Integer.parseInt(lineSplit[0]);
		minor = Integer.parseInt(lineSplit[1]);
		revision = Integer.parseInt(lineSplit[2]);
	}
	
	public int compare() 
	{
		if(major > v_major)
			return -1;
		else if(major == v_major)
		{
			if(minor > v_minor)
				return -2;
			else if(major == v_minor)
			{
				if(revision > v_revision)
					return -3;
			}		
		}
		
		return 1;
	}
}