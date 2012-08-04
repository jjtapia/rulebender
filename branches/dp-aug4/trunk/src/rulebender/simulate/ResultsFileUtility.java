package rulebender.simulate;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;

public class ResultsFileUtility 
{	
	
	private static String getResultsDirectoryForIFile(IFile selectedFile) 
	{
		// Get the path location of the project
		String pathToReturn = selectedFile.getProject().getLocation().makeAbsolute().toOSString()+ System.getProperty("file.separator");
		
		// Add the results directory
		pathToReturn += "results" + System.getProperty("file.separator");
		
		// Add the name of the file
		pathToReturn += selectedFile.getName().substring(0, selectedFile.getName().indexOf(".bngl")) + System.getProperty("file.separator");
		
		System.out.println("Returning Path: " + pathToReturn);
		
		return pathToReturn;
	}

	public static String getParameterScanResultsDirectoryForIFile(IFile selectedFile) 
	{
		return (getResultsDirectoryForIFile(selectedFile) + 
				"parascan-" + getCurrentDateAndTime() + 
				System.getProperty("file.separator"));
	}
	
	public static String getSimulationResultsDirectoryForIFile(IFile selectedFile) 
	{
		return (getResultsDirectoryForIFile(selectedFile) + 
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
