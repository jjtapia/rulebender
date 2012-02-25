package rulebender.contactmap.view;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.FileEditorInput;

import bngparser.grammars.BNGGrammar.prog_return;

import rulebender.contactmap.models.ContactMapModel;
import rulebender.contactmap.models.CMapModelBuilder;
import rulebender.contactmap.prefuse.ContactMapVisual;
import rulebender.editors.bngl.BNGLEditor;
import rulebender.editors.bngl.model.BNGASTReader;
import rulebender.editors.bngl.model.BNGLModel;
import rulebender.errorview.model.BNGLError;

public class ContactMapSelectionListener implements ISelectionListener, IPartListener2
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
		m_view.getSite().getPage().addPartListener(this);
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
		}
	}
	
	private void setCurrentModel(BNGLModel model)
	{
		m_currentModel = model;
		
		// Get an existing map.
		prefuse.Display toShow = lookupDisplay(m_currentModel);
				
		// Set the correct contact map.
		m_view.setCMap(toShow);		
		
		// Activate the part.  This is so the property view will add listeners
		// to the view.
		m_view.getViewSite().getPage().activate(m_view.getViewSite().getPart());
		
	}
	
	private void bnglEditorSelection(IWorkbenchPart part, ISelection selection)
	{
		String osString = ((FileEditorInput) ((BNGLEditor) part).getEditorInput()).getPath().toOSString();
		
		// If it's the same file
		if(m_currentModel != null && osString.equals(m_currentModel.getPathID()))
		{
			//TODO it could be a text selection.
			System.out.println("current");
		}

		// If it's a different file, then call the local private method that 
		// handles it. 
		else
		{
			System.out.println("File selected seen in cmap");
			
			setCurrentModel(((BNGLEditor) part).getModel());
		}
	}
	
	/**
	 * Can be null if there is no Display (bngl compile error)
	 * @param model
	 * @return
	 */
	private prefuse.Display lookupDisplay(BNGLModel model)
	{	
		return m_contactMapRegistry.get(model.getPathID());
	}

	/**
	 * Generates a contact map using the abstract syntax tree from 
	 * the bngl parser.  
	 *  
	 * @param sourcePath The path of the resource.  This is used for selection later.   
	 * @param ast - The abstract syntax tree for the bngl model.
	 * @return
	 */
	private prefuse.Display generateContactMap(String sourcePath, prog_return ast)
	{	
		// If the ast has not been generated for this model, then 
		// just return null so there is not contact map displayed.
		// Also, an empty file produce a complete ast, so the length
		// requirement catches that.
		
		if(ast == null || ast.toString() == null || ast.toString().split("\\n").length <= 20)
		{
			return null;
		}
		
		// Create the builder for the cmap
		CMapModelBuilder cmapModelBuilder = new CMapModelBuilder();
		
		// Create the astReader and register the cmapModelBuilder
		BNGASTReader astReader = new BNGASTReader(cmapModelBuilder);
		
		// Use the reader to construct the model for the given ast.
		// Sometimes an ast is not null, but is not complete due to errors. 
		// This try/catch block catches those situations.
		try
		{
			astReader.buildWithAST(ast);	
		}
		catch(NullPointerException e)
		{
			// e.printStackTrace();
			//Debug
			System.out.println("Failed to produce CMapModel on ast:\n" + ast.toString());
		 	return null;
		}
		
		// Get the model from the builder.		
		ContactMapModel cModel = cmapModelBuilder.getCMapModel();
		cModel.setSourcePath(sourcePath);
		
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
		ContactMapVisual cVisual = new ContactMapVisual(m_view, cModel, dim);
		
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
	
	private void updateDisplayForPathAndAST(String path, prog_return ast)
	{
		// Clear the current entry.
		m_contactMapRegistry.remove(path);
		
		// Generate a new display
		prefuse.Display display = generateContactMap(path, ast);
		
		// Add the new representation.
		m_contactMapRegistry.put(path, display);
		
		// If it is the model that is currently displayed, 
		// then send the Display object to the view.
		if(path.equals(m_currentModel.getPathID()))
		{
			m_view.setCMap(display);
		}
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) 
	{
		
		if(partRef.getId().equals("rulebender.editors.bngl"))
		{	
			// Set the current file.
			setCurrentModel(((BNGLEditor) partRef.getPart(false)).getModel());
		
				// Create a property changed listener for when files are saved.
		PropertyChangeListener pcl = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent propertyChangeEvent) 
			{
				String filePath = ((BNGLModel) propertyChangeEvent.getSource()).getPathID();
				String propertyName = propertyChangeEvent.getPropertyName();
				
				if(propertyName.equals(BNGLModel.AST))
				{
					//Update the display object that is associated with the path and ast. 
					System.out.println("Property Changed on AST");
					updateDisplayForPathAndAST(filePath, (prog_return) propertyChangeEvent.getNewValue());
				}
				else if(propertyName.equals(BNGLModel.ERRORS))
				{
					// Don't care.
				}
			}
		};
			
		m_currentModel.addPropertyChangeListener(pcl);
		
		// generate the display and add the initial cmap to the registry.
		
		m_contactMapRegistry.put(m_currentModel.getPathID(), generateContactMap(m_currentModel.getPathID(), m_currentModel.getAST()));
		
		m_view.setCMap(lookupDisplay(m_currentModel));
		
		} // end if it's an editor block
	}


	private void bnglFileClosed(String path)
	{
		m_contactMapRegistry.remove(path);
		if(m_contactMapRegistry.size() == 0)
		{
			m_view.setCMap(null);
		}
		// The selection of any remaining files occurs before
		// the close event, don't worry about changing the 
		// m_currentModel
		
	}
	
	@Override
	public void partClosed(IWorkbenchPartReference partRef) 
	{
		if(partRef.getId().equals("rulebender.editors.bngl"))
		{			
			BNGLEditor editor = ((BNGLEditor)partRef.getPart(false));
			String osString = ((FileEditorInput) editor.getEditorInput()).getPath().toOSString();
			
			bnglFileClosed(osString);
		}
		
	}
	@Override
	public void partActivated(IWorkbenchPartReference partRef) 
	{
		
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) 
	{
		
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) 
	{
		
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) 
	{
		
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) 
	{
		
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) 
	{
		
	}

}
