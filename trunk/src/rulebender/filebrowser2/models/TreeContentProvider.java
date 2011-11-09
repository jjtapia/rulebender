package rulebender.filebrowser2.models;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeContentProvider implements ITreeContentProvider {
	/**
	 * @return array of children nodes
	 */
	public Object[] getChildren(Object parentElement) {
		return ((ITreeNode) parentElement).getChildren().toArray();
	}

	/**
	 * @return parent node
	 */
	public Object getParent(Object element) {
		return ((ITreeNode) element).getParent();
	}

	/**
	 * @return whether has children
	 */
	public boolean hasChildren(Object element) {
		return ((ITreeNode) element).hasChildren();
	}

	/**
	 * the getElements method is called only in response to the tree viewer's
	 * setInput method and should answer with the appropriate domain objects of
	 * the inputElement.
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	/**
	 * any time you change the tree viewer's input via the setInput method, the
	 * inputChanged method will be called.
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
