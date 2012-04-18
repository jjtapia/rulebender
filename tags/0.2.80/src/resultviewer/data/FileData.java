package resultviewer.data;

import java.io.File;

public abstract class FileData {
	protected String fileName;
	protected prefuse.Display display;
	
	public String getFileName() {
		return this.fileName;
	}
	
	abstract protected void readData(File file);
	
	public void setDisplay(prefuse.Display dis) {
		this.display = dis;
	}
	
	public prefuse.Display getDisplay() {
		return this.display;
	}

}
