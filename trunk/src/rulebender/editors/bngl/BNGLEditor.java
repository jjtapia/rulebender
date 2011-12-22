package rulebender.editors.bngl;


import java.io.PrintStream;

import org.antlr.runtime.ANTLRStringStream;
import org.eclipse.ui.editors.text.TextEditor;

import bngparser.BNGParseData;
import bngparser.BNGParserUtility;
import bngparser.grammars.BNGGrammar;
import bngparser.grammars.BNGGrammar.prog_return;

import rulebender.core.utility.ANTLRFilteredPrintStream;
import rulebender.core.utility.Console;
import rulebender.editors.bngl.BNGLConfiguration;
import rulebender.editors.bngl.BNGLDocumentProvider;
import rulebender.editors.bngl.model.BNGLModel;

public class BNGLEditor extends TextEditor 
{
	// The model for the text
	private BNGLModel m_model;
	// The color manager for the syntax highlighting.
	private BNGLColorManager m_colorManager;
	
	private String m_path;
	
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
			m_model = new BNGLModel(this.getPartName(), ast);
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
			m_model = new BNGLModel(getTitle(), getAST());
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
		System.setErr(new ANTLRFilteredPrintStream(Console.getMessageConsoleStream(getTitle()), getTitle(), old));
		
		try
		{
			toReturn = produceParseData().getParser().prog();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
			//DEBUG
			//Console.displayOutput(getTitle(), getTitle() + " Errors:");
			//TODO The errors from parsing need to be printed to the console.
			
		}	
		System.err.flush();
		System.setErr(old);
		
		return toReturn;
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

}
