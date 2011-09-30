package rulebender.views.influencegraph;

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
import rulebender.views.InfluenceGraphView;

public class InfluenceGraphSelectionListener implements ISelectionListener 
{
	private ContactMapView m_view;
	
	private String currentFile;
	
	private HashMap<String, prefuse.Display> contactMapRegistry;
	
	public InfluenceGraphSelectionListener(InfluenceGraphView influenceGraphView)
	{
//		setView(view);
		
		// Create the registry
		contactMapRegistry = new HashMap<String, prefuse.Display>();
		
		// Register the view as a listener for workbench selections.
		m_view.getSite().getPage().addPostSelectionListener(this);
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	public void tempRefresh() {
		// TODO Auto-generated method stub
		
	}
}
