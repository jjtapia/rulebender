package rulebender.views.contactmap;

import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;

import org.antlr.runtime.RecognitionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

import bngparser.grammars.BNGGrammar.prog_return;

import rulebender.modelbuilders.BNGASTReader;
import rulebender.modelbuilders.CMapModelBuilder;
import rulebender.models.contactmap.CMapModel;
import rulebender.prefuse.contactmap.CMapVisual;
import rulebender.utility.BNGParserCommands;
import rulebender.views.ContactMapView;

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
	public prefuse.Display generateContactMap(String fileName)
	{
		prog_return ast = null;
		
		// Get the ast
		try 
		{
			ast = BNGParserCommands.getASTForFileName(fileName);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecognitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Null check for the ast
		if(ast == null)
		{
			System.out.println("The AST is null.\nExiting...");
		}
		else
		{
			// print it out if it is good.
			//System.out.println(ast.toString()+"\n\n================================================================");
		}
		
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

}
