package rulebender.core.prefuse;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.util.GraphicsLib;
import prefuse.util.PrefuseLib;
import prefuse.util.display.DisplayLib;
import prefuse.visual.VisualItem;
import rulebender.simulationjournaling.Message;
import rulebender.simulationjournaling.comparison.SimilarityMatrices;
import rulebender.simulationjournaling.model.BackgroundFileLoader;
import rulebender.simulationjournaling.model.SMClickControlDelegate;
import rulebender.simulationjournaling.model.SmallMultiple;
import rulebender.simulationjournaling.view.SmallMultiplesView;
import rulebender.simulationjournaling.view.TimelineView;

/**
 * This class defines the pane that contains an array of prefuse.Display objects,
 * each an overview for that BNGL model.  It is a subclass of the AWT JLayeredPane.
 * @author johnwenskovitch
*/

public class SmallMultiplesPanel extends JLayeredPane implements ActionListener /*TimelineItemSelectionListener*/ {
	
	private static final long serialVersionUID = -5595319590026393256L;
	private static String COMPONENT_GRAPH = "component_graph";
	
	// Directory that we're currently exploring
	private String m_directory = null;
	
	// Number of models in that directory
	private int m_numFiles;
	
	// Layout information
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
	
	// A directory selection button
	private JButton btnChooseDirectory;
	
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
	
	// The directory to begin at for the Directory Chooser
	private String m_chooserCurrentDirectory = null;
	
	// A HashMap that holds any pre-existing but not currently displayed
	// contact map prefuse visualizations. This is so that a new contact map
	// is not created every time a user selects a new bngl editor tab.
	private HashMap<String, SmallMultiple> m_contactMapRegistry;
		
	// A HashMap that holds the name of a position file and its path
	private HashMap<String, String> m_positionLookup;
	
	// List of currently selected model indices
	private ArrayList<Integer> m_selectedModels;
	
	// Size of models initialized to the sizes of their panels
	private boolean m_modelSizeInitialized;
	
	// When the interface first loads, only a single panel should get displayed
	private JPanel singlePanel;
	
	// Selected item from timeline tree view
	private String m_selectedItemFromTimelineTreeView;
	
	// Temp global variables for calculation speed results
	long startTime, endLoadTime, endSimilarityTime, endSortTime;
	
	
	/**
	 * Constructor:
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
		m_modelSizeInitialized = false;
		
		highlightBorder = new LineBorder(Color.RED, WIDE_BORDER);
		highlightBackgroundColor = Color.getHSBColor(0.1472f, 0.1472f, 1f);
		
		m_contactMapRegistry = new HashMap<String, SmallMultiple>();
		m_positionLookup = new HashMap<String, String>();
		
		m_selectedModels = new ArrayList<Integer>();
		
		
		/// Moved to initializeSmallMultiplesDisplay()
		
		// Figure out the number of multiples are in the folder
		//m_numFiles = findNumberOfSmallMultiples(m_directory);
		
		// Set the layout of the small multiples panel
		//selectLayout(m_numFiles);
		
		//m_individualSize = findIndividualPanelSize(m_overallSize);
		
		//sm = new SmallMultiple[m_numFiles];
		
		// Initialize the label and dropdown list for the layouts
		lblLayouts = new JLabel();
		ddlLayouts = new JComboBox();
				
		// Create the label text, populate the dropdown list (temp hardcoded layouts), and add the listener
		lblLayouts.setText("Choose layout: ");
		lblLayouts.setHorizontalAlignment(SwingConstants.RIGHT);
		
		//populatePositionDropdown(m_directory);
		populatePositionDropdownDefault();
		
		//ddlLayouts.setSelectedIndex(0);
		ddlLayouts.addActionListener(this);
		
		// Initialize the buttons and add listener
		btnPopulate = new JButton("Populate from Folder");
		btnClear = new JButton("Clear the Canvas");
		btnChooseDirectory = new JButton("Select a Directory to Explore");
		
		btnPopulate.addActionListener(this);
		btnClear.addActionListener(this);
		btnChooseDirectory.addActionListener(this);
		
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
		upperPanel.setLayout(new GridLayout(1, 5));
		
		/// Moved to initializeSmallMultiplesDisplay
		//lowerPanel.setLayout(new GridLayout(rows, cols, -1, -1));
		
		this.setLayout(new GridLayout(1, 1));
		
		/// Moved to initializeSmallMultiplesDisplay
		// Create the array of panels
		//myPanel = new JPanel[m_numFiles];
		//for (int i = 0; i < m_numFiles; i++) {
		//	myPanel[i] = new JPanel();
		//	myPanel[i].setName(""+i);
		//	myPanel[i].setToolTipText("testing");
		//} //for
		
		startTime = System.nanoTime();
		
		// Initialize the small multiples layout
		//initializeSmallMultiplesDisplay((String)ddlLayouts.getSelectedItem());
		initializeBlankDisplay();
		
		for (int i = 0; i < m_numFiles; i++) {
			lowerPanel.add(myPanel[i]);
		} //if
		
		// Add the dropdown list and label to the upper panel
		upperPanel.add(btnChooseDirectory);
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
				
		// Print system times for results
		printSystemTimes();
		
	} //SmallMultiplesPanel (constructor)
	
	private void populatePositionDropdownDefault() {
		ddlLayouts.addItem("-- Use default layouts --");
		m_positionLookup.put("-- Use default layouts --", null);		
		ddlLayouts.setSelectedIndex(0);
	} //popularePositionDropdownDefault
	
	/**
	 * Populates the dropdown list of position files
	 * 
	 * @param directory - The directory to search through for position files
	 */
	private void populatePositionDropdown(String directory) {
		// Start by clearing everything that's already in the DDL
		if (ddlLayouts.getItemCount() > 0) {
			ddlLayouts.removeAllItems();
		} //if
		
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
	
	/**
	 * Lookup the name of a position file given a filepath
	 * 
	 * @param selection - selected filepath
	 * 
	 * @return - position file name
	 */
	private String lookupPosition(String selection) {
		return m_positionLookup.get(selection);
	} //lookupPosition
	
	/**
	 * Determines if a file is a .POS file
	 * 
	 * @param child - File to check
	 * 
	 * @return - Whether or not the file parameter is a position file
	 */
	private boolean isPositionFile(File child) {
		String filepath = child.getPath();
		return ((filepath.substring(filepath.length()-4, filepath.length()).equals(".pos")) || (filepath.substring(filepath.length()-4, filepath.length()).equals(".POS")));
	} //isPositionFile
	
	/**
	 * Determines if a file is a .BNGL file
	 * 
	 * @param child - File to check
	 * 
	 * @return - Whether or not the file parameter is a BNGL file
	 */
	private boolean isBNGLFile(File child) {
		String filepath = child.getPath();
		return ((filepath.substring(filepath.length()-5, filepath.length()).equals(".bngl")) || (filepath.substring(filepath.length()-5, filepath.length()).equals(".BNGL")));
	} //isBNGLFile
	
	/**
	 * Load SmallMultiples from disk or from memory and place them into the SmallMultiplesPanel
	 * 
	 * @param directory - Directory to search for SmallMultiples
	 * @param layoutChoice - Layout to use on the SmallMultiples
	 */
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
	
	/**
	 * Populate a list of all model names in a directory
	 * 
	 * @param directory - The directory to search for models
	 * 
	 * @return - The list of all models
	 */
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
	
	/**
	 * Initializes the SmallMultiplesDisplay
	 *  - Generates adjacency and similarity matrices
	 *  - Sorts the models from most to least complete
	 * 
	 * @param layoutChoice - Layout to use on the SmallMultiples
	 */
	public void initializeSmallMultiplesDisplay(String layoutChoice) {
		
		// Wipe the display
		initializeBlankDisplay();
		
		// Figure out the number of multiples are in the folder
		m_numFiles = findNumberOfSmallMultiples(m_directory);
		
		// If there are no models in the selected directory, display a message; otherwise, show the models as normal 
		if (m_numFiles == 0) {
			
			noModelsFound();
			
		} else {
			// Set the layout of the small multiples panel
			selectLayout(m_numFiles);
		
			m_individualSize = findIndividualPanelSize(m_overallSize);
		
			// temp value until bug is fixed
			//m_individualSize = new Dimension(200,200);
		
			sm = new SmallMultiple[m_numFiles];
		
			singlePanel.removeAll();
			//singlePanel.add(new JLabel("Models loading; please wait!"));
			singlePanel.validate();
		
			singlePanel.repaint();
			lowerPanel.repaint();
			fullPanel.repaint();
			this.repaint();
		
			lowerPanel.removeAll();
			//fullPanel.remove(lowerPanel);
			//fullPanel.validate();
		
			//lowerPanel = new JPanel();
			
			startTime = System.nanoTime();
			
			lowerPanel.setLayout(new GridLayout(rows, cols, -1, -1));
				
			myPanel = new JPanel[m_numFiles];
			for (int i = 0; i < m_numFiles; i++) {
				myPanel[i] = new JPanel();
				myPanel[i].setName(""+i);
				//myPanel[i].setToolTipText("testing");
				myPanel[i].add(new JLabel("Model loading; please wait"));
				lowerPanel.add(myPanel[i]);
			} //for
		
		
			fullPanel.add(lowerPanel, BorderLayout.CENTER);
			fullPanel.repaint();
		
		
			// Load the small multiples if they don't exist in the array already
			populateSmallMultiplesDisplay(m_directory, layoutChoice);
		
			endLoadTime = System.nanoTime();
		
			String[] modelNames = populateModelNames(m_directory);
			matrixLayout = new SimilarityMatrices(modelNames, sm);		
				
			matrixLayout.fillSimilarityMatrices();
			matrixLayout.printSimilarityMatrices();

			endSimilarityTime = System.nanoTime();
		
			// Sort the small multiples here
			int largestIndex = findMostCompleteModel();
			sortModels(largestIndex, matrixLayout);
		
			endSortTime = System.nanoTime();
		
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
				} else {
					myPanel[i].remove(0);
					myPanel[i].add(sm[i].getDisplay());
					myPanel[i].validate();
				} //if-else
			
				myPanel[i].setBorder(border);
				myPanel[i].setBackground(Color.WHITE);
				myPanel[i].repaint();
				sm[i].getDisplay().repaint();
			
			} //for
			
			//Pass directory info to TimelineView
			sendDirectoryChoiceToTimelineView(m_directory);
			
		} //if-else
		
		
		
				
		//lowerPanel.validate();
		fullPanel.validate();
		
		lowerPanel.repaint();
		fullPanel.repaint();
		repaint();
		
		// Update the sizes of the JPanels and Displays
		myResize(m_overallSize);
		
		// Display is no longer blank
		displayBlank = false;
		
	} //initializeSmallMultiplesDisplay
	
	public void reloadModel(int modelIndex) {
		
		// Get path of model to reload
		String modelNameToReload = sm[modelIndex].getNetworkViewer().getFilepath();
		String layoutChoice = (String)ddlLayouts.getSelectedItem();
		
		// Clear the panel
		myPanel[modelIndex] = new JPanel();
		myPanel[modelIndex].setName(""+modelIndex);
		//myPanel[i].setToolTipText("testing");
		myPanel[modelIndex].add(new JLabel("Model loading; please wait"));
		myPanel[modelIndex].repaint();
				
		// Load the model
		SmallMultiple modelToReload = lookupDisplay(modelNameToReload);
		
		if (modelToReload == null) {
			// If the model hasn't already been loaded, load it (using the thread class), then store it in the registry
			SmallMultipleLoaderThread thread = new SmallMultipleLoaderThread(modelIndex, new File(modelNameToReload), layoutChoice);
			thread.run();
		} else {
			// If the model has already been loaded, retrieve it from the HashMap, and make sure it's using the right layout
			sm[modelIndex] = modelToReload;
			sm[modelIndex].setLayoutPath(lookupPosition(layoutChoice));
		} //if-else
		
		// Display the model
		sm[modelIndex].getDisplay().getVisualization().run("layout");
		sm[modelIndex].getDisplay().getVisualization().run("color");
		sm[modelIndex].getDisplay().getVisualization().run("bubbleLayout");
		sm[modelIndex].getDisplay().getVisualization().run("bubbleColor");
	
		if (myPanel[modelIndex].getComponentCount() == 0) {
			myPanel[modelIndex].add(sm[modelIndex].getDisplay());
		} else {
			myPanel[modelIndex].removeAll();
			myPanel[modelIndex].repaint();
			myPanel[modelIndex].add(sm[modelIndex].getDisplay());
		} //if-else
	
		myPanel[modelIndex].setBorder(border);
		myPanel[modelIndex].setBackground(Color.WHITE);
		myPanel[modelIndex].repaint();
		myPanel[modelIndex].validate();
		sm[modelIndex].getDisplay().repaint();
		
		myPanel[modelIndex].repaint();
		//lowerPanel.repaint();
		
		// Refresh the display
		//fullPanel.validate();
		
		//lowerPanel.repaint();
		//fullPanel.repaint();
		
		// Update the sizes of the JPanels and Displays
		myResize(m_overallSize);
		
	} //reloadModel
	
	/**
	 * Returns the last panel selected
	 * 
	 * @return - the last panel selected
	 */
	public int getLastPanelSelected() {
		return m_lastPanelSelected;
	} //getLastPanelSelected
	
	/** 
	 * Sets the last panel selected
	 * 
	 * @param sel - the last panel selected
	 */
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
	
	/**
	 * Determine if a contact map is stored in memory
	 * 
	 * @param filepath - filepath of the contact map we're looking for
	 * 
	 * @return - either the contact map if found, or null otherwise
	 */
	private SmallMultiple lookupDisplay(String filepath) {
	    return m_contactMapRegistry.get(filepath);
	} //lookupDisplay
	
	/**
	 * Highlight a panel (clearing all other highlights as an option)
	 * 
	 * @param panelIndex - The panel index to highlight
	 * @param resetHighlighting - Whether or not to reset all other highlights
	 */
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
	
	/**
	 * Highlight multiple panels (for when comparison is done via context menu)
	 * 
	 * @param panelIndex1 - Index of the first panel to highlight
	 * @param panelIndex2 - Index of the second panel to highlight
	 */
	public void highlightPanels(int panelIndex1, int panelIndex2) {
		highlightPanel(panelIndex1, true);
		highlightPanel(panelIndex2, false);
	} //highlightPanels
	
	/**
	 * Set panels to the highlighted collection
	 * 
	 * @param panels - set of panels to set to the collection
	 */
	public void setHighlightedPanels(ArrayList<Integer> panels) {
		// First clear the currently highlighted models
		m_selectedModels.clear();
		
		// Then add in the list of models to highlight
		addHighlightedPanels(panels);
	} //setHighlightedPanels
	
	/**
	 * Add panels to the highlighted collection
	 * 
	 * @param panels - set of panels to add to the collection
	 */
	public void addHighlightedPanels(ArrayList<Integer> panels) {
		// Add in the list of models to highlight
		for (int i = 0; i < panels.size(); i++) {
			addHighlightedPanel(panels.get(i));
		} //for
	} //addHighlightedPanels
	
	/**
	 * Add a single panel to the highlighted collection
	 * 
	 * @param panel - panel index to add to the collection
	 */
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
	
	public int findPanelFromModelName(String modelName) {
		int foundModel = -1;
		
		for (int i = 0; i < m_numFiles; i++) {
			String filename = ((SMClickControlDelegate)sm[i].getNetworkViewer().getClickControl()).getFileName();
			if (filename.equals(removeBNGLFromPath(modelName))) {
				foundModel = i;
				break;
			} //if
		} //for
		
		return foundModel;
	} //findPanelFromModelName
	
	private String removeBNGLFromPath(String path) {
		return path.substring(0, (path.length() - 5));
	} //removeBNGLFromPath
	
	/**
	 * Add a single panel to the highlighted list
	 * 
	 * @param panel - Index of the panel to add to the list
	 */
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
	
	/**
	 * Remove panels from the highlighted collection
	 * 
	 * @param panels - Set of panels to remove from the highlighted collection
	 */
	public void removeHighlightedPanels(ArrayList<Integer> panels) {
		while (panels.size() != 0) {
			removeHighlightedPanel(panels.get(0));
		} //while
	} //removeHighlightedPanels
	
	/**
	 * Remove a single panel from the highlighted collection
	 * 
	 * @param panel - Index of the panel to remove
	 */
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
	
	/**
	 * Get the collection of highlighted panels
	 * 
	 * @return - The collection of highlighted panels
	 */
	public ArrayList<Integer> getHighlightedPanels() {
		return m_selectedModels;
	} //getHightlightedPanels
	
	/**
	 * Empties the list of highlighted panels and clears all highlights
	 */
	public void removeAllSelections() {
		m_selectedModels.clear();
		for (int i = 0; i < sm.length; i++) {
			removeHighlightedPanel(i);
		} //for
	} //removeAllSelections
	
	/**
	 * Returns whether or not exactly two panels are highlighted (for comparison)
	 * 
	 * @return - whether or not exactly two panels are highlighted
	 */
	public boolean twoPanelsHighlighted() {
		return ((m_selectedModels.size() == 2) && (m_selectedModels.get(0) != -1) && (m_selectedModels.get(1) != -1));
	} //twoPanelsHighlighted
	
	/**
	 * Compare the models currently highlighted
	 */
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
	
	/**
	 * Set the compare similarities button to selected (if similarities comparison is done another way)
	 */
	public void selectCompareSimilaritiesButton() {
		rbtnCompareSimilarities.setSelected(true);
	} //selectCompareSimilaritiesButton
	
	/**
	 * Set the compare differences button to selected (if differences comparison is done another way)
	 */
	public void selectCompareDifferencesButton() {
		rbtnCompareDifferences.setSelected(true);
	} //selectCompareDifferencesButton
	
	/**
	 * Remove the highlighting from all panels
	 */
	public void removeHighlighting() {
		for (int i = 0; i < m_numFiles; i++) {
			removeHighlightingOnPanel(i);
		} //for
		
		//m_selectedModels.clear();
	} //removeHighlighting
	
	/**
	 * Remove the highlighting from a single panel
	 * 
	 * @param panelIndex - the index of the panel to highlight
	 */
	private void removeHighlightingOnPanel(int panelIndex) {
		myPanel[panelIndex].setBackground(Color.WHITE);
		sm[panelIndex].getDisplay().setBackground(Color.WHITE);
		myPanel[panelIndex].setBorder(border);
		sm[panelIndex].getDisplay().setSize(myPanel[panelIndex].getWidth() - 2, myPanel[panelIndex].getHeight() - 2);
		sm[panelIndex].getDisplay().repaint();
		sm[panelIndex].getDisplay().getVisualization().repaint();
		myPanel[panelIndex].repaint();
	} //removeHighlightingOnPanel
	
	/**
	 * Clear all BubbleSet comparison overlays
	 */
	private void clearBubblesetOverlays() {
		for (int i = 0; i < sm.length; i++) {
			((SMClickControlDelegate)sm[i].getNetworkViewer().getClickControl()).clearBubbleSets();
		} //for
	} //clearBubblesetOverlays
	
	/**
	 * Returns whether or not any panel is currently highlighted
	 * 
	 * @return - whether or not any panel is currently highlighted
	 */
	public boolean isCurrentlyHighlighted() {
		return m_currentlyHighlighted;
	} //isCurrentlyHighlighted
	
	/**
	 * Sets whether or not any panel is currently highlighted
	 * 
	 * @param high - whether or not any panel is currently highlighted
	 */
	public void setCurrentlyHighlighted(boolean high) {
		m_currentlyHighlighted = high;
	} //setCurrentlyHighlighted
	
	/**
	 * Initializes a blank display
	 */
	public void initializeBlankDisplay() {
		
		// Remove old components (if necessary) from the grid
		for (int i = 0; i < m_numFiles; i++) {
			while (myPanel[i].getComponentCount() != 0) {
				myPanel[i].remove(0);
			} //while
		} //for
		
		lowerPanel.removeAll();
		lowerPanel.validate();
				
		// We only want a single panel on the screen while it's a blank display
		rows = 1;
		cols = 1;
		
		// Paint blank grid with one panel
		singlePanel = new JPanel();
		singlePanel.setBorder(border);
		singlePanel.setBackground(Color.WHITE);
		singlePanel.setLayout(new GridLayout(1, 1, 0, 0));
		singlePanel.repaint();
		
		lowerPanel.setLayout(new GridLayout(rows, cols, -1, -1));
		lowerPanel.add(singlePanel);

		lowerPanel.validate();
		
		// Update the size of the single panel and Displays
		myResize(m_overallSize);
		
		
		/*
		for (int i = 0; i < m_numFiles; i++) {
			myPanel[i].setBorder(border);
			myPanel[i].setBackground(Color.WHITE);
			myPanel[i].repaint();
		} //for
		*/
		// Display is now blank
		displayBlank = true;
				
	} //initializeBlankDisplay
	
	public void noModelsFound() {
		initializeBlankDisplay();
		JLabel lbl = new JLabel("No models to display in this directory.", JLabel.CENTER);
		singlePanel.add(lbl);
	} //noModelsFound
	
	/**
	 * Returns the collection of JPanels storing the SmallMultiples
	 * 
	 * @return - the collection of JPanels
	 */
	public JPanel[] getSmallMultiplesPanels() {
		return myPanel;
	} //getSmallMultiplesPanels
	
	/**
	 * Loads a visualization into a panel
	 * 
	 * @param vis - The Visualization object to place in a panel
	 * @param index - The index of the panel to place the Visualization object
	 */
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
	        		if (layoutChoice != null) {
	        			SmallMultiplePositionThread positionThread = new SmallMultiplePositionThread(i, layoutChoice);
		        		positionThread.run();
	        		} else {
	        			System.err.println("layoutChoice was null... this shouldn't be happening...");
	        		} //if-else
	        		
	        	} //for
	        	//initializeSmallMultiplesDisplay(layoutChoice);	
	        } //if-else
	        
	        if (!m_currentlyHighlighted) {
	        	m_lastPanelSelected = -1;	
	        } //if
	        
	        for (int i = 0; i < m_numFiles; i++) {
	        	fitToPanel(i, sm[0].getDisplay().getVisualization().getBounds(Visualization.ALL_ITEMS));
	        } //for
	        

		} else if (e.getSource() == btnPopulate) {
			initializeSmallMultiplesDisplay((String)ddlLayouts.getSelectedItem());
			
		} else if (e.getSource() == btnClear) {
			initializeBlankDisplay();
			
		} else if (e.getSource() == btnChooseDirectory) {
			
			JFileChooser chooser;
			String choosertitle = "Select a Directory";
			
			chooser = new JFileChooser(); 
		    
			if (m_chooserCurrentDirectory == null) {
				chooser.setCurrentDirectory(new File(Platform.getLocation().toString()));
			} else {
				chooser.setCurrentDirectory(new File(m_chooserCurrentDirectory));
			} //if-else
						
		    chooser.setDialogTitle(choosertitle);
		    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		    // disable the "All files" option.
		    chooser.setAcceptAllFileFilterUsed(false);

		    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
		        System.out.println("getCurrentDirectory(): " +  chooser.getCurrentDirectory());
		        System.out.println("getSelectedFile() : " +  chooser.getSelectedFile());
		    
		        m_directory = chooser.getSelectedFile().toString();
				m_chooserCurrentDirectory = chooser.getCurrentDirectory().toString();
		        
		        // Load the models
		        //initializeSmallMultiplesDisplay((String)ddlLayouts.getSelectedItem());
		        initializeSmallMultiplesDisplay("-- Use default layouts --");
		        
		        // Load the position files dropdownlist
		        populatePositionDropdown(m_directory);
				ddlLayouts.setSelectedIndex(0);
				
				printSystemTimes();
		        
		    } else {
		        System.out.println("No Selection!");
		        return;
		    } //if-else
			
		    
			
			
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
	
	/**
	 * Returns the Visualization object in a given panel
	 * 
	 * @param i - The panel index of the Visualization object to retrieve
	 * 
	 * @return - The Visualization object in that panel
	 */
	public Visualization getVisualization(int i) {
		return sm[i].getDisplay().getVisualization();
	} //getVisualization
	
	/** 
	 * Returns the SmallMultiple object in a given panel
	 * 
	 * @param i - The panel index of the SmallMultiple object to retrieve
	 * 
	 * @return - The SmallMultiple object in that panel
	 */
	public SmallMultiple getMultiple(int i) {
		return sm[i];
	} //getMultiple

	/**
	 * Determine the number of objects (components and edges) in a model to check the size and determine the most complete model
	 * 
	 * @param vis - The Visualization object to check
	 * 
	 * @return - the number of components and edges in the model
	 */
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
			cols = 2;
			rows = 1;
		} else if (numFiles <= 4) {
			cols = 2;
			rows = 2;
		} else if (numFiles <= 6) {
			cols = 3;
			rows = 2;
		} else if (numFiles <= 8) {
			cols = 4;
			rows = 2;
		} else if (numFiles <= 9) {
			cols = 3;
			rows = 3;
		} else if (numFiles <= 12) {
			cols = 4;
			rows = 3;
		} else if (numFiles <= 15) {
			cols = 5;
			rows = 3;
		} else if (numFiles <= 16) {
			cols = 4;
			rows = 4;
		} else if (numFiles <= 20) {
			cols = 5;
			rows = 4;
		} else if (numFiles <= 24) {
			cols = 6;
			rows = 4;
		} else if (numFiles <= 25) {
			cols = 5;
			rows = 5;
		} else if (numFiles <= 30) {
			cols = 6;
			rows = 5;
		} else if (numFiles <= 35) {
			cols = 7;
			rows = 5;
		} else {
			cols = 6;
			rows = 6;
		} //if-else
	} //selectLayout
	
	/**
	 * Class to load the Small Multiples (from disk) in a Thread rather than sequentially
	 */
	public class SmallMultipleLoaderThread extends Thread {
		
		int m_currentMultiple;
		File m_child;
		String m_layoutChoice;
		
		/**
		 * Initialize the instance variables for the Thread
		 * 
		 * @param currentMultiple - the panel index of the multiple we're loading
		 * @param child - the file for the model we're loading
		 * @param layoutChoice - the layout for the SmallMultiple
		 */
		public SmallMultipleLoaderThread(int currentMultiple, File child, String layoutChoice) {
			m_currentMultiple = currentMultiple;
			m_child = child;
			m_layoutChoice = layoutChoice;
		} //SmallMultipleThreadLoader (constructor)
		
		/**
		 * Run the Thread
		 */
		public void run() {
			// Loads the small multiple into memory, then adds the multiple to the registry
			sm[m_currentMultiple] = BackgroundFileLoader.loadContactMap(m_child.getAbsolutePath(), lookupPosition(m_layoutChoice), m_individualSize, m_view);
			m_contactMapRegistry.put(sm[m_currentMultiple].getNetworkViewer().getFilepath(), sm[m_currentMultiple]);
		} //run
		
	} //SmallMultipleLoaderThread (inner class)
	
	/**
	 * Class to update the positions of the Small Multiple nodes in a Thread rather than sequentially
	 */
	public class SmallMultiplePositionThread extends Thread {
		
		int m_currentMultiple;
		String m_layoutChoice;
		
		/**
		 * Initialize the instance variables for the Thread
		 * 
		 * @param i - the panel index of the SmallMultiple we're re-laying-out
		 * @param layoutChoice - layout choice we're following
		 */
		public SmallMultiplePositionThread(int i, String layoutChoice) {
			m_currentMultiple = i;
			m_layoutChoice = layoutChoice;
		} //SmallMultiplePositionThread (constructor)
		
		/**
		 * Run the Thread
		 */
		public void run() {
			sm[m_currentMultiple].setLayoutPath(lookupPosition(m_layoutChoice));
			sm[m_currentMultiple].getDisplay().getVisualization().run("layout");
			sm[m_currentMultiple].getDisplay().getVisualization().run("bubbleLayout");
		} //run
		
	} //SmallMultiplePositionThread
	
	
	public void printSystemTimes() {
		System.out.println("Start Time:          " + startTime);
		System.out.println("End Load Time:       " + endLoadTime);
		System.out.println("End Similarity Time: " + endSimilarityTime);
		System.out.println("End Sort Time:       " + endSortTime);
		System.out.println();
		System.out.println("Model Load Time:     " + (endLoadTime - startTime));
		System.out.println("Similarity Time:     " + (endSimilarityTime - endLoadTime));
		System.out.println("Sort Time:           " + (endSortTime - endSimilarityTime));
		
		System.out.println("");
		
		//printModelSizes();
	} //printSystemTimes
	
	
	public void printModelSizes() {
		for (int i = 0; i < m_numFiles; i++) {
			int nodeCount = 0, edgeCount = 0;
			String modelName = "";
			
			Visualization vis = sm[i].getDisplay().getVisualization();
			
			// Get the model name
			modelName = ((SMClickControlDelegate)sm[i].getNetworkViewer().getClickControl()).getModelNameFromFilepath(sm[i].getNetworkViewer().getFilepath());
			
			// Count the number of nodes
			Iterator compareIter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
			while (compareIter.hasNext()) {
				VisualItem item = (VisualItem) compareIter.next();
				if (item.getString("molecule") != null) {
					nodeCount++;
				} //if
			} //while
			
			// Count the number of edges
			Iterator compareEdgeIter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.EDGES));
			while (compareEdgeIter.hasNext()) {
				Edge selectedEdge = (Edge) compareEdgeIter.next();
				
				VisualItem source = (VisualItem) selectedEdge.getSourceNode();
				VisualItem target = (VisualItem) selectedEdge.getTargetNode();
				
				if ((source.getString("molecule") == null) || (target.getString("molecule") == null)) {
					continue;
				} //if
				
				edgeCount++;
			} //while	
			
			
			System.out.println("Model name: " + modelName);
			System.out.println("Number of nodes: " + nodeCount);
			System.out.println("Number of edges: " + edgeCount);
			System.out.println();
			
		} //for
	} //printModelSizes
	
	
	/**
	 * Adam: There is a native resize method, but I needed to do more so I created
	 * this one.  This default version calls the parameterized version with
	 * the m_currentSize as input.
	 */
	public void myResize() {
		myResize(m_overallSize);
		
		// The initial panel size is now set, so we shouldn't automatically resize the models
		m_modelSizeInitialized = true;
	} //myResize
	
	/**
	 * Adam: There is a native resize method, but I needed to do more so I created
	 * this one.
	 * 
	 * Updates the size of the each pane
	 * 
	 * @param size - the size of the overall ViewPart
	 */
	public void myResize(Dimension size) {	
		
		// Update the m_overall size with new size information from the parent 
		m_overallSize = size;
		
		m_individualSize = findIndividualPanelSize(size);
		
		if ((rows == 1) && (cols == 1)) {
			m_individualSize = size;
		} //if
		
		for (int i = 0; i < m_numFiles; i++) {
			
			myPanel[i].setBounds(myPanel[i].getBounds().x, myPanel[i].getBounds().y, m_individualSize.width, m_individualSize.height);
			
			if (myPanel[i].getComponentCount() == 1) {
				((Display) myPanel[i].getComponent(0)).setSize(new Dimension(m_individualSize.width-BORDER_WIDTH*2, m_individualSize.height-BORDER_WIDTH*2));				
				((Display) myPanel[i].getComponent(0)).setBounds(BORDER_WIDTH, BORDER_WIDTH, m_individualSize.width-BORDER_WIDTH*2, m_individualSize.height-BORDER_WIDTH*2);
			} //if
			
			sm[i].getDisplay().getVisualization().run("layout");
			
		} //for
		
		
		// Initialize the size of the models to their panels on the first resize only
		if (!m_modelSizeInitialized) {
			for (int i = 0; i < m_numFiles; i++) {
				//fitToPanel(i, sm[0].getDisplay().getVisualization().getBounds(Visualization.ALL_ITEMS));
				
				// Temp variable to hold the bounds of sm[0]
				Rectangle2D bounds = sm[0].getDisplay().getVisualization().getBounds(COMPONENT_GRAPH);
				
				// Set bounds to the first multiple if they exist; otherwise set bounds to the current multiple
				if ((bounds.getHeight() != 0) && (bounds.getWidth() != 0)) {
					fitToPanel(i, bounds);
				} else {
					fitToPanel(i, sm[i].getDisplay().getVisualization().getBounds(COMPONENT_GRAPH));
				} //if
				
			} //for	
		} //if
		
	} //myResize (with dimension parameter provided)
	
	/**
	 * Fit the model to the size of the individual JPanel
	 * 
	 * @param panelIndex - index of the panel we're fitting to the panel
	 * @param bounds - the bounds passed in, presumably from the primary model
	 */
	public void fitToPanel(int panelIndex, Rectangle2D bounds) {
		
		int m_margin = 5;
		
        Visualization vis = sm[panelIndex].getDisplay().getVisualization();
        Rectangle2D mybounds = vis.getBounds(COMPONENT_GRAPH);
        //Rectangle2D bounds = vis.getBounds(Visualization.ALL_ITEMS);
        GraphicsLib.expand(bounds, m_margin + (int)(1/sm[panelIndex].getDisplay().getScale()));
        DisplayLib.fitViewToBounds(sm[panelIndex].getDisplay(), mybounds, 0);
        //Point2D center = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
        
        vis.run("layout");
        vis.run("color");
        vis.run("bubbleLayout");
        vis.run("bubbleColor");
		
	} //fitToPanel
	
	public void setDisplay(Display d) {
		// TODO Auto-generated method stub
		
	} //setDisplay

	/*
	@Override
	public void notify(String modelName) {
		// Highlight the panel based on the Timeline Tree item selected
		m_selectedItemFromTimelineTreeView = modelName;
		
		int foundModel = findPanelFromModelName(modelName);
		
		if (foundModel != -1) {
			addHighlightedPanel(foundModel);	
		} //if
		
	} //notify*/
	
	public void sendDirectoryChoiceToTimelineView(final String dir) {
		org.eclipse.swt.widgets.Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		        IWorkbenchWindow iw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		        
				TimelineView view = (TimelineView) getView(/*PlatformUI.getWorkbench().getActiveWorkbenchWindow()*/iw,  "rulebender.simulationjournaling.view.timelineview");
				
				if (view != null) {
					Message msg = new Message();
					msg.setType("DirectorySelection");
					msg.setDetails(dir);
					
					view.iGotAMessage(msg);
				} else {
					System.err.println("Could not find TimelineView to pass message.");
				} //if-else
		    } //run
		});
		
		
		
		/*
		// Find the TimelineView to pass a message to
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		        IWorkbenchWindow iw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		        
				TimelineView view = (TimelineView) getView(iw,  "rulebender.simulationjournaling.view.timelineview");
				
				if (view != null) {
					System.out.println("Found view!");
				} //if
		    } //run
		});
		*/
	} //sendDirectoryChoiceToTimelineView
	
	public static IViewPart getView(IWorkbenchWindow window, String viewId) {
	    IViewReference[] refs = window.getActivePage().getViewReferences();
	    for (IViewReference viewReference : refs) {
	        if (viewReference.getId().equals(viewId)) {
	            return viewReference.getView(true);
	        } //if
	    } //for
	    return null;
	} //getView

} //SmallMultiplesPanel