package action;

import java.util.ArrayList;

public class ActionListCreator 
{

	public static ArrayList<ActionInterface> getActionItems()
	{
		// Initialize arraylist
		ArrayList<ActionInterface> actionItems = new ArrayList<ActionInterface>();
		
		/*
		 * Add ActionsInterface objects to the arraylist here.
		 */
		//actionItems.add(new CheckModelActionView());
		actionItems.add(new RunSimulationAction());
		actionItems.add(new ParameterScanAction());
		//actionItems.add(new GenerateModelGuideAction());
		
		// return arraylist
		return actionItems;
	}
}
