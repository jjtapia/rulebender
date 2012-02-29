package rulebender.simulate;

import rulebender.simulate.bngexecution.BNGExecutionJob;
import rulebender.simulate.parameterscan.ParameterScanData;
import rulebender.simulate.parameterscan.ParameterScanJob;

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
	public static void parameterScan(String filePath, ParameterScanData data, String bngPath, String scriptFullPath, String resultsPath)
	{
		String name = "Parameter Scan: " + filePath.substring(filePath.lastIndexOf(System.getProperty("file.separator"))+1, filePath.indexOf(".bngl"));
		ParameterScanJob job = new ParameterScanJob(name, filePath, bngPath, scriptFullPath, data, resultsPath);
		
		job.schedule();
	}
	
	/**
	 * 
	 * @param filePath
	 */
	public static void runBNGLFile(String filePath, String bngFullPath, String resultsPath)
	{
		String name = "Executing file: " + filePath.substring(filePath.lastIndexOf(System.getProperty("file.separator"))+1, filePath.indexOf(".bngl"));
		BNGExecutionJob job = new BNGExecutionJob(name, filePath, bngFullPath, resultsPath);
		
		job.schedule();
	}	
}
