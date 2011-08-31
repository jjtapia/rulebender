package rulebender.prefuse.networkviewer;

import java.awt.geom.Rectangle2D;

import prefuse.Display;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;

public class FitOverviewListener implements ItemBoundsListener {
    private Rectangle2D m_bounds = new Rectangle2D.Double();
    private Rectangle2D m_temp = new Rectangle2D.Double();
    private double m_d = 15;
   
    public void itemBoundsChanged(Display d) {
        d.getItemBounds(m_temp);
        GraphicsLib.expand(m_temp, 25/d.getScale());
        
        double dd = m_d/d.getScale();
        double xd = Math.abs(m_temp.getMinX()-m_bounds.getMinX());
        double yd = Math.abs(m_temp.getMinY()-m_bounds.getMinY());
        double wd = Math.abs(m_temp.getWidth()-m_bounds.getWidth());
        double hd = Math.abs(m_temp.getHeight()-m_bounds.getHeight());
        if ( xd>dd || yd>dd || wd>dd || hd>dd ) {
            m_bounds.setFrame(m_temp);
            DisplayLib.fitViewToBounds(d, m_bounds, 0);
        }
    }
}
