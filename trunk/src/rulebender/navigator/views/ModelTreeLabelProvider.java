package rulebender.navigator.views;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import rulebender.filebrowser.models.FileBrowserTreeNodeInterface;

public class ModelTreeLabelProvider implements ILabelProvider 
{

	public void addListener(ILabelProviderListener listener) 
	{
		// TODO Auto-generated method stub	
	}

	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	public boolean isLabelProperty(Object element, String property) 
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void removeListener(ILabelProviderListener listener) 
	{
		// TODO Auto-generated method stub	
	}

	public Image getImage(Object element) 
	{
		// TODO Auto-generated method stub
		return null;
	}

	public String getText(Object element) 
	{
		return ((FileBrowserTreeNodeInterface) element).getName();
	}

}
