package rulebender.speciesgraph.view;

import java.awt.Dimension;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import rulebender.core.utility.Console;
import rulebender.results.view.ResultsView;
import rulebender.speciesgraph.prefuse.SpeciesGraphVisual;

public class SpeciesGraphSelectionListener implements ISelectionListener
{
	private SpeciesGraphView m_view;
	
	public SpeciesGraphSelectionListener(SpeciesGraphView view)
	{
		setView(view);
		
		// Register the view as a listener for workbench selections.
		m_view.getSite().getPage().addPostSelectionListener(this);		
	}
	
	/**
	 * Called on a selection event. 
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
	{	
		// DEBUG
		System.out.println("Selection:\n\t" + "\tpart: " + part.getTitle() +
				           "\n\tselection: " + selection.toString());
		System.out.println("\tclass: " + part.getClass().toString());
		
		if(part.getClass() == ResultsView.class && !selection.equals(""))
		{
			m_view.setSpeciesGraph(generateSpeciesGraph(selection.toString()));
		}
		// If it's not a bngl file
		else
		{
			
		}
	}
	
	/**
	 * Generates a contact map using the antlr parser for a filename. 
	 * @param fileName
	 * @return
	 */
	public prefuse.Display generateSpeciesGraph(String speciesString)
	{
		System.out.println("***Generating Species Graph: " + speciesString);
		speciesString = speciesString.substring(1, speciesString.length()-1);
		System.out.println("***Cleaned Species Graph: " + speciesString);
		
		String[] splitTypeAndExpression = speciesString.split("\t");
		
		if(splitTypeAndExpression.length < 2)
		{
			return null;
		}
		
		System.out.println("\tLong enough...");
		
		String type = splitTypeAndExpression[0];
		String itemExp = splitTypeAndExpression[1];
		
		prefuse.Display toReturn = null;
		
		System.out.println("\ttype: " + type);
		
		if (type.equals("species") || type.equals("pattern")) 
		{
			// species
			String[] tmp = itemExp.split(" ");
			int id = Integer.parseInt(tmp[0]);
			String expression = "";
			
			if (tmp.length > 1) {
				expression = tmp[1];
			} 
			
			else {
				System.out.println("FIXME Problem Creating Species Graph!");
			}

			
			// TODO Add this label to the window somewhere.
			/*
			// label
			JLabel label = new JLabel(type + ": "
					+ itemExp.replaceFirst("-1", ""));
			panel.add(label);
	
			 */
			
			// Set a dimension
			Dimension dim = m_view.getSize();
			
			// Get the CMapVisual object for the CMapModel
			SpeciesGraphVisual sGraph = new SpeciesGraphVisual(id, expression, dim);
			
			// display
			toReturn = sGraph.getDisplay();
		}
		
		// TODO do not have rule vis yet.  Probably can stop differentiation here and awkward parameter string for this method.
		else if (type.equals("rule")) 
		{

		}
				
		return toReturn;
	}
	
	/**
	 * Used to set the view.
	 * @param view
	 */
	private void setView(SpeciesGraphView view) 
	{
		m_view = view;
	}
}
