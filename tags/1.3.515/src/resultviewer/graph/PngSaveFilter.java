package resultviewer.graph;

import java.io.File;
import javax.swing.filechooser.FileFilter;

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
