package rulebender.editors.bngl.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import bngparser.grammars.BNGGrammar.prog_return;

public class BNGLModel 
{
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
	
	private String m_pathID;
	private prog_return m_ast;
	
	
	public BNGLModel(String pathID, prog_return ast)
	{
		setAST(ast);
		setPathID(pathID);
	}
	
	private void setPathID(String path)
	{
		m_pathID = path;
	}
	
	public void setAST(prog_return ast)
	{
		m_ast = ast;
		pcs.firePropertyChange(m_pathID, null, m_ast);
	}

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
	
}
