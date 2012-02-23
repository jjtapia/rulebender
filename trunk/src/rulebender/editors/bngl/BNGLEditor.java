package rulebender.editors.bngl;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.ralfebert.rcp.tools.preferredperspective.IPrefersPerspective;

import bngparser.BNGParseData;
import bngparser.BNGParserUtility;
import bngparser.grammars.BNGGrammar.prog_return;
import rulebender.contactmap.properties.StatePropertySource;
import rulebender.contactmap.view.ContactMapView;
import rulebender.core.utility.ANTLRFilteredPrintStream;
import rulebender.core.utility.Console;
import rulebender.core.utility.FileInputUtility;
import rulebender.editors.bngl.BNGLConfiguration;
import rulebender.editors.bngl.BNGLDocumentProvider;
import rulebender.editors.bngl.model.BNGLModel;
import rulebender.errorview.model.BNGLError;

/**
 * This class defines the editor for bngl. 
 * 
 * The ISelectionListener implementation listens for selections in the tool, and 
 * IPrefersPerspective is for loading a perspective when an editor is loaded (this is not used at the moment).    
 * IPreferse
 * @author adammatthewsmith
 *
 */
public class BNGLEditor extends TextEditor implements ISelectionListener, IPrefersPerspective 
{
	// The model for the text
	private BNGLModel m_model;
	// The color manager for the syntax highlighting.
	private BNGLColorManager m_colorManager;
	
	//private String m_path;
	
	public BNGLEditor() 
	{
		// Call the TextEditor constructor
		super();
		
		// Create the colormanager.
		m_colorManager = new BNGLColorManager();
		
		// Set the SourceViewerConfiguration which takes care of many different
		// types of configs and decoration.
		setSourceViewerConfiguration(new BNGLConfiguration(m_colorManager));
		
		// Set the DocumentProvider which handles the representation of the file
		// and how the text is partitioned.
		setDocumentProvider(new BNGLDocumentProvider());
		
		// Can't create the m_model here because we need the part name,
		// and the part name is not set until after the constructor.  
		// So i used a lazy load in the getter.
	
		// Register with the ISelectionService
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPostSelectionListener(this);
  	}
	
	
	/*
	@Override
	public void initializeEditor()
	{
		super.initializeEditor();
		
	}
	
	*/
	
	@Override
	public void editorSaved()
	{	
		clearErrorMarkers();
				
		setAST(getAST());
		
	}	
	
	private void clearErrorMarkers() 
	{
		//Get the ifile reference for this editor input.
		IFile file = ((FileEditorInput) ((IEditorInput) getEditorInput())).getFile();
	
		try 
		{
			file.deleteMarkers("rulebender.markers.errormarker", true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) 
		{
			e.printStackTrace();
		}
	}


	private BNGParseData produceParseData()
	{
		//  Get the text in the document.
		String text = this.getSourceViewer().getDocument().get();
		
		return BNGParserUtility.produceParserInfoForBNGLText(text);
	}
	
	private void setAST(prog_return ast)
	{	
		if (m_model == null)
		{
			m_model = new BNGLModel(this.getPartName());
			m_model.setAST(ast);
		}
		else
		{
			m_model.setAST(ast);
		}
	}
	
	/**
	 * Getter for the ast data structure.  The 
	 * @return
	 */
	public BNGLModel getModel()
	{
		if(m_model == null)
		{
			m_model = new BNGLModel(getTitle());
			m_model.setAST(getAST());
			
		}
		
		return m_model;
	}

	/**
	 * Returns the AST for the text in the editor, or null if there are errors. 
	 * Also, the errors are reported to the console.
	 * 
	 * @return prog_return or NULL 
	 * 
	 */
	private prog_return getAST()
	{	
		// The abstract syntax tree that will be returned.  On a failure, it will be null. 
		prog_return toReturn = null;
		
		// Save a link to the orinal error out.
		PrintStream old = System.err;
		

		Console.clearConsole(getTitle());
		
		// Set the error out to a new printstream that will only display the antlr output.
		ANTLRFilteredPrintStream errorStream = new ANTLRFilteredPrintStream(Console.getMessageConsoleStream(getTitle()), getTitle(), old, getTitle()); 
		System.setErr(errorStream);
		
		try
		{
			toReturn = produceParseData().getParser().prog();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.out.println("Caught in the getAST Method.");
			
			//DEBUG
			//Console.displayOutput(getTitle(), getTitle() + " Errors:");
		}	
		
		setErrors(errorStream.getErrorList());
		
		System.err.flush();
		System.setErr(old);
		
		return toReturn;
	}

	private void setErrors(ArrayList<BNGLError> errorList) 
	{
		// Add the error list to the model
		m_model.setErrors(errorList);
		
		// Get the document.
		IDocument document = getDocumentProvider().getDocument(getEditorInput());
	
		// Get the ifile reference for this editor input.
		IFile file = ((FileEditorInput) ((IEditorInput) getEditorInput())).getFile();
			
		// Create a reference to a region that will be used to hold information
		// about the error location.
		IRegion region = null;
		
		// Set the annotations.
		for(BNGLError error : errorList)
		{
			// Get the information about the location.
			try 
			{
				region = document.getLineInformation(error.getLineNumber()-1);
			}
			catch (BadLocationException exception) 
			{
				exception.printStackTrace();
			}
			
			//make a marker
			IMarker marker = null;
			try
			{
				marker = file.createMarker("rulebender.markers.bnglerrormarker");
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				marker.setAttribute(IMarker.MESSAGE, error.getMessage());
				marker.setAttribute(IMarker.LINE_NUMBER, error.getLineNumber()-1);
				marker.setAttribute(IMarker.CHAR_START, region.getOffset());
				marker.setAttribute(IMarker.CHAR_END, region.getOffset()+region.getLength());
				
				System.out.println("Made the marker!");
			}
			catch(Exception exception)
			{
				exception.printStackTrace();
			}
		}
		
		//IFile thisResource = getDocumentProvider().getDocument(getEditorInput());
		
		/* This stuff might not be necessary. 
		//The DocumentProvider enables to get the document currently loaded in the editor
		IDocumentProvider idp = getDocumentProvider();
		//This is the document we want to connect to. This is taken from
		//the current editor input.
		IDocument document = idp.getDocument(getEditorInput());
		//The IannotationModel enables to add/remove/change annotation to a Document
		//loaded in an Editor
		IAnnotationModel iamf = idp.getAnnotationModel(getEditorInput());
		//Note: The annotation type id specify that you want to create one of your
		//annotations
		SimpleMarkerAnnotation ma = new SimpleMarkerAnnotation(“rulebender.markers.testmarker”, marker);
		
		//Finally add the new annotation to the model
		iamf.connect(document);
		iamf.addAnnotation(ma, newPosition(selection.ggetOffset(), selection.getLength()));
		iamf.disconnect(document);
		*/
	}

	public void dispose() 
	{
		clearErrorMarkers();
		
		super.dispose();
	}
	
	@Override
	public boolean isEditable()
	{
		return true;
	}
	
	@Override
	public boolean isEditorInputModifiable() {
	    return true;
	}

	@Override
	public boolean isEditorInputReadOnly() {
	    return false;
	}


	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
	{
		System.out.println(getEditorInput().getName() + " sees it.=================");
		
		// If it's from the contact map.
		if (part.getClass() == ContactMapView.class)
		{
			IStructuredSelection iSSelection = (IStructuredSelection) selection;
			Object propertyItem = iSSelection.getFirstElement();
			
			if(propertyItem instanceof StatePropertySource)
			{
				
				stateSelected((StatePropertySource) propertyItem);
			}		
		}
		else
		{
			
		}	
	}
	
	private void stateSelected(StatePropertySource stateSource) 
	{
		//String component = stateSource.getComponent();
		//String name = stateSource.getName();
		
	//	selectFromRegExp(component + "~" + name); 
	}
	
//	private void selectFromRegExp(String regExp)
//	{
//
//		ITextEditor editor = (ITextEditor) this.getAdapter(ITextEditor.class);
//				
//		IDocumentProvider provider = editor.getDocumentProvider();
//		IDocument document = provider.getDocument(this.getEditorInput());
//			
//		Pattern p = Pattern.compile(regExp);
//		Matcher m = p.matcher(document.get());
//		
//		
//		while(m.find()) 
//        {
//
//		} 
//	}
//		
	public void selectALine(String path, int num)
	{		
		/*
		 * Now we select the line. 
		 */
		ITextEditor editor = (ITextEditor) this.getAdapter(ITextEditor.class);
		
		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(this.getEditorInput());
		
		IRegion region = null;
		
		try 
		{
			region = document.getLineInformation(num);
		}
		catch (BadLocationException e) 
		{
			e.printStackTrace();
		}
		
		selectAndReveal(region.getOffset(), region.getLength());	
	}


	@Override
	public String getPreferredPerspectiveId() 
	{
		return "rulebender.perspective";
	}
}
