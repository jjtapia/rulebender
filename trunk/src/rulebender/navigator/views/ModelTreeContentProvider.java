package rulebender.navigator.views;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

import rulebender.navigator.model2.*;

public class ModelTreeContentProvider extends ArrayContentProvider implements
		ITreeContentProvider {

	public Object[] getChildren(Object parentElement) 
	{
		//DEBUG
		System.out.println("Calling getChildren on: " + ((FileBrowserTreeNodeInterface) parentElement).getName());
		return ((FileBrowserTreeNodeInterface) parentElement).getChildren();
	}

	public Object getParent(Object element) 
	{
		return ((FileBrowserTreeNodeInterface) element).getParent();
	}

	public boolean hasChildren(Object element) 
	{
		return false;
	}

	public Object[] getElements(Object element)
	{
		return ((FileBrowserTreeNodeInterface) element).getChildren();
	}
}
