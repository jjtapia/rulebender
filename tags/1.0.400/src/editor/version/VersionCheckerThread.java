package editor.version;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.eclipse.swt.widgets.Display;

public class VersionCheckerThread implements Runnable
{

	/**
	 * Default Constructor.  Nothing needs to happen.
	 */
	public VersionCheckerThread()
	{
		
	}
	
	
	/**
	 * run retrieves the version file from the server to see if there is a new version available.  If there 
	 * is a new version, a shell is created and the user can choose to
	 * travel to the page.
	 * 
	 * @author Adam M. Smith
	 */
	public void run()
	{
		// DEBUG
		System.out.println("Check Update: Starting");
		long start = System.currentTimeMillis();
		long stop;
		
		URL updateFileURL = null;
		URLConnection con = null;
		InputStream inStream = null;
		BufferedReader input = null;
		
		boolean haveConnection = true;
		
		try 
		{
			// Create a URL
			updateFileURL =
				new URL("http://vis.cs.pitt.edu/resources/docs/versions.vt");
			
			//DEBUG
			//System.out.println("UpdateFileURL object created");
			// Create a connection using the url
			con = updateFileURL.openConnection();
			
			stop = System.currentTimeMillis();
			//System.out.println("con opened after " + (stop-start) + " milliseconds");
			
			
			con.setConnectTimeout(5000);
			
			// Not sure what this does.
			//con.setDoInput(true);
			
			// Get the input stream from the connection and URL
			inStream = con.getInputStream();
			
			//DEBUG
		//	System.out.println("Input stream received");
			
			// Create a buffered reader from the input stream of the
			// connection and url
			input =
				new BufferedReader(new InputStreamReader(inStream));
			
			//DEBUG
			//System.out.println("Buffered reader created.");
			
		} 
		catch (Exception e)
		{
			
			stop = System.currentTimeMillis();
			
			//DEBUG
			//System.out.println("Check update giving up after " + (stop-start) +  " milliseconds");
			//e.printStackTrace();
			haveConnection = false;
		}
		
		if(haveConnection)
		{
			// Get the line from the file .
		    // The line is just the version of the most recent software.
			ArrayList<Version> versions = new ArrayList<Version>();
			
			String readVersion = null;
			String readChanges = null;
			
			try {
				while(input.ready())
				{
					String currentLine = input.readLine();
					
					if(currentLine.toLowerCase().contains("version="))
					{
						readVersion = currentLine.split("\"")[1];
					}
					
					readChanges="";
					
					currentLine = input.readLine();
					
					if(currentLine.toLowerCase().contains("changes="))
					{
						readChanges += currentLine.substring(currentLine.indexOf('\"')+1);
						
						if(readChanges.charAt(readChanges.length()-1) != '\"')
						{
							do 
							{
							  currentLine = input.readLine().trim();
							  readChanges += currentLine+"\n";
							  
							}  while(currentLine != null && currentLine.charAt(currentLine.length()-1) != '\"');
						}
						
						readChanges = readChanges.substring(0, readChanges.length()-1);
					}
				
					versions.add(new Version(readVersion, readChanges));
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String allNewChanges = "";
			boolean foundfirst = false;
			
			for(Version vt : versions)
			{
				// If the version on the server is older,
				// then call getNewVersionDialogue
				if(vt.compare() < 0 && !foundfirst)
				{
					foundfirst = true;
					allNewChanges += vt.versionString+vt.changesString;
				}
				else if(vt.compare() < 0)
				{
					allNewChanges += "\n\n"+vt.versionString+vt.changesString;
				}
			}
			
			if(foundfirst)
			{
				Display.getDefault().syncExec(new NewVersionWindow(Display.getDefault(), allNewChanges));
			}
		
			stop = System.currentTimeMillis();
			
			//DEBUG
			System.out.println("Check Update: Complete after " + (stop-start) + " milliseconds");
		}
	}
}
