package rulebender.prefuse.networkviewer.contactmap;

import java.awt.event.MouseEvent;



import prefuse.controls.ControlAdapter;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.visual.VisualItem;
import rulebender.prefuse.networkviewer.PrefuseTooltip;

public abstract class HoverTooltip extends ControlAdapter {
	PrefuseTooltip activeTooltip;

	public void itemExited(VisualItem item, MouseEvent e) {
		if(activeTooltip != null) {
			activeTooltip.stopShowing();
		}
	}
	
	public void itemEntered(VisualItem item, MouseEvent e) {
		if(item instanceof Node) {
			showNodeTooltip(item, e);
		} else if(item instanceof Edge) {
			showEdgeTooltip(item, e);
		}
		
		// Add aggregator
	}
	
	public void itemPressed(VisualItem item, java.awt.event.MouseEvent e) {
		if(activeTooltip != null) {
			activeTooltip.stopShowingImmediately();
		}
	}
	
	public void itemReleased(VisualItem item, java.awt.event.MouseEvent e) {
		if(item instanceof Node) {
		showNodeTooltip(item, e);
		} else if(item instanceof Edge) {
			showEdgeTooltip(item, e);
		}
	}
	
	protected abstract void showNodeTooltip(VisualItem item, java.awt.event.MouseEvent e); /* {
		Visualization v = item.getVisualization();
		
		
		  showTooltip(new GraphViewNodeTooltip(
		 
					(Display)e.getSource(),
					((prefuse.data.Node)v.getSourceTuple(item)).getString("name"),
					((prefuse.data.Node)v.getSourceTuple(item)).getString("gender")),
					item,
					e); 
					
		
	}
				*/
	protected abstract void showEdgeTooltip(VisualItem item, java.awt.event.MouseEvent e);
		/*
	 {
		Visualization v = item.getVisualization();
		Edge edge = (prefuse.data.Edge)v.getSourceTuple(item);
		String connector;
		
		if(edge.isDirected()) {
			connector = " -> ";
		} else {
			connector = " - ";
		}
		
		showTooltip(new InterlinkenEdgeTooltip(
					(Display)e.getSource(),
					edge.getSourceNode().getString("name") + connector + edge.getTargetNode().getString("name")),
					item,
	
					e);
	}
		*/
	
	
	protected void showTooltip(PrefuseTooltip ptt, VisualItem item, java.awt.event.MouseEvent e) {
		if(activeTooltip != null) {
			activeTooltip.stopShowingImmediately();
		}
		
		activeTooltip = ptt;

		activeTooltip.startShowing(e.getX(), e.getY());
	}
} // end HoverTooltip

