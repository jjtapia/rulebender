package rulebender.simulationjournaling.model;

import java.awt.Color;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.util.display.DisplayLib;
import rulebender.core.prefuse.overview.FitOverviewListener;
import rulebender.core.prefuse.overview.OverviewControl;

/**
 *
 * @author Juanjo Vega
 */
public class SmallMultipleOverview extends Display {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public SmallMultipleOverview(Display display) {
        super(display.getVisualization());

        setBackground(Color.WHITE);
        //setBorder(BorderFactory.createTitledBorder("Overview"));

        DisplayLib.fitViewToBounds(this, getVisualization().getBounds(Visualization.ALL_ITEMS), 0);
        //addItemBoundsListener(new FitOverviewListener());

        //OverviewControl zoomToFitRectangleControl = new OverviewControl(display, this);
        //addControlListener(zoomToFitRectangleControl);
        //addPaintListener(zoomToFitRectangleControl);
        
    } //SmallMultipleOverview (constructor)
	
} //SmallMultipleOverview