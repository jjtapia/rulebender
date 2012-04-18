package resultviewer.tree;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeContentProvider implements ITreeContentProvider
{	
	public Object[] getChildren(Object parentElement) {
		return ((ITreeNode)parentElement).getChildren().toArray();
	}
 
	public Object getParent(Object element) {
		return ((ITreeNode)element).getParent();
	}
 
	public boolean hasChildren(Object element) {
		return ((ITreeNode)element).hasChildren();
	}
 
	/*
	 * the getElements method is called only in response to the 
	 * tree viewer's setInput method and should answer with 
	 * the appropriate domain objects of the inputElement.
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}
 
	public void dispose() {	
	}
 
	/*
	 * anytime you change the tree viewer's input via the setInput method, 
	 * the inputChanged method will be called.
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}	
	
}

