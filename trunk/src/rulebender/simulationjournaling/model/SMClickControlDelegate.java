package rulebender.simulationjournaling.model;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.part.FileEditorInput;

import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.StrokeAction;
import prefuse.controls.ControlAdapter;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.ColorLib;
import prefuse.util.PrefuseLib;
import prefuse.util.StrokeLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import rulebender.core.prefuse.SmallMultiplesPanel;
import rulebender.core.prefuse.collinsbubbleset.layout.BubbleSetLayout;
import rulebender.simulationjournaling.Message;
import rulebender.simulationjournaling.view.SmallMultiplesView;
import rulebender.simulationjournaling.view.TimelineView;

public class SMClickControlDelegate extends ControlAdapter implements ISelectionProvider, ActionListener {

	// Component graph string variable
	private static String COMPONENT_GRAPH = "component_graph";
	private static String AGG_COMP = "aggregates_component";
	private static String AGG = "aggregates";
	
	// The current selection
	private ISelection m_selection;
	
	// The selection listeners for this ISelectionProvider.  Objects are added
	// or removed by Eclipse.
	private ListenerList m_listeners;
	
	// The SmallMultiplesView (Eclipse RCP container; extends ViewPart) that this
	// click control delegate is associated with.
	private SmallMultiplesView m_view;
		
	// The path of the source bngl file.
	private String m_sourcePath;
	
	// The name of the model
	private String m_modelName;
	
	// The Visualization object that this ClickControlDelegate is attached to.
	private Visualization m_vis;
	
	// An aggregate table that represents the bubble sets.
	private AggregateTable bubbleTable;
	
	// An aggregate item entered in the AggregateTable
	private AggregateItem different;
	private AggregateItem similar;
	private AggregateItem itemHighlight;
	
	// Flags for whether the AggregateTable set is activated
	private boolean differentActivated;
	private boolean similarActivated;
	private boolean itemHighlightActivated;
	
	// List of model names
	private ArrayList<String> m_modelList;
	
	// Popup Menu items
	private JMenuItem removeOverlayMenuItem;
	private JMenuItem openModelMenuItem;
	private JMenuItem refreshModelMenuItem;
	private JMenuItem[] differencesList;
	private JMenuItem[] similaritiesList;
	
	// Popup Menu items (states)
	private JMenuItem showStatesMenuItem;
	private JMenuItem showAllStatesMenuItem;
	private JMenuItem hideStatesMenuItem;
	private JMenuItem hideAllStatesMenuItem;
	
	// Popup Menu items (compartments)
	private JMenuItem showCompartmentsMenuItem;
	private JMenuItem showAllCompartmentsMenuItem;
	private JMenuItem hideCompartmentsMenuItem;
	private JMenuItem hideAllCompartmentsMenuItem;
	
	// Variable to hold the most recent mouseclick info to pass along
	private MouseEvent mostRecentClick;
	
	// Whether or not this SmallMultiple is currently selected/highlighted
	private boolean m_currentlySelected;
	
	// Whether or not the states and/or compartments are shown in this SmallMultiple
	private boolean m_statesShown;
	private boolean m_compartmentsShown;
	
	// Variables to measure delay for single vs double click
	private int delay;
	private Timer timer;
	
	// Temp global variables for calculation speed results
	long startTime, endTime;
	
	/**
	 * Constructor:
	 *  - Sets instance variables for view, source path, and visualization
	 *  - Initializes instance variables for highlighting and selection
	 *  - Creates the bubble table for comparison highlights
	 *  - Initializes selection listeners
	 * 
	 * @param view - ViewPart that holds all small multiples
	 * @param sourcePath - Path to the BNGL file
	 * @param v - SmallMultiple Visualization object
	 */
	public SMClickControlDelegate(SmallMultiplesView view, String sourcePath, Visualization v) {
		m_view = view;
				
		m_sourcePath = sourcePath;
		m_modelName = getModelNameFromFilepath(m_sourcePath);
		
		m_currentlySelected = false;
		m_statesShown = false;
		
		delay = (Integer)Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval");
		//delay = 250;
		timer = new Timer(delay, this);
		
		// Set the local reference to the visualization that this controller is
		// attached to.
		m_vis = v;

		m_modelList = new ArrayList<String>();
		
		// Create the bubbletable that is going to be used for aggregates.
		bubbleTable = m_vis.addAggregates("bubbles");

		// Add the shape column to the table.
		bubbleTable.addColumn(VisualItem.POLYGON, float[].class);
		bubbleTable.addColumn("type", String.class);

		// For collins
		bubbleTable.addColumn("SURFACE", ArrayList.class);
		bubbleTable.addColumn("aggregate_threshold", double.class);
		bubbleTable.addColumn("aggreagate_negativeEdgeInfluenceFactor",	double.class);
		bubbleTable.addColumn("aggregate_nodeInfluenceFactor", double.class);
		bubbleTable.addColumn("aggreagate_negativeNodeInfluenceFactor",	double.class);
		bubbleTable.addColumn("aggregate_edgeInfluenceFactor", double.class);
		
		initializeBubbleTableProperties();
		
		differentActivated = false;
		similarActivated = false;
		itemHighlightActivated = false;
		
		// Selections
		m_listeners = new ListenerList();
		
		m_view.getSite().setSelectionProvider(this);
	
	} //SMClickControlDelegate
	
	/**
	 * Creates ColorAction for the comparison BubbleTable highlights, and runs the layout/color actions for the BubbleTable
	 */
	public void initializeBubbleTableProperties() {
	
		// Set the bubble stroke size
		StrokeAction aggStrokea = new StrokeAction("bubbles", StrokeLib.getStroke(0f));

		// Set the color of the stroke.
		ColorAction aStroke = new ColorAction("bubbles", VisualItem.STROKECOLOR, ColorLib.rgb(10, 10, 10));

		// A color palette. We define a color action later that depends on it.
		/*int[] palette = new int[] { 
			ColorLib.rgba(255, 180, 180, 150),
			ColorLib.rgba(190, 190, 255, 150), 
			ColorLib.rgba(244, 202, 228, 150),
			ColorLib.rgba(179, 226, 205, 150),
			ColorLib.rgba(255, 0, 0, 150)
		};*/

		// Set the fill color for the bubbles.
		//ColorAction aFill = new ColorAction("bubbles", VisualItem.FILLCOLOR, ColorLib.rgba(240, 50, 40, 150));
		//DataColorAction aFill = new DataColorAction("bubbles", "type", Constants.NOMINAL, VisualItem.FILLCOLOR, palette);
		// DataColorAction(java.lang.String group, java.lang.String dataField, int dataType, java.lang.String colorField, int[] palette) 
		ColorAction aFill;
		
		if (differentActivated) {
			aFill = new ColorAction("bubbles", VisualItem.FILLCOLOR, ColorLib.rgba(240, 50, 40, 150)); //red
			//aFill = new ColorAction("bubbles", VisualItem.FILLCOLOR, ColorLib.rgba(50, 50, 240, 150)); //blue
		} else if (similarActivated) {
			aFill = new ColorAction("bubbles", VisualItem.FILLCOLOR, ColorLib.rgba(50, 240, 240, 150)); //green
		} else if (itemHighlightActivated) {
			aFill = new ColorAction("bubbles", VisualItem.FILLCOLOR, ColorLib.rgba(255, 240, 0, 150)); //yellow
		} else {
			aFill = new ColorAction("bubbles", VisualItem.FILLCOLOR, ColorLib.rgba(150, 150, 150, 150)); //gray
		} //if-else
		
		// create an action list containing all color assignments
		ActionList color = new ActionList();

		color.add(new BubbleSetLayout("bubbles", "component_graph"));
		color.add(aggStrokea);
		color.add(aStroke);
		color.add(aFill);
		// color.add(new RepaintAction());

		ActionList layout = new ActionList();

		// layout.add(new AggregateBubbleLayout("bubbles"));
		layout.add(new BubbleSetLayout("bubbles", "component_graph"));
		layout.add(new RepaintAction());

		m_vis.putAction("bubbleLayout", layout);
		m_vis.putAction("bubbleColor", color);

		m_vis.run("bubbleLayout");
		m_vis.run("bubbleColor");
		
	} //initializeBubbleTableProperties
	
	/**
	 * Returns the BubbleTable for this SmallMultiple object
	 * 
	 * @return - BubbleTable
	 */
	public AggregateTable getBubbleTable() {
		return bubbleTable;
	} //getBubbleTable

	/**
	 * Sets the BubbleTable for this SmallMultiple object
	 * 
	 * @param _bubble - BubbleTable with VisualItems to highlight
	 */
	public void setBubbleTable(AggregateTable _bubble) {
		bubbleTable = _bubble;
		m_vis.run("bubbleLayout");
		m_vis.run("bubbleColor");
	} //setBubbleTable
	
	/**
	 * Returns the differences AggregateItem
	 * 
	 * @return
	 */
	public AggregateItem getDifferent() {
		return different;
	} //getContext

	/**
	 * Sets the differences AggregateItem
	 * 
	 * @param _con - differences to highlight
	 */
	public void setDifferent(AggregateItem _con) {
		different = _con;
		deactivateAggregates();
		differentActivated = true;
	} //setContext
	
	/**
	 * Returns the similarities AggregateItem
	 * 
	 * @return
	 */
	public AggregateItem getSimilar() {
		return similar;
	} //getSimilar
	
	/**
	 * Sets the similarities AggregateItem
	 * 
	 * @param _sim - similarities to highlight
	 */
	public void setSimilar(AggregateItem _sim) {
		similar = _sim;
		deactivateAggregates();
		similarActivated = true;
	} //setSimilar
	
	/**
	 * Returns the single item to highlight
	 * 
	 * @return
	 */
	public AggregateItem getItemHighlight() {
		return itemHighlight;
	} //getItemHighlight
	
	/**
	 * Sets the single item to highlight
	 * 
	 * @param _high - single item to highlight
	 */
	public void setItemHighlight(AggregateItem _high) {
		itemHighlight = _high;
		deactivateAggregates();
		itemHighlightActivated = true;
	} //setItemHighlight
	
	/**
	 * Empty the BubbleTable and run the layout/color actions to clear the overlay
	 */
	public void clearBubbleSets() {
		//center.setVisible(false);
		bubbleTable.clear();
		initializeBubbleTableProperties();
		m_vis.run("bubbleLayout");
		m_vis.run("bubbleColor");
		//center = null;
	} //clearBubbleSets

	/**
	 * Set all aggregate highlight flags to false, effectively telling them not to highlight
	 */
	public void deactivateAggregates() {
		differentActivated = false;
		similarActivated = false;
		itemHighlightActivated = false;
	} //deactivateAggregates
	
	/**
	 * Called when no VisualItem is hit on mouse click:
	 *  - Right click - creates the context menu depending on what is currently highlighted and what is currently selected
	 *  - Left click - checks Timer to see if it's a single or double click; if double, then the parent CMAPNetworkViewer handles the zoom action
	 * 
	 * @param e - The mouse click
	 */
	public void mouseClicked(MouseEvent e) {
		
		mostRecentClick = e;
		
		Display.getDefault().syncExec(new Runnable(){

			/**
			 * Pulls the SmallMultiplesView into context
			 */
			@Override
			public void run() 
			{
				try 
				{
					m_view.getSite().getPage().showView("rulebender.simulationjournaling.view.smallmultiplesview");
				}
				catch (PartInitException e2) {
					e2.printStackTrace();
				}
				
			}
		});
		
		/**
		 * Handles right-click action to generate context menu on this SmallMultiple 
		 */
		if ((e.getButton() == MouseEvent.BUTTON3) || (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown())) {
			
			JPopupMenu popupMenu = new JPopupMenu();
			
			SmallMultiplesPanel smPanel = m_view.getSmallMultiplesPanel();
			JPanel[] panels = smPanel.getSmallMultiplesPanels();
			int panelIndexClicked = Integer.parseInt(mostRecentClick.getComponent().getParent().getName());

			if (m_modelList.size() == 0) {
				getModelList(smPanel, panels);
			} //if
			
			// Create submenus for comparing similarities and differences
			JMenu similarityMenu = createSimilarModelListSubmenu(panelIndexClicked);
			JMenu differencesMenu = createDifferencesModelListSubmenu(panelIndexClicked);
			popupMenu.add(similarityMenu);
			popupMenu.add(differencesMenu);
			
			// Add an option to remove the overlay if the overlay is currently displayed
			if ((smPanel.isCurrentlyHighlighted())) {
				removeOverlayMenuItem = new JMenuItem("Remove comparison overlay");
				popupMenu.add(removeOverlayMenuItem);
				removeOverlayMenuItem.addActionListener(this);
			} //if
			
			// Show/hide states menu item
			if (m_statesShown) {
				JMenu m = new JMenu("Hide states...");
				
				hideStatesMenuItem = new JMenuItem("...on this model");
				hideStatesMenuItem.addActionListener(this);
				
				hideAllStatesMenuItem = new JMenuItem("...on all models");
				hideAllStatesMenuItem.addActionListener(this);
				
				m.add(hideStatesMenuItem);
				m.add(hideAllStatesMenuItem);
				
				popupMenu.add(m);
				
			} else {
				JMenu m = new JMenu("Show states...");
				
				showStatesMenuItem = new JMenuItem("...on this model");
				showStatesMenuItem.addActionListener(this);
				
				showAllStatesMenuItem = new JMenuItem("...on all models");
				showAllStatesMenuItem.addActionListener(this);
				
				m.add(showStatesMenuItem);
				m.add(showAllStatesMenuItem);
				
				popupMenu.add(m);
				
			} //if-else
			
			// Show/hide compartments menu item
			if (m_compartmentsShown) {
				JMenu m = new JMenu("Hide compartments...");
				
				hideCompartmentsMenuItem = new JMenuItem("...on this model");
				hideCompartmentsMenuItem.addActionListener(this);
				
				hideAllCompartmentsMenuItem = new JMenuItem("...on all models");
				hideAllCompartmentsMenuItem.addActionListener(this);
				
				m.add(hideCompartmentsMenuItem);
				m.add(hideAllCompartmentsMenuItem);
				
				popupMenu.add(m);
			
			} else {
				JMenu m = new JMenu("Show compartments...");
				
				showCompartmentsMenuItem = new JMenuItem("...on this model");
				showCompartmentsMenuItem.addActionListener(this);
				
				showAllCompartmentsMenuItem = new JMenuItem("...on all models");
				showAllCompartmentsMenuItem.addActionListener(this);
				
				m.add(showCompartmentsMenuItem);
				m.add(showAllCompartmentsMenuItem);
				
				popupMenu.add(m);
				
			} //if-else
			
			// Open model menu option
			openModelMenuItem = new JMenuItem("Open model");
			popupMenu.add(openModelMenuItem);
			openModelMenuItem.addActionListener(this);
			
			// Refresh model menu option
			// TODO: restore this function when you have the time
			//refreshModelMenuItem = new JMenuItem("Refresh model");
			//popupMenu.add(refreshModelMenuItem);
			//refreshModelMenuItem.addActionListener(this);
			
			
			popupMenu.show(e.getComponent(), e.getX(), e.getY());
			
			
		/**
		 * Handles the left-click action to check for single- or double-click
		 */
		} else {
			
			// Determine if we just had a double-click (single-click call is in actionPerformed)
			
			if (e.getClickCount() > 2) {
				return;
			} //if
			
			mostRecentClick = e;
			
			if (timer.isRunning()) {
				timer.stop();
				doubleLeftClick(mostRecentClick);
				
			} else {
				timer.restart();
			} //if-else
			
		} //if
				
	} //mouseClicked

	/**
	 * Single left click action is to highlight the current panel (or remove highlighting if it is currently highlighted)
	 * 
	 * @param e - The mouse click
	 */
	private void singleLeftClick(MouseEvent e) {
		final int panelIndexClicked = Integer.parseInt(mostRecentClick.getComponent().getParent().getName());
		System.out.println("Single click on panel " + panelIndexClicked);
		
		// Highlight panel clicked if not highlighted, de-highlight if already highlighted
		final SmallMultiplesPanel smPanel = m_view.getSmallMultiplesPanel();
		
		if (m_currentlySelected) {
			smPanel.removeHighlightedPanel(panelIndexClicked);
			m_currentlySelected = false;
		} else {
			smPanel.addHighlightedPanel(panelIndexClicked);
			m_currentlySelected = true;
		} //if-else
		
		
		// Find the TimelineView to pass a selection message to
		org.eclipse.swt.widgets.Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		        IWorkbenchWindow iw = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		        
				TimelineView view = (TimelineView) getView(/*PlatformUI.getWorkbench().getActiveWorkbenchWindow()*/iw,  "rulebender.simulationjournaling.view.timelineview");
				
				if (view != null) {
					Message msg = new Message();
					msg.setType("ModelSelection");
					
					String filepath = smPanel.getMultiple(panelIndexClicked).getNetworkViewer().getFilepath();
					String modelname = getModelNameFromFilepath(filepath);
					msg.setDetails(modelname);
					
					view.iGotAMessage(msg);
				} else {
					System.err.println("Could not find TimelineView to pass message.");
				} //if-else
		    } //run
		});
				
		
	} //singleLeftClick
	
	/**
	 * Sets this SmallMultiple as either currently selected or not
	 * 
	 * @param set - boolean choice between selected/not selected
	 */
	public void setCurrentlySelected(boolean set) {
		m_currentlySelected = set;
	} //setCurrentlySelected

	/**
	 * Just debug code to print that a double-click action was performed (rather than a single-click)
	 * 
	 * @param e - The mouse click
	 */
	private void doubleLeftClick(MouseEvent e) {
		int panelIndexClicked = Integer.parseInt(mostRecentClick.getComponent().getParent().getName());
		System.out.println("Double click on panel " + panelIndexClicked);
	} //doubleLeftClick

	/**
	 * Builds the list of all models in the directory
	 * 
	 * @param smPanel - the SmallMultiplesPanel holding the model
	 * @param panels - the set of JPanels inside the SmallMultiplesPanel
	 */
	private void getModelList(SmallMultiplesPanel smPanel, JPanel[] panels) {
		for (int i = 0; i < panels.length; i++) {
			String filepath = smPanel.getMultiple(i).getCMAPNetworkViewer().getFilepath();
			String modelname = getModelNameFromFilepath(filepath);
			//System.out.println(modelname);
			m_modelList.add(modelname);
		} //for
	} //getModelList
	
	/**
	 * Takes the filepath to the BNGL file and retrieves the model's name from that filepath
	 * 
	 * @param filepath - the path to the BNGL file
	 * 
	 * @return - the name of the model
	 */
	private String getModelNameFromFilepath(String filepath) {
		
		// Remove the .bngl extension
		String modelName = filepath.substring(0, (filepath.length() - 5));
		
		// Remove everything up until the filename by searching for the first / or \ from the right
		for (int i = modelName.length(); i > 0; i--) {
			if ((modelName.substring(i-1, i).equals("\\")) || ((modelName.substring(i-1, i).equals("/")))) {
				return modelName.substring(i, modelName.length());
			} //if
		} //for
		
		return modelName;
	} //getModelNameFromFilepath
	
	/**
	 * Iterates over the list of all models in the directory, adding all but the currently selected model to a context sub-menu for different comparison
	 * 
	 * @param modelIndexClicked - the index of the currently selected model
	 * 
	 * @return - the differences sub-menu generated
	 */
	private JMenu createDifferencesModelListSubmenu(int modelIndexClicked) {
		JMenu m = new JMenu("Compare differences to...");
		int count = 0;
				
		differencesList = new JMenuItem[m_modelList.size() - 1];
		
		// Loop through all models in the panel, add to the menu if it's not the panel currently clicked on 
		for (int i = 0; i < m_modelList.size(); i++) {
			if (i != modelIndexClicked) {
				differencesList[count] = new JMenuItem(m_modelList.get(i));
				differencesList[count].addActionListener(this);
				m.add(differencesList[count]);
				count++;
			} //if
		} //for
		
		return m;
	} //createDifferencesModelListSubmenu
	
	/**
	 * Iterates over the list of all models in the directoty, adding all but the currently selected model to a context sub-menu for similarity comparison
	 * 
	 * @param modelIndexClicked - the index of the currently selected model
	 * 
	 * @return - the similarities sub-menu generated
	 */
	private JMenu createSimilarModelListSubmenu(int modelIndexClicked) {
		JMenu m = new JMenu("Compare similarity to...");
		int count = 0;
				
		similaritiesList = new JMenuItem[m_modelList.size() - 1];
		
		// Loop through all models in the panel, add to the menu if it's not the panel currently clicked on 
		for (int i = 0; i < m_modelList.size(); i++) {
			if (i != modelIndexClicked) {
				similaritiesList[count] = new JMenuItem(m_modelList.get(i));
				similaritiesList[count].addActionListener(this);
				m.add(similaritiesList[count]);
				count++;
			} //if
		} //for
		
		return m;
	} //createDifferencesModelListSubmenu
	
	/**
	 * Deactivates highlighting on all SmallMultiplePanels
	 * 
	 * @param smPanel - the SmallMultiplesPanel that holds all SmallMultiples
	 * @param panels - the set of JPanels stored in the SmallMultiplesPanel
	 */
	private void clearPanels(SmallMultiplesPanel smPanel, JPanel[] panels) {
		for (int i = 0; i < panels.length; i++) {
			clearOverlay(i, smPanel);
			((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).deactivateAggregates();
		} //for
		
		// Highlights are now cleared
		smPanel.setCurrentlyHighlighted(false);
	} //clearPanels
	
	/**
	 * Calls a variety of functions depending on which item in the context menu was selected
	 * 
	 * @param e2 - ActionEvent that was triggered by a selection from the context menu
	 */
	public void actionPerformed(ActionEvent e2) {
		
		// Get the panels, and determine which panel was clicked on / selected
		SmallMultiplesPanel smPanel = m_view.getSmallMultiplesPanel();
		JPanel[] panels = smPanel.getSmallMultiplesPanels();
		
		int panelIndexClicked = Integer.parseInt(mostRecentClick.getComponent().getParent().getName());
		System.out.println("Panel index clicked: " + panelIndexClicked);
		
		// If we select the open model menu item
		if (e2.getSource() == openModelMenuItem) {
			openBNGLFile(smPanel, panelIndexClicked);
			
		} else if (e2.getSource() == refreshModelMenuItem) {
			reloadModel(smPanel, panelIndexClicked);
		    	
		} else if (e2.getSource() == removeOverlayMenuItem) {
			// If we select this menu item, we are deselecting model comparison
			clearPanels(smPanel, panels);
			m_currentlySelected = false;
			
		} else if (e2.getSource() == hideAllStatesMenuItem) {
			// If we select this menu item, we hide all states in all small multiples
			hideAllStates(smPanel, panels);
			
		} else if (e2.getSource() == hideStatesMenuItem) {
			// If we select this menu item, we hide the states in this small multiple only
			hideStates();
			
		} else if (e2.getSource() == showStatesMenuItem) {
			// If we select this menu item, we show the states in this small multiple only
			showStates();
			
		} else if (e2.getSource() == showAllStatesMenuItem) {
			// If we select this menu item, we show the states in all small multiples
			showAllStates(smPanel, panels);
			
		} else if (e2.getSource() == hideAllCompartmentsMenuItem) {
			// If we select this menu item, we are deselecting the visibility of all compartments
			hideAllCompartments(smPanel, panels);
			
		} else if (e2.getSource() == hideCompartmentsMenuItem) {
			// If we select this menu item, we are deselecting the visibility of compartments in this small multiple only
			hideCompartments();
			
		} else if (e2.getSource() == showAllCompartmentsMenuItem) {
			// If we select this menu item, we are deslecting the visibility of all compartments
			showAllCompartments(smPanel, panels);
			
		} else if (e2.getSource() == showCompartmentsMenuItem) {
			// If we select this menu item, we are deselecting the visibility of compartments in this small multiple only
			showCompartments();
			
		} else if (e2.getSource().getClass().getName().equals("javax.swing.JMenuItem")) {
			// If we select any JMenuItem item from the dropdown list
			
			// Clear all highlighting and start fresh
			clearPanels(smPanel, panels);
				
			// Update the last panel clicked
			smPanel.setLastPanelSelected(panelIndexClicked);
			
			// Get the visualization for the selected model and clear any highlights
			Visualization selectedModel = smPanel.getVisualization(panelIndexClicked);
			clearBubbleSets();

			// Try to get the panel index of the panel we're comparing against from the differences JMenuItem array
			int comparisonIndex = getDifferencesComparisonModelIndex(e2);
				
			// If we couldn't find a difference, then we found a similarity, so search that list
			if (comparisonIndex == -1) {
				int similarityIndex = getSimilaritiesComparisonModelIndex(e2);
					
				// If we couldn't find a similarity, then some unexpected error occurred... otherwise, compare the models, get the similarities, and display
				if (similarityIndex == -1) {
					Thread.dumpStack();						
				} else {
					startTime = System.nanoTime();
					handleSimilaritiesOption(smPanel, similarityIndex, selectedModel, panelIndexClicked);
					endTime = System.nanoTime();
					
					printSystemTimes();
					
					smPanel.selectCompareSimilaritiesButton();
					comparisonIndex = similarityIndex;
				} //if-else
									
			} else {
				// Compare the models, get the differences, and display
				startTime = System.nanoTime();
				handleDifferencesOption(smPanel, comparisonIndex, selectedModel, panelIndexClicked);
				endTime = System.nanoTime();
				
				printSystemTimes();
				
				smPanel.selectCompareDifferencesButton();
					
			} //if-else
				
			// If highlights are off, we can forget which panel was most recently clicked
			if (!(smPanel.isCurrentlyHighlighted())) {
				smPanel.setLastPanelSelected(-1);
			} else {
				// This SmallMultiple is currently selected
				m_currentlySelected = true;
				
				// Tell the SmallMultiplesPanel which models are currently selected
				ArrayList<Integer> selectedPanels = new ArrayList<Integer>();
				selectedPanels.add(comparisonIndex);					
				selectedPanels.add(panelIndexClicked);
				smPanel.setHighlightedPanels(selectedPanels);
			} //if
	
		} else {
			// The click occurs in whitespace, not on any item
			// doubleClick() call is in mouseClicked()
			timer.stop();
			singleLeftClick(mostRecentClick);
			
		} //if-else
		
	} //actionPerformed
	
	/**
	 * Hides all state nodes in this SmallMultiple 
	 */
	public void hideStates() {
		Iterator iter = m_vis.items("component_graph");
		
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();

			if (item instanceof NodeItem) {
				// hide the state nodes
				String type = item.getString("type");
				if (type != null && type.equals("state")) {
					item.setVisible(false);
				} //if
			
			} else if (item instanceof EdgeItem) {
				// hide edges linked to component nodes
				String displaymode = item.getString("displaymode");

				if (displaymode != null) {

					if (displaymode.equals("both") || displaymode.equals("component")) {
						// edge linked to component nodes
						item.setVisible(true);
					} else if (displaymode.equals("state")) {
						// edge linked to state nodes
						item.setVisible(false);
					} //if-else
				} //if
			} //if-else
		} //while
		
		// apply layout actions
		applyActions();
		
		// states are now hidden
		m_statesShown = false;
		
	} //hideStates
	
	/**
	 * Shows all state nodes in this SmallMultiple
	 */
	public void showStates() {
		Iterator iter = m_vis.items("component_graph");
		
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			
			if (item instanceof NodeItem) {
				// show state nodes
				String type = item.getString("type");
				if (type != null && type.equals("state")) {
					item.setVisible(true);
				} //if
			
			} else if (item instanceof EdgeItem) {
				// show edges linked to state nodes
				String displaymode = item.getString("displaymode");

				if (displaymode != null) {
					if (displaymode.equals("both") || displaymode.equals("state")) {
						// edge linked to state nodes
						item.setVisible(true);
					} else if (displaymode.equals("component")) {
						// edge linked to component nodes
						item.setVisible(false);
					} //if-else
				} //if
			} //if-else
		} //while
		
		// apply color/layout actions
		applyActions();
		
		// states are now shown
		m_statesShown = true;
		
	} //showStates
	
	/**
	 * Hides all state nodes in all SmallMultiples
	 * 
	 * @param smPanel - the SmallMultiplesPanel that holds all SmallMultiples
	 * @param panels - the set of JPanels stored in the SmallMultiplesPanel
	 */
	private void hideAllStates(SmallMultiplesPanel smPanel, JPanel[] panels) {
		for (int i = 0; i < panels.length; i++) {
			((SMClickControlDelegate)smPanel.getMultiple(i).getCMAPNetworkViewer().getClickControl()).hideStates();
		} //for
	} //hideAllStates
	
	/**
	 * Shows all state nodes in all SmallMultiples
	 * 
	 * @param smPanel - the SmallMultiplesPanel that holds all SmallMultiples
	 * @param panels - the set of JPanels stored in the SmallMultiplesPanel
	 */
	private void showAllStates(SmallMultiplesPanel smPanel, JPanel[] panels) {
		for (int i = 0; i < panels.length; i++) {
			((SMClickControlDelegate)smPanel.getMultiple(i).getCMAPNetworkViewer().getClickControl()).showStates();
		} //for		
	} //showAllStates
	
	/**
	 * Hides all compartments in all SmallMultiples
	 * 
	 * @param smPanel - the SmallMultiplesPanel that holds all SmallMultiples
	 * @param panels - the set of JPanels stored in the SmallMultiplesPanel
	 */
	private void hideAllCompartments(SmallMultiplesPanel smPanel, JPanel[] panels) {
		for (int i = 0; i < panels.length; i++) {
			((SMClickControlDelegate)smPanel.getMultiple(i).getCMAPNetworkViewer().getClickControl()).hideCompartments();
		} //for
	} //hideAllCompartments
	
	/**
	 * Shows all compartments in all SmallMultiples
	 * 
	 * @param smPanel - the SmallMultiplesPanel that holds all SmallMultiples
	 * @param panels - the set of JPanels stored in the SmallMultiplesPanel
	 */
	private void showAllCompartments(SmallMultiplesPanel smPanel, JPanel[] panels) {
		for (int i = 0; i < panels.length; i++) {
			((SMClickControlDelegate)smPanel.getMultiple(i).getCMAPNetworkViewer().getClickControl()).showCompartments();
		} //for	
	} //showAllCompartments
	
	/**
	 * Hides all compartment aggregates in this SmallMultiple
	 */
	private void hideCompartments() {
		
		Iterator iter = m_vis.items("compartments");
		
		// Iterate across all compartments, making them hidden
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			item.setVisible(false);
		} //while
		
		// apply color/layout actions
		applyActions();
		
		// compartments are now shown
		m_compartmentsShown = false;
		
	} //hideCompartments
	
	/**
	 * Shows all compartment aggregates in this SmallMultiple
	 */
	private void showCompartments() {
		
		Iterator iter = m_vis.items("compartments");
		
		// Iterate across all compartments, making them visible
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			item.setVisible(true);
		} //while
		
		// apply color/layout actions
		applyActions();
		
		// compartments are now hidden
		m_compartmentsShown = true;
		
	} //showCompartments
	
	/**
	 * Applies all color/layout actions on this SmallMultiple
	 */
	public void applyActions() {
		m_vis.run("color");
		m_vis.run("complayout");
		m_vis.run("compartmentlayout");
		m_vis.run("bubbleColor");
		m_vis.run("bubbleLayout");
	} //applyActions
	
	/**
	 * Some differences comparison was selected from the context menu, so we need to compute the differences between the models and then highlight those differences
	 * 
	 * @param smPanel - the SmallMultiplesPanel that holds all SmallMultiples
	 * @param comparisonIndex - the panel index of the model we're comparing against
	 * @param selectedModel - the model selected
	 * @param panelIndexClicked - the panel index of the model selected
	 */
	public void handleDifferencesOption(SmallMultiplesPanel smPanel, int comparisonIndex, Visualization selectedModel, int panelIndexClicked) {
		// First compare the most complete model to the selected Model 
		Visualization modelToCompareAgainst = smPanel.getVisualization(comparisonIndex);
		clearOverlay(comparisonIndex, smPanel);

		// Compare the models to get the collection of different components, molecules, and edges
		ArrayList<ArrayList<String>> differences = compareModels(selectedModel, modelToCompareAgainst, true); 

		// Add the differences to the 'different' group, then re-color
		//highlightDifferences(differences, modelToCompareAgainst, i, smPanel);
		highlightDifferences(differences, modelToCompareAgainst, comparisonIndex, smPanel/*, "center"*/);

		// Now do everything in the opposite direction using the context bubbleset
		clearOverlay(panelIndexClicked, smPanel);
		differences.clear();
		differences = compareModels(modelToCompareAgainst, selectedModel, true);
		highlightDifferences(differences, selectedModel, panelIndexClicked, smPanel/*, "context"*/);
	
		// Highlights are currently on
		smPanel.setCurrentlyHighlighted(true);
		smPanel.highlightPanels(panelIndexClicked, comparisonIndex);
		smPanel.repaint();
	} //handleDifferencesOption
	
	/**
	 * Some similarities comparison was selected from the context menu, so we need to compute the similarities between the models and then highlight those similarities
	 * 
	 * @param smPanel - the SmallMultiplesPanel that holds all SmallMultiples
	 * @param comparisonIndex - the panel index of the model we're comparing against
	 * @param selectedModel - the model selected
	 * @param panelIndexClicked - the panel index of the model selected
	 */
	public void handleSimilaritiesOption(SmallMultiplesPanel smPanel, int comparisonIndex, Visualization selectedModel, int panelIndexClicked) {
		// First compare the most complete model to the selected Model 
		Visualization modelToCompareAgainst = smPanel.getVisualization(comparisonIndex);
		clearOverlay(comparisonIndex, smPanel);

		// Compare the models to get the collection of similar components, molecules, and edges
		ArrayList<ArrayList<String>> similarities = compareModels(selectedModel, modelToCompareAgainst, false); 

		// Add the similarities to the group, then re-color
		highlightSimilarities(similarities, modelToCompareAgainst, comparisonIndex, smPanel);

		// Don't need to recompute similarities (because they're the same), so just highlight in the second model
		clearOverlay(panelIndexClicked, smPanel);
		highlightSimilarities(similarities, selectedModel, panelIndexClicked, smPanel);
			
		// Highlights are currently on
		smPanel.setCurrentlyHighlighted(true);
		smPanel.highlightPanels(panelIndexClicked, comparisonIndex);
		smPanel.repaint();
		
	} //handleSimilaritiesOption
	
	/**
	 * The openModel option was selected from the context menu, so we need to switch perspectives and open the model
	 * 
	 * @param smPanel - the SmallMultiplesPanel that holds all SmallMultiples
	 * @param panelIndexClicked - the panel index of the model selected
	 */
	private void openBNGLFile(SmallMultiplesPanel smPanel, int panelIndexClicked) {
		
		// Switch to the default perspective
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					PlatformUI.getWorkbench().showPerspective("rulebender.perspective", PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				} catch (WorkbenchException e) {
					e.printStackTrace();
				} //try-catch
				
			} //run				
		});
		
		// Find the file that needs to be opened
	    IWorkspace workspace = ResourcesPlugin.getWorkspace();  
	    IPath location = Path.fromOSString(smPanel.getMultiple(panelIndexClicked).getNetworkViewer().getFilepath());
	    IFile iFile = workspace.getRoot().getFileForLocation(location);
	    
		final IEditorInput editorInput = new FileEditorInput(iFile);
		
		// clumsy hack
		IWorkbenchPage page_last = null;
	    
	    for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
	        for (IWorkbenchPage page : window.getPages()) {
	            page_last = page;
	        } //for
	    } //for
	    
	    final IWorkbenchPage p = page_last;
		
	    // Open the file using the BNGL editor
		Display.getDefault().syncExec(new Runnable() {

			@Override
			public void run() {
				try {
					p.openEditor(editorInput, "rulebender.editors.bngl");
				} catch (PartInitException e) {
					e.printStackTrace();
				} //try-catch
				
			} //run				
		});

		
	} //openBNGLFile

	/**
	 * Reload the model in the selected panel (for use if the model fails to load the first time, or to restore the model to its original state)
	 * 
	 * @param smPanel - The small multiples panel
	 * @param panelIndexClicked - The panel selected to reload
	 */
	private void reloadModel(SmallMultiplesPanel smPanel, int panelIndexClicked) {
		smPanel.reloadModel(panelIndexClicked);	
	} //if
	
	/**
	 * Checks to see if an item in the differences context sub-menu was selected
	 * 
	 * @param e2 - The ActionEvent that might be on the differences list
	 * 
	 * @return - The model index of the selected item in the context menu
	 */
	private int getDifferencesComparisonModelIndex(ActionEvent e2) {
		
		// Loop through all menu items, looking for the model name of the JMenuItem selected
		for (int i = 0; i < differencesList.length; i++) {
			if (e2.getSource() == differencesList[i]) {
				
				// Loop through all models, looking for the model index of the selected menu item
				for (int j = 0; j < m_modelList.size(); j++) {
					if (differencesList[i].getText().equals(m_modelList.get(j))) {
						return j;
					} //if
				} //for
			
			} //if
		} //for
		
		return -1;
	} //getComparisonIndex
	
	/**
	 * Checks to see if an item in the similarities context sub-menu was selected
	 * 
	 * @param e2 - The ActionEvent that might be on the similarities list
	 * 
	 * @return - The model index of the selected item in the context menu
	 */
	private int getSimilaritiesComparisonModelIndex(ActionEvent e2) {
		
		// Loop through all menu items, looking for the model name of the JMenuItem selected
		for (int i = 0; i < similaritiesList.length; i++) {
			if (e2.getSource() == similaritiesList[i]) {
				
				// Loop through all models, looking for the model index of the selected menu item
				for (int j = 0; j < m_modelList.size(); j++) {
					if (similaritiesList[i].getText().equals(m_modelList.get(j))) {
						return j;
					} //if
				} //for
			
			} //if
		} //for
		
		return -1;
	} //getComparisonIndex
	
	/**
	 * Looks for similarities/differences between two Visualization objects
	 * 
	 * @param selectedModel - The Visualization object of the model selected
	 * @param compareModel - The Visualization object of the model to compare against
	 * @param findingDifferences - Flag as to whether we're looking for similarities or differences
	 * 
	 * @return - The list of similarities/differences between the two models
	 */
	private ArrayList<ArrayList<String>> compareModels(Visualization selectedModel, Visualization compareModel, boolean findingDifferences) {
		
		// Compare the components, molecules, and edges between the two models, storing the differences
		ArrayList<String> components = compareComponents(selectedModel, compareModel, findingDifferences);
		ArrayList<String> molecules = compareMolecules(selectedModel, compareModel, findingDifferences);
		ArrayList<String> edges = compareEdges(selectedModel, compareModel, findingDifferences);
	
		// Create the structure to hold all of the differences, add all of the differences, and return
		ArrayList<ArrayList<String>> items = new ArrayList<ArrayList<String>>();
		
		items.add(components);
		items.add(molecules);
		items.add(edges);
		
		//printDifferences(differentComponents);
		//printDifferences(differentEdges);
		
		return items;
	} //compareModels
	
	/**
	 * Looks for similarities/differences between two Visualization objects, considering components only
	 * 
	 * @param selectedModel - The Visualization object of the model selected
	 * @param compareModel - The Visualization object of the model to compare against
	 * @param findingDifferences - Flag as to whether we're looking for similarities or differences
	 * 
	 * @return - The list of similarities/differences between the two models
	 */
	private ArrayList<String> compareComponents(Visualization selectedModel, Visualization compareModel, boolean findingDifferences) {
		
		ArrayList<MoleculeCounter> compareTracker = new ArrayList<MoleculeCounter>();
		ArrayList<MoleculeCounter> selectedTracker = new ArrayList<MoleculeCounter>();
		
		ArrayList<String> components = new ArrayList<String>();
		Boolean itemFound = false;
		
		// Loop through the compareModel, and make note of anything that's not in the selectedModel
		
		// Start by looping through the nodes of compareModel
		Iterator compareIter = compareModel.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
		
		while (compareIter.hasNext()) {
			VisualItem item = (VisualItem) compareIter.next();
			
			if (item.getString("molecule") != null) {
				StringBuilder compareStringBuilder = buildStringRepresentation(item);
				
				String compareMolecule = item.getString("molecule");
				String compareComponent = item.getString("component");
				String compareState = item.getString(VisualItem.LABEL);
				String id = "0";
				Boolean found = false;
				
				if (compareMolecule == null) {
					compareMolecule = "null";
				} //compareMolecule
				
				if (compareComponent == null) {
					compareComponent = "null";
				} //if
				
				if (compareState == null) {
					compareState = "null";
				} //if
				
				// Keep track of how many of each molecule/component pair have been found
				for (int i = 0; i < compareTracker.size(); i++) {
					if ((compareMolecule.equals(compareTracker.get(i).getMolecule())) && (compareComponent.equals(compareTracker.get(i).getComponent())) && (compareState.equals(compareTracker.get(i).getState()))) {
						MoleculeCounter tempMol = new MoleculeCounter(compareMolecule, compareComponent, compareState, compareTracker.get(i).getCount() + 1);

						//tracker.get(i).setCount(tracker.get(i).getCount() + 1);
						id = Integer.toString(compareTracker.get(i).getCount() + 1);

						compareTracker.remove(i);
						compareTracker.add(tempMol);
						
						found = true;
						break;
					} //if
				} //for
				
				// If we find a new molecule/component pair, add a new item to the array
				if (!found) {
					MoleculeCounter tempMol = new MoleculeCounter(compareMolecule, compareComponent, compareState, 0);
					compareTracker.add(tempMol);
					id = Integer.toString(0);
				} //if
				
				compareStringBuilder.append("-");
				compareStringBuilder.append(id);
				String compareString = compareStringBuilder.toString();			
				
				
				// Now loop through the selectedModel and look for similarities/differences
				Iterator selectedIter = selectedModel.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
				
				while (selectedIter.hasNext()) {
					//Reset itemFound flag
					itemFound = false;
					
					VisualItem selectedItem = (VisualItem) selectedIter.next();
				
					if (selectedItem.getString("molecule") != null) {
						
						StringBuilder selectedStringBuilder = buildStringRepresentation(selectedItem);
					
						String selectedMolecule = selectedItem.getString("molecule");
						String selectedComponent = selectedItem.getString("component");
						String selectedState = selectedItem.getString(VisualItem.LABEL);
						String selectedID = "0";
						Boolean selectedFound = false;
						
						if (selectedMolecule == null) {
							selectedMolecule = "null";
						} //if
						
						if (selectedComponent == null) {
							selectedComponent = "null";
						} //if
						
						if (selectedState == null) {
							selectedState = "null";
						} //if
						
						// Keep track of how many of each molecule/component pair have been found
						for (int i = 0; i < selectedTracker.size(); i++) {
							if ((selectedMolecule.equals(selectedTracker.get(i).getMolecule())) && (selectedComponent.equals(selectedTracker.get(i).getComponent())) && (selectedState.equals(selectedTracker.get(i).getState()))) {
								MoleculeCounter tempMol = new MoleculeCounter(selectedMolecule, selectedComponent, selectedState, selectedTracker.get(i).getCount() + 1);

								//tracker.get(i).setCount(tracker.get(i).getCount() + 1);
								selectedID = Integer.toString(selectedTracker.get(i).getCount() + 1);

								selectedTracker.remove(i);
								selectedTracker.add(tempMol);
								
								selectedFound = true;
								break;
							} //if
						} //for
						
						// If we find a new molecule/component pair, add a new item to the array
						if (!selectedFound) {
							MoleculeCounter tempMol = new MoleculeCounter(selectedMolecule, selectedComponent, selectedState, 0);
							selectedTracker.add(tempMol);
							selectedID = Integer.toString(0);
						} //if
						
						selectedStringBuilder.append("-");
						selectedStringBuilder.append(selectedID);
						String selectedString = selectedStringBuilder.toString();			
						
						
						// If an item is found in both the compare model and the selected model, then add it to the list if we're looking for similarities, otherwise skip it.
						if (compareString.equals(selectedString)) {
							if (!findingDifferences) {
								//components.add(buildStringRepresentation(item).toString());
								components.add(compareString);
							} else {
								itemFound = true;
								break;	
							} //if-else
							
						} //if
					
					} //if
						
				} //while (selectedIter)
					
				selectedTracker.clear();
				
				// If an item hasn't been found and we're looking for differences, add it to the list of components
				if (!itemFound && findingDifferences) {
					components.add(compareString);
				} //if
							
			} //if
			
		} //while (compareIter)
		
		return components;
		
	} //compareComponents
	
	/**
	 * Looks for similarities/differences between two Visualization objects, considering molecules only
	 * 
	 * @param selectedModel - The Visualization object of the model selected
	 * @param compareModel - The Visualization object of the model to compare against
	 * @param findingDifferences - Flag as to whether we're looking for similarities or differences
	 * 
	 * @return - The list of similarities/differences between the two models
	 */
	private ArrayList<String> compareMolecules(Visualization selectedModel, Visualization compareModel, boolean findingDifferences) {
		
		ArrayList<String> molecules = new ArrayList<String>();
		
		// Now let's just take care of the molecules
		Iterator moleculeIter = compareModel.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
				
		while (moleculeIter.hasNext()) {
			// When we come across a new molecule, count how many binding sites it has in each model.  If there are that many in the differentComponents, then the whole molecule is unique 
			VisualItem item = (VisualItem) moleculeIter.next();
					
			if (item.getString("molecule") != null) {
				String molecule = item.getString("molecule");
				int count = 0, compArrayCount = 0;
						

						
						
			} //if
					
					
					
		} //while
		
		return molecules;
		
	} //compareMolecules
	
	/**
	 * Looks for similarities/differences between two Visualization objects, considering edges only
	 * 
	 * @param selectedModel - The Visualization object of the model selected
	 * @param compareModel - The Visualization object of the model to compare against
	 * @param findingDifferences - Flag as to whether we're looking for similarities or differences
	 * 
	 * @return - The list of similarities/differences between the two models
	 */
	private ArrayList<String> compareEdges(Visualization selectedModel, Visualization compareModel, boolean findingDifferences) {
		
		ArrayList<String> edges = new ArrayList<String>();
		boolean itemFound = false;
		
		// Finally let's loop through the edges
		Iterator compareEdgeIter = compareModel.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.EDGES));
		
		while (compareEdgeIter.hasNext()) {
			String sourceString, targetString;
			
			// Get the two endpoints of each edge and find their labels
			Edge edge = (Edge) compareEdgeIter.next();
			
			VisualItem source = (VisualItem) edge.getSourceNode();
			VisualItem target = (VisualItem) edge.getTargetNode();
			
			if ((source.getString("molecule") == null) || (target.getString("molecule") == null)) {
				continue;
			} //if
			
			// Correction that may not be correct... skip edges in the same molecule unless they bind to themselves
			// TODO: find out if this is valid
			if (source.getString("molecule").equals(target.getString("molecule"))) {
				if (!(source.getString(VisualItem.LABEL).equals(target.getString(VisualItem.LABEL)))) {
					continue;
				} //if
			} //if
			
			// Build string representations of each molecule/component/state for comparison
			sourceString = buildStringRepresentation(source).toString();
			targetString = buildStringRepresentation(target).toString();
			
			// Iterate over all of the edges in the selected model
			Iterator selectedEdgeIter = selectedModel.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.EDGES));
			
			while (selectedEdgeIter.hasNext()) {
				//Reset itemFound flag
				itemFound = false;
				
				String selectedSourceString, selectedTargetString;
				
				// Get the two endpoints of each edge and find their labels
				Edge selectedEdge = (Edge) selectedEdgeIter.next();
				
				VisualItem selectedSource = (VisualItem) selectedEdge.getSourceNode();
				VisualItem selectedTarget = (VisualItem) selectedEdge.getTargetNode();
				
				if ((selectedSource.getString("molecule") == null) || (selectedTarget.getString("molecule") == null)) {
					continue;
				} //if
				
				// Correction that may not be correct... skip edges in the same molecule unless they bind to themselves
				// TODO: find out if this is valid
				if (selectedSource.getString("molecule").equals(selectedTarget.getString("molecule"))) {
					if (!(selectedSource.getString(VisualItem.LABEL).equals(selectedTarget.getString(VisualItem.LABEL)))) {
						continue;
					} //if
				} //if
				
				// Build string representations of each molecule/component/state for comparison
				selectedSourceString = buildStringRepresentation(selectedSource).toString();
				selectedTargetString = buildStringRepresentation(selectedTarget).toString();
				
				// Now let's check to see if the edges are the same... if they are, then if we're looking for similarities, then add the edge, otherwise skip it
				if ((sourceString.equals(selectedSourceString)) && (targetString.equals(selectedTargetString))) {
					if (!findingDifferences) {
						edges.add(sourceString + "|" + targetString);
					} else {
						itemFound = true;
						break;	
					} //if-else
				} //if
				
				
			} //while
			
			// If an item hasn't been found and we're looking for differences, add it to the list of edges
			if (!itemFound && findingDifferences) {
				edges.add(sourceString + "|" + targetString);
			} //if
						
		} //while
		
		return edges;
		
	} //compareEdges
	
	/**
	 * Takes a VisualItem and returns a StringBuilder representation listing the molecule, component, and state of that VisualItem
	 * 
	 * @param item - The VisualItem we want to build a StringBuilder representation for
	 * 
	 * @return - The StringBuilder representing the VisualItem parameter
	 */
	private StringBuilder buildStringRepresentation(VisualItem item) {
		StringBuilder temp = new StringBuilder();
		
		String molecule = item.getString("molecule");
		String component = item.getString("component");
		String state = item.getString(VisualItem.LABEL);
		
		if (molecule == null) {
			molecule = "null";
		} //if
		
		if (component == null) {
			component = "null";
		} //if
		
		if (state == null) {
			state = "null";
		} //if
		
		if (item.getString("component") == null) {
			temp.append(molecule);
			temp.append("(");
			temp.append(state);
			temp.append(")");
		} else {
			temp.append(molecule);
			temp.append("(");
			temp.append(component);
			temp.append(").");
			temp.append(state);
		} //if-else
		
		return temp/*.toString()*/;
	} //buildStringRepresentation
	
	/**
	 * Prints the list of similarities/differences to the console (for debug, currently unused)
	 * 
	 * @param differences - The list of similarities/differences that gets printed
	 */
	@SuppressWarnings("unused")
	private void printDifferences(ArrayList<String> differences) {
		System.out.println("-----------------------------------------------");
		for (int i = 0; i < differences.size(); i++) {
			System.out.println(differences.get(i));
		} //for
		System.out.println("-----------------------------------------------");
	} //printDifferences
	
	/**
	 * Clear the different objects from the given visualization
	 * 
	 * @param refresh - Whether or not to refresh the visualization
	 * @param vis - The visualization to clear
	 */
	private void clearOverlay(int modelIndex, SmallMultiplesPanel smPanel) {		
		
		((SMClickControlDelegate)smPanel.getMultiple(modelIndex).getNetworkViewer().getClickControl()).clearBubbleSets();
		smPanel.removeHighlighting();
		//smPanel.setCurrentlyHighlighted(false);
	
		
		/*
		
		// Old method pre-Collins
		
		// Clear any selected visual items.
		TupleSet selectedSet = vis.getFocusGroup("different");
		
		if(selectedSet != null) {	
			selectedSet.clear();
			vis.run("color");
		} //if
		
		*/
		
	} //clearOverlay
	
	/**
	 * Highlights similarities on the provided model
	 * 
	 * @param similarities - The list of similarities to highlight
	 * @param vis - The Visualization object we're highlighting
	 * @param modelIndex - The panel index of the model being highlighted
	 * @param smPanel - The SmallMultiplesPanel that contains the collection of SmallMultiples
	 */
	private void highlightSimilarities(ArrayList<ArrayList<String>> similarities, Visualization vis, int modelIndex, SmallMultiplesPanel smPanel) {
		// Get the bubbleTable for the model
		AggregateTable modelBubbleTable = ((SMClickControlDelegate)smPanel.getMultiple(modelIndex).getNetworkViewer().getClickControl()).getBubbleTable();
		AggregateItem modelSimilarities;
		
		modelSimilarities = ((SMClickControlDelegate)smPanel.getMultiple(modelIndex).getNetworkViewer().getClickControl()).getSimilar();
		modelSimilarities = (AggregateItem) modelBubbleTable.addItem();
		
		highlightComponentsBubbleSet(similarities.get(0), vis, modelBubbleTable, modelSimilarities);
		highlightMoleculesBubbleSet(similarities.get(1), vis, modelBubbleTable, modelSimilarities);
		highlightEdgesBubbleSet(similarities.get(2), vis, modelBubbleTable, modelSimilarities);
		
		modelSimilarities.set("type", "similar");
		modelSimilarities.setVisible(true);
		
		((SMClickControlDelegate)smPanel.getMultiple(modelIndex).getNetworkViewer().getClickControl()).setSimilar(modelSimilarities);
		((SMClickControlDelegate)smPanel.getMultiple(modelIndex).getNetworkViewer().getClickControl()).initializeBubbleTableProperties();
		((SMClickControlDelegate)smPanel.getMultiple(modelIndex).getNetworkViewer().getClickControl()).setBubbleTable(modelBubbleTable);
		
		vis.run("bubbleLayout");
		vis.run("bubbleColor");
		
	} //highlightSimilarities
	
	/**
	 * Highlights differences on the provided model
	 * 
	 * @param differences - The list of differences to highlight
	 * @param vis - The Visualization object we're highlighting
	 * @param modelIndex - The panel index of the model being highlighted
	 * @param smPanel - The SmallMultiplesPanel that contains the collection of SmallMultiples
	 */
	private void highlightDifferences(ArrayList<ArrayList<String>> differences, Visualization vis, int modelIndex, SmallMultiplesPanel smPanel) {
		
		// Get the bubbleTable for the model
		AggregateTable modelBubbleTable = ((SMClickControlDelegate)smPanel.getMultiple(modelIndex).getNetworkViewer().getClickControl()).getBubbleTable();
		AggregateItem modelDifferences;
		
		modelDifferences = ((SMClickControlDelegate)smPanel.getMultiple(modelIndex).getNetworkViewer().getClickControl()).getDifferent();
		modelDifferences = (AggregateItem) modelBubbleTable.addItem();
			
		highlightComponentsBubbleSet(differences.get(0), vis, modelBubbleTable, modelDifferences);
		highlightMoleculesBubbleSet(differences.get(1), vis, modelBubbleTable, modelDifferences);
		highlightEdgesBubbleSet(differences.get(2), vis, modelBubbleTable, modelDifferences);
			
		modelDifferences.set("type", "different");
		modelDifferences.setVisible(true);
			
		((SMClickControlDelegate)smPanel.getMultiple(modelIndex).getNetworkViewer().getClickControl()).setDifferent(modelDifferences);
		((SMClickControlDelegate)smPanel.getMultiple(modelIndex).getNetworkViewer().getClickControl()).initializeBubbleTableProperties();
		((SMClickControlDelegate)smPanel.getMultiple(modelIndex).getNetworkViewer().getClickControl()).setBubbleTable(modelBubbleTable);
		
		vis.run("bubbleLayout");
		vis.run("bubbleColor");
		
		/*
		
		// Old method before trying bubble sets
		
		// Get the 'different' focus group
		TupleSet selectedSet = vis.getFocusGroup("different");
		
		// Make the highlights
		highlightComponents(differences.get(0), vis, selectedSet);
		highlightMolecules(differences.get(1), vis, selectedSet);
		highlightEdges(differences.get(2), vis, selectedSet);
			
		// Now update the coloring
		vis.run("color");
		
		*/
		
	} //highlightDifferences
	
	/**
	 * Highlights the provided list of components on the provided model
	 * 
	 * @param components - The list of components to highlight
	 * @param vis - The Visualization object we're highlighting
	 * @param modelBubbleTable - The AggregateTable we're filling
	 * @param modelGroup - The AggregateItem we're filling
	 */
	private void highlightComponentsBubbleSet(ArrayList<String> components, Visualization vis, AggregateTable modelBubbleTable, AggregateItem modelGroup) {
		
		ArrayList<MoleculeCounter> compareTracker = new ArrayList<MoleculeCounter>();
		
		// First loop through and highlight components
		for (int i = 0; i < components.size(); i++) {
				
			// Loop through the nodes of the model, building a string representation identical to the one before, then compare
			Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
							
			while (iter.hasNext()) {
				VisualItem item = (VisualItem) iter.next();
				
				if (item.getString("molecule") != null) {
					StringBuilder compareStringBuilder = buildStringRepresentation(item);
					
					String compareMolecule = item.getString("molecule");
					String compareComponent = item.getString("component");
					String compareState = item.getString(VisualItem.LABEL);
					String id = "0";
					Boolean found = false;
					
					if (compareMolecule == null) {
						compareMolecule = "null";
					} //compareMolecule
					
					if (compareComponent == null) {
						compareComponent = "null";
					} //if
					
					if (compareState == null) {
						compareComponent = "null";
					} //if
					
					// Keep track of how many of each molecule/component pair have been found
					for (int j = 0; j < compareTracker.size(); j++) {
						if ((compareMolecule.equals(compareTracker.get(j).getMolecule())) && (compareComponent.equals(compareTracker.get(j).getComponent())) && (compareState.equals(compareTracker.get(j).getState()))) {
							MoleculeCounter tempMol = new MoleculeCounter(compareMolecule, compareComponent, compareState, compareTracker.get(j).getCount() + 1);

							//tracker.get(i).setCount(tracker.get(i).getCount() + 1);
							id = Integer.toString(compareTracker.get(j).getCount() + 1);

							compareTracker.remove(j);
							compareTracker.add(tempMol);
							
							found = true;
							break;
						} //if
					} //for
					
					// If we find a new molecule/component pair, add a new item to the array
					if (!found) {
						MoleculeCounter tempMol = new MoleculeCounter(compareMolecule, compareComponent, compareState, 0);
						compareTracker.add(tempMol);
						id = Integer.toString(0);
					} //if
					
					compareStringBuilder.append("-");
					compareStringBuilder.append(id);
					String compareString = compareStringBuilder.toString();			

										
					// Compare the string we just built against the current component from the ArrayList
					// If there's a match, add to the highlighted class of objects
					if (compareString.equals(components.get(i))) {
						modelGroup.addItem(vis.getVisualItem(COMPONENT_GRAPH, item));
						addNodeToCenterOrContext(modelGroup, (Node)item, vis);	
						break;
					} //if
														
				} //if
			} //while
			
			compareTracker.clear();
			
		} //for
		
	} //highlightComponentsBubbleSet
	
	/**
	 * Highlights the provided list of molecules on the provided model (not yet implemented)
	 * 
	 * @param molecules - The list of molecules to highlight
	 * @param vis - The Visualization object we're highlighting
	 * @param modelBubbleTable - The AggregateTable we're filling
	 * @param modelGroup - The AggregateItem we're filling
	 */
	private void highlightMoleculesBubbleSet(ArrayList<String> molecules, Visualization vis, AggregateTable modelBubbleTable, AggregateItem modelGroup) {
		
		
		
		
	} //highlightMoleculesBubbleSet
	
	/**
	 * Highlights the provided list of edges on the provided model
	 * 
	 * @param edges - The list of edges to highlight
	 * @param vis - The Visualization object we're highlighting
	 * @param modelBubbleTable - The AggregateTable we're filling
	 * @param modelGroup - The AggregateItem we're filling
	 */
	private void highlightEdgesBubbleSet(ArrayList<String> edges, Visualization vis, AggregateTable modelBubbleTable, AggregateItem modelGroup) {
		
		for (int i = 0; i < edges.size(); i++) {
			
			// Break apart the information from the differentEdges array into source and target of the edge
			StringTokenizer st = new StringTokenizer(edges.get(i), "|");
			String arraySource = st.nextToken();
			String arrayTarget = st.nextToken();
			
			// Loop through the nodes of the model, building a string representation identical to the one before, then compare
			Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.EDGES));
						
			while(iter.hasNext()) {

				// Get the two endpoints of each edge and find their labels
				Edge edge = (Edge) iter.next();
				String sourceString, targetString;
							
				VisualItem source = (VisualItem) edge.getSourceNode();
				VisualItem target = (VisualItem) edge.getTargetNode();
							
				if ((source.getString("molecule") == null) || (target.getString("molecule") == null)) {
					continue;
				} //if
							
				// Correction that may not be correct... skip edges in the same molecule unless they bind to themselves
				// TODO: find out if this is valid
				if (source.getString("molecule").equals(target.getString("molecule"))) {
					if (!(source.getString(VisualItem.LABEL).equals(target.getString(VisualItem.LABEL)))) {
						continue;
					} //if
				} //if
							
				// Build string representations of VisualItems for comparison
				sourceString = buildStringRepresentation(source).toString();
				targetString = buildStringRepresentation(target).toString();
								
				if ((sourceString.equals(arraySource)) && (targetString.equals(arrayTarget))) {
					//modelCenter.addItem(vis.getVisualItem(COMPONENT_GRAPH, edge.getSourceNode()));
					//modelCenter.addItem(vis.getVisualItem(COMPONENT_GRAPH, edge.getTargetNode()));
					modelGroup.addItem(vis.getVisualItem(COMPONENT_GRAPH, edge));
					
					//addBondToCenterOrContext(modelCenter, edge, vis);
					break;
				} //if
							
							
			} //while			
			
		} //for
		
	} //highlightEdgesBubbleSet
	
	
	// Old highlight methods
	/*
	private void highlightComponents(ArrayList<String> differentComponents, Visualization vis, TupleSet selectedSet) {
		
		// First loop through and highlight components
		for (int i = 0; i < differentComponents.size(); i++) {
		
			// Loop through the nodes of the model, building a string representation identical to the one before, then compare
			Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
					
			while (iter.hasNext()) {
				VisualItem item = (VisualItem) iter.next();
			
				if (item.getString("molecule") != null) {
					String compareString = item.getString("molecule") + "." + item.getString("component") + "." + item.getString(VisualItem.LABEL);
			
					// Compare the string we just built against the current component from the ArrayList
					// If there's a match, add to the highlighted class of objects
					if (compareString.equals(differentComponents.get(i))) {
						selectedSet.addTuple(item);
						break;
					} //if
												
				} //if
					
			} //while
					
		} //for
		
	} //highlightComponents
	
	private void highlightMolecules(ArrayList<String> differentMolecules, Visualization vis, TupleSet selectedSet) {
		
		// Then loop through and highlight molecules
		for (int i = 0; i < differentMolecules.size(); i++) {
			
			
			
			
			
			
			
		} //for
		
		
		
		
		
	} //highlightMolecules
	
	private void highlightEdges(ArrayList<String> differentEdges, Visualization vis, TupleSet selectedSet) {
		
		// Finally loop through and highlight edges
		for (int i = 0; i < differentEdges.size(); i++) {
			
			// Break apart the information from the differentEdges array into source and target of the edge
			StringTokenizer st = new StringTokenizer(differentEdges.get(i), "|");
			String arraySource = st.nextToken();
			String arrayTarget = st.nextToken();
			
			// Loop through the nodes of the model, building a string representation identical to the one before, then compare
			Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.EDGES));
			
			while(iter.hasNext()) {

				// Get the two endpoints of each edge and find their labels
				Edge edge = (Edge) iter.next();
				String sourceString, targetString;
				
				VisualItem source = (VisualItem) edge.getSourceNode();
				VisualItem target = (VisualItem) edge.getTargetNode();
				
				if ((source.getString("molecule") == null) || (target.getString("molecule") == null)) {
					continue;
				} //if
				
				// Correction that may not be correct... skip edges in the same molecule unless they bind to themselves
				// TODO: find out if this is valid
				if (source.getString("molecule").equals(target.getString("molecule"))) {
					if (!(source.getString(VisualItem.LABEL).equals(target.getString(VisualItem.LABEL)))) {
						continue;
					} //if
				} //if
				
				// Build string representations of VisualItems for comparison
				sourceString = buildStringRepresentation(source);
				targetString = buildStringRepresentation(target);
				
				if ((sourceString.equals(arraySource)) && (targetString.equals(arrayTarget))) {
					selectedSet.addTuple(edge);
					break;
				} //if
				
				
			} //while
			
			
		} //for
		
	} //highlightEdges
	*/
	
	/**
	 * When an edge is highlighted, also highlight the components at the ends (currently unused)
	 * 
	 * @param aggregate - The AggregateItem we're filling
	 * @param e - The edge being highlighted
	 * @param vis - The Visualization object being highlighted
	 */
	@SuppressWarnings("unused")
	private void addBondToCenterOrContext(AggregateItem aggregate, Edge e, Visualization vis) {
		
		Node leftnode = e.getSourceNode();
		
		// for state node, also add component node to center
		if (e.get("leftparent") != null) {
			aggregate.addItem(vis.getVisualItem(COMPONENT_GRAPH, (Node)e.get("leftparent")));
			leftnode = (Node)e.get("leftparent");
		} //if
		
		// add all state nodes of leftnode
		if (leftnode.get("states") != null) {
			ArrayList<Node> state_nodes = (ArrayList<Node>) leftnode.get("state_nodes");

			if (state_nodes != null) {
				for (int i = 0; i < state_nodes.size(); i++) {
					VisualItem stateitem = vis.getVisualItem(COMPONENT_GRAPH, state_nodes.get(i));
					aggregate.addItem(stateitem);
				} //for
			} //if
		} //if
		
		Node rightnode = e.getTargetNode();
		
		if (e.get("rightparent") != null) {
			aggregate.addItem(vis.getVisualItem(COMPONENT_GRAPH, (Node)e.get("rightparent")));
			rightnode = (Node)e.get("rightparent");
		} //if
		
		// add all state nodes of rightnode
		if (rightnode.get("states") != null) {
			ArrayList<Node> state_nodes = (ArrayList<Node>) rightnode.get("state_nodes");

			if (state_nodes != null) {
				for (int i = 0; i < state_nodes.size(); i++) {
					VisualItem stateitem = vis.getVisualItem(COMPONENT_GRAPH, state_nodes.get(i));
					aggregate.addItem(stateitem);
				} //for
			} //if
		} //if
		
	} //addBondToCenterOrContext

	/**
	 * When a node is highlighted, also highlight the states
	 * 
	 * @param aggregate - The AggregateItem we're filling
	 * @param node - The node being highlighted
	 * @param vis - The Visualization object being highlighted
	 */
	private void addNodeToCenterOrContext(AggregateItem aggregate, Node node, Visualization vis) {
		
		Node comp_node = node;
		
		String type = node.getString("type");
		if (type != null && type.equals("state")) {
			comp_node = (Node)node.get("stateparent");
			aggregate.addItem(vis.getVisualItem(COMPONENT_GRAPH, comp_node));
		} //if
		
		// add all state nodes of this component node
		if (comp_node.get("states") != null) {
			ArrayList<Node> state_nodes = (ArrayList<Node>) comp_node.get("state_nodes");

			if (state_nodes != null) {
				for (int i = 0; i < state_nodes.size(); i++) {
					VisualItem stateitem = vis.getVisualItem(COMPONENT_GRAPH, state_nodes.get(i));
					aggregate.addItem(stateitem);
				} //for
			} //if
		} //if
		
	} //addNodeToCenterOrContext
	
	/**
	 * Add all components and states of components in the Mole to the BubbleSet aggregate 
	 * 
	 * @param aggregate - The AggregateItem being filled
	 * @param molecule - The molecule being highlighted
	 * @param vis - The Visualization object being highlighted
	 */
	private void addAggregateToCenterOrContext(AggregateItem aggregate, AggregateItem molecule, Visualization vis) {
		// Add all components and states of components in the aggregate to the bubbleset aggregate
		Iterator iter = molecule.items();
				
		while (iter.hasNext()) {
			Node node = (Node)iter.next();
			Node comp_node = node;
			
			String type = node.getString("type");
			if (type != null && type.equals("state")) {
				comp_node = (Node)node.get("stateparent");
				aggregate.addItem(vis.getVisualItem(COMPONENT_GRAPH, comp_node));
			} //if
			
			// add all state nodes of this component node
			if (comp_node.get("states") != null) {
				ArrayList<Node> state_nodes = (ArrayList<Node>) comp_node.get("state_nodes");

				if (state_nodes != null) {
					for (int i = 0; i < state_nodes.size(); i++) {
						VisualItem stateitem = vis.getVisualItem(COMPONENT_GRAPH, state_nodes.get(i));
						aggregate.addItem(stateitem);
					} //for
				} //if
			} //if
			
		} //while		
		
	} //addAggregateToCenterOrContext
	
	/**
	 * If a component/edge is clicked, highlight that VisualItem in all SmallMultiples
	 * 
	 * @param item - The VisualItem selected
	 * @param e - The MouseEvent triggered by the click
	 */
	public void itemClicked(VisualItem item, MouseEvent e) {
		
		mostRecentClick = e;
		
		// Saved the code for right-click/left-click check, but right now we'll only worry about left-clicks
		
		/*
		// Right Click
		if (e.getButton() == MouseEvent.BUTTON3 || (e.getButton() == MouseEvent.BUTTON1 && e.isControlDown())) {
				
		if (item instanceof NodeItem) 
			{
				nodeRightClicked(item, e);
			}
			
			else if (item instanceof EdgeItem) 
			{
				edgeRightClicked(item, e);
			}
			else if (item instanceof AggregateItem)
			{
				aggregateRightClicked(item, e);
			}
		}
				
		// Left click.  This check has to come after the right click because the condition is also
		// true in the case of a control click.
		else if (e.getButton() == MouseEvent.BUTTON1) {
			*/	
		
		// Get the panels, and determine which panel was clicked on / selected
		SmallMultiplesPanel smPanel = m_view.getSmallMultiplesPanel();
		JPanel[] panels = smPanel.getSmallMultiplesPanels();
				
		if (!(item instanceof AggregateItem)) {
			int panelIndexClicked = Integer.parseInt(mostRecentClick.getComponent().getParent().getName());
			System.out.println("Panel index clicked: " + panelIndexClicked);
			
			// Update the last panel clicked
			smPanel.setLastPanelSelected(panelIndexClicked);
		} //if
		
		// Clear all highlighting and start fresh
		clearPanels(smPanel, panels);			
		clearBubbleSets();
		
		startTime = System.nanoTime();
		
		if ((item instanceof NodeItem)) {
			nodeLeftClicked((NodeItem)item, e, smPanel, panels);
		} else if (item instanceof EdgeItem) {
			edgeLeftClicked((EdgeItem)item, e, smPanel, panels);
		} else if (item instanceof AggregateItem) {
			aggregateLeftClicked((AggregateItem)item, e, smPanel, panels);
		} //if-else
		
		endTime = System.nanoTime();
		
		printSystemTimes();
			/*
		} //if-else
		*/
	} //itemClicked
	
	/**
	 * If a node is clicked, highlight that node in all SmallMultiples
	 * 
	 * @param item - The NodeItem selected
	 * @param e - The MouseEvent triggered by the selection
	 * @param smPanel - The SmallMultiplesPanel containing the set of SmallMultiples
	 * @param panels - The collection of JPanels stored in the SmallMultiplesPanel
	 */
	private void nodeLeftClicked(NodeItem item, MouseEvent e, SmallMultiplesPanel smPanel, JPanel[] panels) {
		// Clear any current highlights/selections
		smPanel.removeAllSelections();
		
		// Get the name of the node clicked (molecule, component, state)
		String nodeName = buildStringRepresentation(item).toString();
		
		// Highlight that (molecule, component, state) in all models (if it exists)
		for (int i = 0; i < panels.length; i++) {
			Visualization vis = smPanel.getMultiple(i).getDisplay().getVisualization();
			AggregateTable modelBubbleTable = ((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).getBubbleTable();
			AggregateItem modelGroup;
			
			modelGroup = ((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).getItemHighlight();
			modelGroup = (AggregateItem) modelBubbleTable.addItem();
			
			// Loop through the nodes of the model, building a string representation identical to the one before, then compare
			Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
							
			while (iter.hasNext()) {
				VisualItem myItem = (VisualItem) iter.next();
					
				if (myItem.getString("molecule") != null) {
					String compareString = buildStringRepresentation(myItem).toString();
					
					// Compare the string we just built against the current component from the ArrayList
					// If there's a match, add to the highlighted class of objects
					if (compareString.equals(nodeName)) {
						modelGroup.addItem(vis.getVisualItem(COMPONENT_GRAPH, myItem));
						addNodeToCenterOrContext(modelGroup, (Node)myItem, vis);	
						break;
					} //if
														
				} //if
			} //while
			
			modelGroup.set("type", "itemHighlight");
			modelGroup.setVisible(true);
			
			((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).setItemHighlight(modelGroup);
			((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).initializeBubbleTableProperties();
			((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).setBubbleTable(modelBubbleTable);
			
			vis.run("bubbleLayout");
			vis.run("bubbleColor"); 
			
		} //for
		
		smPanel.setCurrentlyHighlighted(true);
		
	} //nodeLeftClicked
	
	/**
	 * If an edge is clicked, highlight that edge in all SmallMultiples
	 * 
	 * @param edge - The EdgeItem selected
	 * @param e - The MouseEvent triggered by the selection
	 * @param smPanel - The SmallMultiplesPanel containing the set of SmallMultiples
	 * @param panels - The collection of JPanels stored in the SmallMultiplesPanel
	 */
	private void edgeLeftClicked(EdgeItem edge, MouseEvent e, SmallMultiplesPanel smPanel, JPanel[] panels) {
		// Clear any current highlights/selections
		smPanel.removeAllSelections();
		
		// Get the endpoints of the edge clicked (2 sets of molecule, component, state)
		VisualItem source = (VisualItem) edge.getSourceNode();
		VisualItem target = (VisualItem) edge.getTargetNode();
		
		String sourceString = buildStringRepresentation(source).toString();
		String targetString = buildStringRepresentation(target).toString();
		
		// Highlight that edge in all models (if it exists)
		for (int i = 0; i < panels.length; i++) {
			Visualization vis = smPanel.getMultiple(i).getDisplay().getVisualization();
			AggregateTable modelBubbleTable = ((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).getBubbleTable();
			AggregateItem modelGroup;
			
			modelGroup = ((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).getItemHighlight();
			modelGroup = (AggregateItem) modelBubbleTable.addItem();
			
			// Loop through the nodes of the model, building a string representation identical to the one before, then compare
			Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.EDGES));
			
			while (iter.hasNext()) {
				
				// Get the two endpoints of each edge and find their labels
				Edge myEdge = (Edge) iter.next();
				String myEdgeSourceString, myEdgeTargetString;
							
				VisualItem myEdgeSource = (VisualItem) myEdge.getSourceNode();
				VisualItem myEdgeTarget = (VisualItem) myEdge.getTargetNode();
							
				if ((source.getString("molecule") == null) || (target.getString("molecule") == null)) {
					continue;
				} //if
							
				// Correction that may not be correct... skip edges in the same molecule unless they bind to themselves
				// TODO: find out if this is valid
				if (source.getString("molecule").equals(target.getString("molecule"))) {
					if (!(source.getString(VisualItem.LABEL).equals(target.getString(VisualItem.LABEL)))) {
						continue;
					} //if
				} //if
							
				// Build string representations of VisualItems for comparison
				myEdgeSourceString = buildStringRepresentation(myEdgeSource).toString();
				myEdgeTargetString = buildStringRepresentation(myEdgeTarget).toString();
								
				if ((myEdgeSourceString.equals(sourceString)) && (myEdgeTargetString.equals(targetString))) {
					modelGroup.addItem(vis.getVisualItem(COMPONENT_GRAPH, myEdge));
					break;
				} //if
				
			} //while
			
			modelGroup.set("type", "itemHighlight");
			modelGroup.setVisible(true);
			
			((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).setItemHighlight(modelGroup);
			((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).initializeBubbleTableProperties();
			((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).setBubbleTable(modelBubbleTable);
			
			vis.run("bubbleLayout");
			vis.run("bubbleColor"); 
			
		} //for
		
	} //edgeLeftClicked
	
	/**
	 * If a molecule is clicked, highlight that edge in all SmallMultiples
	 * 
	 * @param item - The AggregateItem selected
	 * @param e - The MouseEvent triggered by the selection
	 * @param smPanel - The SmallMultiplesPanel containing the set of SmallMultiples
	 * @param panels - The collection of JPanels stored in the SmallMultiplesPanel
	 */
	private void aggregateLeftClicked(AggregateItem item, MouseEvent e, SmallMultiplesPanel smPanel, JPanel[] panels) {
		System.out.println("WE HIT A MOLECULE!");
		
		// Get the name of the aggregate clicked (molecule)
		String nodeName = item.getString("molecule");
		
		System.out.println("molecule hit = " + nodeName);
		
		
		// Highlight that (molecule) in all models (if it exists)
		for (int i = 0; i < panels.length; i++) {
			Visualization vis = smPanel.getMultiple(i).getDisplay().getVisualization();
			AggregateTable modelBubbleTable = ((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).getBubbleTable();
			AggregateItem modelGroup;
			
			modelGroup = ((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).getItemHighlight();
			modelGroup = (AggregateItem) modelBubbleTable.addItem();
			
			// Loop through the aggregates of the model, building a string representation identical to the one before, then compare
			//TODO: don't want to iterate over Graph.NODES; want to find aggregates...
			Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
			//Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, vis.));	
			
			while (iter.hasNext()) {
				VisualItem myItem = (VisualItem) iter.next();
				
				if (myItem instanceof NodeItem) {
					continue;
				} //if
					
				if (myItem.getString("molecule") != null) {
					String compareString = myItem.getString("molecule");
					
					// Compare the string we just built against the current component from the ArrayList
					// If there's a match, add to the highlighted class of objects
					if (compareString.equals(nodeName)) {
						modelGroup.addItem(vis.getVisualItem(COMPONENT_GRAPH, myItem));
						addAggregateToCenterOrContext(modelGroup, (AggregateItem)myItem, vis);	
						break;
					} //if
														
				} //if
			} //while
			
			modelGroup.set("type", "itemHighlight");
			modelGroup.setVisible(true);
			
			((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).setItemHighlight(modelGroup);
			((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).initializeBubbleTableProperties();
			((SMClickControlDelegate)smPanel.getMultiple(i).getNetworkViewer().getClickControl()).setBubbleTable(modelBubbleTable);
			
			vis.run("bubbleLayout");
			vis.run("bubbleColor"); 
			
		} //for
		
		smPanel.setCurrentlyHighlighted(true);
		
	} //aggregateLeftClicked
	
	public void printSystemTimes() {
		System.out.println("Start Time:   " + startTime);
		System.out.println("End Time:     " + endTime);
		System.out.println("Time Elapsed: " + (endTime - startTime));		
	} //printSystemTimes
	
	public String getFileName() {
		return getModelNameFromFilepath(m_sourcePath);
	} //getFileName
	
	
	/*
	public void mouseMoved(MouseEvent e) {
		showTooltip(e);
	} //mouseMoved
	
	public void itemEntered(VisualItem item, MouseEvent e) {
		showTooltip(e);
	} //itemEntered
	
	public void showTooltip(MouseEvent e) {
		int panelIndex = Integer.parseInt(e.getComponent().getParent().getName());
		System.out.println(panelIndex);
	} //showTooltip
	*/
	
	public static IViewPart getView(IWorkbenchWindow window, String viewId) {
	    IViewReference[] refs = window.getActivePage().getViewReferences();
	    for (IViewReference viewReference : refs) {
	        if (viewReference.getId().equals(viewId)) {
	            return viewReference.getView(true);
	        } //if
	    } //for
	    return null;
	} //getView
	
	
	
	
	
	/*
	 * ---------------------------------------------------------
	 * ISelectionProvider stuff below.
	 * 
	 */
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		m_listeners.add(listener);		
	} //addSelectionChangedListener
	
	@Override
	public ISelection getSelection() {
		return m_selection;
	} //getSelection
	
	

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		// This is commented out because for some reason all of the listeners are removed
		// when a new file is opened...
		// Commenting this out does not let the listeners ever be removed, but it does not matter
		// since the SMClickControlDelegates are created and destroyed so frequently. 
		
		//m_listeners.remove(listener);		
	} //removeSelectionChangedListener
	
	

	@Override
	public void setSelection(ISelection selection) {
		
		m_selection = selection;
		final SMClickControlDelegate thisInstance = this;
		
		Object[] listeners = m_listeners.getListeners();

		System.out.println("Listeners: " + listeners.length);
		
		for(int i = 0; i < listeners.length; i++) {
			final ISelectionChangedListener scl = (ISelectionChangedListener) listeners[i];
			
			Display.getDefault().syncExec(new Runnable(){
				@Override
				public void run() {
					scl.selectionChanged(new SelectionChangedEvent(thisInstance, m_selection));
				} //run
			});
		} //for
		
	} //setSelection

} //SMClickControlDelegate