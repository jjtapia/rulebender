package rulebender.filebrowser.models;

import org.eclipse.swt.graphics.Image;

public interface FileBrowserTreeNodeInterface 
{
	public FileBrowserTreeNodeInterface getParent();

	public Object[] getChildren();
	
	public boolean hasChildren();
	
	public String getName();
	
	public Image getImage();
}
