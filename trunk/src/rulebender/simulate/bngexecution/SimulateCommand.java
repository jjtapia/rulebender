package rulebender.simulate.bngexecution;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

import rulebender.preferences.OS;
import rulebender.preferences.PreferencesClerk;
import rulebender.simulate.CommandInterface;

public class SimulateCommand implements CommandInterface
{
	private String m_bnglFile;
	private String m_bngFullPath;
	private String m_resultsDirectory;
	private boolean m_viewResults;
		
	private String[] m_command;
		
	/**
	 * Constructor for a Simulation Command.  Create the object, and then call the
	 * getCommand() method to get the string of the command to execute. 
	 * 
	 * @param bngFile - The string location of the file to run the scan on.
	 */
	public SimulateCommand(String bngFile, String bngFullPath, String resultsDirectory, boolean viewResults)
	{
		m_bnglFile = bngFile;
		m_viewResults = viewResults;
		m_bngFullPath = bngFullPath;
		m_resultsDirectory = resultsDirectory;
	}
	
	/**
	 * Constructs and returns the String that should be executed in the shell.
	 */
	public String[] getCommand() 
	{
		// Windows
		if (PreferencesClerk.getOS() == OS.WINDOWS) 
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
		
		instructionAL.add(m_bngFullPath);
		
		instructionAL.add("-outdir");
		instructionAL.add(m_resultsDirectory);
		
		if (m_viewResults == false) {
			// Check model, not run
			instructionAL.add("-check");
		}
			
		instructionAL.add(m_bnglFile);		

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
			try 
			{
				pw = new PrintWriter(batfile);
			} 
			catch (FileNotFoundException e1) 
			{
			
				e1.printStackTrace();
			}
			
			// disk name (C, or D, or ...)
			String modelDisk = m_bnglFile.substring(0, 1);
			
			pw.write(modelDisk + ":\n");
			pw.write("cd " + modelDisk + ":\\" + "\n");
			
			String check="";
			if(!m_viewResults)
			{
				check = " -check ";
			}		
			
			String cmd_model = m_bnglFile;
			
			// eliminate "C:\" three characters
			cmd_model = cmd_model.substring(3);
			cmd_model = convertStyleUsingPOSIX(cmd_model);
			
			// using " " to support space in directory name
			pw.write("cmd.exe /c perl " +
					"\"" + m_bngFullPath + "\"" +
					" -outdir " + m_resultsDirectory +
					check + " " +
					"\"" + cmd_model + "\"" +
					"\n");
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
