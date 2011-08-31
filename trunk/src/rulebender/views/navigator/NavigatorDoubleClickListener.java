package rulebender.views.navigator;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;

import rulebender.models.filebrowser.FileBrowserTreeNodeInterface;

public class NavigatorDoubleClickListener implements IDoubleClickListener
{

	public void doubleClick(DoubleClickEvent event) 
	{
		TreeViewer viewer = (TreeViewer) event.getViewer();
		ISelection selection = event.getSelection();
	
		viewer.setExpandedState((FileBrowserTreeNodeInterface)selection,!viewer.getExpandedState((FileBrowserTreeNodeInterface)selection));
		
		//System.out.println("Double click on " + ((FileBrowserTreeNodeInterface) selection).getName());
	}

}
