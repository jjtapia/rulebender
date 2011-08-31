package rulebender.old.parser;

/**
 * CurrentFile.java
 * 
 * This source file defines the model for a file that is being edited in the 
 * BNG IDE.
 * 
 * @author Yao Sun - All original code
 * @author Adam M. Smith - Additions and documentations marked with '-ams <date>'
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

import prefuse.visual.VisualItem;
import rulebender.utility.Console;

public class CurrentFile {
	
	/*
	private String filename;
	private String filepath;// halfpath
	String prevcontent;
	private BNGTextArea bngTextArea;
	CTabItem tabitem;
	String ss1;// content in file
	String ss2;// content in editor
	int ostype; // 1:windows 2:man 3:other
	
	boolean makeNewVisualModels;
	
	/**
	 * Constructor
	/
	public CurrentFile(String fpath, String fname, int inostype, boolean empty, int tabIndex)
	{
		produceNewModels();
	}

	
	public void check() 
	{
		Console.displayOutput(Console.getConsoleLineDelimeter()+"Checking Model...");
	
		runbng(false);
	}

	public void run() 
	{
		Console.displayOutput("Running Model..."+ Console.getConsoleLineDelimeter());
		runbng(true);
	}

/**
 * Given the passed in ParameterScanData, do some checks to the file,
 * run the parameter scan, and print the log to the console. 
 * @param data
 *
	public void parscan(ParameterScanData data) 
	{
		
		// Get the string value of the text.
		ss2 = getBNGTextArea().doc.get();
				
		
		// slash for different operating system
		String tmpSlash = "";
		if (ConfigurationManager.getConfigurationManager().getOSType() == 1) 
		{
			tmpSlash = "\\";
		}
		else 
		{
			tmpSlash = "/";
		}
		
		
		// Create String objects for the perl script and the 
		// execution line to insert into it. 
		String modifiedPerlScript = "";
		String modifiedExecLine;
		
		// Read the scan_var.pl file into the modifiedPerlScript string. 
		try {
			
			BufferedReader br = new BufferedReader(new FileReader(
					new File(ConfigurationManager.getConfigurationManager().getBNGFPath(), "scan_var.pl")));
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
				
				String tmpBNGFPath = ConfigurationManager.getConfigurationManager().getBNGFPath();
				
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
				modifiedExecLine = "\"\\\"" + ConfigurationManager.getConfigurationManager().getBNGFPath() + "/"
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
		
		
		String paraName = data.getName().trim();
		String minV = (data.getMinValue()+"").trim();
		String maxV = (data.getMaxValue()+"").trim();
		String pointsNum = (data.getPointsToScan()+"").trim();
		String logScale = data.isLogScale() ? "true" : "false";
		String steady = data.isSteadyState() ? "true" : "false";
		String simulationTime = (data.getSimulationTime()+"").trim();
		String timePoints = (data.getNumTimePoints()+"").trim();

		//RUN -----------------------------

		// show the parameters in console
		String parameters = "Parameter Name: " + paraName + Console.getConsoleLineDelimeter();
		parameters += "Minimum Value: " + minV + Console.getConsoleLineDelimeter();
		parameters += "Maximum Value: " + maxV + Console.getConsoleLineDelimeter();
		parameters += "Number of Points to Scan: " + pointsNum + Console.getConsoleLineDelimeter();
		parameters += "Log Scale: " + logScale + Console.getConsoleLineDelimeter();
		parameters += "Steady State: " + steady + Console.getConsoleLineDelimeter();
		parameters += "Simulation Time: " + simulationTime + Console.getConsoleLineDelimeter();
		parameters += "Number of Time Points: " + timePoints + Console.getConsoleLineDelimeter();
		
		Console.displayOutput(Console.getConsoleLineDelimeter()+parameters + "\n\nRunning...");
		
		ParameterScanCommand command = new ParameterScanCommand(getFilepath() + ConfigurationManager.getConfigurationManager().getSlash() + getFileName(),
																modifiedParScanPath, 
																parScanFolder.getAbsolutePath(),
																data);
		
		CommandRunner<ParameterScanCommand> runner = new CommandRunner<ParameterScanCommand>(command, parScanFolder);

		// runs the command
		runner.run();

		// The errors are stored in the error log.
		if(!runner.getErrorLog().equals(""));
			Console.displayOutput("\nError Log:\n" + runner.getErrorLog());
		
		// results ========================================================
		Rectangle bounds = new Rectangle(0, 0, 600, 600);

		if (resViewer == null || resViewer.getShell() == null) 
		{

			// create a result viewer
			resViewer = new ResultViewer(resultsFolderPath, modelName
					+ "\t" + "para_scan" + "\t" + command.getPrefix(), ".scan",
					bounds);
			BNGEditor.setResViewer(resViewer);
			// resViewer.setBlockOnOpen(true);
			resViewer.open();

			if (resViewer != null && resViewer.getGraphFrame() != null
					&& resViewer.getGraphFrame().isShowing()) {
				resViewer.getGraphFrame().dispose(); // close the graph
			}
			BNGEditor.setResViewer(resViewer);
		} 
		else 
		{
			resViewer.getShell().setVisible(true);
			resViewer.refresh();
	
			resViewer.openDefaultFile(modelName + "\t" + "para_scan"
					+ "\t" + command.getPrefix(), ".scan");
		}
	}

	
	/**
	 * 
	 * @param viewresults
	 /
	public void runbng(boolean viewresults) 
	{
		Console.displayOutput("Running BioNetGen Simulator...");
		// the bngfpath or bngfname can not be empty
		if (ConfigurationManager.getConfigurationManager().getBNGFPath() == null || ConfigurationManager.getConfigurationManager().getBNGFName() == null) {
			Console.displayOutput("Error finding BNG.");
			(new SetBNGPathDialogue(BNGEditor.getMainEditorShell(), BNGEditor.getEditor())).show();
		} 
		// BNG is located so continue.
		else 
		{
			String tmpSlash = "";
			// Windows
			if (ostype == 1) {
				tmpSlash = "\\";
			}
			else {
				tmpSlash = "/";
			}
			// if the bng file does not exist, return
			File bngfile = new File(ConfigurationManager.getConfigurationManager().getBNGFPath() + tmpSlash + ConfigurationManager.getConfigurationManager().getBNGFName());
			if (!bngfile.exists()) {
				Console.displayOutput("Error finding BNG.");
				(new SetBNGPathDialogue(BNGEditor.getMainEditorShell(), BNGEditor.getEditor())).show();
				bngfile = null;
				return;
			}
			bngfile = null;
		
			String stemp2 = "";
			// file path and file name
			String str1 = getFilepath(), str2 = getFileName();
			
			// Regular expression for showing an error line. 
			String regex1 = "at line (\\d+) of file";
			Pattern p1 = Pattern.compile(regex1);
			Matcher m;
			Boolean result;
			int ti;

			Console.displayOutput(Console.getConsoleLineDelimeter());

			String curTime = getCurrentDateAndTime(); // get current time

			// workspace path
			String workspacePath = ConfigurationManager.getConfigurationManager().getWorkspacePath();
			
			// Results folder path.
			String resultsFolderPath = workspacePath + tmpSlash + "BNGResults";
			
			// file names in the current directory with the same name of model
			String[] fnames;
			
			// bng output files path list
			// read from BNG output because the directory is not fixed;
			// sometimes it depends on whether the user specify "Prefix" in the BNGL file
			ArrayList<String> outputPaths = new ArrayList<String>();
			// bng read-from files path list
			// which should be copy to the results directory and also keep in original directory
			ArrayList<String> inputPaths = new ArrayList<String>();
			
			
			// Create the simulation command and then run it in the simulation runner.
			SimulateCommand simCommand = new SimulateCommand(getFilepath() + tmpSlash + getFileName(), viewresults);
			CommandRunner<SimulateCommand> runner = new CommandRunner<SimulateCommand>(simCommand, new File(resultsFolderPath));
			runner.run();
			
			String[] lines = runner.getFullLog().split(Console.getConsoleLineDelimeter());
			
			System.out.println();
			
			for(String s : lines) 
			{
				Console.displayOutput(s + Console.getConsoleLineDelimeter());
				
				// add bng output file path to path list if not exists
				String pathstr = bngOutputFilePathExtractor(s);
				if (!pathstr.equals("") && !outputPaths.contains(pathstr)) 
				{
					// relative path
					if (pathstr.indexOf(tmpSlash) == -1) 
					{
						pathstr = workspacePath + tmpSlash + pathstr;
					}
					outputPaths.add(pathstr);
				}
				
				// add bng input file path to path list if not exists
				pathstr = bngInputFilePathExtractor(s);
				if (!pathstr.equals("") && !inputPaths.contains(pathstr)) {
					// relative path
					if (pathstr.indexOf(tmpSlash) == -1) {
						pathstr = workspacePath + tmpSlash + pathstr;
					}
					inputPaths.add(pathstr);
				}
			}

			// transfer the results to the Viewer
			m = p1.matcher(runner.getFullLog());
			result = m.find();
			if (result) {
				ti = Integer.parseInt(m.group(1));
				getBNGTextArea().getStyledTextArea().setFocus();
				getBNGTextArea().selectErrorLine(ti - 1);
			} 
			
			if (viewresults)
			{
				// get the model name
				String modelName = str2;
				if (modelName.lastIndexOf('.') != -1) {
					modelName = modelName.substring(0,
							modelName.lastIndexOf('.'));
				}

				// save the console results to log file
				File logFile = new File(str1 + tmpSlash + modelName
						+ "_console.log");
				PrintWriter pw;
				try {
					pw = new PrintWriter(logFile);
					pw.print(stemp2);
					pw.close();
					
					// add log file to output files list
					outputPaths.add(logFile.getAbsolutePath());

					// resultFolder
					java.io.File resultsFolder = new java.io.File(
							resultsFolderPath);
					if (!resultsFolder.isDirectory()) {
						resultsFolder.mkdir();
					}

					// create a folder with the name of model if not exists
					java.io.File modelFolder = new java.io.File(
							resultsFolderPath + tmpSlash + modelName + tmpSlash);
					if (!modelFolder.isDirectory()) {
						modelFolder.mkdir();
					}

					// create a new folder by the current time
					java.io.File timefolder = new java.io.File(
							resultsFolderPath + tmpSlash + modelName + tmpSlash
									+ curTime + tmpSlash);
					if (!timefolder.isDirectory()) {
						timefolder.mkdir();
					}
					

					// move output files to new directory
					for (int i = 0; i < outputPaths.size(); i++) {
						File tfile = new File(outputPaths.get(i));
						String fileName = tfile.getName();
						File newFile = new File(resultsFolderPath + tmpSlash
								+ modelName + tmpSlash + curTime + tmpSlash
								+ fileName);
						try {
							// copy files to new directory
							copyUseChannel(tfile, newFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.gc();

						// delete all result files in current directory
						tfile.delete();
					}

					// copy input files to new directory
					for (int i = 0; i < inputPaths.size(); i++) {
						File tfile = new File(inputPaths.get(i));
						String fileName = tfile.getName();
						File newFile = new File(resultsFolderPath + tmpSlash
								+ modelName + tmpSlash + curTime + tmpSlash
								+ fileName);
						try {
							// copy files to new directory
							copyUseChannel(tfile, newFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.gc();
					}
					
					
					// move other files with the prefix of model name to new directory
					fnames = new java.io.File(str1 + tmpSlash)
							.list(new Resultfilefilter(modelName));
					for (int i = 0; i < fnames.length; i++) {
						File tfile = new File(str1 + tmpSlash + fnames[i]);
						String fileName = tfile.getName();
						File newFile = new File(resultsFolderPath + tmpSlash
								+ modelName + tmpSlash + curTime + tmpSlash
								+ fileName);
						try {
							copyUseChannel(tfile, newFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.gc();
						if (!tfile.getName().endsWith(".bngl") && !tfile.getName().endsWith(".net")) {
							tfile.delete();
						}
					}
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
				Rectangle bounds = new Rectangle(0, 0, 600, 600);
				}
			}
		}
	}
	
	
	/**
	 * Called when the Contact Map button is pressed.
	 * 
	 * -ams
	 /
	void generateCMapModel() {
	
		Console.displayOutput(Console.getConsoleLineDelimeter());
		Console.displayOutput(Console.getConsoleLineDelimeter()+ "Parsing Contact Map model...");

		/*
		 * This mess checks a ton of factors in the textarea and calls the CMap
		 * constructor with 1 of 2 sets of parameters.
		 * 
		 * The CMap constructor looks like 'CMap(String molestr, String rulestr,
		 * boolean moleculetype)'
		 * 
		 * The difference in the calls is
		 * 
		 * 1. The first one uses a valid and nonempty textarea.blockinfo[2] and
		 * the second uses a valid and nonempty textarea.blockinfo[4] for the
		 * molestr.
		 * 
		 * 2. The first passes true to moleculetype and the second passes false.
		 * 
		 * -ams 5/1/10
		 /
		try {
			boolean block2valid = false; // molecule types block
			boolean block3valid = false; // compartment info block
			boolean block4valid = false; // seed species block
			boolean block6valid = false; // rules block
			
			if (getBNGTextArea().blockinfo[2].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[2].caretbegin2,
									getBNGTextArea().blockinfo[2].caretend1
											- getBNGTextArea().blockinfo[2].caretbegin2)
							.trim().length() != 0) {
				block2valid = true;
			}
			
			if (getBNGTextArea().blockinfo[3].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[3].caretbegin2,
									getBNGTextArea().blockinfo[3].caretend1
											- getBNGTextArea().blockinfo[3].caretbegin2)
							.trim().length() != 0) {
				block3valid = true;
			}
			
			if (getBNGTextArea().blockinfo[4].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[4].caretbegin2,
									getBNGTextArea().blockinfo[4].caretend1
											- getBNGTextArea().blockinfo[4].caretbegin2)
							.trim().length() != 0) {
				block4valid = true;
			}
			
			if (getBNGTextArea().blockinfo[6].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[6].caretbegin2,
									getBNGTextArea().blockinfo[6].caretend1
											- getBNGTextArea().blockinfo[6].caretbegin2)
							.trim().length() != 0) {
				block6valid = true;
			}
			
			if (block2valid) 
			{
			
				cModel = new CMapModel(getBNGTextArea().doc.get(
						getBNGTextArea().blockinfo[2].caretbegin2,
						getBNGTextArea().blockinfo[2].caretend1
								- getBNGTextArea().blockinfo[2].caretbegin2).trim(),true);
				
				//compartments info
				if (block3valid) {
					cModel.addCompartmentsInfo(getBNGTextArea().doc.get(
								getBNGTextArea().blockinfo[3].caretbegin2,
								getBNGTextArea().blockinfo[3].caretend1
										- getBNGTextArea().blockinfo[3].caretbegin2)
								.trim());
					
					// add seed species info to parse initial compartments
					if (block4valid) {
						cModel.addSeedSpeciesInfo(getBNGTextArea().doc.get(
								getBNGTextArea().blockinfo[4].caretbegin2,
								getBNGTextArea().blockinfo[4].caretend1
										- getBNGTextArea().blockinfo[4].caretbegin2).trim());
					}
					
				}
				
				// add rules info
				if (block6valid){
				cModel.addRulesInfo(getBNGTextArea().doc.get(
						getBNGTextArea().blockinfo[6].caretbegin2,
						getBNGTextArea().blockinfo[6].caretend1
								- getBNGTextArea().blockinfo[6].caretbegin2)
						.trim());
				}
				
			} 
			else if (block4valid) 
			{
				cModel = new CMapModel(getBNGTextArea().doc.get(
						getBNGTextArea().blockinfo[4].caretbegin2,
						getBNGTextArea().blockinfo[4].caretend1
								- getBNGTextArea().blockinfo[4].caretbegin2).trim(), false);
				//compartments info
				if (block3valid) {
					cModel.addCompartmentsInfo(getBNGTextArea().doc.get(
								getBNGTextArea().blockinfo[3].caretbegin2,
								getBNGTextArea().blockinfo[3].caretend1
										- getBNGTextArea().blockinfo[3].caretbegin2)
								.trim());
					
				}
				
				// add rules info
				if (block6valid) {
				cModel.addRulesInfo(getBNGTextArea().doc.get(
						getBNGTextArea().blockinfo[6].caretbegin2,
						getBNGTextArea().blockinfo[6].caretend1
								- getBNGTextArea().blockinfo[6].caretbegin2)
						.trim());
				}
			} 
			
			else {
				Console.displayOutput(Console.getConsoleLineDelimeter()+"Molecule info block not valid, Contact Map NOT shown.");
			}
			
			Console.displayOutput(Console.getConsoleLineDelimeter()+"Contact Map model finished parsing.");
		} 
		
		catch (org.eclipse.jface.text.BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is what is called when the 'Influence Graph' button is clicked. -ams
	 * 5/1/10
	 /
	void generateIGraphModel() {
		// Similar to the CMap, several conditions must be met to produce the
		// IMap. Some
		// Conditions will use a different IMap constructor.
		// -ams 5/1/10

		Console.displayOutput(Console.getConsoleLineDelimeter());
		Console.displayOutput(Console.getConsoleLineDelimeter()+"Parsing file for Influence Graph...");
		
		try {
			boolean block2valid = false; // molecule types block
			boolean block4valid = false; // seed species block
			boolean block6valid = false; // rules block
			if (getBNGTextArea().blockinfo[2].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[2].caretbegin2,
									getBNGTextArea().blockinfo[2].caretend1
											- getBNGTextArea().blockinfo[2].caretbegin2)
							.trim().length() != 0) {
				block2valid = true;
			}
			
			if (getBNGTextArea().blockinfo[4].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[4].caretbegin2,
									getBNGTextArea().blockinfo[4].caretend1
											- getBNGTextArea().blockinfo[4].caretbegin2)
							.trim().length() != 0) {
				block4valid = true;
			}
			
			if (getBNGTextArea().blockinfo[6].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[6].caretbegin2,
									getBNGTextArea().blockinfo[6].caretend1
											- getBNGTextArea().blockinfo[6].caretbegin2)
							.trim().length() != 0) {
				block6valid = true;
			}
				
			if (block2valid) {
				if (block6valid) {
					iModel = new IMapModel(getBNGTextArea().doc.get(
							getBNGTextArea().blockinfo[2].caretbegin2,
							getBNGTextArea().blockinfo[2].caretend1
									- getBNGTextArea().blockinfo[2].caretbegin2).trim(),
							getBNGTextArea().doc.get(
									getBNGTextArea().blockinfo[6].caretbegin2,
									getBNGTextArea().blockinfo[6].caretend1
											- getBNGTextArea().blockinfo[6].caretbegin2)
									.trim(), true);
				}
				else {
					Console.displayOutput(Console.getConsoleLineDelimeter()+"Reaction rule block not valid, Influence Graph NOT shown.");
				}
			}
			else if (block4valid) {
				if (block6valid) {
					iModel  = new IMapModel(getBNGTextArea().doc.get(
							getBNGTextArea().blockinfo[4].caretbegin2,
							getBNGTextArea().blockinfo[4].caretend1
									- getBNGTextArea().blockinfo[4].caretbegin2).trim(),
							getBNGTextArea().doc.get(
									getBNGTextArea().blockinfo[6].caretbegin2,
									getBNGTextArea().blockinfo[6].caretend1
											- getBNGTextArea().blockinfo[6].caretbegin2)
									.trim(), false);
				}
				else {
					Console.displayOutput(Console.getConsoleLineDelimeter()+"Reaction rule block not valid, Influence Graph NOT shown.");
				}
			}
			else {
				Console.displayOutput(Console.getConsoleLineDelimeter()+"Molecule info block not valid, Influence Graph NOT shown.");
			}
			
			Console.displayOutput(Console.getConsoleLineDelimeter()+"Influence Graph model finished parsing.");
		} catch (org.eclipse.jface.text.BadLocationException e) {
			e.printStackTrace();
		}
	}

	private String getCurrentDateAndTime() {
		Date dateNow = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy_HH-mm-ss");
		String curTime = dateFormat.format(dateNow);
		return curTime;
	}

	

	public void produceNewModels()
	{
		
		//System.out.println("producing new models");
		
		// Clear the models
		cModel = null;
		cVisualModel = null;
		iModel = null;
		iVisualModel = null;
		
		// Try to generate new models.
		generateCMapModel();
		generateIGraphModel();
		
		// There could have been errors in the cmap.
		if(cModel == null)
		{
			// Do something about it.  We used to focus the console, but it is always shown now.
		}
		
		// If there was no error producing the cmap.
		else
		{ 	
			VisualizationViewerController.loadVisualizationViewController().informOfNewCMapModel();
		}
		
		// There could have been errors in the imap.
		if(iModel == null)
		{
			// Do something about it.  We used to focus the console, but it is always shown now.
		}
		
		// If there was no error producing the influence graph
		else
		{
			// Tell the visualization view controller that there was a new model created.
			// The controller will then call the methods to generate the visual model 
			// if it is necessary at this time.
			VisualizationViewerController.loadVisualizationViewController().informOfNewIGraphModel();
		}
	}
	
	public IMapVisual getIMapVisualModel()
	{
		if(iVisualModel == null)
		{
			if(iModel == null)
			{
				//System.out.println("THIS SHOULD NEVER HAPPEN AND IF IT DOES THEN THE MODEL SHOULD HAVE BEEN BUILT SOMEWHERE ELSE BECAUSE IT CANNOT HAPPEN HERE");
				//generateIMapModel();
			}
			else
			{
				iVisualModel = new IMapVisual(iModel, VisualizationViewerController.loadVisualizationViewController().getIGraphSize(),
						   							  VisualizationViewerController.loadVisualizationViewController().getIGraphOverviewSize());
			}
		}
		
		return iVisualModel;
	}
	
	public CMapVisual getCMapVisualModel()
	{
		//TODO 
		// It looks like the Display objects are resized automatically... I cannot see where this is happening.
		// If I want to manually do it, then I will need some logic here that checks to see if the display size
		// has changed. For now I'm going to ignore it because there are more important.
		if(cVisualModel == null)
		{
			if(cModel == null)
			{
				//System.out.println("THIS SHOULD NEVER HAPPEN AND IF IT DOES THEN THE MODEL SHOULD HAVE BEEN BUILT SOMEWHERE ELSE BECAUSE IT CANNOT HAPPEN HERE");
				// generateCMapModel();
			}
			else
			{
			
				cVisualModel = new CMapVisual(cModel, VisualizationViewerController.loadVisualizationViewController().getCMapSize(),
													  VisualizationViewerController.loadVisualizationViewController().getCMapOverviewSize());
				
			}
		}
		
		//TODO  This gets called several times for each time the cmap actually needs to be generated...
		//System.out.println("getCMapVisualModel");
		return cVisualModel;
	}
	//*/
}