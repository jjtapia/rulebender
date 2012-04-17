package rulebender.core.prefuse;

import java.io.File;
import javax.swing.filechooser.FileFilter;
/**
 * This class defines a filter for png files for when the user wants
 * to save a png version of a visualization.
 * @author adammatthewsmith
 *
 */
public class PngSaveFilter extends FileFilter {
	
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		String s = f.getName();

		return s.endsWith(".png") || s.endsWith(".PNG");
	}

	public String getDescription() {
		return "*.png,*.PNG";
	}

}
