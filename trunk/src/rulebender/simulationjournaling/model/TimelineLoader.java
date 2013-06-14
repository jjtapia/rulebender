package rulebender.simulationjournaling.model;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.swing.text.Document;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.InputSource;

public class TimelineLoader {

	private String m_filePath;	
	
	public TimelineLoader() {
		setFilePath(null);
	} //TimelineLoader (constructor)
	
	public TimelineLoader(String filepath) {
		setFilePath(filepath);
	} //TimelineLoader (constructor)
	
	public String getFilePath() {
		return m_filePath;
	} //getFilePath
	
	public void setFilePath(String filepath) {
		m_filePath = filepath;
	} //setFilePath
	
	private ArrayList<String> loadFile() {
		ArrayList<String> lines = new ArrayList<String>();
		File timelineFile = null;
		Scanner s = null;
		String line = null;
				
		timelineFile = new File(m_filePath);
		
		if (timelineFile.exists()) {
			try {
				s = new Scanner(timelineFile);
				
				// Read each line of the file, and store the lines in the string array
				while (s.hasNext()) { 
					line = s.nextLine();
					
					// Ignore the blank line(s) that may exist at the end of the file
					if (!(line.equals(""))) {
						lines.add(line);
					} //if
					
				} //while
							
			} catch (Exception e) {
				System.err.println("Error occurred when reading the timeline file:");
				System.err.println("Error: " + e.getMessage());
			} //try-catch
			
		} else {
			return null;
		} //if-else
				
		return lines;
	} //loadFile
	
	public ArrayList<TimelineItem> parseFile() {
		StringTokenizer st = null;
		ArrayList<String> lines = null;
		ArrayList<TimelineItem> files = new ArrayList<TimelineItem>();
		TimelineItem temp = null;
		String filename = null;
		
		// Load the file, filling the list of lines
		lines = loadFile();
		
		Iterator<?> fileIter = lines.iterator();
		while (fileIter.hasNext()) {
			
			String line = (String) fileIter.next();
			st = new StringTokenizer(line, " ");
			
			// Read the first token of the line to determine what kind of information we get from this line
			String choice = st.nextToken();
						
			if (choice.equals("ROOT:")) {
				// The first line of the file gives the root file
				
				// Create a new timeline item for this line (passing in the next token, which is the name of the file), and add to the array
				temp = new TimelineItem(st.nextToken());
				files.add(temp);
				
			} else if (choice.equals("PARENTOF:")) {
				// The next few lines of the file give the parent of each other file
				
				// Create a new timeline item for this line (passing in the next two tokens, which are the name of the file and the name of the parent), and add to the array
				temp = new TimelineItem(st.nextToken(), st.nextToken());
				files.add(temp);
		
		
			} else if (choice.equals("SIMULATIONS:")) {
				// The rest of the file lists the simulations associated with each file
				
				// Read the filename, and search the array of timeline items for the matching filename
				filename = st.nextToken();
				Iterator<?> itemsIter = files.iterator();
				
				while (itemsIter.hasNext()) {
					TimelineItem item = (TimelineItem) itemsIter.next();
					if (item.getName().equals(filename)) {
						
						// If we find the file, remove it, add all simulations to the simulations list, and push the file back into the array
						files.remove(item);
						
						while (st.hasMoreTokens()) {
							item.addSimulation(st.nextToken());
						} //while
						
						files.add(item);
						break;
					} //if
					
				} //while
		
				
			} else {
				System.err.println("Invalid line found when reading the timeline file.");
				return null;
			} //if-else
			
		} //while
		
		return files;
		
	} //parseFile
	
	public String createXML(ArrayList<TimelineItem> files) {
		
		StringBuilder xml = new StringBuilder();
		TimelineItem root = null;
		
		//xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
		//xml.append("<!DOCTYPE tree SYSTEM \"treeml.dtd\">\n");
		xml.append("<tree>\n");
		xml.append("  <declarations>\n");
		xml.append("    <attributeDecl name=\"name\" type=\"String\"/>\n");
		xml.append("    <attributeDecl name=\"simulation\" type=\"String\"/>\n");
		xml.append("  </declarations>\n");
		
		// Get the root of the document tree
		for (int i = 0; i < files.size(); i++) {
			TimelineItem temp = files.get(i);
			
			// If the TimelineItem has no parent, then it's the root
			if (temp.getParent() == null) {
				root = temp;
				break;
			} //if
		} //for
				
		// Recursive call to create the XML structure
		xml.append(createNodeAndChildren(root, files, 0));

		xml.append("</tree>\n");
		
		return xml.toString();

	} //createXML
	
	public String createNodeAndChildren(TimelineItem currentFile, ArrayList<TimelineItem> files, int level) {
		StringBuilder line = new StringBuilder();
		
		// Count number of children (to determine if this is a branch or a leaf)
		int children = countChildren(currentFile, files);
		
		if (children == 0) {
			line.append(createLeaf(currentFile, files, level));
		} else {
			line.append(createBranch(currentFile, files, level));
		} //if-else
				
		return line.toString();
	} //createNodeAndChildren
	
	public String createLeaf(TimelineItem currentFile, ArrayList<TimelineItem> files, int level) {
		StringBuilder line = new StringBuilder();
		
		// Tab in
		line.append(addTab(level));
		
		// Create leaf
		line.append("<leaf>\n");
		
		// Add attributes
		line.append(addAttributes(currentFile, level+1));
	
		// Tab in
		line.append(addTab(level));
		
		// End leaf
		line.append("</leaf>\n");
		
		return line.toString();
	} //createLeaf
	
	public String createBranch(TimelineItem currentFile, ArrayList<TimelineItem> files, int level) {
		StringBuilder line = new StringBuilder();
	
		// Tab in
		line.append(addTab(level));
		
		// Create the line
		line.append("<branch>\n");
				
		// Add the line attributes
		line.append(addAttributes(currentFile, level+1));
				
		// Find all children of the currentFile and recursively add those branches
		for (int i = 0; i < files.size(); i++) {
			TimelineItem temp = files.get(i);
			
			// Skip over the root
			if (temp.getParent() == null) {
				continue;
			} //if
			
			if (temp.getParent().equals(currentFile.getName())) {
				line.append(createNodeAndChildren(temp, files, level+1));
			} //if
		} //for
				
		// Tab in
		line.append(addTab(level));
		
		// End the line
		line.append("</branch>\n");
		
		return line.toString();
	} //createBranch
	
	public String addAttributes(TimelineItem currentFile, int level) {
		StringBuilder attr = new StringBuilder();
		
		// Tab in
		attr.append(addTab(level));
		
		// Add name attribute
		attr.append(addAttribute("name", currentFile.getName()));
		
		for (int i = 0; i < currentFile.getSimulations().size(); i++) {
			attr.append(addAttribute("simulation", currentFile.getSimulations().get(i)));
		} //for
		
		return attr.toString();
	} //addAttributes
	
	public String addAttribute(String field, String value) {
		StringBuilder attr = new StringBuilder();
		
		attr.append("<attribute name=\"");
		attr.append(field);
		attr.append("\" value=\"");
		attr.append(value);
		attr.append("\" />\n");
		
		return attr.toString();
	} //addAttribute
	
	public String addTab(int level) {
		StringBuilder tab = new StringBuilder();
		
		for (int i = 0; i < level+1; i++) {
			tab.append("  ");
		} //for
		
		return tab.toString();
	} //addTab
	
	public int countChildren(TimelineItem currentFile, ArrayList<TimelineItem> files) {
		int count = 0;
		
		for (int i = 0; i < files.size(); i++) {
			TimelineItem temp = files.get(i);
			
			// Skip over the root
			if (temp.getParent() == null) {
				continue;
			} //if
			
			if (temp.getParent().equals(currentFile.getName())) {
				count++;
			} //if
		} //for
		
		return count;
	} //countChildren
	
} //TimelineLoader (class)