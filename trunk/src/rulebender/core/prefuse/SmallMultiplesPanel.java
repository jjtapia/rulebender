package rulebender.core.prefuse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.util.ColorLib;
import prefuse.util.GraphicsLib;
import prefuse.util.PrefuseLib;
import prefuse.util.display.DisplayLib;
import prefuse.visual.VisualItem;
import rulebender.simulationjournaling.comparison.SimilarityMatrices;
import rulebender.simulationjournaling.model.BackgroundFileLoader;
import rulebender.simulationjournaling.model.SMClickControlDelegate;
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
	
	// Temporary values for while I'm just hardcoding in the directory
	// TODO: get this directory path automatically from a user's RuleBender interactions
	//private String m_directory = "C:\\Users\\John\\runtime-rulebender.product\\TLR\\TLR\\";
	//private String m_directory = "C:\\Users\\John\\runtime-rulebender.product\\egfr_net\\egfr_net\\";
	private String m_directory = "C:\\Users\\John\\runtime-rulebender.product\\fceri\\models\\";
	//private String m_directory = "C:\\Users\\John\\runtime-rulebender.product\\stat\\stat\\";
	
	private int m_numFiles;
	
	private int rows;
	private int cols;	
	String sourcePath[];
	
	// The width of the borders in pixels
	private final int BORDER_WIDTH = 1;
	private final int WIDE_BORDER = 3;
	
	// Defines the size of the overall panel, as well as the size of each of the small multiple panels
	private Dimension m_overallSize;
	private Dimension m_individualSize;
	
	// The buttons to auto-populate the layout and to clear the layout
	private JButton btnPopulate;
	private JButton btnClear;
	
	// The radio buttons to control comparing similarities or differences
	private JRadioButton rbtnCompareSimilarities;
	private JRadioButton rbtnCompareDifferences;
	ButtonGroup rbtnGroupCompare;
	
	// The border object that separates the inner JPanels
	private Border border;
	private Border highlightBorder;
	
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
	
	// The state of the grid (for when the layout is updated)
	private boolean displayBlank = true;
	
	// Highlight background color
	private Color highlightBackgroundColor;
	
	// Holds a contact map small multiple
	SmallMultiple sm[];
	
	// The collection of similarity matrices
	SimilarityMatrices matrixLayout;
	
	// The scores of the matrix comparison
	double[] similarityScores;
	
	// The SmallMultiplesView that this panel is associated with
	SmallMultiplesView m_view;
	
	// The last panel index that was selected (for recognizing deselection)
	private int m_lastPanelSelected;
	
	// If something is currently highlighted in the interface
	private boolean m_currentlyHighlighted;
	
	// A HashMap that holds any pre-existing but not currently displayed
	// contact map prefuse visualizations. This is so that a new contact map
	// is not created every time a user selects a new bngl editor tab.
	private HashMap<String, SmallMultiple> m_contactMapRegistry;
		
	// A HashMap that holds the name of a position file and its path
	private HashMap<String, String> m_positionLookup;
	
	// List of currently selected model indices
	private ArrayList<Integer> m_selectedModels;
	
	
	/**
	 * Two functions:
	 *   1. Set the size of the overall panel
	 *   2. Calculate the size of the individual panels
	 * 
	 * @param size - the size of the panel
	 */
	public SmallMultiplesPanel(Dimension size, SmallMultiplesView view) { 
		
		m_overallSize = size;
		
		m_view = view;
		
		m_lastPanelSelected = -1;
		m_currentlyHighlighted = false;
		
		highlightBorder = new LineBorder(Color.RED, WIDE_BORDER);
		highlightBackgroundColor = Color.getHSBColor(0.1472f, 0.1472f, 1f);
		
		m_contactMapRegistry = new HashMap<String, SmallMultiple>();
		m_positionLookup = new HashMap<String, String>();
		
		m_selectedModels = new ArrayList<Integer>();
		
		// Figure out the number of multiples are in the folder
		m_numFiles = findNumberOfSmallMultiples(m_directory);
		
		// Set the layout of the small multiples panel
		selectLayout(m_numFiles);
		
		m_individualSize = findIndividualPanelSize(m_overallSize);
		
		sm = new SmallMultiple[m_numFiles];
		
		// Initialize the label and dropdown list for the layouts
		lblLayouts = new JLabel();
		ddlLayouts = new JComboBox();
				
		// Create the label text, populate the dropdown list (temp hardcoded layouts), and add the listener
		lblLayouts.setText("Choose layout: ");
		lblLayouts.setHorizontalAlignment(SwingConstants.RIGHT);
		
		populatePositionDropdown(m_directory);
		
		ddlLayouts.setSelectedIndex(0);
		ddlLayouts.addActionListener(this);
		
		// Initialize the buttons and add listener
		btnPopulate = new JButton("Populate from Folder");
		btnClear = new JButton("Clear the Canvas");
		
		btnPopulate.addActionListener(this);
		btnClear.addActionListener(this);
		
		// Initialize the radio buttons, and to a group, add listener
		rbtnGroupCompare = new ButtonGroup();
		rbtnCompareSimilarities = new JRadioButton("Compare similarities");
		rbtnCompareDifferences = new JRadioButton("Compare differences");
		
		rbtnCompareSimilarities.setHorizontalAlignment(SwingConstants.CENTER);
		rbtnCompareDifferences.setHorizontalAlignment(SwingConstants.CENTER);
		
		rbtnGroupCompare.add(rbtnCompareSimilarities);
		rbtnGroupCompare.add(rbtnCompareDifferences);
		
		rbtnCompareSimilarities.addActionListener(this);
		rbtnCompareDifferences.addActionListener(this);

		
		// Initialize the major panels
		fullPanel = new JPanel();
		upperPanel = new JPanel();
		lowerPanel = new JPanel();
		
		// Set the layout on the panels and the RCP View
		fullPanel.setLayout(new BorderLayout());
		upperPanel.setLayout(new GridLayout(1, 4));
		lowerPanel.setLayout(new GridLayout(rows, cols, -1, -1));
		this.setLayout(new GridLayout(1, 1));
		
		// Create the array of panels
		myPanel = new JPanel[m_numFiles];
		for (int i = 0; i < m_numFiles; i++) {
			myPanel[i] = new JPanel();
			myPanel[i].setName(""+i);
			myPanel[i].setToolTipText("testing");
		} //for
		
		// Initialize the small multiples layout
		initializeSmallMultiplesDisplay((String)ddlLayouts.getSelectedItem());
		//initializeBlankDisplay();
		
		for (int i = 0; i < m_numFiles; i++) {
			lowerPanel.add(myPanel[i]);
		} //if
		
		// Add the dropdown list and label to the upper panel
		upperPanel.add(lblLayouts);
		upperPanel.add(ddlLayouts);
		//upperPanel.add(btnPopulate);
		//upperPanel.add(btnClear);
		upperPanel.add(rbtnCompareSimilarities);
		upperPanel.add(rbtnCompareDifferences);
		
		// Add the upper and lower panels to the main panel
		fullPanel.add(upperPanel, BorderLayout.NORTH);
		fullPanel.add(lowerPanel, BorderLayout.CENTER);
		
		// Add the main panel to the view
		this.add(fullPanel);
		
		// Update the sizes of the JPanels and Displays
		myResize(m_overallSize);
		
		// Zoom out the models to the size of the panels
		for (int i = 0; i < m_numFiles; i++) {
			//fitToPanel(i);
			//fitToPanel2(i);
		} //for
		
	} //SmallMultiplesPanel (constructor)
	
	private void populatePositionDropdown(String directory) {
		File dir = new File(directory);
		
		ddlLayouts.addItem("-- Use default layouts --");
		m_positionLookup.put("-- Use default layouts --", null);
		
		// Only try to iterate across all files if we're passed in a directory
		if (dir.isDirectory()) {
			for (File child : dir.listFiles()) {
				if (isPositionFile(child)) {
					// Add position file to the dropdown list
					ddlLayouts.addItem(child.getName());
					
					// Add position file to the HashMap that stores the absolute location
					m_positionLookup.put(child.getName(), child.getAbsolutePath());
					
				} //if
			} //for
		} //if
		
	} //populatePositionDropdown
	
	private String lookupPosition(String selection) {
		return m_positionLookup.get(selection);
	} //lookupPosition
	
	private boolean isPositionFile(File child) {
		String filepath = child.getPath();
		return ((filepath.substring(filepath.length()-4, filepath.length()).equals(".pos")) || (filepath.substring(filepath.length()-4, filepath.length()).equals(".POS")));
	} //isPositionFile
	
	private boolean isBNGLFile(File child) {
		String filepath = child.getPath();
		return ((filepath.substring(filepath.length()-5, filepath.length()).equals(".bngl")) || (filepath.substring(filepath.length()-5, filepath.length()).equals(".BNGL")));
	} //isBNGLFile
	
	private void populateSmallMultiplesDisplay(String directory, String layoutChoice) {
		File dir = new File(directory);
		int currentMultiple = 0;
		
		// Only try to iterate across all files if we're passed in a directory
		if (dir.isDirectory()) {
			for (File child : dir.listFiles()) {
				if (isBNGLFile(child)) {
					
					if (sm[currentMultiple] == null) {
						SmallMultiple loadModel = lookupDisplay(child.getAbsolutePath());
						
						if (loadModel == null) {
							// If the model hasn't already been loaded, load it (using the thread class), then store it in the registry
							SmallMultipleLoaderThread thread = new SmallMultipleLoaderThread(currentMultiple, child, layoutChoice);
							thread.run();
						} else {
							// If the model has already been loaded, retrieve it from the HashMap, and make sure it's using the right layout
							sm[currentMultiple] = loadModel;
							sm[currentMultiple].setLayoutPath(lookupPosition(layoutChoice));
						} //if-else
						
					} else {
						sm[currentMultiple].setLayoutPath(lookupPosition(layoutChoice));
						
					} //if-else
					
					currentMultiple++;
				} //if
			} //for
		} //if
		
	} //populateSmallMultiplesDisplay
	
	private String[] populateModelNames(String directory) {
		File dir = new File(directory);
		String modelNames[] = new String[m_numFiles];
		int currentModel = 0;
		
		// Only try to iterate across all files if we're passed in a directory
		if (dir.isDirectory()) {
			for (File child : dir.listFiles()) {
				if (isBNGLFile(child)) {
					modelNames[currentModel] = child.getName();
					currentModel++;
				} //if
			} //for
		} //if
		
		return modelNames;
	} //populateModelNames
	
	public void initializeSmallMultiplesDisplay(String layoutChoice) {
		
		// Load the small multiples if they don't exist in the array already
		populateSmallMultiplesDisplay(m_directory, layoutChoice);
		
		String[] modelNames = populateModelNames(m_directory);
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
			
			sm[i].getDisplay().getVisualization().run("layout");
			sm[i].getDisplay().getVisualization().run("color");
			sm[i].getDisplay().getVisualization().run("bubbleLayout");
			sm[i].getDisplay().getVisualization().run("bubbleColor");
			
			if (myPanel[i].getComponentCount() == 0) {
				myPanel[i].add(sm[i].getDisplay());
			} //if
			
			myPanel[i].setBorder(border);
			myPanel[i].setBackground(Color.WHITE);
			myPanel[i].repaint();
			sm[i].getDisplay().repaint();
		} //for
				
		// Display is no longer blank
		displayBlank = false;
		
	} //initializeSmallMultiplesDisplay
	
	public int getLastPanelSelected() {
		return m_lastPanelSelected;
	} //getLastPanelSelected
	
	public void setLastPanelSelected(int sel) {
		// If an old panel is being de-selected, remove all highlighting
		// If a new panel is being selected, highlight that panel.
		if (sel != -1) {
			if (m_lastPanelSelected == sel) {
				removeHighlighting();
			} else {
				highlightPanel(sel, true);
			} //if-else
		} //if

		m_lastPanelSelected = sel;
	} //setLastPanelSelected
	
	private SmallMultiple lookupDisplay(String filepath) {
	    return m_contactMapRegistry.get(filepath);
	} //lookupDisplay
	
	public void highlightPanel(int panelIndex, boolean resetHighlighting) {
		// First reset all panels without highlighting if requested
		if (resetHighlighting) {
			removeHighlighting();	
		} //if
		
		// Now highlight the selected index
		//myPanel[panelIndex].setBorder(highlightBorder);
		myPanel[panelIndex].setBackground(highlightBackgroundColor);
		sm[panelIndex].getDisplay().setBackground(highlightBackgroundColor);
		sm[panelIndex].getDisplay().setSize(myPanel[panelIndex].getWidth() - 2, myPanel[panelIndex].getHeight() - 2);
		sm[panelIndex].getDisplay().repaint();
		sm[panelIndex].getDisplay().getVisualization().repaint();
		myPanel[panelIndex].repaint();
	} //highlightPanel
	
	public void highlightPanels(int panelIndex1, int panelIndex2) {
		highlightPanel(panelIndex1, true);
		highlightPanel(panelIndex2, false);
	} //highlightPanels
	
	public void setHighlightedPanels(ArrayList<Integer> panels) {
		// First clear the currently highlighted models
		m_selectedModels.clear();
		
		// Then add in the list of models to highlight
		addHighlightedPanels(panels);
	} //setHighlightedPanels
	
	public void addHighlightedPanels(ArrayList<Integer> panels) {
		// Add in the list of models to highlight
		for (int i = 0; i < panels.size(); i++) {
			addHighlightedPanel(panels.get(i));
		} //for
	} //addHighlightedPanels
	
	public void addHighlightedPanel(int panel) {
		// Add panel to currently selected list
		addPanelToList(panel);

		// Highlight the panel selected
		highlightPanel(panel, false);
		
		// Check if comparison should be performed
		if (twoPanelsHighlighted()) {
			compareModels();
		} else {
			clearBubblesetOverlays();
		} //if-else
		
	} //addHighlightedPanel
	
	private void addPanelToList(int panel) {
		// Check to be sure it isn't already in the list
		for (int i = 0; i < m_selectedModels.size(); i++) {
			if (m_selectedModels.get(i) == panel) {
				return;
			} //if
		} //for
		
		// Add the panel to the list
		m_selectedModels.add(panel);
		
		// If more than two panels get selected, remove the oldest
		if (m_selectedModels.size() > 2) {
			clearBubblesetOverlays();
			((SMClickControlDelegate)sm[m_selectedModels.get(0)].getNetworkViewer().getClickControl()).setCurrentlySelected(false);
			removeHighlightedPanel(m_selectedModels.get(0));
		} //if
		
	} //addPanelToList
	
	public void removeHighlightedPanels(ArrayList<Integer> panels) {
		while (panels.size() != 0) {
			removeHighlightedPanel(panels.get(0));
		} //while
	} //removeHighlightedPanels
	
	public void removeHighlightedPanel(int panel) {
		for (int i = 0; i < m_selectedModels.size(); i++) {
			if (m_selectedModels.get(i) == panel) {
				m_selectedModels.remove(i);
				break;
			} //if
		} //for
		
		if (!(twoPanelsHighlighted())) {
			clearBubblesetOverlays();
		} //if
		
		removeHighlightingOnPanel(panel);
	} //removeHighlightedPanels
	
	public ArrayList<Integer> getHighlightedPanels() {
		return m_selectedModels;
	} //getHightlightedPanels
	
	public void removeAllSelections() {
		m_selectedModels.clear();
		for (int i = 0; i < sm.length; i++) {
			removeHighlightedPanel(i);
		} //for
	} //removeAllSelections
	
	public boolean twoPanelsHighlighted() {
		return ((m_selectedModels.size() == 2) && (m_selectedModels.get(0) != -1) && (m_selectedModels.get(1) != -1));
	} //twoPanelsHighlighted
	
	public void compareModels() {
		//  Check radio buttons to see if we are comparing similarities or differences
		boolean comparingSimilarities = rbtnCompareSimilarities.isSelected();
		boolean comparingDifferences = rbtnCompareDifferences.isSelected();
		
		if (comparingSimilarities) {
			// if comparing similarities, call similarity comparison function in panel1's SMClickControlDelegate
			((SMClickControlDelegate)sm[m_selectedModels.get(0)].getNetworkViewer().getClickControl()).handleSimilaritiesOption(this, m_selectedModels.get(0), sm[m_selectedModels.get(1)].getDisplay().getVisualization(), m_selectedModels.get(1));
			
		} else if (comparingDifferences) {
			// else if comparing differences, call differences comparison function in panel1's SMClickControlDelegate 
			((SMClickControlDelegate)sm[m_selectedModels.get(0)].getNetworkViewer().getClickControl()).handleDifferencesOption(this, m_selectedModels.get(0), sm[m_selectedModels.get(1)].getDisplay().getVisualization(), m_selectedModels.get(1));
			
		} //if-else
		
	} //compareModels
	
	public void selectCompareSimilaritiesButton() {
		rbtnCompareSimilarities.setSelected(true);
	} //selectCompareSimilaritiesButton
	
	public void selectCompareDifferencesButton() {
		rbtnCompareDifferences.setSelected(true);
	} //selectCompareDifferencesButton
	
	public void removeHighlighting() {
		for (int i = 0; i < m_numFiles; i++) {
			myPanel[i].setBackground(Color.WHITE);
			sm[i].getDisplay().setBackground(Color.WHITE);
			myPanel[i].setBorder(border);
			sm[i].getDisplay().setSize(myPanel[i].getWidth() - 2, myPanel[i].getHeight() - 2);
			sm[i].getDisplay().repaint();
			sm[i].getDisplay().getVisualization().repaint();
			myPanel[i].repaint();
		} //for
		
		//m_selectedModels.clear();
	} //removeHighlighting
	
	private void removeHighlightingOnPanel(int panelIndex) {
		myPanel[panelIndex].setBackground(Color.WHITE);
		sm[panelIndex].getDisplay().setBackground(Color.WHITE);
		myPanel[panelIndex].setBorder(border);
		sm[panelIndex].getDisplay().setSize(myPanel[panelIndex].getWidth() - 2, myPanel[panelIndex].getHeight() - 2);
		sm[panelIndex].getDisplay().repaint();
		sm[panelIndex].getDisplay().getVisualization().repaint();
		myPanel[panelIndex].repaint();
	} //removeHighlightingOnPanel
	
	private void clearBubblesetOverlays() {
		for (int i = 0; i < sm.length; i++) {
			((SMClickControlDelegate)sm[i].getNetworkViewer().getClickControl()).clearBubbleSets();
		} //for
	} //clearBubblesetOverlays
	
	public boolean isCurrentlyHighlighted() {
		return m_currentlyHighlighted;
	} //isCurrentlyHighlighted
	
	public void setCurrentlyHighlighted(boolean high) {
		m_currentlyHighlighted = high;
	} //setCurrentlyHighlighted
	
	public void initializeBlankDisplay() {
		
		// Remove old components (if necessary) from the grid
		for (int i = 0; i < m_numFiles; i++) {
			while (myPanel[i].getComponentCount() != 0) {
				myPanel[i].remove(0);
			} //while
		} //for
				
		// Repaint blank grid
		for (int i = 0; i < m_numFiles; i++) {
			myPanel[i].setBorder(border);
			myPanel[i].setBackground(Color.WHITE);
			myPanel[i].repaint();
		} //for
		
		// Display is now blank
		displayBlank = true;
				
	} //initializeBlankDisplay
	
	public JPanel[] getSmallMultiplesPanels() {
		return myPanel;
	} //getSmallMultiplesPanels
	
	public void setVisualization(Visualization vis, int index) {
		sm[index].getDisplay().setVisualization(vis);
		sm[index].getDisplay().repaint();
	} //setVisualization
		
	/**
	 * Whenever the dropdown list selection is modified, regenerate the contact maps with the newly selected layout
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == ddlLayouts) {
	        String layoutChoice = (String)ddlLayouts.getSelectedItem();
	        System.out.println(layoutChoice);
	        
	        if (displayBlank) {
	        	initializeBlankDisplay();
	        } else {
	        	for (int i = 0; i < m_numFiles; i++) {
	        		SmallMultiplePositionThread positionThread = new SmallMultiplePositionThread(i, layoutChoice);
	        		positionThread.run();
	        	} //for
	        	//initializeSmallMultiplesDisplay(layoutChoice);	
	        } //if-else
	        
	        if (!m_currentlyHighlighted) {
	        	m_lastPanelSelected = -1;	
	        } //if
	        
	        	        
		} else if (e.getSource() == btnPopulate) {
			initializeSmallMultiplesDisplay((String)ddlLayouts.getSelectedItem());
			
		} else if (e.getSource() == btnClear) {
			initializeBlankDisplay();
			
		} else if (e.getSource() == rbtnCompareSimilarities) {
			if (twoPanelsHighlighted()) {
				compareModels();
			} else {
				clearBubblesetOverlays();
			} //if-else
			
		} else if (e.getSource() == rbtnCompareDifferences) {
			if (twoPanelsHighlighted()) {
				compareModels();
			} else {
				clearBubblesetOverlays();
			} //if
			
		} //if-else
		

		
		
        
	} //actionPerformed
	
	/**
	 * Figure out which of the models is the largest / most complete
	 * 
	 * @return - the index of the most complete model
	 */
	public int findMostCompleteModel() {
		int largestModelIndex = 0;
		
		if (m_numFiles == 0) {
			return -1;
		} //if
		
		int largestModelSize = countLabels(sm[0].getDisplay().getVisualization());
		
		for (int i = 1; i < m_numFiles; i++) {
			if (countLabels(sm[i].getDisplay().getVisualization()) > largestModelSize) {
				largestModelSize = countLabels(sm[i].getDisplay().getVisualization());
				largestModelIndex = i;
			} //if
		} //for
		
		return largestModelIndex;
	} //findMostCompleteModel
	
	public Visualization getVisualization(int i) {
		return sm[i].getDisplay().getVisualization();
	} //getVisualization
	
	
	public SmallMultiple getMultiple(int i) {
		return sm[i];
	} //getMultiple

	
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
	
	/**
	 * Sort the models based on the computed similarity scores
	 * 
	 * @param largestModelIndex - index of the most complete model
	 * @param smScores - the computed similarity matrices
	 */

	public void sortModels(int largestModelIndex, SimilarityMatrices smScores) {
		if (largestModelIndex == -1) {
			return;
		} //if
		
		similarityScores = computeModelSimilarityScores(largestModelIndex, smScores);
		
		for (int i = 0; i < m_numFiles; i++) {
			for (int j = 0; j < (m_numFiles - (i + 1)); j++) {
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
		SmallMultiple temp = sm[index1];
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
	public int findNumberOfSmallMultiples(String directory) {
		int fileCount = 0;
		
		// Loop through all files, and count the number of .BNGL files
		File dir = new File(directory);
		
		// Only try to iterate across all files if we're passed in a directory
		if (dir.isDirectory()) {
			for (File child : dir.listFiles()) {
				if (isBNGLFile(child)) {
					fileCount++;
				} //if
			} //for
		} //if
		
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
	 * Class to load the Small Multiples in a Thread rather than sequentially
	 */
	public class SmallMultipleLoaderThread extends Thread {
		
		int m_currentMultiple;
		File m_child;
		String m_layoutChoice;
		
		public SmallMultipleLoaderThread(int currentMultiple, File child, String layoutChoice) {
			m_currentMultiple = currentMultiple;
			m_child = child;
			m_layoutChoice = layoutChoice;
		} //SmallMultipleThreadLoader (constructor)
		
		public void run() {
			// Loads the small multiple into memory, then adds the multiple to the registry
			sm[m_currentMultiple] = BackgroundFileLoader.loadContactMap(m_child.getAbsolutePath(), lookupPosition(m_layoutChoice), m_individualSize, m_view);
			m_contactMapRegistry.put(sm[m_currentMultiple].getNetworkViewer().getFilepath(), sm[m_currentMultiple]);
		} //run
		
	} //SmallMultipleLoaderThread (inner class)
	
	/*
	 * Class to update the positions of the Small Multiple nodes in a Thread rather than sequentially
	 */
	public class SmallMultiplePositionThread extends Thread {
		
		int m_currentMultiple;
		String m_layoutChoice;
		
		public SmallMultiplePositionThread(int i, String layoutChoice) {
			m_currentMultiple = i;
			m_layoutChoice = layoutChoice;
		} //SmallMultiplePositionThread (constructor)
		
		public void run() {
			sm[m_currentMultiple].setLayoutPath(lookupPosition(m_layoutChoice));
			sm[m_currentMultiple].getDisplay().getVisualization().run("layout");
			sm[m_currentMultiple].getDisplay().getVisualization().run("bubbleLayout");
		} //run
		
	} //SmallMultiplePositionThread
	
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
			
			sm[i].getDisplay().getVisualization().run("layout");
			
			fitToPanel2(i);
			
		} //for
		
	} //myResize (with dimension parameter provided)
	
	@Deprecated
	public void fitToPanel(int panelIndex) {
		//m_individualSize = findIndividualPanelSize(m_overallSize);
		
		double panelWidth = m_individualSize.width;
		double panelHeight = m_individualSize.height;
		
		double visualizationWidth = sm[panelIndex].getDisplay().getWidth();
		double visualizationHeight = sm[panelIndex].getDisplay().getWidth();
		
		double widthScale = panelWidth / visualizationWidth;
		double heightScale = panelHeight / visualizationHeight;
		
		double zoomScale = (widthScale < heightScale ? widthScale : heightScale); 
		
		Point2D.Double center = new Point2D.Double(panelWidth/2, panelHeight/2);
		
		sm[panelIndex].getDisplay().zoomAbs(center, zoomScale);
		
	} //fitToPanel
	
	public void fitToPanel2(int panelIndex) {
		
		int m_margin = 5;
		
        Visualization vis = sm[panelIndex].getDisplay().getVisualization();
        //Rectangle2D bounds = vis.getBounds(COMPONENT_GRAPH);
        Rectangle2D bounds = vis.getBounds(Visualization.ALL_ITEMS);
        GraphicsLib.expand(bounds, m_margin + (int)(1/sm[panelIndex].getDisplay().getScale()));
        DisplayLib.fitViewToBounds(sm[panelIndex].getDisplay(), bounds, 0);
		
	} //fitToPanel2
	
	public void setDisplay(Display d) {
		// TODO Auto-generated method stub
		
	} //setDisplay

} //SmallMultiplesPanel