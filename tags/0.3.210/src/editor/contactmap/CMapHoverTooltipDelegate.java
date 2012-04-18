package editor.contactmap;

import java.awt.event.MouseEvent;

import networkviewer.BondTooltip;
import networkviewer.ComponentTooltip;
import networkviewer.HoverTooltip;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.visual.VisualItem;

public class CMapHoverTooltipDelegate extends HoverTooltip {

	// A const for the string value of the molecule column in the nodes
	private static final String COMP_PARENT_LABEL = "molecule";
	
	@Override
	protected void showEdgeTooltip(VisualItem item, MouseEvent e) 
	{
		showTooltip(new BondTooltip((Display) e.getSource()), item, e);
	}

	@Override
	protected void showNodeTooltip(VisualItem item, MouseEvent e) 
	{
		Visualization v = item.getVisualization();
	
		if(item.get("states") != null)
			showTooltip(new ComponentTooltip(
						(Display)e.getSource(),
						((prefuse.data.Node)v.getSourceTuple(item)).getString(VisualItem.LABEL),
						((prefuse.data.Node)v.getSourceTuple(item)).getString(COMP_PARENT_LABEL)),
						item, e);
	}
}