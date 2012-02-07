package rulebender.editors.bngl;

import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import bngparser.BNGParseData;
import bngparser.BNGParserUtility;
import bngparser.grammars.BNGGrammar.prog_return;

import rulebender.contactmap.prefuse.CMapClickControlDelegate;
import rulebender.contactmap.view.ContactMapView;
import rulebender.core.utility.ANTLRFilteredPrintStream;
import rulebender.core.utility.Console;
import rulebender.core.utility.FileInputUtility;
import rulebender.editors.bngl.BNGLConfiguration;
import rulebender.editors.bngl.BNGLDocumentProvider;
import rulebender.editors.bngl.model.BNGLModel;
import rulebender.errorview.model.BNGLError;
import rulebender.errorview.view.ErrorView;

public class BNGLEditor extends TextEditor implements ISelectionListener 
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
		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPostSelectionListener(this);
  	}
	
	
	//TODO I'm trying to find a way to set up the model and title other than lazy loading. 
	// This method is called before the part name is set. 
	/*
	@Override
	public void initializeEditor()
	{
		super.initializeEditor();

		System.out.println("name:" +this.getEditorInput().getName() + "\ntitle:" + getTitle() + "\npart name: " + this.getPartName() +
				"\n000000000000000000000000");
		
	this.setPartName("cock balls");
	
	System.out.println("title:" + getTitle() + "\npart name: " + this.getPartName() +
			"\n000000000000000000000000");
	
	
	}
		*/
		
	@Override
	public void editorSaved()
	{	
		setAST(getAST());
		
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
		m_model.setErrors(errorList);
	}


	public void dispose() 
	{
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
		// If it's from the BNGLEditor then we want to set the contact map.
		if(part.getClass() == ErrorView.class && !selection.isEmpty())
		{
			IStructuredSelection iSSelection = (IStructuredSelection) selection;
			BNGLError error = (BNGLError) iSSelection.getFirstElement();
			
			selectALine(error.getFilePath(), error.getLineNumber());
		}
		// If it's from the contact map.
		else if (part.getClass() == ContactMapView.class)
		{
			System.out.println("Part Title: " + part.getTitle());
			System.out.println("toString: " + selection.toString());
			
		}
		else
		{
			
		}	
	}
	
	public void selectALine(String path, int num)
	{	
		
		// CORRECTION:  Weird, but the num is always 1 too great...
		// 1 less would make sense (0 indexing in parser)...
		num--;
		
		/*
		 * First make sure that the file is open.
		 */

		// Get a reference to the file
		File file = new File(path);
		
		FileInputUtility.openFileInEditor(file);
		
		/*
		 * Now we select the line. 
		 */
		ITextEditor editor = (ITextEditor) this.getAdapter(ITextEditor.class);
		
		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(this.getEditorInput());
		//IDocument document = provider.getDocument(path);
		
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
}
