package rulebender.editors.dat.model;

import java.io.File;

public abstract class FileData implements Cloneable{
	protected String fileName; // file name
	protected File file;

	/**
	 * 
	 * @return file name
	 */
	public String getFileName() {
		return this.fileName;
	}

	/**
	 *  Read data from file.
	 */
	abstract protected void readData();
}
