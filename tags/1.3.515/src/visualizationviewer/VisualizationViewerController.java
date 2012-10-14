package visualizationviewer;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

import javax.swing.table.TableModel;

import link.LinkHub;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.visual.VisualItem;
import visualizationviewer.annotation.AnnotationPanelControllerNoModelGuide;

import editor.BNGEditor;
import editor.CurrentFile;

public class VisualizationViewerController
{

  // If you do not visualize files as you open them,
  // then open the visualizer, and then switch through the tabs,
  // they will update the screen but only for the first time that you see
  // them.

  // The file that the user is currently viewing in the editor.
  private CurrentFile currentFile;

  // The instance of the visualization viewer that this controller controls.
  private static VisualizationViewer viewer;

  // The singleton instance of the controller
  private static VisualizationViewerController control;


  // The current display of species graph
  // private Display speciesGraph;

  /**
   * Private constructor for singleton pattern
   */
  private VisualizationViewerController()
  {
    viewer = VisualizationViewer.getViewer(this);
  }


  /**
   * Public method for getting/creating the singleton object.
   * 
   * @return
   */
  public static synchronized VisualizationViewerController loadVisualizationViewController()
  {
    if (control == null)
    {
      control = new VisualizationViewerController();

      // Tell the LinkHub to use the AnnotationPanelController class for
      // all of the
      // linking of the annotation panel.
      // The control object is passed so that the
      // AnnotationPanelController can pass
      // back a tablemodel to be displayed in the view.
      LinkHub.getLinkHub().registerLinkedViewsListener(
          new AnnotationPanelControllerNoModelGuide(control));
    }

    return control;
  }


  /**
   * /** Delegate method called when a file becomes the active file.
   */
  public void fileBecomesFocus(CurrentFile currentFile_in)
  {
    System.out.println("**fileBecomesFocus called");

    currentFile = currentFile_in;

    if (currentFile != null)
    {
      System.out.println("File changed to " + currentFile.getFileName());
    }
    else
    {
      System.out.println("File changed to NULL");
    }
    if (viewer.isVisible())
    {
      // When a new file becomes focused, we should update
      // the visualizations for that file.

      // This will attempt to update the contact map
      // and then set the renewCMap boolean to false
      // if it is successful.
      updateCMap();
      updateIGraph();
    }

    System.out
        .println("Sending null to the visviewer controller because a new file is selected");
    updateAnnotationTable(null);
  }


  /**
   * This method updates the contact map by getting the visual model from the
   * current file and then sending the display to the viewer.
   */
  private void updateCMap()
  {
    // If there is an error in producing a model,
    // then the currentFile.getCMapVisualModel.getDisplay() will
    // throw a nullPointerException.
    try
    {
      // DEBUG
      // System.out.println("currentFile : " + (currentFile == null ?
      // "null" : "not null"));
      // System.out.println("currentFile.getCMapVisualModel() : " +
      // (currentFile.getCMapVisualModel() == null ? "null" :
      // "not null"));
      // System.out.println("currentFile.getCMapVisualModel().getDisplay() : "
      // + (currentFile.getCMapVisualModel().getDisplay() == null ? "null"
      // : "not null"));

      viewer.setCMap(currentFile.getCMapVisualModel().getDisplay());
      viewer.setCMapOverview(currentFile.getCMapVisualModel()
          .getOverviewDisplay());

      // need to update because some values are unavailable for the first
      // time
      this.updateCMapOverview();
    }

    // If there is a nullPointerException then set the visuals to null,
    // which
    // just displays nothing.
    // This also catches null files.
    catch (NullPointerException e)
    {
      viewer.setCMap(null);
      viewer.setCMapOverview(null);

      // DEBUG
      e.printStackTrace();
    }

    // System.out.println("finished updating cmap:\n\trenewcmap = " +
    // renewCMap);

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
      // fitWindow(currentFile.getIMapVisualModel().getDisplay());
      viewer.setIGraphOverview(currentFile.getIMapVisualModel()
          .getOverviewDisplay());

      // need to update because some values are unavailable for the first
      // time
      this.updateIGraphOverview();
    }

    // If there is a nullPointerException then set the visuals to null,
    // which I hope
    // just displays nothing.
    catch (NullPointerException e)
    {
      viewer.setIGraph(null);
      viewer.setIGraphOverview(null);
    }

  }


  /**
   * Public way to update the Contact Map.
   */
  public void informOfNewCMapModel()
  {
    // System.out.println("Informed of new CMap Model from "+"for currentFile = "
    // + currentFile.getFileName() );
    if (viewer.isVisible())
    {
      updateCMap();
    }

  }


  /**
   * Public way to update the Influence Graph.
   */
  public void informOfNewIGraphModel()
  {
    if (viewer.isVisible())
    {
      updateIGraph();
    }
  }


  /**
   * This should never be called by the species browser. The
   * upDateSpeciesBrowser(Display d) method will launch frame in its own way.
   */
  public void openVisualizationViewerFrame()
  {
    updateCMap();
    updateIGraph();

    viewer.setLocation(BNGEditor.getMainEditorShell().getBounds().x
        + BNGEditor.getMainEditorShell().getBounds().width, BNGEditor
        .getMainEditorShell().getBounds().y);

    viewer.setVisible(true);
  }


  /**
   * The species browser has first priority over the cmap and igraph. When the
   * user clicks on something that launches the species graph, it is immediately
   * loaded. This is why you should not load the viewer with
   * openVisualizationViewerFrame() when using species.
   * 
   * @param d
   */
  public void updateSpeciesBrowser(Display d)
  {
    if (!viewer.isVisible())
      viewer.setVisible(true);

    viewer.setSpeciesBrowser(d);
  }


  /**
   * Set the visualization for the species browser overview.
   * 
   * @param d
   */
  public void updateSpeciesBrowserOverview(Display d)
  {
    if (d != null)
    {
      viewer.setSpeciesBrowserOverview(d);
      // speciesGraph = d;
    }

    /*
     * I'm pretty sure this is unnecesary because if it is null then nothing
     * will happen and the previous one will be in the window still.
     * 
     * else { if (speciesGraph != null)
     * viewer.setSpeciesBrowserOverview(speciesGraph); }
     */
  }


  /**
   * Update the table in annotation panel of VisualizationViewer
   * 
   * @param tm
   */
  public void updateAnnotationTable(TableModel tm)
  {

    System.out.println("Updating Annotation from controller");
    if (tm == null)
    {
      System.out.println("\tAnnotation Will Clear");
    }

    viewer.setAnnotationPanel(tm);
    System.out.println("\tAnnotation Cleared");

  }


  /**
   * Get current file object
   * 
   * @return
   */
  public CurrentFile getCurrentFile()
  {
    return currentFile;
  }


  /**
   * Update the overview of cmap using current cmap display
   */
  public void updateCMapOverview()
  {
    try
    {
      viewer.setCMapOverview(currentFile.getCMapVisualModel()
          .getOverviewDisplay());
    }
    catch (NullPointerException e)
    {
      viewer.setCMapOverview(null);
    }
  }


  public void updateCMapSelectBox()
  {
    viewer.updateCMapSelectBox();
  }


  /**
   * Update the overview of igraph using current igraph display
   */
  public void updateIGraphOverview()
  {
    try
    {
      viewer.setIGraphOverview(currentFile.getIMapVisualModel()
          .getOverviewDisplay());
    }
    catch (NullPointerException e)
    {
      viewer.setIGraphOverview(null);
    }
  }


  public void updateIGraphSelectBox()
  {
    viewer.updateIGraphSelectBox();
  }


  /*
   * Copy from networkviewer.FitOverviewListener
   */
  private void fitWindow(Display d)
  {

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
    if (xd > dd || yd > dd || wd > dd || hd > dd)
    {
      m_bounds.setFrame(m_temp);
      DisplayLib.fitViewToBounds(d, m_bounds, 0);
    }
  }


  public void updateSpeciesBrowserSelectBox()
  {
    viewer.updateSpeciesBrowserSelectBox();
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

    // viewer.resizeSpeciesBrowserDisplay();
    // viewer.resizeSpeciesBrowserOverviewDisplay();
    // viewer.resizeCMapOverviewDisplay();
    // viewer.resizeIGraphOverviewDisplay();
    viewer.resizeCMap();
    viewer.resizeIGraph();
    viewer.resizeSpeciesBrowser();
  }


  public void close()
  {
    viewer.dispose();
  }


  public Visualization getInfluenceGraphVisualization()
  {
    return VisualizationViewer.getViewer(this).getInfluenceGraphVisualization();
  }


  public Visualization getContactMapVisualization()
  {
    return VisualizationViewer.getViewer(this).getContactMapVisualization();
  }


  public static void quitting()
  {
    if (viewer != null)
    {
      viewer.dispose();
    }
  }
}
