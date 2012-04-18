package visualizationviewer;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

import javax.swing.table.TableModel;

import prefuse.Display;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;

import editor.BNGEditor;
import editor.CurrentFile;

public class VisualizationViewerController {

	//If you do not visualize files as you open them, then open the visualizer, and then switch through the tabs, they will update the screen but only for the first time that you see them.
	
	// booleans to know whether or not the visual model for the cmap 
	// or igraph need to be regenerated.
	private boolean	renewCMap = true,
					renewIGraph = true;
	
	// The file that the user is currently viewing in the editor.
	private CurrentFile currentFile;
	
	// The instance of the visualization viewer that this controller controls.
	private static VisualizationViewer viewer;
	
	// The singleton instance of the controller
	private static VisualizationViewerController control;
	
	// The current display of species graph
	private Display speciesGraph;
	
	/**
	 * Private constructor for singleton pattern
	 */
	private VisualizationViewerController()
	{
		viewer = VisualizationViewer.getViewer(this);
	}
	
	/**
	 * Public method for getting/creating the singleton object.
	 * @return
	 */
	public static synchronized VisualizationViewerController loadVisualizationViewController()
	{
		if(control == null)
			control = new VisualizationViewerController();

		return control;
	}


	/**
	 * Delegate method called when the contact map tab is selected by the
	 * user in the viewer.
	 */
	protected void cMapSelected()
	{
		// If we need to renew the cmap, then do it.
		if(renewCMap)
		{
			updateCMap();
		}
	}

	/**
	 * Delegate method called when the influence graph tab is selected by the
	 * user in the viewer.
	 */
	protected void iGraphSelected() 
	{
		//System.out.println("renewIGraph: " + renewIGraph);
		if(renewIGraph)
		{
			updateIGraph();
		}
	}

	/**
	 * Delegate method called when the species graph tab is selected by the
	 * user in the viewer.
	 */
	protected void speciesBrowserSelected() 
	{
		// TODO
		// add renew logic
		
		// Where is this happening?
	}
	
	/**
	 * Delegate method called when a file becomes the active file.
	 */
	public void fileBecomesFocus(CurrentFile currentFile_in) 
	{
		
		currentFile = currentFile_in;
		
		if(currentFile != null)
		{
			//System.out.println("File changed to " +  currentFile.getFileName());
		}
		if(viewer.isVisible())
		{
			// When a new file becomes focused, we should update 
			// the visualizations for that file.  
			// To be more efficient we will only load the currently viewed
			// visualization.
			if (viewer.getSelectedComponent() == VisualizationViewer.CMAP)
			{
				// This will attempt to update the contact map
				// and then set the renewCMap boolean to false
				// if it is successful.
				updateCMap();		
	
				// We still need to update the iGraph.
				renewIGraph = true;
			}
			
			// Same logic as above, but for the igraph.
			else if (viewer.getSelectedComponent() == VisualizationViewer.IGRAPH)
			{
				updateIGraph();				
				
				renewCMap = true;
			}
			
			// If it is the species browser then both igraph and cmap should be
			// renewed later.
			else if (viewer.getSelectedComponent() == VisualizationViewer.SPECIESBROWSER)
			{
				renewCMap = true;
				renewIGraph = true;
			}
		}
		updateAnnotationTable(null);
	}
	
	
	/**
	 * This method updates the contact map by getting the visual model from
	 * the current file and then sending the display to the viewer.
	 */
	private void updateCMap()
	{
		// If there is an error in producing a model,
		// then the currentFile.getCMapVisualModel.getDisplay() will
		// throw a nullPointerException.
		try
		{
			viewer.setCMap(currentFile.getCMapVisualModel().getDisplay());
			viewer.setCMapOverview(currentFile.getCMapVisualModel().getOverviewDisplay());

			// TODO could be part of the flicker
			// need to update because some values are unavailable for the first time
			this.updateCMapOverview();
			renewCMap = false;
		}
		
		// If there is a nullPointerException then set the visuals to null, which
		// just displays nothing.
		// This also catches null files. 
		catch(NullPointerException e)
		{
			//System.out.println("Something was null... setting vis to null.");
			viewer.setCMap(null);
			viewer.setCMapOverview(null);
			renewCMap = true;
		}
		
		//System.out.println("finished updating cmap:\n\trenewcmap = " + renewCMap);
		
	}	

	/**
	 * This method updates the influence graph by getting the visual model from
	 * the current file and then sending the display to the viewer.
	 */
	private void updateIGraph()
	{
		// If there is an error in producing a model,
		// then the currentFile.getIGraphVisualModel.getDisplay() will
		// throw a nullPointerException.
		try
		{	
			viewer.setIGraph(currentFile.getIMapVisualModel().getDisplay());
			//fitWindow(currentFile.getIMapVisualModel().getDisplay());
			viewer.setIGraphOverview(currentFile.getIMapVisualModel().getOverviewDisplay());
			
			//TODO could be part of the flicker
			// need to update because some values are unavailable for the first time
			this.updateIGraphOverview();
			renewIGraph = false;
		}
		
		// If there is a nullPointerException then set the visuals to null, which I hope
		// just displays nothing.
		catch(NullPointerException e)
		{
			viewer.setIGraph(null);
			viewer.setIGraphOverview(null);
			renewIGraph = true;
		}
		
	}
	
	/**
	 * Public way to update the Contact Map.  
	 */
	public void informOfNewCMapModel() 
	{
		//System.out.println("Informed of new CMap Model from "+f+"for currentFile = " + currentFile.getFileName() );
		if (viewer.isVisible())
		{
			if(viewer.getSelectedComponent() == VisualizationViewer.CMAP)
				updateCMap();
			else
				renewCMap = true;
		}
		
	}

	/**
	 * Public way to update the Influence Graph.  
	 */
	public void informOfNewIGraphModel() 
	{
		if (viewer.isVisible())
		{
			if(viewer.getSelectedComponent() == VisualizationViewer.IGRAPH)
				updateIGraph();
				
			else
				renewIGraph = true;
		}
	}

	/**
	 *  This should never be called by the species browser.  
	 *  The upDateSpeciesBrowser(Display d) method will launch
	 *  frame in its own way.
	 */
	public void openVisualizationViewerFrame() 
	{
		updateCMap();
		
		renewCMap = false;
		renewIGraph = true;
		
		viewer.focusCMap();
		
		viewer.setLocation(BNGEditor.getMainEditorShell().getBounds().x +
				BNGEditor.getMainEditorShell().getBounds().width,
				BNGEditor.getMainEditorShell().getBounds().y);
		
		viewer.setVisible(true);
	}
	
	/**
	 * The species browser has first priority over the cmap and igraph.
	 * When the user clicks on something that launches the species graph,
	 * it is immediately loaded.  This is why you should not load the
	 * viewer with openVisualizationViewerFrame() when using species.
	 * @param d
	 */
	public void updateSpeciesBrowser(Display d)
	{
		if(!viewer.isVisible())
			viewer.setVisible(true);
		
		viewer.setSpeciesBrowser(d);
		viewer.focusSpeciesBrowser();

	}

	/**
	 * Set the visualization for the species browser overview.
	 * @param d
	 */
	public void updateSpeciesBrowserOverview(Display d)
	{	
		if (d != null) {
			viewer.setSpeciesBrowserOverview(d);
			speciesGraph = d;
		}
		else {
			if (speciesGraph != null)
				viewer.setSpeciesBrowserOverview(speciesGraph);
		}
	}
	
	/**
	 * Update the table in annotation panel of VisualizationViewer
	 * @param tm
	 */
	public void updateAnnotationTable(TableModel tm) {
		viewer.setAnnotationPanel(tm);
	}
	
	/**
	 * Get current file object
	 * @return
	 */
	public CurrentFile getCurrentFile() {
		return currentFile;
	}
	
	/**
	 * Update the overview of cmap using current cmap display
	 */
	public void updateCMapOverview() {
		try {
			viewer.setCMapOverview(currentFile.getCMapVisualModel()
					.getOverviewDisplay());
		} catch (NullPointerException e) {
			viewer.setCMapOverview(null);
		}
	}
	
	/**
	 * Update the overview of igraph using current igraph display
	 */
	public void updateIGraphOverview() {
		try {
			viewer.setIGraphOverview(currentFile.getIMapVisualModel()
					.getOverviewDisplay());
		} catch (NullPointerException e) {
			viewer.setIGraphOverview(null);
		}
	}

	
	public Dimension getCMapSize() 
	{
		return VisualizationViewer.getViewer(this).getCMapSize();
	}
	
	public Dimension getIGraphSize() 
	{
		return VisualizationViewer.getViewer(this).getIGraphSize();
	}
	
	public Dimension getSpeciesBrowserSize() 
	{
		return VisualizationViewer.getViewer(this).getSpeciesBrowserSize();
	}
	
	public Dimension getCMapOverviewSize() 
	{
		System.out.println("getCMapOverviewSize called in controller");
		return VisualizationViewer.getViewer(this).getCMapOverviewSize();
	}
	
	public Dimension getIGraphOverviewSize() 
	{
		return VisualizationViewer.getViewer(this).getIGraphOverviewSize();
	}
	
	public Dimension getSpeciesBrowserOverviewSize() 
	{
		return VisualizationViewer.getViewer(this).getSpeciesBrowserOverviewSize();
	}
	
	public void windowResized() 
	{
		System.out.println("Resized");
		
		viewer.resizeSpeciesBrowserOverviewDisplay();
		viewer.resizeCMapOverviewDisplay();
		viewer.resizeIGraphOverviewDisplay();
		viewer.resizeCMapDisplay();
		viewer.resizeIGraphDisplay();
		viewer.resizeSpeciesBrowserDisplay();
	}
	
	public void updateCMapSelectBox() {
		viewer.updateCMapSelectBox();
	}
	
	public void updateIGraphSelectBox() {
		viewer.updateIGraphSelectBox();
	}
	
	public void updateSpeciesBrowserSelectBox()
	{
		viewer.updateSpeciesBrowserSelectBox();
	}

	/*
	 * Copy from networkviewer.FitOverviewListener
	 */
	private void fitWindow(Display d) {

		Rectangle2D m_bounds = new Rectangle2D.Double();
		Rectangle2D m_temp = new Rectangle2D.Double();
		double m_d = 15;

		d.getItemBounds(m_temp);
		GraphicsLib.expand(m_temp, 25 / d.getScale());

		double dd = m_d / d.getScale();
		double xd = Math.abs(m_temp.getMinX() - m_bounds.getMinX());
		double yd = Math.abs(m_temp.getMinY() - m_bounds.getMinY());
		double wd = Math.abs(m_temp.getWidth() - m_bounds.getWidth());
		double hd = Math.abs(m_temp.getHeight() - m_bounds.getHeight());
		if (xd > dd || yd > dd || wd > dd || hd > dd) {
			m_bounds.setFrame(m_temp);
			DisplayLib.fitViewToBounds(d, m_bounds, 0);
		}
	}
}
	