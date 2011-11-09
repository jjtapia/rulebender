package rulebender.core.modelcollection;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;

import org.antlr.runtime.RecognitionException;

import rulebender.utility.BNGParserCommands;
import bngparser.grammars.BNGGrammar.prog_return;

public class ModelCollectionController 
{
	private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	private static ModelCollectionController si_controller;
	
	private ModelCollection m_collection;
	
	private ModelCollectionController()
	{
		m_collection = ModelCollection.getCollection();
	}
	
	public static synchronized ModelCollectionController getModelCollectionController()
	{
		if(si_controller == null)
		{
			si_controller = new ModelCollectionController();
		}
		
		return si_controller;
	}
	
	public void fileHasBeenSaved(String path)
	{
		prog_return ast = null;
		
		// Get the ast
		try 
		{
			ast = BNGParserCommands.getASTForFileName(path);
			
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
			// Need to send errors to the editor.
			System.out.println("The AST is null.\nExiting...");
		}
		else
		{
			// print it out if it is good.
			//System.out.println(ast.toString()+"\n\n================================================================");
			m_collection.addModel(path, ast);
		}
		
		// Fire off a property changed event.  The old value is null because
		// it doesn't matter. 
		propertyChangeSupport.firePropertyChange(path, null, ast);
	}
	
	public prog_return getModel(String fileName) 
	{
		return m_collection.getModel(fileName);
	}
	
	public void addProptertyChangeListener(PropertyChangeListener pcl)
	{
		propertyChangeSupport.addPropertyChangeListener(pcl);
	}
}