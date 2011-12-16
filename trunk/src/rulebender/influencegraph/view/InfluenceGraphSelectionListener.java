package rulebender.influencegraph.view;

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
import rulebender.contactmap.models.CMapModelBuilder;
import rulebender.contactmap.prefuse.CMapVisual;
import rulebender.contactmap.view.ContactMapView;
import rulebender.core.utility.BNGParserCommands;
import rulebender.editors.bngl.BNGLEditor;
import rulebender.editors.bngl.model.BNGASTReader;
import rulebender.editors.bngl.model.BNGLModel;
import rulebender.influencegraph.models.InfluenceGraphModel;
import rulebender.influencegraph.models.InfluenceGraphModelBuilder;
import rulebender.influencegraph.prefuse.IGraphVisual;

public class InfluenceGraphSelectionListener implements ISelectionListener 
{
	private InfluenceGraphView m_view;
	
	private String currentFile;
	
	BNGLModel currentModel;
	
	private HashMap<String, prefuse.Display> influenceGraphRegistry;
	
	public InfluenceGraphSelectionListener(InfluenceGraphView influenceGraphView)
	{
		setView(influenceGraphView);
		
		// Create the registry
		influenceGraphRegistry = new HashMap<String, prefuse.Display>();
		
		// Register the view as a listener for workbench selections.
		m_view.getSite().getPage().addPostSelectionListener(this);
	}

	private void setView(InfluenceGraphView view) 
	{
		m_view = view;	
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
	{
		if(part.getClass() == BNGLEditor.class)
		{
			//DEBUG 
			System.out.println("********* SELECTION OF BGNL EDITOR ****************");
			bnglEditorSelection(part, selection);
		}
		// If it's not a bngl file
		else
		{
			currentModel = null;
			m_view.setIGraph(null);
		}
	}
	
	private void bnglEditorSelection(IWorkbenchPart part, ISelection selection)
	{
		// If it's the same file
		if(currentModel != null && part.getTitle().equals(currentModel.getPathID()))
		{
			//TODO it could be a text selection.
			System.out.println("same file");
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
		currentModel = ((BNGLEditor) part).getModel();
		
		if(!influenceGraphRegistry.containsKey(currentModel.getPathID()))
		{
			// Create a property changed listener for when files are saved.
			PropertyChangeListener pcl = new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent evt) 
				{
					//DEBUG
					System.out.println("PropertyChange Class: " + evt.getClass());
					System.out.println("Property Changed: " + evt.getPropertyName());
					System.out.println("Propogation ID: " + evt.getPropagationId());
					
					//Update the display object that is associated with the path and ast. 
					updateDisplayForPathAndAST(evt.getPropertyName(), (prog_return) evt.getNewValue());
				}};
				
				currentModel.addPropertyChangeListener(pcl); 
		}
		
		// Clear the contact map
		m_view.setIGraph(null);
		
		// Get an existing map.
		prefuse.Display toShow = lookupCurrentDisplay();
				
		// Set the correct contact map.
		m_view.setIGraph(toShow);
	}
	
	/**
	 * Generates a contact map using the abstract syntax tree. 
	 * @param fileName
	 * @return
	 */
	
	//FIXME All of this needs to be rewritten.  Look at the contactmap selection listener to see it. 
	private prefuse.Display generateInfluenceGraph(prog_return ast)
	{
		
		// If the ast has not been generated for this model, then 
		// just return null so there is not contact map displayed.
		if(ast == null)
		{
			return null;
		}
		
		// Create the builder for the cmap
		InfluenceGraphModelBuilder iGraphModelBuilder = new InfluenceGraphModelBuilder();
		// Create the astReader and register the cmapModelBuilder
		BNGASTReader astReader = new BNGASTReader(iGraphModelBuilder);
		// Use the reader to construct the model for the given ast.
		astReader.buildWithAST(ast);
		// Get the model from the builder.		
		InfluenceGraphModel iModel = iGraphModelBuilder.getIGraphModel();
		
		// This should never happen. If the ast is not null (already checked)
		// then a null InfluenceGraphModel indicates a fatal error.
		if(iModel == null)
		{
			System.out.println("The CMapModel is null.\nExiting");
			System.exit(0);
		}

		// Set a dimension TODO get the correct dimension
		Dimension dim = m_view.getSize();

		// Get the CMapVisual object for the CMapModel
		IGraphVisual iVisual = new IGraphVisual(iModel, dim);
		
		return iVisual.getDisplay();
	}
	
	/**
	 *TODO
	 * Temporary way to refresh the contact map.
	 * This will be deleted (or changed) when the editor can
	 * send the refresh actions. 
	 */
	public void tempRefresh()
	{
		m_view.setIGraph(null);
		m_view.setIGraph(lookupCurrentDisplay());
	}

	private prefuse.Display lookupCurrentDisplay()
	{
		// Try to get an existing display.
		prefuse.Display toShow = influenceGraphRegistry.get(currentModel.getPathID());
		
		// If it does not exist yet then generate it and add it to the registry.
		if(toShow == null)
		{
			toShow = generateInfluenceGraph(currentModel.getAST());
			influenceGraphRegistry.put(currentModel.getPathID(), toShow);
		}
		
		// This can still be null if there was an error generating the ast.
		return toShow;
	}

	private void updateDisplayForPathAndAST(String path, prog_return ast)
	{
		System.out.println("Updating:" + path);
		// Clear the current entry.
		influenceGraphRegistry.remove(path);
		
		prefuse.Display display = generateInfluenceGraph(ast);
		
		// Add the new representation.
		influenceGraphRegistry.put(path, display);
		
		if(path.equals(currentModel.getPathID()))
		{
			m_view.setIGraph(display);
		}
	}

}
