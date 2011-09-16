package rulebender.views;

import java.awt.Dimension;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import rulebender.modelbuilders.BNGASTReader;
import rulebender.modelbuilders.CMapModelBuilder;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RuleReturnScope;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.antlr.stringtemplate.language.AngleBracketTemplateLexer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import bngparser.dataType.ChangeableChannelTokenStream;
import bngparser.grammars.BNGGrammar;
import bngparser.grammars.BNGLexer;
import bngparser.grammars.BNGGrammar.prog_return;
import rulebender.models.contactmap.CMapModel;
import rulebender.prefuse.contactmap.CMapVisual;
import rulebender.utility.BNGParserCommands;

import prefuse.Display;

public class ContactMapView extends ViewPart implements ISelectionListener 
{

	Display d;
	
	public static String FCERI_JI = "/Users/mr_smith22586/Documents/workspace/CMapTest/testModels/fceri_ji.bngl";
	
	public ContactMapView() 
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		Composite swtAwtComponent = new Composite(parent, SWT.EMBEDDED);
		
		java.awt.Frame frame = SWT_AWT.new_Frame( swtAwtComponent);
		
		frame.add(generateContactMap());
	}

	@Override
	public void setFocus() 
	{
		// TODO Auto-generated method stub

	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
	{
		System.out.println("Selection: ");
		
	}
	
	public Display generateContactMap()
	{
		prog_return ast = null;
		
		// Get the ast
		try 
		{
			ast = BNGParserCommands.getASTForFileName(FCERI_JI);
			
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
		Dimension dim = new Dimension(600, 600);

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
		CMapVisual cVisual = new CMapVisual(cModel, dim, new Dimension(100,100));
		
		return cVisual.getDisplay();
	}
}