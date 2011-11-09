package rulebender.core.modelcollection;

import java.util.Hashtable;

import bngparser.grammars.BNGGrammar.prog_return;

public class ModelCollection 
{
	// The Singleton Instance
	private static ModelCollection si_collection;
	
	Hashtable<String, prog_return> m_models;
	
	private ModelCollection()
	{
		m_models = new Hashtable<String, prog_return>();
	}
	
	public static synchronized ModelCollection getCollection()
	{
		if(si_collection == null)
		{
			si_collection = new ModelCollection();
		}
		
		return si_collection;
	}
	
	/**
	 * Add an abstract syntax tree to the model repository
	 * based on the string path to the file that it came from.
	 * @param path
	 * @param ast
	 */
	public void addModel(String path, prog_return ast)
	{
		if(m_models.containsKey(path))
		{
			m_models.remove(path);
		}
		
		m_models.put(path, ast);
	}
	
	/**
	 * Returns an ast for a path.
	 * @param path
	 * @return
	 */
	public prog_return getModel(String path)
	{
		return m_models.get(path);
	}
	
	/**
	 * Remove a model from the repository.
	 * @param path
	 */
	public void removeModel(String path)
	{
		m_models.remove(path);
	}
}
