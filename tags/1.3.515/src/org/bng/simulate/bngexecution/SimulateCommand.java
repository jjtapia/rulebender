package org.bng.simulate.bngexecution;

import java.util.ArrayList;

import org.bng.simulate.CommandInterface;

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
			
		// DEBUG
		for(String s : m_command)
		{
			System.out.print(s + " ");
		}
		System.out.println("\n");
		
		return m_command;
	}

	@Override
	public String getBNGFullPath() 
	{
		return m_bngFullPath;
	}
}
