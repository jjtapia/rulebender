package rulebender.simulate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Platform;

import rulebender.simulate.parameterscan.ParameterScanData;

public class BioNetGenUtility 
{
	// Private constructor for uninstantiability
	private BioNetGenUtility()
	{
		throw new AssertionError();
	}
	
	// What kinds of functions need to be called?

	public static void parameterScan(String filePath, ParameterScanData data)
	{
		//FIXME May have to add some more directories on here. 
		String BNGFPath = Platform.getPreferencesService().getString("rulebender.views.preferences.preferencePage", "SIM_PATH", "", null);
		
		// Validate the path.
		
		// Generate the perl script
		// Create String objects for the perl script and the 
		// execution line to insert into it. 
		String modifiedPerlScript = "";
		String modifiedExecLine;
		
		// Read the scan_var.pl file into the modifiedPerlScript string. 
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(
					new File(BNGFPath, "scan_var.pl")));
			try 
			{
				while (br.ready()) {
					modifiedPerlScript = modifiedPerlScript + br.readLine()
							+ getBNGTextArea().getStyledTextArea().getLineDelimiter();
				}
			} 
			catch (IOException e) 
			{}
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		// Now that we have the perl file read in, this tries to match
		// some execute regexp.  I think basically this is making a 
		// new perl script for each platform.
		Pattern p = Pattern.compile("exec\\s*=.*(\".*BNGPATH.*\\.[Pp][Ll]\\s*\")");
		Matcher m = p.matcher(modifiedPerlScript);
		
		// If the pattern matches
		if (m.find()) 
		{	
			// If the os is windows then fill in the execute line with the proper
			// path structure
			if (ConfigurationManager.getConfigurationManager().getOSType() == 1) 
			{
				modifiedExecLine = "";
				
				String tmpBNGFPath = BNGFPath;
				
				while (tmpBNGFPath.indexOf('\\') != -1) 
				{
					modifiedExecLine = modifiedExecLine
							+ tmpBNGFPath.substring(0,
									tmpBNGFPath.indexOf('\\')) + "\\\\";
					tmpBNGFPath = tmpBNGFPath.substring(
							tmpBNGFPath.indexOf('\\') + 1, tmpBNGFPath.length());
				}
				modifiedExecLine = modifiedExecLine + tmpBNGFPath + "\\\\"
						+ ConfigurationManager.getConfigurationManager().getBNGFName() + "\"";
				

				modifiedExecLine = "\"perl " + modifiedExecLine;
			} 
			
			// If the os is mac/linux then fill in the execute line with the proper
			// path structure
			else
			{
				modifiedExecLine = "\"\\\"" + BNGFPath + "/"
						+ ConfigurationManager.getConfigurationManager().getBNGFName() + "\\\"\"";
			}
				
			// Complete the modified perl script by inserting the modified execution line. 
			modifiedPerlScript = modifiedPerlScript.substring(0, modifiedPerlScript.indexOf(m.group(1)))
					+ modifiedExecLine
					+ modifiedPerlScript.substring(modifiedPerlScript.indexOf(m.group(1))
							+ m.group(1).length(), modifiedPerlScript.length());
			
		}
		// At this point the new perl script is in tempstr and will be 
		// written out later.

		// get the model name
		String modelName = getFileName();
		
		//remove the .bngl suffix
		if (modelName.lastIndexOf('.') != -1) 
			modelName = modelName.substring(0,
					modelName.lastIndexOf('.'));

		String resultsFolderPath = ConfigurationManager.getConfigurationManager().getWorkspacePath()
		   + ConfigurationManager.getConfigurationManager().getSlash() + "BNGResults";
		
		// resultsFolder
		File resultsFolder = new File(resultsFolderPath + tmpSlash);
		
		if (!resultsFolder.isDirectory()) 
		{
			resultsFolder.mkdirs();
		}

		// path for ModifiedParScan.pl
		String modifiedParScanPath = "";

		// Write out the ModifiedParScan.pl file.
		PrintWriter pw;
		File parScanFolder = null;
		
		try {

			// pw = new PrintWriter(new BufferedWriter(new
			// FileWriter(new File("ModifiedParScan.pl"))));

			// create a folder with the name of model if not exists
			java.io.File modelFolder = new java.io.File(
					resultsFolderPath + tmpSlash + modelName + tmpSlash);
			
			if (!modelFolder.isDirectory()) 
			{
				modelFolder.mkdir();
			}

			// create the folder para_scan under the folder of model
			// name if not exists
			parScanFolder = new File(
					resultsFolderPath + tmpSlash + modelName + tmpSlash
							+ "para_scan" + tmpSlash);
			if (!parScanFolder.isDirectory()) {
				parScanFolder.mkdir();
			}

			// path for ModifiedParScan.pl
			modifiedParScanPath = resultsFolderPath + tmpSlash
					+ modelName + tmpSlash + "para_scan" + tmpSlash
					+ "ModifiedParScan.pl";

			pw = new PrintWriter(new BufferedWriter(new FileWriter(
					new File(modifiedParScanPath))));
			pw.write(modifiedPerlScript);
			pw.flush();
			pw.close();
		} 
		
		catch (IOException e) 
		{
			e.printStackTrace();
		}

		
		// Decide where the results go???
		
		// Get a parameterscan command
		
		// Run it in the commandRunner.
	}
	
	public static void runBNGLFile(String filePath)
	{
		// Get the SimulateCommand object
		
		// Run it in the CommandRunner
	}
	
}
