package action;

import java.util.ArrayList;

public class ToolBarActionListCreator {
	public static ArrayList<ActionInterface> getActionItems() {
		// Initialize arraylist
		ArrayList<ActionInterface> actionItems = new ArrayList<ActionInterface>();

		/*
		 * Add ActionsInterface objects to the arraylist here, and they will be
		 * added to the toolbar.
		 */
		actionItems.add(new NewAction());
		actionItems.add(new OpenAction());
		actionItems.add(new SaveAction());
		actionItems.add(new SaveAsAction());
		actionItems.add(new FindAction());
		actionItems.add(new VisualizeAction());
		actionItems.add(new RunSimulationAction());
		actionItems.add(new ParameterScanAction());

		// return arraylist
		return actionItems;
	}
}
