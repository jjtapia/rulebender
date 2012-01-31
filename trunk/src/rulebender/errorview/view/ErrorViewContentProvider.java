package rulebender.errorview.view;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ErrorViewContentProvider  implements IStructuredContentProvider 
{

	public void inputChanged(Viewer v, Object oldInput, Object newInput) 
	{
	}

	public void dispose() 
	{
	}

	public Object[] getElements(Object parent) 
	{
		if (parent instanceof Object[]) 
		{
			return (Object[]) parent;
		}
        
		return new Object[0];
	}
}