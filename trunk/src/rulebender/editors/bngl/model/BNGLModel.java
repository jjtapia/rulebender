package rulebender.editors.bngl.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import rulebender.errorview.model.BNGLError;

import bngparser.grammars.BNGGrammar.prog_return;

public class BNGLModel 
{
	
	public static String AST = "ast";
	public static String ERRORS = "errors";
	
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private String m_pathID;
	private prog_return m_ast = null;
	private ArrayList<BNGLError> m_errors;
	
	public BNGLModel(String pathID)
	{
		setPathID(pathID);
	}
	
	/**
	 * Set the absolute path for the model. 
	 * 
	 * @param path The absolute String path for the model file.
	 */
	private void setPathID(String path)
	{
		m_pathID = path;
	}
	
	public void setAST(prog_return ast)
	{
		m_ast = ast;
		pcs.firePropertyChange(AST, null, m_ast);
	}

	/**
   * Get the absolute path for the model. 
   * 
   * @return path The absolute String path for the model file.
   */
	public String getPathID() 
	{
		return m_pathID;
	}
	
	public prog_return getAST()
	{
		return m_ast;
	}

	public void addPropertyChangeListener(PropertyChangeListener pcl) 
	{
		pcs.addPropertyChangeListener(pcl);
	}

	public void setErrors(ArrayList<BNGLError> errorList) 
	{
		m_errors = errorList;
		pcs.firePropertyChange(ERRORS, null, m_errors);
	}
	
	public ArrayList<BNGLError> getErrors()
	{
		return m_errors;
	}
	
}
