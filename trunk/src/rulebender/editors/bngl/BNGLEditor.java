package rulebender.editors.bngl;

import java.io.FileReader;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.eclipse.ui.editors.text.TextEditor;

import bngparser.BNGParserUtility;
import bngparser.dataType.ChangeableChannelTokenStream;
import bngparser.grammars.BNGGrammar;
import bngparser.grammars.BNGLexer;
import bngparser.grammars.BNGGrammar.prog_return;

import rulebender.editors.bngl.BNGLConfiguration;
import rulebender.editors.bngl.BNGLDocumentProvider;
import rulebender.editors.bngl.model.BNGLModel;
import rulebender.utility.Console;

public class BNGLEditor extends TextEditor 
{
	
	// The model for the text
	private BNGLModel m_model;
	// The color manager for the syntax highlighting.
	private BNGLColorManager m_colorManager;
	
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
  	}
		
	@Override
	public void editorSaved()
	{
		// DEBUG
		System.out.println("Title: " + getTitle());
		
		try
		{
			setAST(produceAST());
		}
		catch(Exception e)
		{
			Console.displayOutput("PARSING ERRORS: *********");
			Console.displayOutput(e.getStackTrace().toString());
		}
	}	
	
	private prog_return produceAST() throws Exception
	{
		//  Get the text in the document.
		String text = this.getSourceViewer().getDocument().get();
		
		return BNGParserUtility.produceASTForBNGLText(text);
		
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
	 * Getter for the ast data structure.
	 * @return
	 */
	public BNGLModel getModel()
	{
		if(m_model == null)
		{
			try
			{
				m_model = new BNGLModel(getTitle(), produceAST());
			}
			catch (Exception e)
			{
				e.printStackTrace();
				
				Console.displayOutput("PARSING ERRORS: *********");
				
				m_model = new BNGLModel(getTitle(), null);
			}
		}
		
		return m_model;
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
