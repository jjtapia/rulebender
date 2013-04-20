package rulebender.core.prefuse.overview;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.Color;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.util.display.DisplayLib;
import rulebender.logging.Logger;

/**
 *
 * @author Juanjo Vega
 */
public class Overview extends Display 
{

   
	private static final long serialVersionUID = 1L;

	/**
   * Using the Visualization object from the passed in Display, set up a 
   * second display for an overview context.
   */
	public Overview(Display display) 
	{
        super(display.getVisualization());

        setBackground(Color.WHITE);
        //setBorder(BorderFactory.createTitledBorder("Overview"));

        Logger.log(Logger.LOG_LEVELS.ERROR, Overview.class,
            "display null? " + (display == null));
        Logger.log(Logger.LOG_LEVELS.ERROR, Overview.class, 
            "this.getVisualization null? " + (this.getVisualization() == null));
        
        DisplayLib.fitViewToBounds(this, 
                                   this.getVisualization().getBounds(Visualization.ALL_ITEMS),
                                   0);
        
        addItemBoundsListener(new FitOverviewListener());

        OverviewControl zoomToFitRectangleControl = new OverviewControl(display, this);
        addControlListener(zoomToFitRectangleControl);
        addPaintListener(zoomToFitRectangleControl);
    }
}
