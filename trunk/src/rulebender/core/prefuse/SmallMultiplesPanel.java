package rulebender.core.prefuse;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import bngparser.BNGParseData;
import bngparser.BNGParserUtility;
import bngparser.grammars.BNGGrammar.prog_return;

import prefuse.Display;
import rulebender.contactmap.models.CMapModelBuilder;
import rulebender.contactmap.models.ContactMapModel;
import rulebender.contactmap.prefuse.ContactMapVisual;
import rulebender.contactmap.view.ContactMapView;
import rulebender.core.prefuse.overview.Overview;
import rulebender.core.utility.ANTLRFilteredPrintStream;
import rulebender.core.utility.Console;
import rulebender.editors.bngl.model.BNGASTReader;
import rulebender.editors.bngl.model.BNGLModel;
import rulebender.errorview.model.BNGLError;
import rulebender.simulationjournaling.model.SmallMultiple;
import rulebender.simulationjournaling.view.SmallMultiplesView;


/**
 * This class defines the pane that contains an array of prefuse.Display objects,
 * each an overview for that BNGL model.  It is a subclass of the AWT JLayeredPane.
 * @author johnwenskovitch
*/

public class SmallMultiplesPanel extends JLayeredPane {
	
	private static final long serialVersionUID = -5595319590026393256L;
	
	// Temporary values for while I'm just hardcoding in the layout
	private int m_numFiles = 9;
	private int rows;
	private int cols;	
	
	// These are temporary variables for testing purposes
	//private ContactMapView m_view = new ContactMapView();
	//private SmallMultiplesView m_view[];
	private SmallMultiplesView m_view;
	private BNGLModel m_model;
	
	// The width of the borders in pixels
	private final int BORDER_WIDTH = 1;
	
	// Defines the size of the overall panel, as well as the size of each of the small multiple panels
	private Dimension m_overallSize;
	private Dimension m_individualSize;
	
	// The border object that separates the inner JPanels
	private Border border;
	
	// The array of panels that holds each of the small multiples
	private JPanel[] myPanel;
	
	// The array of contact maps, one for each of the panels
	private Display[] smallMultiple;
	
	// Temporary variable to hold a contact map small multiple
	SmallMultiple sm;
		
	/**
	 * Two functions:
	 *   1. Set the size of the overall panel
	 *   2. Calculate the size of the individual panels
	 * 
	 * @param size - the size of the panel
	 */
	public SmallMultiplesPanel(Dimension size) { 
		m_overallSize = size;
		
		//TODO: figure out the number of multiples are in the folder
		// m_numFiles = findNumberOfSmallMultiples();
		
		// Set the layout of the small multiples panel
		selectLayout(m_numFiles);
		
		m_individualSize = findIndividualPanelSize(size);
		
		this.setLayout(new GridLayout(rows, cols, -1, -1));
		
		// Instantiate the border object.
		border = new LineBorder(Color.GRAY, BORDER_WIDTH);
		
		// Create the array of panels
		myPanel = new JPanel[m_numFiles];
		
		// Instantiate each of the JPanels and set their borders
		for (int i = 0; i < m_numFiles; i++) {
			myPanel[i] = new JPanel();
			myPanel[i].setBorder(border);
			myPanel[i].setBackground(Color.WHITE);
		} //for
		
		//prefuse.Display sm2 = loadTempContactMap();
		
		// Add each of the small multiple panels to the JLayeredPane
		for (int i = 0; i < m_numFiles; i++) {
			// Temporary JLabel to identify each panel
			//TODO: replace these with the contact maps
			//JLabel temp = new JLabel();
			//temp.setText(Integer.toString(i));
			//myPanel[i].add(temp);
			//myPanel[i].add(sm.getDisplay());

			//prefuse.Display sm2 = loadTempContactMap(i);
			prefuse.Display sm2 = loadTempContactMap();
			
			//m_view[i] = new SmallMultiplesView();
			//m_view[i].setSmallMultiple(sm2);
			
			//myPanel[i].add(new Overview(sm2));
			myPanel[i].add(sm2);
			
			this.add(myPanel[i], new Integer(0));
			//this.add(m_view[i], new Integer(0));
			
		} //for
		
		
		// Update the sizes of the JPanels and Displays
		myResize(size);
		
	} //SmallMultiplesPanel (constructor)
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Temporary methods to load a sample contact map into each small multiple
	//
	public prefuse.Display loadTempContactMap() {
	//public prefuse.Display loadTempContactMap(int i) {
		
		//TODO: get the sourcePath and the AST for the model	
		String sourcePath = "C:\\Users\\John\\runtime-rulebender.product\\test_project\\test_folder\\egfr_net.bngl";
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
		
		// Set a dimension
		//Dimension dim = m_view.getSize();
		Dimension dim = m_individualSize;
				
		// Get the CMapVisual object for the CMapModel
		SmallMultiple cVisual = new SmallMultiple(m_view, cModel, dim);
		//SmallMultiple cVisual = new SmallMultiple(m_view[i], cModel, dim);
		
		return cVisual.getDisplay();
		
	} //loadTempContactMap
	
	
	
	
	public BNGLModel getModel(String sourcePath) {
	    if (m_model == null) {
	    	
	    	m_model = new BNGLModel(sourcePath);
	    	m_model.setAST(getAST(sourcePath));
	    } //if

	    return m_model;
	} //getModel
	
	private prog_return getAST(String sourcePath) {
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
	
	private BNGParseData produceParseData(String src) {
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
	/*
	private void setErrors(ArrayList<BNGLError> errorList) {
	    // Add the error list to the model
	    m_model.setErrors(errorList);

	    // Get the document.
	    IDocument document = getDocumentProvider().getDocument(getEditorInput());

	    // Get the ifile reference for this editor input.
	    IFile file = ((FileEditorInput) ((IEditorInput) getEditorInput())).getFile();

	    // Create a reference to a region that will be used to hold information
	    // about the error location.
	    IRegion region = null;

	    // Set the annotations.
	    for (BNGLError error : errorList) {
	    	// Get the information about the location.
	    	try {
	    		region = document.getLineInformation(error.getLineNumber() - 1);
	    	} catch (BadLocationException exception) {
	    		exception.printStackTrace();
	    	} //try-catch

	    	// make a marker
	    	IMarker marker = null;
	    	try {
	    		marker = file.createMarker("rulebender.markers.bnglerrormarker");
	        	marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	        	marker.setAttribute(IMarker.MESSAGE, error.getMessage());
	        	marker.setAttribute(IMarker.LINE_NUMBER, error.getLineNumber() - 1);
	        	marker.setAttribute(IMarker.CHAR_START, region.getOffset());
	        	marker.setAttribute(IMarker.CHAR_END, region.getOffset() + region.getLength());

	        	System.out.println("Made the marker!");
	    	} catch (Exception exception) {
	    		exception.printStackTrace();
	    	} //try-catch
	    } //for
	} //setErrors
	*/
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	

	/**
	 * Determine the size of each of the individual JPanels from the overall panel dimension
	 * 
	 * @param overallSize - the size of the overall small multiples panel
	 */
	public Dimension findIndividualPanelSize(Dimension overallSize) {
		int height = 0, width = 0;
				
		height = (int) overallSize.getHeight() / rows;
		width = (int) overallSize.getWidth() / cols;
				
		return new Dimension(width, height);
	} //findIndividualPanelSize
	
	
	/** 
	 * Determine the number of BNGL files in the current working directory - this should be the number of
	 * small multiples that we need to make space for and layout
	 */
	public int findNumberOfSmallMultiples() {
		int fileCount = 0;
		
		//TODO: figure out what the current working directory is, and count the number of BNGL files
		
		
		return fileCount;
	} //findNumberOfSmallMultiples
	
	/**
	 * Determine the layout of the small multiples panels given the total number of contact maps we
	 * want to display
	 * 
	 * @param numFiles - the number of small multiples to display
	 */
	public void selectLayout(int numFiles) {
		if (numFiles <= 2) {
			cols = 1;
			rows = 2;
		} else if (numFiles <= 4) {
			cols = 2;
			rows = 2;
		} else if (numFiles <= 6) {
			cols = 2;
			rows = 3;
		} else if (numFiles <= 9) {
			cols = 3;
			rows = 3;
		} else if (numFiles <= 12) {
			cols = 3;
			rows = 4;
		} else if (numFiles <= 16) {
			cols = 4;
			rows = 4;
		} else if (numFiles <= 20) {
			cols = 4;
			rows = 5;
		} else if (numFiles <= 25) {
			cols = 5;
			rows = 5;
		} else if (numFiles <= 30) {
			cols = 5;
			rows = 6;
		} else {
			cols = 6;
			rows = 6;
		} //if-else
	} //selectLayout

	
	
	
	
	
	
	
	
	/*
	 * Grabbed from LayeredPane - needs to be updated to reflect the new layout
	 */
	
	/**
	 * There is a native resize method, but I needed to do more so I created
	 * this one.  This default version calls the parameterized version with
	 * the m_currentSize as input.
	 */
	public void myResize() {
		myResize(m_overallSize);
	} //myResize
	
	/**
	 * There is a native resize method, but I needed to do more so I created
	 * this one.
	 * 
	 * Updates the size of the each pane
	 * 
	 * @param size
	 */
	public void myResize(Dimension size) {	
		
		m_individualSize = findIndividualPanelSize(size);
		
		for (int i = 0; i < m_numFiles; i++) {
			
			//TODO Set correct x and y locations for the panel (instead of the 0,0)
			
			myPanel[i].setBounds(myPanel[i].getBounds().x, myPanel[i].getBounds().y, m_individualSize.width, m_individualSize.height);
			
			if (myPanel[i].getComponentCount() == 1) {
				((Display) myPanel[i].getComponent(0)).setSize(new Dimension(m_individualSize.width-BORDER_WIDTH*2, m_individualSize.height-BORDER_WIDTH*2));				
				((Display) myPanel[i].getComponent(0)).setBounds(BORDER_WIDTH, BORDER_WIDTH, m_individualSize.width-BORDER_WIDTH*2, m_individualSize.height-BORDER_WIDTH*2);
			} //if
			
		} //for
		
		/*
		m_currentSize = size;
		
		int overviewWidth = (int) (m_currentSize.getWidth() * OVERVIEW_WIDTH);
		int overviewHeight = (int) (m_currentSize.getHeight() * OVERVIEW_HEIGHT);
		
		
		if(mainJPanel != null && overviewJPanel != null)
		{
			mainJPanel.setBounds(0, 0, m_currentSize.width, m_currentSize.height);
			
			overviewJPanel.setBounds(0, size.height-overviewHeight, overviewWidth-BORDER_WIDTH, overviewHeight-BORDER_WIDTH);
			
			if(mainJPanel.getComponentCount() == 1 && overviewJPanel.getComponentCount() == 1)
			{
				((Display) mainJPanel.getComponent(0)).setSize(new Dimension(m_currentSize.width-BORDER_WIDTH*2, m_currentSize.height-BORDER_WIDTH*2));				
				((Display) mainJPanel.getComponent(0)).setBounds(BORDER_WIDTH, BORDER_WIDTH, m_currentSize.width-BORDER_WIDTH*2, m_currentSize.height-BORDER_WIDTH*2);
				((Display) overviewJPanel.getComponent(0)).setBounds(BORDER_WIDTH, BORDER_WIDTH, overviewWidth-BORDER_WIDTH*3, overviewHeight-BORDER_WIDTH*2);
				((Display) overviewJPanel.getComponent(0)).setSize(new Dimension(overviewWidth-BORDER_WIDTH*3, overviewHeight-BORDER_WIDTH*2));
				
			}
		}
		*/
		
	} //myResize (with dimension parameter provided)

	public void setDisplay(Display d) {
		// TODO Auto-generated method stub
		
	} //setDisplay

} //SmallMultiplesPanel
