package rulebender.core.prefuse.networkviewer.contactmap;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;

import prefuse.action.layout.Layout;
import prefuse.data.Node;
import prefuse.visual.VisualItem;

public class ComponentLayout extends Layout {

	/**
	 * Create a new SpecifiedLayout.
	 * 
	 * @param group
	 *            the data group to layout
	 */
	public ComponentLayout(String group) {
		super(group);

	}

	/**
	 * @see prefuse.action.Action#run(double)
	 */
	public void run(double frac) {
		// set position for state nodes
		Iterator iter = m_vis.items(m_group);
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			try {
				// get node type
				String nodetype = item.getString("type");
				if (nodetype != null && nodetype.equals("component")) {
					ArrayList<Node> state_nodes = (ArrayList<Node>)item.get("state_nodes");
					
					if (state_nodes == null)
						continue ;
					
					int offset = 0;
					
					for (int i = 0; i < state_nodes.size(); i++) {
						VisualItem stateitem = m_vis.getVisualItem(m_group, state_nodes.get(i));
						
						// get bounds of parent component
						Rectangle2D compBounds = item.getBounds();
						// get bounds of state
						Rectangle2D stateBounds = stateitem.getBounds();
						
						
						if (stateitem.isVisible()) {
							offset ++;
							// set position
							setX(stateitem, null, compBounds.getX() + compBounds.getWidth() + stateBounds.getWidth()/2 - 2);
							setY(stateitem, null, compBounds.getY() + offset * compBounds.getHeight());
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
