package rulebender.prefuse.networkviewer.contactmap;
import java.awt.geom.Point2D;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.SwingUtilities;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.controls.ControlAdapter;
import prefuse.data.Node;
import prefuse.data.Table;
import prefuse.data.event.EventConstants;
import prefuse.data.event.TableListener;
import prefuse.visual.AggregateItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;

/**
 * Interactive drag control that is "aggregate-aware"
 */
public class AggregateDragControl extends ControlAdapter implements TableListener {
	private boolean repaint = true;//, resetItem;
    private VisualItem activeItem;
    protected Point2D down = new Point2D.Double();
    protected Point2D temp = new Point2D.Double();
    protected boolean dragged;
    protected ArrayList<String> action = new ArrayList<String>();
    protected Visualization m_vis;
    protected String m_group;
    
    /**
     * Creates a new drag control that issues repaint requests as an item
     * is dragged.
     */
    public AggregateDragControl() {
    }
    
    /**
     * Creates a new drag control that issues repaint requests as an item
     * is dragged. Allows the repaint option to be disabled.
     */
    public AggregateDragControl(boolean _repaint, String _action) {
    	repaint = _repaint;
    	action.add(_action);
    }
    
    /**
     * Creates a new drag control that issues repaint requests as an item
     * is dragged. Allows the repaint option to be disabled.
     */
    public AggregateDragControl(boolean _repaint, String _action, Visualization vis, String group) {
    	repaint = _repaint;
    	action.add(_action);
    	m_vis = vis;
    	m_group = group;
    }
    
    
    /**
     * @see prefuse.controls.Control#itemEntered(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemEntered(VisualItem item, MouseEvent e) {
        Display d = (Display)e.getSource();
        d.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        activeItem = item;
        if ( !(item instanceof AggregateItem) )
            setFixed(item, true);
    }
    
    /**
     * @see prefuse.controls.Control#itemExited(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemExited(VisualItem item, MouseEvent e) {
        if ( activeItem == item ) {
            activeItem = null;
            setFixed(item, false);
        }
        Display d = (Display)e.getSource();
        d.setCursor(Cursor.getDefaultCursor());
    }
    
    /**
     * @see prefuse.controls.Control#itemPressed(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemPressed(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        dragged = false;
        Display d = (Display)e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), down);
        if ( item instanceof AggregateItem )
            setFixed(item, true);
    }
    
    /**
     * @see prefuse.controls.Control#itemReleased(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemReleased(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        if ( dragged ) {
            activeItem = null;
            setFixed(item, false);
            dragged = false;
        }            
    }
    
    /**
     * @see prefuse.controls.Control#itemDragged(prefuse.visual.VisualItem, java.awt.event.MouseEvent)
     */
    public void itemDragged(VisualItem item, MouseEvent e) {
        if (!SwingUtilities.isLeftMouseButton(e)) return;
        dragged = true;
        Display d = (Display)e.getComponent();
        d.getAbsoluteCoordinate(e.getPoint(), temp);
        double dx = temp.getX()-down.getX();
        double dy = temp.getY()-down.getY();
        
        move(item, dx, dy);
		
		if (item instanceof NodeItem) {
			String type = item.getString("type");
			if (type != null) {

				// move state nodes
				if (type.equals("component") && item.get("states") != null) {
					ArrayList<Node> state_nodes = (ArrayList<Node>) item
							.get("state_nodes");

					if (state_nodes != null) {

						for (int i = 0; i < state_nodes.size(); i++) {
							VisualItem stateitem = m_vis.getVisualItem(m_group,
									state_nodes.get(i));
							move(stateitem, dx, dy);
						}
					}
				}

				// move parent component node and other state nodes
				if (type.equals("state")) {
					// move parent component node
					Node comp = (Node) item.get("stateparent");
					VisualItem compitem = m_vis.getVisualItem(m_group, comp);
					move(compitem, dx, dy);
					// move other state nodes
					ArrayList<Node> state_nodes = (ArrayList<Node>) comp
							.get("state_nodes");

					if (state_nodes != null) {

						for (int i = 0; i < state_nodes.size(); i++) {

							VisualItem stateitem = m_vis.getVisualItem(m_group,
									state_nodes.get(i));

							if (!stateitem.equals(item)) {
								move(stateitem, dx, dy);
							}
						}
					}
				}
				else {
					
				}
			}
		}
        
        down.setLocation(temp);
        
        if ( repaint )
            item.getVisualization().repaint();
        
      //TODO
		// This whole thing is sloppy but it will work.  
        if ( action != null )
        {
        	for(String a : action)
        	{
        		try
        		{
        			d.getVisualization().run(a);
        		}
        		catch(Exception theException)
        		{
        			// Do nothing.
        		}
        	}
        }
    }

    public void addAction(String a)
    {
    	action.add(a);
    }
    
    protected static void setFixed(VisualItem item, boolean fixed) {
        if ( item instanceof AggregateItem ) {
            Iterator items = ((AggregateItem)item).items();
            while ( items.hasNext() ) {
                setFixed((VisualItem)items.next(), fixed);
            }
        } else {
            item.setFixed(fixed);
        }
    }
    
    protected static void move(VisualItem item, double dx, double dy) {
        if ( item instanceof AggregateItem ) {
            Iterator items = ((AggregateItem)item).items();
            while ( items.hasNext() ) {
                move((VisualItem)items.next(), dx, dy);
            }
        } else {
            double x = item.getX();
            double y = item.getY();
            item.setStartX(x);  item.setStartY(y);
            item.setX(x+dx);    item.setY(y+dy);
            item.setEndX(x+dx); item.setEndY(y+dy);
        }
    }

	public void tableChanged(Table t, int start, int end, int col, int type) {
	       if ( activeItem == null || type != EventConstants.UPDATE 
	                || col != t.getColumnNumber(VisualItem.FIXED) )
	            return;
	        int row = activeItem.getRow();
	        //if ( row >= start && row <= end )
	          //  resetItem = false;
	 
		
	}
	
	public void setVisAndGroup(Visualization vis, String group) {
		m_vis = vis;
		m_group = group;
	}
    
} // end of class AggregateDragControl