package rulebender.navigator.model;

import java.io.File;
import java.util.List;
import org.eclipse.swt.graphics.Image;

/**
 * 
 * File Node
 * 
 */
public class FileNode extends TreeNode {
	private File fFile; // actual data object
	private FileData fData;
	private String fileName;
	private String filePath;

	public FileNode(ITreeNode parent, File file) {
		super("FileNode", parent);
		fFile = file;
		if (fFile != null) {
			String filename = fFile.getName();
			setName(filename);
			String filepath = fFile.getPath();
			setPath(filepath);
		}
	}

	/**
	 * Set display name of files based on their types.
	 * 
	 * @param filename
	 *            real name of file
	 */
	public void setName(String filename) {
		String suffix = null;
		if (filename.endsWith(".net"))
			suffix = "NET";
		if (filename.endsWith(".cdat"))
			suffix = "CDAT";
		if (filename.endsWith(".gdat"))
			suffix = "GDAT";
		if (filename.endsWith(".scan"))
			suffix = "SCAN";
		if (filename.endsWith(".bngl"))
			suffix = "BNGL";
		if (filename.endsWith(".log"))
			suffix = "LOG";
		if (filename.endsWith(".pl"))
			suffix = "PL";
		if (filename.endsWith(".m"))
			suffix = "M";
		if (filename.endsWith(".xml"))
			suffix = "XML";
		if (filename.endsWith(".rxn"))
			suffix = "RXN";
		if (filename.endsWith(".cfg"))
			suffix = "CFG";
		this.fileName = (suffix + ": " + filename);
	}

	/**
	 * @return the display name of file
	 */
	public String getName() {
		return fileName;
	}

	/**
	 * 
	 * @return file path
	 */
	public String getPath() {
		return this.filePath;
	}

	/**
	 * 
	 * @param filepath
	 */
	public void setPath(String filepath) {
		this.filePath = filepath;
	}

	/**
	 * @return file image
	 */
	public Image getImage() {
		return null;
	}

	protected void createChildren(List children) {
	}

	public boolean hasChildren() {
		return false;
	}

	/**
	 * 
	 * @return FileData object
	 */
	public FileData getFileData() {
		// different FileData for different format
		if (fData == null) {
			String filename = fFile.getName();
			if (filename.endsWith(".net") || filename.endsWith(".bngl")) {
				fData = new NETFileData(this.fFile);
			} else if (filename.endsWith(".cdat") || filename.endsWith(".gdat")
					|| filename.endsWith(".scan")) {
				fData = new DATFileData(this.fFile);
			} else if (filename.endsWith(".log") || filename.endsWith(".pl")
					|| filename.endsWith(".m") || filename.endsWith(".xml")
					|| filename.endsWith(".rxn") || filename.endsWith(".cfg")) {
				fData = new LogFileData(this.fFile);
			}
		}
		return fData;
	}

	/**
	 * Reset the fileData after refresh the file_tv tree
	 * 
	 * @param fData
	 *            FileData object
	 */
	public void setfData(FileData fData) {
		this.fData = fData;
	}

	public boolean equals(Object other) {
		if (!(other instanceof FileNode)) {
			return false;
		}

		if (((FileNode) other).getPath().equals(this.getPath())) {
			return true;
		}

		return false;
	}

	public int hashCode() {
		String path = this.getPath();
		int hashcode = 0;
		for (int i = 0; i < path.length(); i++) {
			Character ch = path.charAt(i);
			hashcode += ch.hashCode();
		}
		return hashcode;
	}
}
