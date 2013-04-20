package rulebender.simulate;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import rulebender.prereq.PreReqChecker;
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
	 * Runs a parameter scan and puts the results in a directory called 
	 * 'results' in the same folder as the model. 
	 * 
	 * @param filePath
	 * @param data
	 * @param bngFullPath
	 * @param scriptFullPath
	 * @return true if job submitted, false otherwise. 
	 */
	public static void parameterScan(IFile iFile, ParameterScanData data, 
	                                 String bngPath, String scriptFullPath, 
	                                 String resultsPath)
	{
		if(!PreReqChecker.isPerlInPath())
		{
			MessageBox errorMessage = 
			    new MessageBox(Display.getDefault().getActiveShell(), 
			        SWT.ICON_ERROR | SWT.OK);
			errorMessage.setText("Perl Not Found");
			errorMessage.setMessage("Warning: It appears that Perl is not in your " +
					                    "PATH environment variable.\nPlease install Perl " +
					                    "if you want to run simulations.");
			
			errorMessage.open();
		}
		
		else
		{
      String relPath = iFile.getFullPath().toOSString();
			String name = "Parameter Scan: " + relPath;
			ParameterScanJob job = new ParameterScanJob(name,
			                                            iFile, 
			                                            bngPath,
			                                            scriptFullPath,
			                                            data,
			                                            resultsPath);
			
			job.schedule();
		}
	}
	
	/**
	 * 
	 * @param iFile
	 */
	public static void runBNGLFile(IFile iFile,
	                               String bngFullPath, 
	                               String resultsPath)
	{
	  // Make sure that the prerequisites are in place
		if(!PreReqChecker.isPerlInPath())
		{
			MessageBox errorMessage = 
			    new MessageBox(Display.getDefault().getActiveShell(), 
			                   SWT.ICON_ERROR | SWT.OK);
			errorMessage.setText("Perl Not Found");
			errorMessage
			  .setMessage("Warning: It appears that Perl is not in your " +
			              "PATH environment variable.\nPlease install Perl " +
			              "if you want to run simulations.");
			
			errorMessage.open();
		}
		// Run the file.  
		else
		{
			String relPath = iFile.getFullPath().toOSString();
			//FIXME Do I want this pretext?
			String name = "Executing file: " + relPath;
			BNGExecutionJob job = 
			    new BNGExecutionJob(name, iFile, bngFullPath, resultsPath);
			
			job.schedule();
		}
		
		
	}	
}
