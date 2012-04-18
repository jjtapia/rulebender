package resultviewer.tree;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class TreeLabelProvider extends LabelProvider implements IFontProvider, IColorProvider{
	
	public String getText(Object element) {
		return ((ITreeNode) element).getName();
	}

	public Image getImage(Object element) {
		return ((ITreeNode) element).getImage();
	}

	public Font getFont(Object element) {
		
		return null;
	}

	public Color getBackground(Object element) {

		return null;
	}

	public Color getForeground(Object element) {

		return null;
	}
}
