package org.bng.simulate.parameterscan;

import java.io.File;

import java.util.ArrayList;

import org.bng.simulate.CommandInterface;

public class ParameterScanCommand implements CommandInterface
{
	private String m_bnglFile;
	private String m_parScanScriptLocation;
	private String m_resultsDirectory; 
	private String m_bngFullPath;
	
	private ParameterScanData m_data;
	
	private String m_prefix;
	
	private String[] m_scanInstruction;
	
	
	/**
	 * Constructor for a ParameterScanCommand.  Create the object, and then call the
	 * getCommand() method to get the string of the command to execute. 
	 * 
	 * @param bngFile - The string location of the file to run the scan on.
	 * @param parScanScriptLocation - The string location of the perl script that runs the scan.
	 * @param resultsDirectory - Where the results will go: used for the construction of the prefix.
	 * @param data - The object that holds all of the parameterscandata.
	 */
	public ParameterScanCommand(String bngFile, String bngFullPath, String parScanScriptLocation, 
								String resultsDirectory, ParameterScanData data)
	{
		m_bnglFile = bngFile;
		m_parScanScriptLocation = parScanScriptLocation;
		m_data = data;
		m_resultsDirectory = resultsDirectory;
		m_bngFullPath = bngFullPath;
	}

	public String[] getCommand() 
	{
		// This arraylist will hold the actual command line command
		ArrayList<String> scanInstructionAL = new ArrayList<String>();

		// Add the perl instruction
		scanInstructionAL.add("perl");
		
		// Add the location of the scan_var.pl or ModfiedParScan.pl 
		//scanInstructionAL.add("\""+m_parScanScriptLocation +"\"");
		scanInstructionAL.add(m_parScanScriptLocation);
		
		// Add all of the parameter simulation options.
		if(m_data.isSteadyState())
			scanInstructionAL.add("-steady_state");
		if(m_data.isLogScale())
			scanInstructionAL.add("-log");
		
		scanInstructionAL.add("-n_steps");
		scanInstructionAL.add(""+m_data.getNumTimePoints());
		
		scanInstructionAL.add("-prefix");
		scanInstructionAL.add(constructPrefix());
		
		scanInstructionAL.add("-t_end");
		scanInstructionAL.add(""+m_data.getSimulationTime());
		
		// Adding quotes here breaks stuff...
		//scanInstructionAL.add("\"" + m_bnglFile + "\"");
		scanInstructionAL.add(m_bnglFile);
		scanInstructionAL.add(m_data.getName());
		scanInstructionAL.add(""+m_data.getMinValue());
		scanInstructionAL.add(""+m_data.getMaxValue());
		scanInstructionAL.add(""+m_data.getPointsToScan());	
		
		// Create a template so that the toArray function knows what kind of 
		// object to return. 
		String[] template = {""};
		
		// Set the member variable to the array version of the 
		// instruction arraylist. 
		m_scanInstruction = scanInstructionAL.toArray(template);
		
		// DEBUG
		for(String s : m_scanInstruction)
		{
			System.out.print(s + " ");
		}
		System.out.println("\n");
		
		return m_scanInstruction;
	}
	
	/**
	 * Constructs the prefix of the results files based on the name of the
	 * parameter being scanned, and the files that are already in the 
	 * results directory. 
	 * 
	 * @return String prefix - The prefix to use in the filename.
	 */
	private String constructPrefix()
	{
		// Init the prefix string
		String prefix = "";
		
		// Init the base of the prefix.
		String prefixBase = m_data.getName() + "_";
		
		// Utility var for the upcoming loop.
		boolean found = false;
		
		// While there are still prefix folders with the same prefix
		// as the current prefixID, keep upping the counter and checking.
		for(Integer prefixID = 0; found == false; prefixID++) 
		{
			String prefixIdStr = prefixID.toString();
			
			// add zeros before the number
			while (prefixIdStr.length() < 3) 
			{
				prefixIdStr = "0" + prefixIdStr;
			}
			
			prefix = prefixBase + prefixIdStr;
			prefix = prefix.trim();
			
			File prefixFolder = new File(m_resultsDirectory + System.getProperty("file.separator") 
					+ System.getProperty("file.separator") + prefix);
			
			System.out.println("Checking for folder = " + prefixFolder.getAbsolutePath());
			
			if (!prefixFolder.isDirectory())
			{
				System.out.println("prefix folder does not exist");
				found = true;	
			}
			
			else 
			{
				System.out.println("prefix folder exists");
			}
		}
		
		m_prefix = prefix;
		
		System.out.println("prefix is " + prefix);
		
		return m_prefix;
	}
	
	public String getPrefix()
	{
		return m_prefix;
	}

	@Override
	public String getBNGFullPath() 
	{
		return m_bngFullPath;
	}
}
