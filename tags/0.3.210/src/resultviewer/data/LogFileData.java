package resultviewer.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LogFileData extends FileData{

	private String content;
	
	public LogFileData(File file) {
		this.file = file;
		this.fileName = file.getName();
		this.content = "";
		this.readData();
	}

	public void readData() {
		try {
			Scanner in = new Scanner(file);
			while (in.hasNext()) {
				String line = in.nextLine();
				content += line + "\n";
			}
			// close the input stream
			in.close();
		} catch (FileNotFoundException e) {
			// file not found
			System.out.println("File: " + file.getName() + " not found!");
			e.printStackTrace();
		}
		
	}
	
	public String getFileContent() {
		return this.content;
	}

}
