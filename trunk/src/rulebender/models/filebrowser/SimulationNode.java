package rulebender.models.filebrowser;

import org.eclipse.swt.graphics.Image;

public class SimulationNode implements FileBrowserTreeNodeInterface
{	
	private FileBrowserTreeNodeInterface m_parent;
	private FileNode m_netFileNode;
	private FileNode m_cdatFileNode;
	private FileNode m_gdatFileNode;
	private FileNode m_logFileNode;
	private FileNode m_bnglFileNode;
	private FileNode m_xmlFileNode;
	
	public SimulationNode(FileBrowserTreeNodeInterface parent, FileNode netFile, FileNode cdatFile, FileNode gdatFile,
						  FileNode logFile, FileNode bnglFile, FileNode xmlFile)
	{
		m_parent = parent;
		setNetFile(netFile);
		setCDATFile(cdatFile);
		setGDATFile(gdatFile);
		setLogFile(logFile);
		setBNGLFile(bnglFile);
		setXMLFile(xmlFile);
	}
	
	public FileBrowserTreeNodeInterface[] getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	public Image getImage() {
		// TODO Auto-generated method stub
		return null;
	}
	
	/*---------------------------Accessors and Mutators----------------------*/
	
	public FileNode getNetFile() {
		return m_netFileNode;
	}

	public void setNetFile(FileNode netFile) {
		this.m_netFileNode = netFile;
	}
	
	public FileNode getCDATFile() 
	{
		return m_cdatFileNode;
	}

	public void setCDATFile(FileNode cdatFile) {
		this.m_cdatFileNode = cdatFile;
	}
	
	public FileNode getGDATFile() 
	{
		return m_gdatFileNode;
	}

	public void setGDATFile(FileNode gdatFile) {
		this.m_gdatFileNode = gdatFile;
	}

	public FileBrowserTreeNodeInterface getParent() {
		return m_parent;
	}
	
	public void setBNGLFile(FileNode bnglFile) {
		this.m_bnglFileNode = bnglFile;
	}
	
	public void setXMLFile(FileNode xmlFile) {
		this.m_xmlFileNode = xmlFile;
	}
	
	public void setLogFile(FileNode logFile)
	{
		m_logFileNode = logFile;
	}

	public boolean hasChildren() {
		// TODO Auto-generated method stub
		return false;
	}

}
