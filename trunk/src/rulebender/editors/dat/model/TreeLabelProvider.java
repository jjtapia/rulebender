package rulebender.editors.dat.model;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class TreeLabelProvider extends LabelProvider implements IFontProvider,
		IColorProvider {

	/**
	 * @return text content of tree node
	 */
	public String getText(Object element) {
		return ((ITreeNode) element).getName();
	}

	/**
	 * @return image of tree node
	 */
	public Image getImage(Object element) {
		return ((ITreeNode) element).getImage();
	}

	/**
	 * @return font of tree node text
	 */
	public Font getFont(Object element) {

		return null;
	}

	/**
	 * @return background color of tree node text
	 */
	public Color getBackground(Object element) {

		return null;
	}

	/**
	 * @return foreground color of tree node text
	 */
	public Color getForeground(Object element) {

		return null;
	}
}
