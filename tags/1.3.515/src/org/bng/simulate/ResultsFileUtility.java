package org.bng.simulate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import editor.ConfigurationManager;

public class ResultsFileUtility 
{	
	
	private static String getResultsDirectoryForFile(File selectedFile) 
	{
		// Get the path location of the project
		String pathToReturn = ConfigurationManager.getConfigurationManager().getWorkspacePath() + System.getProperty("file.separator");
		
		//Add the results directory
		pathToReturn += "BNGResults" + System.getProperty("file.separator");
		
		// Add the name of the file
		pathToReturn += selectedFile.getName().substring(0, selectedFile.getName().indexOf(".bngl")) + System.getProperty("file.separator");
		
		System.out.println("Returning Path: " + pathToReturn);
		
		return pathToReturn;
	}

	public static String getParameterScanResultsDirectoryForFile(File selectedFile) 
	{
		return (getResultsDirectoryForFile(selectedFile) + 
				"parascan-" + getCurrentDateAndTime() + 
				System.getProperty("file.separator"));
	}
	
	public static String getSimulationResultsDirectoryForFile(File selectedFile) 
	{
		return (getResultsDirectoryForFile(selectedFile) + 
				getCurrentDateAndTime() + 
				System.getProperty("file.separator"));
	}
	
	/**
	 * Used for time-stamping results files.  Returns the date in a
	 * dd-mm-yy_hh-mm-ss format.
	 * 
	 * @return
	 */
	private static String getCurrentDateAndTime() 
	{
		Date dateNow = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy_HH-mm-ss");
		String curTime = dateFormat.format(dateNow);
		return curTime;
	}
		
}
