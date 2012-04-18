package networkviewer;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import bubbleset.BubbleModel;

import prefuse.action.Action;
import prefuse.action.layout.Layout;
import prefuse.util.GraphicsLib;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.VisualItem;

public class AggregateBubbleLayout extends Layout 
{
	private int m_margin = 5; // convex hull pixel margin
	private double[] m_pts;   // buffer for computing convex hulls
		  
		
	public AggregateBubbleLayout(String aggrGroup) 
	{
		super(aggrGroup);
	}
	
	@Override
	public void run(double arg0) 
	{
		AggregateTable aggr = (AggregateTable)m_vis.getGroup(m_group);
		
		// do we have any  to process?
		int num = aggr.getTupleCount();
		if ( num == 0 ) return;
	
		// update buffers
		// I *think* 4*2*items is 4 for the bounding box points, 2 for x y 
		// coords, and the max is taken across all aggregateItems in the 
		// table so....?
		int maxsz = 0;
		for ( Iterator aggrs = aggr.tuples(); aggrs.hasNext();  )
		    maxsz = Math.max(maxsz, 4*2*
		            ((AggregateItem)aggrs.next()).getAggregateSize());
		
		if ( m_pts == null || maxsz > m_pts.length) 
		{
		    m_pts = new double[maxsz];
		}
		
		// compute and assign convex hull for each aggregate (bubble)
		Iterator aggrs = m_vis.visibleItems(m_group);
		while ( aggrs.hasNext() ) 
		{
			// Get the bubble
			AggregateItem aitem = (AggregateItem)aggrs.next();
		
		    int idx = 0;
		    if ( aitem.getAggregateSize() == 0 ) continue;
		    
		    VisualItem item = null;
		    Iterator iter = aitem.items();
		    // For each thing in the bubble
		    while ( iter.hasNext() ) 
		    {
		    	// Add it to the points array at the correct place
		        item = (VisualItem)iter.next();
		        if ( item.isVisible() ) {
		            addPoint(m_pts, idx, item, m_margin);
		            idx += 2*4;
		        }
		    }
		    // if no aggregates are visible, do nothing
			if ( idx == 0 ) continue;
			
			// compute convex hull with the pts array and the length of the 
			
			//double[] nhull = GraphicsLib.convexHull(m_pts, idx);
			// Compute BubbleSets here.
			
			BubbleModel bm = new BubbleModel(m_pts, idx, null, 0);
			
			double[] nhull = bm.getIsoContourPointsDoubleArray();
			
			// prepare viz attribute array
			float[]  fhull = (float[])aitem.get(VisualItem.POLYGON);
			if ( fhull == null || fhull.length < nhull.length )
			    fhull = new float[nhull.length];
			else if ( fhull.length > nhull.length )
			    fhull[nhull.length] = Float.NaN;
			
			// copy hull values
			for ( int j=0; j<nhull.length; j++ )
			    fhull[j] = (float)nhull[j];
			
			aitem.set(VisualItem.POLYGON, fhull);
			aitem.setValidated(false); // force invalidation
		}
	}
		
	private static void addPoint(double[] pts, int idx, 
		                             VisualItem item, int growth)
	{
	    Rectangle2D b = item.getBounds();
	    double minX = (b.getMinX())-growth, minY = (b.getMinY())-growth;
	    double maxX = (b.getMaxX())+growth, maxY = (b.getMaxY())+growth;
	    
	    /*
	    pts[idx]   = minX; pts[idx+1] = minY;
	    pts[idx+2] = minX; pts[idx+3] = maxY;
	    pts[idx+4] = maxX; pts[idx+5] = minY;
	    pts[idx+6] = maxX; pts[idx+7] = maxY;
	    */
	    pts[idx]   = minX; pts[idx+1] = minY;
	    pts[idx+2] = maxX; pts[idx+3] = minY;
	    pts[idx+4] = maxX; pts[idx+5] = maxY;
	    pts[idx+6] = minX; pts[idx+7] = maxY;
	}
	
} // end of class AggregateLayout