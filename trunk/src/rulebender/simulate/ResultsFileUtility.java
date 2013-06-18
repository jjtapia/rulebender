package rulebender.simulate;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;

import rulebender.logging.Logger;

public class ResultsFileUtility 
{	
  public static final String SLASH = System.getProperty("file.separator");
	
  /**
   * Gets the result directory for the bngl ifile parameter.
   * @param selectedFile
   * @return
   */
	private static String getResultsDirectoryForIFile(IFile selectedFile) 
	{
		// Get the path location of the project
		String pathToReturn = 
		    selectedFile
		    .getProject()
		    .getLocation()
		    .makeAbsolute()
		    .toOSString() 
		    + SLASH;
		
		// Add the results directory
		pathToReturn += "results" + SLASH;
		
		// Add the name of the file
		pathToReturn += 
		    selectedFile
		    .getName()
		    .substring(0, selectedFile.getName().indexOf(".bngl"))
		    + SLASH;
		
		Logger.log(Logger.LOG_LEVELS.INFO, 
		           ResultsFileUtility.class, 
		           "Returning Path: " + pathToReturn);
		
		return pathToReturn;
	}

	
	/**
	 * Uses {@link getResultsDirectoryForIfile} to get the directory, and then 
	 * adds the 'parascan' tag in the dir name with the date and time.
	 * @param selectedFile
	 * @return
	 */
	public static String getParameterScanResultsDirectoryForIFile(IFile selectedFile) 
	{
//		return (getResultsDirectoryForIFile(selectedFile) + 
//				"parascan-" + getCurrentDateAndTime() + SLASH);
		return (getResultsDirectoryForIFile(selectedFile) + 
				getCurrentDateAndTime() + SLASH);
	}
	
	
	/**
	 * Uses {@link getResultsDirectoryForIfile} to get the directory and then 
	 * adds the date and time.
	 * @param selectedFile
	 * @return
	 */
	public static String getSimulationResultsDirectoryForIFile(IFile selectedFile) 
	{
		return (getResultsDirectoryForIFile(selectedFile) + 
				getCurrentDateAndTime() + SLASH);
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
//		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy_HH-mm-ss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String curTime = dateFormat.format(dateNow);
		return curTime;
	}
		
}
