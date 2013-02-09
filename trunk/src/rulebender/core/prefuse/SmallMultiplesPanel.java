package rulebender.core.prefuse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
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
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;
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
import rulebender.simulationjournaling.comparison.SimilarityMatrices;
import rulebender.simulationjournaling.model.SmallMultiple;
import rulebender.simulationjournaling.view.SmallMultiplesView;


/**
 * This class defines the pane that contains an array of prefuse.Display objects,
 * each an overview for that BNGL model.  It is a subclass of the AWT JLayeredPane.
 * @author johnwenskovitch
*/

public class SmallMultiplesPanel extends JLayeredPane implements ActionListener {
	
	private static final long serialVersionUID = -5595319590026393256L;
	private static String COMPONENT_GRAPH = "component_graph";
	
	// Temporary values for while I'm just hardcoding in the layout
	//private int m_numFiles = 12;
	private int m_numFiles = 4;
	//private int m_numFiles = 8;
	private int rows;
	private int cols;	
	
	// The width of the borders in pixels
	private final int BORDER_WIDTH = 1;
	
	// Defines the size of the overall panel, as well as the size of each of the small multiple panels
	private Dimension m_overallSize;
	private Dimension m_individualSize;
	
	// The border object that separates the inner JPanels
	private Border border;
	
	// The panel that holds the entire view
	private JPanel fullPanel;
	
	// The upper panel with layout dropdown (and maybe other stuff too)
	private JPanel upperPanel;
	
	// The dropdown box and label for layout list
	private JLabel lblLayouts;
	private JComboBox ddlLayouts;
	
	// The lower panel that will hold the small multiples panels
	private JPanel lowerPanel;
	
	// The array of panels that holds each of the small multiples
	private JPanel[] myPanel;
	
	// The array of contact maps, one for each of the panels
	private Display[] smallMultiple;
	
	// Temporary variable to hold a contact map small multiple
	//SmallMultiple sm;
	Display sm[];
	
	// The collection of similarity matrices
	SimilarityMatrices matrixLayout;
	
	// The scores of the matrix comparison
	double[] similarityScores;
		
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
		
		m_individualSize = findIndividualPanelSize(m_overallSize);
		
		// Initialize the label and dropdown list for the layouts
		lblLayouts = new JLabel();
		ddlLayouts = new JComboBox();
				
		// Create the label text, populate the dropdown list (temp hardcoded layouts), and add the listener
		lblLayouts.setText("Choose layout: ");
		lblLayouts.setHorizontalAlignment(SwingConstants.RIGHT);
		
		ddlLayouts.addItem("-- Use default layouts --");
		// TODO: Automatically pull these from the active directory
		ddlLayouts.addItem("egfr_net.pos");
		ddlLayouts.addItem("egfr_net_1.pos");
		ddlLayouts.addItem("egfr_net_2.pos");
		//ddlLayouts.addItem("egfr_net_3.pos");
		ddlLayouts.setSelectedIndex(0);
		ddlLayouts.addActionListener(this);
		
		// Initialize the major panels
		fullPanel = new JPanel();
		upperPanel = new JPanel();
		lowerPanel = new JPanel();
		
		// Set the layout on the panels and the RCP View
		fullPanel.setLayout(new BorderLayout());
		upperPanel.setLayout(new GridLayout(1, 2));
		lowerPanel.setLayout(new GridLayout(rows, cols, -1, -1));
		this.setLayout(new GridLayout(1, 1));
		
		// Create the array of panels
		myPanel = new JPanel[m_numFiles];
		for (int i = 0; i < m_numFiles; i++) {
			myPanel[i] = new JPanel();
		} //for
		
		// Initialize the small multiples layout
		initializeSmallMultiplesDisplay((String)ddlLayouts.getSelectedItem());
		
		for (int i = 0; i < m_numFiles; i++) {
			lowerPanel.add(myPanel[i]);
		} //if
		
		// Add the dropdown list and label to the upper panel
		upperPanel.add(lblLayouts);
		upperPanel.add(ddlLayouts);
					
		// Add the upper and lower panels to the main panel
		fullPanel.add(upperPanel, BorderLayout.NORTH);
		fullPanel.add(lowerPanel, BorderLayout.CENTER);
		
		// Add the main panel to the view
		this.add(fullPanel);
		
		// Update the sizes of the JPanels and Displays
		myResize(m_overallSize);
		
	} //SmallMultiplesPanel (constructor)
	
	public void initializeSmallMultiplesDisplay(String layoutChoice) {
				
		// Instantiate each of the JPanels and set their borders
		for (int i = 0; i < m_numFiles; i++) {
			if (myPanel[i].getComponentCount() != 0) {
				myPanel[i].remove(0);
			} //if
		} //for
				
		sm = new Display[m_numFiles];
				
		// Load the small multiples
		for (int i = 0; i < m_numFiles; i++) {
			sm[i] = loadTempContactMap(i, layoutChoice);
		} //for
		
		//String[] modelNames = {"egfr_net", "egfr_net_1", "egfr_net_2", "egfr_net_3", "TLR4_RPS_v1", /*"TLR4_RPS_v2", "TLR4_RPS_v3",*/ "TLR4_RPS_v4", "TLR4_RPS_v5", "TLR_v1", "TLR_v15", "TLR_v16", "TLR_v24", "TLR_v25"};
		String[] modelNames = {"egfr_net", "egfr_net_1", "egfr_net_2", "egfr_net_3"};
		//String[] modelNames = {"TLR4_RPS_v1", "TLR4_RPS_v4", "TLR4_RPS_v5", "TLR_v1", "TLR_v15", "TLR_v16", "TLR_v24", "TLR_v25"};
		matrixLayout = new SimilarityMatrices(modelNames, sm);		
				
		matrixLayout.fillSimilarityMatrices();
		matrixLayout.printSimilarityMatrices();

		// Sort the small multiples here
		int largestIndex = findMostCompleteModel();
		sortModels(largestIndex, matrixLayout);
		
		// Instantiate the border object.
		border = new LineBorder(Color.GRAY, BORDER_WIDTH);
				
		// Add each of the sorted small multiple panels to the lower Panel
		for (int i = 0; i < m_numFiles; i++) {
			myPanel[i].add(sm[i]);
			myPanel[i].setBorder(border);
			myPanel[i].setBackground(Color.WHITE);
			myPanel[i].repaint();
		} //for
				
	} //initializeSmallMultiplesDisplay
	
	/**
	 * Whenever the dropdown list selection is modified, regenerate the contact maps with the newly selected layout
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
        JComboBox cb = (JComboBox)e.getSource();
        String layoutChoice = (String)cb.getSelectedItem();
        System.out.println(layoutChoice);
        initializeSmallMultiplesDisplay(layoutChoice);
	} //actionPerformed
	
	/**
	 * Figure out which of the models is the largest / most complete
	 * 
	 * @return - the index of the most complete model
	 */
	public int findMostCompleteModel() {
		int largestModelIndex = 0;
		int largestModelSize = countLabels(sm[0].getVisualization());
		
		for (int i = 1; i < m_numFiles; i++) {
			if (countLabels(sm[i].getVisualization()) > largestModelSize) {
				largestModelSize = countLabels(sm[i].getVisualization());
				largestModelIndex = i;
			} //if
		} //for
		
		return largestModelIndex;
	} //findMostCompleteModel
	
	/**
	 * Sort the models based on the computed similarity scores
	 * 
	 * @param largestModelIndex - index of the most complete model
	 * @param smScores - the computed similarity matrices
	 */
	public void sortModels(int largestModelIndex, SimilarityMatrices smScores) {
		similarityScores = computeModelSimilarityScores(largestModelIndex, smScores);
		
		for (int i = 0; i < m_numFiles; i++) {
			for (int j = i; j < (m_numFiles - 1); j++) {
				if (similarityScores[j] < similarityScores[j+1]) {
					swapModels(j, j+1);
				} //if
			} //for			
		} //for
	
	} //sortModels
	
	/**
	 * Swap the ordering of two of the models in the sm array
	 * 
	 * @param index1 - index of the first model
	 * @param index2 - index of the second model
	 */
	public void swapModels(int index1, int index2) {
		Display temp = sm[index1];
		double tempScore = similarityScores[index1];
		
		sm[index1] = sm[index2];
		similarityScores[index1] = similarityScores[index2];
		
		sm[index2] = temp;
		similarityScores[index2] = tempScore;
		
	} //swap
	
	/**
	 * Compute the similarity scores of each of the models.  
	 * Set the score of the largest model to something big, so it goes first
	 * 
	 * @param largestModelIndex - index of the most complete model
	 * @param smScores - the similarity matrices that were computed earlier
	 * 
	 * @return - the similarity scores of each of the models computed by the formula
	 */
	public double[] computeModelSimilarityScores(int largestModelIndex, SimilarityMatrices smScores) {
		double[] similarityScores = new double[m_numFiles];
		
		for (int i = 0; i < m_numFiles; i++) {
			if (i == largestModelIndex) {
				similarityScores[i] = 9999;
			} else {
				similarityScores[i] = (smScores.getSimilarVerticesMatrix().getSimilarityValue(largestModelIndex, i) * smScores.getPercentSimilarVerticesMatrix().getSimilarityValue(largestModelIndex, i)) + ((smScores.getSimilarEdgesMatrix().getSimilarityValue(largestModelIndex, i) * smScores.getPercentSimilarEdgesMatrix().getSimilarityValue(largestModelIndex, i)));
			} //if
			
		} //for
		
		return similarityScores;
	} //computerModelSimilarityScores
	
	
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Temporary methods to load a sample contact map into each small multiple
	//
	//public prefuse.Display loadTempContactMap() {
	public prefuse.Display loadTempContactMap(int i, String layoutChoice) {
		
		//TODO: get the sourcePath and the AST for the model
		//String sourcePath[] = new String[14];
		String sourcePath[] = new String[4];
		//String sourcePath[] = new String[8];
		String layoutPath = null;
							
		sourcePath[0] = new String("C:\\Users\\John\\runtime-rulebender.product\\egfr_net\\egfr_net\\egfr_net.bngl");
		sourcePath[3] = new String("C:\\Users\\John\\runtime-rulebender.product\\egfr_net\\egfr_net\\egfr_net_1.bngl");
		sourcePath[1] = new String("C:\\Users\\John\\runtime-rulebender.product\\egfr_net\\egfr_net\\egfr_net_2.bngl");
		sourcePath[2] = new String("C:\\Users\\John\\runtime-rulebender.product\\egfr_net\\egfr_net\\egfr_net_3.bngl");
		//sourcePath[0] = new String("C:\\Users\\John\\runtime-rulebender.product\\TLR\\TLR\\TLR4_RPS_v1.bngl");
		//sourcePath[1] = new String("C:\\Users\\John\\runtime-rulebender.product\\TLR\\TLR\\TLR4_RPS_v4.bngl");
		//sourcePath[2] = new String("C:\\Users\\John\\runtime-rulebender.product\\TLR\\TLR\\TLR4_RPS_v5.bngl");
		//sourcePath[3] = new String("C:\\Users\\John\\runtime-rulebender.product\\TLR\\TLR\\TLR4_v1.bngl");
		//sourcePath[4] = new String("C:\\Users\\John\\runtime-rulebender.product\\TLR\\TLR\\TLR4_v15.bngl");
		//sourcePath[5] = new String("C:\\Users\\John\\runtime-rulebender.product\\TLR\\TLR\\TLR4_v16.bngl");
		//sourcePath[6] = new String("C:\\Users\\John\\runtime-rulebender.product\\TLR\\TLR\\TLR4_v24.bngl");
		//sourcePath[7] = new String("C:\\Users\\John\\runtime-rulebender.product\\TLR\\TLR\\TLR4_v25.bngl");
		
		prog_return ast = getModel(sourcePath[i]).getAST();
		
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
		
		cModel.setSourcePath(sourcePath[i]);
		
		// Set a dimension
		//Dimension dim = m_view.getSize();
		Dimension dim = m_individualSize;
				
		// Determine which layout to use given the dropdown list selection
		if (layoutChoice.equals("-- Use default layouts --")) {
			layoutPath = null;
		} else if (layoutChoice.equals("egfr_net.pos")) {
			layoutPath = "C:\\Users\\John\\runtime-rulebender.product\\egfr_net\\egfr_net\\egfr_net.pos";
		} else if (layoutChoice.equals("egfr_net_1.pos")) {
			layoutPath = "C:\\Users\\John\\runtime-rulebender.product\\egfr_net\\egfr_net\\egfr_net_1.pos";
		} else if (layoutChoice.equals("egfr_net_2.pos")) {
			layoutPath = "C:\\Users\\John\\runtime-rulebender.product\\egfr_net\\egfr_net\\egfr_net_2.pos";
		} else if (layoutChoice.equals("egfr_net_3.pos")) {
			layoutPath = "C:\\Users\\John\\runtime-rulebender.product\\egfr_net\\egfr_net\\egfr_net_3.pos";
		} // if-else
		
		
		// Get the CMapVisual object for the CMapModel
		//SmallMultiple cVisual = new SmallMultiple(m_view, cModel, dim);
		SmallMultiple cVisual = new SmallMultiple(cModel, dim, layoutPath);
		//SmallMultiple cVisual = new SmallMultiple(m_view[i], cModel, dim);
		
		return cVisual.getDisplay();
		
	} //loadTempContactMap
	
	private int countLabels(Visualization vis) { 
		int count = 0;
		
		Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
		
		// Loop through the list of nodes
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			
			// Increment the counter if the node is not null
			if (item.getString("molecule") != null) {
				count++;				
			} //if
			
		} //while
		
		iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.EDGES));
		
		// Loop through the list of edges
		while (iter.hasNext()) {
			
			// Get the two endpoints of each edge and find their labels
			Edge edge = (Edge) iter.next();
			
			VisualItem source = (VisualItem) edge.getSourceNode();
			VisualItem target = (VisualItem) edge.getTargetNode();
			
			if ((source.getString("molecule") == null) || (target.getString("molecule") == null)) {
				continue;
			} //if
			
			count++;
		} // while
		
		return count;
	} //countLabels
	
	
	public BNGLModel getModel(String sourcePath) {
		BNGLModel m_model = null;
		
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
				
		height = (int) (overallSize.getHeight() - 25) / rows;
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
			
			myPanel[i].setBounds(myPanel[i].getBounds().x, myPanel[i].getBounds().y, m_individualSize.width, m_individualSize.height);
			
			if (myPanel[i].getComponentCount() == 1) {
				((Display) myPanel[i].getComponent(0)).setSize(new Dimension(m_individualSize.width-BORDER_WIDTH*2, m_individualSize.height-BORDER_WIDTH*2));				
				((Display) myPanel[i].getComponent(0)).setBounds(BORDER_WIDTH, BORDER_WIDTH, m_individualSize.width-BORDER_WIDTH*2, m_individualSize.height-BORDER_WIDTH*2);
			} //if
			
		} //for
		
	} //myResize (with dimension parameter provided)

	public void setDisplay(Display d) {
		// TODO Auto-generated method stub
		
	} //setDisplay

} //SmallMultiplesPanel
