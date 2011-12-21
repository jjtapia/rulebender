package rulebender.contactmap.view;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import bngparser.grammars.BNGGrammar.prog_return;

import rulebender.contactmap.models.CMapModel;
import rulebender.contactmap.models.CMapModelBuilder;
import rulebender.contactmap.prefuse.ContactMapVisual;
import rulebender.editors.bngl.BNGLEditor;
import rulebender.editors.bngl.model.BNGASTReader;
import rulebender.editors.bngl.model.BNGLModel;

public class ContactMapSelectionListener implements ISelectionListener 
{
	private ContactMapView m_view;
	
	private BNGLModel m_currentModel;
	
	private HashMap<String, prefuse.Display> m_contactMapRegistry;
	
	public ContactMapSelectionListener(ContactMapView view)
	{
		setView(view);
		
		// Create the registry
		m_contactMapRegistry = new HashMap<String, prefuse.Display>();
		
		// Register the view as a listener for workbench selections.
		m_view.getSite().getPage().addPostSelectionListener(this);		
	}
	
	/**
	 * Called on a selection event. 
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
	{	
		// If it's from the BNGLEditor then we want to set the contact map.
		if(part.getClass() == BNGLEditor.class)
		{
			bnglEditorSelection(part, selection);
		}
		// If it's not a bngl file
		else
		{
			//m_currentModel = null;
			//m_view.setCMap(null);
		}
	}
	
	private void bnglEditorSelection(IWorkbenchPart part, ISelection selection)
	{
		// If it's the same file
		if(m_currentModel != null && part.getTitle().equals(m_currentModel.getPathID()))
		{
			//TODO it could be a text selection.
			//System.out.println("same file");
		}
		// If it's a different file, then call the local private method that 
		// handles it. 
		else
		{
			newBNGLFileSelected(part);
		}
	}
	
	
	/**
	 * Private method to handle when a new file is selected. 
	 */
	private void newBNGLFileSelected(IWorkbenchPart part)
	{
		// Set the current file.
		m_currentModel = ((BNGLEditor) part).getModel();
		
		if(!m_contactMapRegistry.containsKey(m_currentModel.getPathID()))
		{
			// Create a property changed listener for when files are saved.
			PropertyChangeListener pcl = new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent propertyChangeEvent) 
				{
					//DEBUG
					//System.out.println("PropertyChange Class: " + evt.getClass());
					//System.out.println("Property Changed: " + evt.getPropertyName());
					//System.out.println("Propogation ID: " + evt.getPropagationId());
					
					//Update the display object that is associated with the path and ast. 
					updateDisplayForPathAndAST(propertyChangeEvent.getPropertyName(), (prog_return) propertyChangeEvent.getNewValue());
				}};
				
				m_currentModel.addPropertyChangeListener(pcl); 
		}
		
		// Get an existing map.
		prefuse.Display toShow = lookupCurrentDisplay();
				
		// Set the correct contact map.
		m_view.setCMap(toShow);
	}
	
	private prefuse.Display lookupCurrentDisplay()
	{
		// Try to get an existing display.
		prefuse.Display toShow = m_contactMapRegistry.get(m_currentModel.getPathID());
		
		// If it does not exist yet then generate it and add it to the registry.
		if(toShow == null)
		{
			toShow = generateContactMap(m_currentModel.getAST());
			m_contactMapRegistry.put(m_currentModel.getPathID(), toShow);
		}
		
		// This can still be null if there was an error generating the ast.
		return toShow;
	}

	/**
	 * Generates a contact map using the antlr parser for a filename. 
	 * @param fileName
	 * @return
	 */
	public prefuse.Display generateContactMap(prog_return ast)
	{
		// If the ast has not been generated for this model, then 
		// just return null so there is not contact map displayed.
		if(ast == null)
		{
			return null;
		}
		
		// Create the builder for the cmap
		CMapModelBuilder cmapModelBuilder = new CMapModelBuilder();
		
		// Create the astReader and register the cmapModelBuilder
		BNGASTReader astReader = new BNGASTReader(cmapModelBuilder);
		
		// Use the reader to construct the model for the given ast.
		astReader.buildWithAST(ast);
		
		// Get the model from the builder.		
		CMapModel cModel = cmapModelBuilder.getCMapModel();
		
		//DEBUG
		// This should never happen.
		if(cModel == null)
		{
			System.out.println("The CMapModel is null.\nExiting");
			System.exit(0);
		}
		
		// Set a dimension
		Dimension dim = m_view.getSize();
		
		// Get the CMapVisual object for the CMapModel
		ContactMapVisual cVisual = new ContactMapVisual(cModel, dim);
		
		return cVisual.getDisplay();
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
		m_view.setCMap(lookupCurrentDisplay());
	}
	
	private void updateDisplayForPathAndAST(String path, prog_return ast)
	{
		// Clear the current entry.
		m_contactMapRegistry.remove(path);
		
		// Generate a new display
		prefuse.Display display = generateContactMap(ast);
		
		// Add the new representation.
		m_contactMapRegistry.put(path, display);
		
		// If it is the model that is currently displayed, 
		// then send the Display object to the view.
		if(path.equals(m_currentModel.getPathID()))
		{
			m_view.setCMap(display);
		}
	}

}
