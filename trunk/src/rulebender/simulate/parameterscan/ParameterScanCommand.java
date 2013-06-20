package rulebender.simulate.parameterscan;

import java.io.File;

import java.util.ArrayList;

import rulebender.simulate.CommandInterface;

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
		setBNGFullPath(bngFullPath);
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
		scanInstructionAL.add("-method");
		String[] methodArgs = m_data.getMethod().split("\\s"); // PLA method has two entries
//		scanInstructionAL.add(m_data.getMethod());
		for (int i=0;i<methodArgs.length;i++){
			scanInstructionAL.add(methodArgs[i]);
//			System.out.println(methodArgs[i]);
		}
		
		if(m_data.isSteadyState())
			scanInstructionAL.add("-steady_state");
		if(m_data.isLogScale())
			scanInstructionAL.add("-log");
		if(m_data.isVerbose())
			scanInstructionAL.add("-verbose");
		
		scanInstructionAL.add("-n_steps");
		scanInstructionAL.add(""+m_data.getNumTimePoints());
		
		scanInstructionAL.add("-prefix");
		scanInstructionAL.add(constructPrefix());
		//System.out.println("PREFIX! " + constructPrefix() + "_" + m_data.getName());
		
		// This has been replaced by setting the BNGPATH environment variable 
		// in the ProcessBuilder in the CommandRunner class.
		//scanInstructionAL.add("-bngPath");
		//scanInstructionAL.add("\"" + m_bngFullPath + "\"");
		//scanInstructionAL.add(m_bngFullPath);
		
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
	protected String constructPrefix()
	{
		// Init the prefix string
/*		String prefix = "";

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
*/
		File modelFile = new File(m_bnglFile);
		m_prefix = modelFile.getName().replace(".bngl", "");
		
		System.out.println("prefix is " + m_prefix);
		
		return m_prefix;
	}
	
	public String getPrefix()
	{
		if (m_prefix == null) {
			m_prefix = constructPrefix();
		}
		return m_prefix;
	}

	public String getBNGFullPath() {
		return m_bngFullPath;
	}

	public void setBNGFullPath(String m_bngFullPath) {
		this.m_bngFullPath = m_bngFullPath;
	}
}
