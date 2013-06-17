package rulebender.simulationjournaling.model;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;

import rulebender.contactmap.models.CMapModelBuilder;
import rulebender.contactmap.models.ContactMapModel;
import rulebender.core.utility.ANTLRFilteredPrintStream;
import rulebender.core.utility.Console;
import rulebender.editors.bngl.model.BNGASTReader;
import rulebender.editors.bngl.model.BNGLModel;
import rulebender.simulationjournaling.view.SmallMultiplesView;
import bngparser.BNGParseData;
import bngparser.BNGParserUtility;
import bngparser.grammars.BNGGrammar.prog_return;

public class BackgroundFileLoader extends Thread {

	private static String COMPONENT_GRAPH = "component_graph";
	
	public static SmallMultiple loadContactMap(String sourcePath, String layoutChoice, Dimension dim, SmallMultiplesView view) {
		
		SmallMultiple cVisual;
		
		prog_return ast = getModel(sourcePath).getAST();
			
		// Create the builder for the cmap
		CMapModelBuilder cmapModelBuilder = new CMapModelBuilder();
			
		// Create the astReader and register the cmapModelBuilder
		BNGASTReader astReader = new BNGASTReader(cmapModelBuilder);
					
		// Use the reader to construct the model for the given ast.
		// Sometimes an ast is not null, but is not complete due to errors. 
		// This try/catch block catches those situations.
		try {
			astReader.buildWithAST(ast);	
		} catch(NullPointerException e)	{
			// e.printStackTrace();
			//Debug
			System.out.println("Failed to produce CMapModel on ast:\n" + ast.toString());
		 	return null;
		} //try-catch
			
		// Get the model from the builder.		
		ContactMapModel cModel = cmapModelBuilder.getCMapModel();
		
		cModel.setSourcePath(sourcePath);
			
		// Get the CMapVisual object for the CMapModel
		boolean loaded = false;		
		while (!loaded) {
			try {
				cVisual = new SmallMultiple(view, cModel, dim, layoutChoice);
				loaded = true;
				return cVisual;
			} catch (Exception e) {
				System.err.println("loading fail on a small multiple.  trying again...");
				loaded = false;
			} //try-catch
		} //while
			
		return null;
		
	} //loadTempContactMap
	
	
	public static BNGLModel getModel(String sourcePath) {
		BNGLModel m_model = null;
		
		if (m_model == null) {
	    	
	    	m_model = new BNGLModel(sourcePath);
	    	m_model.setAST(getAST(sourcePath));
	    } //if

	    return m_model;
	} //getModel
	
	private static prog_return getAST(String sourcePath) {
	    // The abstract syntax tree that will be returned.
	    // On a failure, it will be null.
	    prog_return toReturn = null;

	    // Save a link to the orinal error out.
	    PrintStream old = System.err;

	    Console.clearConsole(sourcePath);

	    // Set the error out to a new printstream that will only display the antlr
	    // output.
	    ANTLRFilteredPrintStream errorStream = new ANTLRFilteredPrintStream(Console.getMessageConsoleStream(sourcePath), sourcePath, old, sourcePath);
	    System.setErr(errorStream);

	    try {
	    	toReturn = produceParseData(sourcePath).getParser().prog();
	    } catch (Exception e) {
	      e.printStackTrace();
	      System.out.println("Caught in the getAST Method.");
	    } //try-catch

	    //setErrors(errorStream.getErrorList());

	    System.err.flush();
	    System.setErr(old);

	    return toReturn;
	} //getAST
	
	private static BNGParseData produceParseData(String src) {
	    // Get the text in the document.
	    //String text = this.getSourceViewer().getDocument().get();
		String text = readFileAsString(src);

	    return BNGParserUtility.produceParserInfoForBNGLText(text);
	} //produceParseData
	
	private static String readFileAsString(String filePath) {
		StringBuffer fileData = new StringBuffer(1000);
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			char[] buf = new char[1024];
			int numRead=0;
			while((numRead=reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
				buf = new char[1024];
			} //while
			reader.close();
		} catch (Exception e){ 
			System.out.println("wtf");
		} //try-catch
		
		return fileData.toString();
	} //readFileAsString
		
} //BackgroundFileLoader