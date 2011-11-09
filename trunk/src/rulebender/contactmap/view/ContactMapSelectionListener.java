package rulebender.contactmap.view;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;

import org.antlr.runtime.RecognitionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import bngparser.grammars.BNGGrammar.prog_return;

import rulebender.contactmap.models.CMapModel;
import rulebender.contactmap.prefuse.CMapVisual;
import rulebender.core.modelcollection.ModelCollectionController;
import rulebender.modelbuilders.BNGASTReader;
import rulebender.modelbuilders.CMapModelBuilder;
import rulebender.utility.BNGParserCommands;

public class ContactMapSelectionListener implements ISelectionListener 
{
	private ContactMapView m_view;
	
	private String currentFile;
	
	private HashMap<String, prefuse.Display> contactMapRegistry;
	
	public ContactMapSelectionListener(ContactMapView view)
	{
		setView(view);
		
		// Create the registry
		contactMapRegistry = new HashMap<String, prefuse.Display>();
		
		// Register the view as a listener for workbench selections.
		m_view.getSite().getPage().addPostSelectionListener(this);
		
		// Create a property changed listener for when files are saved.
		PropertyChangeListener pcl = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent evt) 
			{
				//Get the name of the path. 
				String path = evt.getPropertyName();
				updateASTForPath(path);
			}};
		
		ModelCollectionController.getModelCollectionController().addProptertyChangeListener(pcl);
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
		
		if(part.getClass().toString().contains("rulebender.editors"))
		{
			editorSelection(part, selection);
		}
	}
	
	private void editorSelection(IWorkbenchPart part, ISelection selection)
	{
		// Check to see if it is the editor.
		// TODO  Right now I am just seeing if the name of the selection is a path to a bngl file. 
		// There is probably a better way to do this.
		if(part.getTitle().contains(".") && part.getTitle().substring(part.getTitle().lastIndexOf(".")).equals(".bngl"))
		{
			// If it's the same file
			if(part.getTitle().equals(currentFile))
			{
				//TODO it could be a text selection.
				System.out.println("same file");
			}
			// If it's a different file, then call the local private method that 
			// handles it. 
			else
			{
				newFileSelected(part.getTitle());
			}
		}
		// If it's not a bngl file
		else
		{
			currentFile = "";
			m_view.setCMap(null);
		}
	}
		 
	/**
	 * Generates a contact map using the antlr parser for a filename. 
	 * @param fileName
	 * @return
	 */
	
	/*
	continueu here.  I am not sure how to update the models.  In general, I would like to 
	have the editor tell the controller when something has been saved, then the collection controller
	can interact with the model collection and create the new models.  The property changed
	events will tell each of the views that a new ast has been generated (which is done now).
	The problem here is that the editor does not do anything special when a file is opened, 
	
	So, I could just generate a new ast every time there is a non-hit in the hashtable.  
	
	I also need to organize the way that the errors are reported.  All of these things will
	be closely tied together, but I want to keep things as modular as possible.  I either need 
	to completely separate the generation from the error reporting, or mentally bundle everything into 
	the same package (generate->report->store->propertychangedevent.)
	*/
	public prefuse.Display generateContactMap(String fileName)
	{
		
		prog_return ast = ModelCollectionController.getModelCollectionController().getModel(fileName);
		
		// Set a dimension TODO get the correct dimension
		Dimension dim = m_view.getSize();

		// Create the builder for the cmap
		CMapModelBuilder cmapModelBuilder = new CMapModelBuilder();
		// Create the astReader and register the cmapModelBuilder
		BNGASTReader astReader = new BNGASTReader(cmapModelBuilder);
		// Use the reader to construct the model for the given ast.
		astReader.buildWithAST(ast);
		// Get the model from the builder.		
		CMapModel cModel = cmapModelBuilder.getCMapModel();
		
		if(cModel == null)
		{
			System.out.println("The CMapModel is null.\nExiting");
			System.exit(0);
		}

		// Get the CMapVisual object for the CMapModel
		CMapVisual cVisual = new CMapVisual(cModel, dim);
		
		return cVisual.getDisplay();
	}
	
	/**
	 * Private method to handle when a new file is selected. 
	 */
	private void newFileSelected(String fileName)
	{
		// Set the current file.
		currentFile = fileName;
		
		// Clear the contact map
		m_view.setCMap(null);
		
		// Try to get an existing map.
		prefuse.Display toShow = contactMapRegistry.get(currentFile);
		
		// If it does not exist yet then generate it and add it to the registry.
		if(toShow == null)
		{
			toShow = generateContactMap(currentFile);
			contactMapRegistry.put(currentFile, toShow);
		}
		
		// Set the correct contact map.
		m_view.setCMap(toShow);
	}
	

	/**
	 * Used to set the view.
	 * @param view
	 */
	private void setView(ContactMapView view) 
	{
		m_view = view;
	}
	
	/**
	 *TODO
	 * Temporary way to refresh the contact map.
	 * This will be deleted (or changed) when the editor can
	 * send the refresh actions. 
	 */
	public void tempRefresh()
	{
		m_view.setCMap(null);
		m_view.setCMap(generateContactMap(currentFile));
	}
	
	private void updateASTForPath(String path)
	{
		System.out.println("Updating:" + path);
		// Clear the current entry.
		contactMapRegistry.remove(path);
		
		// Add the new representation.
		contactMapRegistry.put(path, generateContactMap(path));
	}

}
