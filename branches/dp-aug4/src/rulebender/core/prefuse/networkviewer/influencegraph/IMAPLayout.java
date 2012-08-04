package rulebender.core.prefuse.networkviewer.influencegraph;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import prefuse.action.layout.Layout;
import prefuse.data.tuple.TupleSet;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * Layout action that positions visual items along a circle. By default, items
 * are sorted in the order in which they iterated over.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class IMAPLayout extends Layout {

	private double m_radius; // radius of the circle layout

	/**
	 * Create a CircleLayout; the radius of the circle layout will be computed
	 * automatically based on the display size.
	 * 
	 * @param group
	 *            the data group to layout
	 */
	public IMAPLayout(String group) {
		super(group);
	}

	/**
	 * Create a CircleLayout; use the specified radius for the the circle
	 * layout, regardless of the display size.
	 * 
	 * @param group
	 *            the data group to layout
	 * @param radius
	 *            the radius of the circle layout.
	 */
	public IMAPLayout(String group, double radius) {
		super(group);
		m_radius = radius;
	}

	/**
	 * Return the radius of the layout circle.
	 * 
	 * @return the circle radius
	 */
	public double getRadius() {
		return m_radius;
	}

	/**
	 * Set the radius of the layout circle.
	 * 
	 * @param radius
	 *            the circle radius to use
	 */
	public void setRadius(double radius) {
		m_radius = radius;
	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	public void run(double frac) {
		TupleSet ts = m_vis.getGroup(m_group);

		Rectangle2D r = getLayoutBounds();
		double height = r.getHeight();
		double width = r.getWidth();
		double cx = r.getCenterX();
		double cy = r.getCenterY();

		double radius = m_radius;
		if (radius <= 0) {
			radius = 0.45 * (height < width ? height : width);
		}

		// sorting items

		// sorted node items
		ArrayList<VisualItem> sortedItems = new ArrayList<VisualItem>();
		// original node items
		ArrayList<VisualItem> originalItems = new ArrayList<VisualItem>();
		// influence node items array
		ArrayList[] iarray;

		// get all items from graph tuple set
		Iterator items = ts.tuples();

		// set original nodes items
		while (items.hasNext()) {
			VisualItem n = (VisualItem) items.next();
			if (n instanceof NodeItem)
				originalItems.add(n);
		}

		// initialize iarray
		iarray = new ArrayList[originalItems.size()];

		// set influence node items array
		items = ts.tuples();
		while (items.hasNext()) {
			VisualItem e = (VisualItem) items.next();
			if (e instanceof EdgeItem) {
				// get src node
				VisualItem srcnode = ((EdgeItem) e).getSourceItem();
				// get tar node
				VisualItem tarnode = ((EdgeItem) e).getTargetItem();

				// get index of the src node in list of originalItems
				int index_src = originalItems.indexOf(srcnode);
				// initialize iarray[index] if null
				if (iarray[index_src] == null) {
					iarray[index_src] = new ArrayList<VisualItem>();
				}
				// add tar node to iarray[index] if not exists
				if (!iarray[index_src].contains(tarnode)) {
					iarray[index_src].add(tarnode);
				}

				// get index of the tar node in list of originalItems
				int index_tar = originalItems.indexOf(tarnode);
				// initialize iarray[index] if null
				if (iarray[index_tar] == null) {
					iarray[index_tar] = new ArrayList<VisualItem>();
				}
				// add src node to iarray[index] if not exists
				if (!iarray[index_tar].contains(srcnode)) {
					iarray[index_tar].add(srcnode);
				}
			}
		}

		// set sorted items based on influence node items array

		for (int t = 0; t < originalItems.size(); t++) {
			if (!sortedItems.contains(originalItems.get(t))) {
				sortedItems.add(originalItems.get(t));
			}

			// counter for sorted items
			int counter = 0;
			while (counter < sortedItems.size()) {
				// get current item in sorted list
				VisualItem cur = sortedItems.get(counter);
				// get the index of cur in original list
				int index = originalItems.indexOf(cur);
				// get the list of influence items at index
				ArrayList ilist = iarray[index];

				// empty list means the rule doesn't influence any other rules
				if (ilist == null) {
					counter++;
					continue;
				}

				// add the influence items to sorted list if not exist
				for (int i = 0; i < ilist.size(); i++) {
					// get the current influence item
					VisualItem curInf = (VisualItem) ilist.get(i);
					// if not exist
					if (!sortedItems.contains(curInf)) {
						// add to sorted list
						sortedItems.add(curInf);
					}
				}
				counter++;
			}
		}

		int nn = sortedItems.size();

		// set layout based on sorted node items
		double angle, x, y, x0 = 0;

		// for (int i=0; i < originalItems.size(); i++) {
		// VisualItem n = originalItems.get(i);

		int curOffset = 0;
		for (int i = 0; i < sortedItems.size(); i++) {
			VisualItem n = sortedItems.get(i);

			/*
			 * // circle angle = Math.PI + (2*Math.PI*i) / nn; x =
			 * Math.cos(angle)*radius + cx; y = Math.sin(angle)*radius + cy;
			 * 
			 * 
			 * if (i == 0) { x0 = x; } else { if (angle > 2 * Math.PI) { double
			 * tmpoffset = 2 * radius - (x -x0); x = x0 + 2 * radius +
			 * tmpoffset; }
			 * 
			 * }
			 */

			// line
			String label = (String)(n.get(VisualItem.LABEL));
			// set the separation be the length of the node label
			int separation = label.length() * 10;
			// set the minimum separation
			if (separation < 80) {
				separation = 80;
			}
			separation += nn * 6;
			
			curOffset += separation;
			x = cx + curOffset;
			y = cy;

			setX(n, null, x);
			setY(n, null, y);

		}
	}

} // end of class CircleLayout
