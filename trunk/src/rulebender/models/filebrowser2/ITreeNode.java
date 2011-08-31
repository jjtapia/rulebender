package rulebender.models.filebrowser2;

import java.util.List;

import org.eclipse.swt.graphics.Image;

/**
 * AN ABSTRACTION OF A TREE VIEWMODEL-ELEMENT + CONTROLLER PER NODE
 */
interface ITreeNode
{
	public String getName();
	public Image getImage();
	public List getChildren();
	public boolean hasChildren();
	public ITreeNode getParent();
}

