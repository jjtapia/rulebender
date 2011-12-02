package rulebender.simulate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import editor.ConfigurationManager;
import editor.simulate.CommandInterface;

public class SimulateCommand implements CommandInterface
{
	private String m_bngFile;
	private boolean m_viewResults;
		
	private String[] m_command;
		
	/**
	 * Constructor for a Simulatino Command.  Create the object, and then call the
	 * getCommand() method to get the string of the command to execute. 
	 * 
	 * @param bngFile - The string location of the file to run the scan on.
	 */
	public SimulateCommand(String bngFile, boolean viewResults)
	{
		m_bngFile = bngFile;
		m_viewResults = viewResults;
	}
	
	/**
	 * Constructs and returns the String that should be executed in the shell.
	 */
	public String[] getCommand() 
	{
		// Windows
		if (ConfigurationManager.getConfigurationManager().getOSType() == 1) 
		{
			return getWindowsCommand();
		}
		else
		{
			return getNonWindowsCommand();
		}
	}
	
	private String[] getNonWindowsCommand()
	{

		// This arraylist will hold the actual command line command
		ArrayList<String> instructionAL = new ArrayList<String>();
		
		// Add the perl instruction
		instructionAL.add("perl");
		
		instructionAL.add(ConfigurationManager.getConfigurationManager().getBNGFPath()+
				ConfigurationManager.getConfigurationManager().getSlash()+
				ConfigurationManager.getConfigurationManager().getBNGFName());

		if (m_viewResults == false) {
			// Check model, not run
			instructionAL.add("-check");
		}
			
		instructionAL.add(m_bngFile);		

		// Create a template so that the toArray function knows what kind of 
		// object to return. 
		String[] template = {""};
		
		// Set the member variable to the array version of the 
		// instruction arraylist. 
		m_command = instructionAL.toArray(template);
			
		return m_command;

	}
	
	private String[] getWindowsCommand()
	{
			// on Windows
			// write commands to a batch file and then execute the batch file
		
			String userHome = System.getProperty("user.home");
		
			String batfilename = userHome+"\\callBNG-win.bat";
			
			File batfile = new File(batfilename);
			batfile.deleteOnExit();
			
			PrintWriter pw = null;
			try {
				pw = new PrintWriter(batfile);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			// disk name (C, or D, or ...)
			String modelDisk = m_bngFile.substring(0, 1);
			
			pw.write(modelDisk + ":\n");
			pw.write("cd " + modelDisk + ":\\" + "\n");
			String cmd_bng = ConfigurationManager.getConfigurationManager().getBNGFPath()+
					ConfigurationManager.getConfigurationManager().getSlash()+
					ConfigurationManager.getConfigurationManager().getBNGFName();
			
			String check="";
			if(!m_viewResults)
			{
				check = " -check ";
			}		
			
			String cmd_model = m_bngFile;
			
			// eliminate "C:\" three characters
			cmd_model = cmd_model.substring(3);
			cmd_model = convertStyleUsingPOSIX(cmd_model);
			
			// using " " to support space in directory name
			pw.write("cmd.exe /c perl " + "\"" + cmd_bng + "\"" + check + " " + "\"" + cmd_model + "\"" + "\n");
			pw.close();
			
			/*
			try {
				Runtime.getRuntime().exec("attrib +H " + userHome+"\\callBNG-win.bat");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//*/
			String[] command = {"cmd.exe", "/c", batfilename};
			m_command = command; 
			
			return m_command;
	}
	
	
	private String convertStyleUsingPOSIX(String path) {
		String results = "";
		for (int i = 0; i < path.length(); i++) {
			if (path.charAt(i) == '\\') {
				results += '/';
			}
			else {
				results += path.charAt(i);
			}
		}
		return results;
	}
}
