package rulebender.editors.bngl;

import org.eclipse.ui.editors.text.TextEditor;

import bngparser.BNGParseData;
import bngparser.BNGParserUtility;
import bngparser.grammars.BNGGrammar;
import bngparser.grammars.BNGGrammar.prog_return;

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
		BNGParseData data = produceParseData();
		BNGGrammar parser = data.getParser();
		
		try
		{
			setAST(data.getParser().prog());
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
			//DEBUG
			System.out.println("Number of syntax errors: " + parser.getNumberOfSyntaxErrors()); 
			Console.displayOutput(getTitle() + " Errors:");
			
			m_model.setAST(null);
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
				m_model = new BNGLModel(getTitle(), produceParseData().getParser().prog());
			}
			catch (Exception e)
			{
				Console.displayOutput("Errors:");
				for(StackTraceElement ste : e.getStackTrace())
				{
					Console.displayOutput(ste.toString());
				}
				
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
