package rulebender.simulate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import rulebender.simulate.parameterscan.ParameterScanCommand;
import rulebender.simulate.parameterscan.ParameterScanData;
import rulebender.simulate.parameterscan.ParameterScanScriptCreator;

/**
 * @author mr_smith22586
 *
 */
public class BioNetGenUtility 
{
	// Private constructor for uninstantiability
	private BioNetGenUtility()
	{
		throw new AssertionError();
	}
	
	/**
	 * Runs a parameter scan and puts the results in a directory called 'results' in the same folder
	 * as the model. 
	 * 
	 * @param filePath
	 * @param data
	 * @param bngFullPath
	 * @param scriptFullPath
	 * @return true if job submitted, false otherwise. 
	 */
	public static boolean parameterScan(String filePath, ParameterScanData data, String bngFullPath, String scriptFullPath)
	{
		if(!validateBNGLFile(filePath) || !validateBNGPath(bngFullPath) || !validateScriptPath(scriptFullPath))
		{
			return false;
		}
		
		String modifiedPerlScriptName = "ModifiedParScan.pl";
		String timeStamp = getCurrentDateAndTime();
		
		// Set up the results directory
		String resultsDir = filePath.substring(0,filePath.indexOf(".bngl")) + 
				System.getProperty("file.separator") +
				"results/parascan-" + timeStamp + System.getProperty("file.separator");
		
		// Make the directory if necessary
		(new File(resultsDir)).mkdirs();
		
		// Create the perl script.
		ParameterScanScriptCreator.produceAndWriteScript(resultsDir, modifiedPerlScriptName, bngFullPath, scriptFullPath);
		
		// Get a parameterscan command
		ParameterScanCommand command = new ParameterScanCommand(filePath, 
																resultsDir + modifiedPerlScriptName,
																resultsDir,
																data);
		
		// Run it in the commandRunner (which extends Job).
		String name = "Parameter Scan on " + filePath.substring(filePath.lastIndexOf(System.getProperty("file.separator")), filePath.indexOf(".bngl"));
		CommandRunner<ParameterScanCommand> runner = new CommandRunner<ParameterScanCommand>(name, command, new File(resultsDir));
		
		runner.schedule();
		
		return true;
	}
	
	/**
	 * 
	 * @param filePath
	 */
	public static void runBNGLFile(String filePath)
	{
		// Get the SimulateCommand object
		
		// Run it in the CommandRunner
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
	
	private static boolean validateBNGLFile(String path)
	{
		if ((new File(path)).exists())
			return true;
		
		return false;
	}
	
	private static boolean validateBNGPath(String path)
	{
		if ((new File(path)).exists())
			return true;
		
		return false;
	}
	
	private static boolean validateScriptPath(String path)
	{
		if ((new File(path)).exists())
			return true;
		
		return false;
	}
	
	
	
	
}
