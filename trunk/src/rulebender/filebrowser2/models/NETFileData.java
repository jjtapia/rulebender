package rulebender.filebrowser2.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.eclipse.jface.text.source.SourceViewer;

/**
 * 
 * Store all the information for NET files
 *
 */
public class NETFileData extends FileData {
	private StringBuffer content; // content of the NET file
	private ArrayList<String> itemList; // list of items which store the name,
										// offset, and length for each
										// begin...end block
	
	private String selectedItem;

	private NETFolderNode netFolderNode;
	private SpeciesFolderNode speciesFolder;
	private ObservableFolderNode observableFolder;

	private SourceViewer sv; // the SourceViewer attached with the NET file

	/**
	 * 
	 * @param file
	 *            NET file
	 */
	public NETFileData(File file) {
		this.file = file;
		this.fileName = file.getName();
		this.content = new StringBuffer();
		itemList = new ArrayList<String>();
		this.readData();
		if (file.getName().endsWith(".net")) {
			this.readSpeciesFromNETFile(); // read from this.content
		}
	}

	/**
	 * 
	 * @return the content of the NET file
	 */
	public String getFileContent() {
		return this.content.toString();
	}

	/**
	 * 
	 * @return NetFolderNode
	 */
	public NETFolderNode getNETFolderNode() {
		return this.netFolderNode;
	}

	/**
	 * 
	 * @return SpeciesFolderNode
	 */
	public SpeciesFolderNode getSpeciesFolder() {
		return this.speciesFolder;
	}

	/**
	 * 
	 * @return ObservableFolderNode
	 */
	public ObservableFolderNode getObservableFolder() {
		return this.observableFolder;
	}

	/**
	 * @Override
	 */
	protected void readData() {
		try {
			Scanner in = new Scanner(file);
			while (in.hasNext()) {
				String line = in.nextLine();
				if (line.startsWith("begin")) {
					// add a newline before each begin statement
					content.append("\n");
					// get the name of the begin...end block
					
					String itemName = "";
					if (line.indexOf("#") != -1) {
						// delete comment
						itemName = line.trim().substring(line.indexOf(" ") + 1,
								line.indexOf("#"));
					} else {
						// no comment
						itemName = line.trim().substring(line.indexOf(" ") + 1);
					}
					// store the offset and length for each begin...end block
					String offset = Integer.toString(content.length());
					String length = Integer.toString(line.length());
					itemList.add(itemName + "\t" + offset + "\t" + length);
				}
				content.append(line);
				content.append("\n");
			}
			// create a NetFolderNode object with itemList
			netFolderNode = new NETFolderNode(file.getName(), itemList);
			// close the input stream
			in.close();
		} catch (FileNotFoundException e) {
			// file not found
			System.out.println("File: " + file.getName() + " not found!");
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @return the SourceViewer object attached to this NET file
	 */
	public SourceViewer getSourceViewer() {
		return this.sv;
	}

	/**
	 * 
	 * @param sv
	 *            the SourceViewer object attached to this NET file
	 */
	public void setSourceViewer(SourceViewer sv) {
		this.sv = sv;
	}

	/**
	 * Return the expression of species based on its id
	 * 
	 * @param id
	 *            species id
	 * @return species expression
	 */
	public String getSpeciesExpByID(int id) {
		Iterator<SpeciesNode> it = speciesFolder.getChildren().iterator();
		while (it.hasNext()) {
			SpeciesNode cur = it.next();
			// compare the id
			if (cur.getId() == id) {
				return cur.getExpression();
			}
		}
		return null;
	}

	/**
	 * Return the id of species based on its expression
	 * 
	 * @param exp
	 *            species expression
	 * @return species id
	 */
	public Integer getSpeciesIDByExp(String exp) {

		Iterator<SpeciesNode> it = speciesFolder.getChildren().iterator();
		while (it.hasNext()) {
			SpeciesNode cur = it.next();

			// compare expression
			if (cur.getExpression().equalsIgnoreCase(exp)) {
				return cur.getId();
			}
		}
		return null;
	}

	/**
	 * Parse species information to create a SpeciesFolderNode object
	 */
	private void readSpeciesFromNETFile() {

		Scanner in = new Scanner(this.content.toString());
		String line;
		// list of species' name
		ArrayList<String> speciesList = new ArrayList<String>();

		while (in.hasNext()) {
			line = in.nextLine().trim();
			if (line.equalsIgnoreCase("begin species")) {
				// build array of species
				do {
					line = in.nextLine().trim();
					if (line.lastIndexOf(")") != -1) {
						// name of species
						String s = line.substring(0, line.lastIndexOf(")") + 1);
						speciesList.add(s);
					}
				} while (!line.equalsIgnoreCase("end species"));
				break;
			}
		}
		speciesFolder = new SpeciesFolderNode("Species", speciesList);

		in.close();
	}

	/**
	 * 
	 * @return string of selected item
	 */
	public String getSelectedItem() {
		return selectedItem;
	}

	/**
	 * 
	 * @param selectedItem
	 */
	public void setSelectedItem(String selectedItem) {
		this.selectedItem = selectedItem;
	}
}
