package rulebender.core.prefuse.networkviewer.contactmap;

/**
 * Class taken from sourceforge prefuse forums.
 */

import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import java.awt.Shape;

import prefuse.Constants;
import prefuse.render.EdgeRenderer;
import prefuse.util.GraphicsLib;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

public class SelfReferenceRenderer extends EdgeRenderer {

	private Ellipse2D m_ellipse = new Ellipse2D.Float();

	protected Shape getRawShape(VisualItem item) 
	{
		EdgeItem edge = (EdgeItem) item;

		VisualItem item1 = edge.getSourceItem();

		VisualItem item2 = edge.getTargetItem();

		try {
			// self interaction
			if (item1 == item2) {

				getAlignedPoint(m_tmpPoints[0], item1.getBounds(), m_xAlign1,
						m_yAlign1);

				getAlignedPoint(m_tmpPoints[1], item2.getBounds(), m_xAlign2,
						m_yAlign2);

				m_curWidth = (int) Math.round(m_width * getLineWidth(item));

				// I'm going to just hack these values, but ideall they would be
				// dependent on the
				// size of the item.
				m_ellipse.setFrame(m_tmpPoints[0].getX(),
						m_tmpPoints[0].getY() - 11, 10, 25);

				return m_ellipse;

			}
		}

		catch (Exception ex) {

			ex.printStackTrace();

			return null;

		}

		/*
		 * Copied and pasted most of below from prefuse source. 
		 * 
		 * I just needed to change the way that directed edges were detected. 
		 */
		
		int type = m_edgeType;
        
        getAlignedPoint(m_tmpPoints[0], item1.getBounds(),
                        m_xAlign1, m_yAlign1);
        getAlignedPoint(m_tmpPoints[1], item2.getBounds(),
                        m_xAlign2, m_yAlign2);
        m_curWidth = (float)(m_width * getLineWidth(item));
        
        // create the arrow head, if needed
        EdgeItem e = (EdgeItem)item;
        
        if((item1.getString("type").equals("hub") ||
        		item2.getString("type").equals("hub")) 
        		&& m_edgeArrow != Constants.EDGE_ARROW_NONE)
        {
        	// instead of using e.getDirected --which depends on a graph being
        	// marked as directed-- I just use whether or not the edge is connected
        	// to a hub.
        	
        //( e.isDirected() && m_edgeArrow != Constants.EDGE_ARROW_NONE ) {
           
        	// get starting and ending edge endpoints
            boolean forward = (m_edgeArrow == Constants.EDGE_ARROW_FORWARD);
            Point2D start = null, end = null;
            start = m_tmpPoints[forward?0:1];
            end   = m_tmpPoints[forward?1:0];
            
            // compute the intersection with the target bounding box
            VisualItem dest = forward ? e.getTargetItem() : e.getSourceItem();
            int i = GraphicsLib.intersectLineRectangle(start, end,
                    dest.getBounds(), m_isctPoints);
            if ( i > 0 ) end = m_isctPoints[0];
            
            // create the arrow head shape
            AffineTransform at = getArrowTrans(start, end, m_curWidth);
            m_curArrow = at.createTransformedShape(m_arrowHead);
            
            // update the endpoints for the edge shape
            // need to bias this by arrow head size
            Point2D lineEnd = m_tmpPoints[forward?1:0]; 
            lineEnd.setLocation(0, -m_arrowHeight);
            at.transform(lineEnd, lineEnd);
        } else {
            m_curArrow = null;
        }
        
        // create the edge shape
        Shape shape = null;
        double n1x = m_tmpPoints[0].getX();
        double n1y = m_tmpPoints[0].getY();
        double n2x = m_tmpPoints[1].getX();
        double n2y = m_tmpPoints[1].getY();
        switch ( type ) {
            case Constants.EDGE_TYPE_LINE:          
                m_line.setLine(n1x, n1y, n2x, n2y);
                shape = m_line;
                break;
            case Constants.EDGE_TYPE_CURVE:
                getCurveControlPoints(edge, m_ctrlPoints,n1x,n1y,n2x,n2y);
                m_cubic.setCurve(n1x, n1y,
                                m_ctrlPoints[0].getX(), m_ctrlPoints[0].getY(),
                                m_ctrlPoints[1].getX(), m_ctrlPoints[1].getY(),
                                n2x, n2y);
                shape = m_cubic;
                break;
            default:
                throw new IllegalStateException("Unknown edge type");
        }
        
        // return the edge shape
        return shape;


	} // getRawShape

}