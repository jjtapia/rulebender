package networkviewer.cmap;

import prefuse.action.assignment.ColorAction;
import prefuse.visual.VisualItem;

public class ComponentColorAction extends ColorAction {

	private int[] palette;

	public ComponentColorAction(String group, String colorField, int[] palette) {
		super(group, colorField);

		this.palette = palette;
	}

	public int getColor(VisualItem item) {
		int color_id = 0;

		String type = item.getString("type");

		if (type == null) {
			color_id = 0;
		}

		if (type.equals("component")) {
			color_id = 0;
			if (item.get("states") != null) {
				color_id = 2;
			}

			if (item.getBoolean("statechange") == true) {
				color_id = 4;
			}
		} else if (type.equals("state")) {
			color_id = 1;

			if (item.getBoolean("statechange") != true) {
				color_id = 3;
			}
		} else {
			color_id = 5;
		}

		return palette[color_id];
	}

}
