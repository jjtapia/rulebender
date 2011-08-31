package rulebender.models.filebrowser2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * 
 * FileData for plain text file types.
 * Types: .log, .pl, .m, .xml, .rxn, .cfg
 *
 */
public class LogFileData extends FileData{

	private StringBuffer content; // content of the file
	
	/**
	 * 
	 * @param file
	 */
	public LogFileData(File file) {
		this.file = file;
		this.fileName = file.getName();
		this.content = new StringBuffer();
		this.readData();
	}

	/**
	 * Read data from file.
	 */
	public void readData() {
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			while ((line = in.readLine()) != null) {
				content.append(line);
				// add newline delimiter
				content.append("\r\n");
			}
			// close the input stream
			in.close();
		} catch (FileNotFoundException e) {
			// file not found
			System.out.println("File: " + file.getName() + " not found!");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @return string of file content
	 */
	public String getFileContent() {
		return this.content.toString();
	}

}
