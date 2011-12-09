package rulebender.simulate.parameterscan;

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

import rulebender.core.utility.Console;
import rulebender.preferences.OS;
import rulebender.preferences.PreferencesClerk;

public class ParameterScanScriptCreator 
{	
	/**
	 * The resultsDir parameter MUST be the complete path to the time-stamped
	 * results folder (eg. .../workspace/model/results/parascan-timestamp)
	 *  
	 * @param resultsDir The path to the results dir.
	 * @param bngFullPath the full path to the simulator.
	 * @param scriptFullPath the full path to the original par_scan.pl. 
	 * @return true if successful, false otherwise.
	 */
	public static boolean produceAndWriteScript(String resultsDir, String newScriptName, String bngFullPath, String scriptFullPath)
	{
		return writeScript(produceScript(bngFullPath, scriptFullPath), resultsDir, newScriptName);
	}
	
	/**
	 * Generate the perl script
	 */
	private static String produceScript(String bngFullPath, String scriptFullPath)
	{
		// Create String objects for the perl script and the 
		// execution line to insert into it. 
		String modifiedPerlScript = "";
		String modifiedExecLine = "";
		
		// Read the scan_var.pl file into the modifiedPerlScript string. 
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(
					new File(scriptFullPath)));
			try 
			{
				while (br.ready()) {
					modifiedPerlScript = modifiedPerlScript + br.readLine() 
							+ Console.getConsoleLineDelimeter();
				}
			} 
			catch (IOException e) 
			{
				return null;
			}
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			return null;
		}
		
		// Now that we have the perl file read in, this tries to match
		// a regular expression for the execution of the bng part of the 
		// script.
		Pattern p = Pattern.compile("exec\\s*=.*(\".*BNGPATH.*\\.[Pp][Ll]\\s*\")");
		Matcher m = p.matcher(modifiedPerlScript);
		
		// If the pattern matches, then we need to insert our information for this
		// parameter scan into the line.
		if (m.find()) 
		{	
			// If the os is windows then fill in the execute line with the proper
			// path structure
			if (PreferencesClerk.getOS() == OS.WINDOWS) 
			{
				modifiedExecLine = "";
				
				String tmpBNGFPath = bngFullPath.substring(0, bngFullPath.lastIndexOf(System.getProperty("file.separator")));
				
				while (tmpBNGFPath.indexOf('\\') != -1) 
				{
					modifiedExecLine = modifiedExecLine
							+ tmpBNGFPath.substring(0,
									tmpBNGFPath.indexOf('\\')) + "\\\\";
					tmpBNGFPath = tmpBNGFPath.substring(
							tmpBNGFPath.indexOf('\\') + 1, tmpBNGFPath.length());
				}
				modifiedExecLine = modifiedExecLine + tmpBNGFPath + "\\\\"
						+ PreferencesClerk.getBNGName() + "\"";
				

				modifiedExecLine = "\"perl " + modifiedExecLine;
			} 
			
			// If the os is mac/linux then fill in the execute line with the proper
			// path structure.
			// The "\"\\\"" is to write "\" (including quotes) into the string.
			else
			{
				modifiedExecLine = "\"\\\"" + bngFullPath + "\\\"\"";			
			}
				
			// Complete the modified perl script by inserting the modified execution line. 
			modifiedPerlScript = modifiedPerlScript.substring(0, modifiedPerlScript.indexOf(m.group(1)))
					+ modifiedExecLine
					+ modifiedPerlScript.substring(modifiedPerlScript.indexOf(m.group(1))
							+ m.group(1).length(), modifiedPerlScript.length());
			
		}	
		
		return modifiedPerlScript;
	}
	
	
	/**
	 * Writes the passed in script to the passed results directory.
	 * 
	 * @param script
	 * @param resultsDir
	 * @return
	 */
	private static boolean writeScript(String script, String resultsDir, String newScriptName)
	{
		// Just in case there is an error with the script. 
		// TODO better error checking. 
		if (script == null)
		{
			return false;
		}
		
		String tmpSlash = System.getProperty("file.separator");
		
		// path for ModifiedParScan.pl
		String modifiedParScanPath = "";

		// Write out the ModifiedParScan.pl file.
		PrintWriter pw;

		try {
			// path for ModifiedParScan.pl
			modifiedParScanPath = resultsDir + tmpSlash + newScriptName;
			
			pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(modifiedParScanPath))));
			pw.write(script);
			pw.flush();
			pw.close();
		} 
		
		catch (IOException e) 
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	} 
}
