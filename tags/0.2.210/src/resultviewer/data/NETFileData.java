package resultviewer.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import org.eclipse.jface.text.source.SourceViewer;

import resultviewer.tree.NETFolderNode;
import resultviewer.tree.ObservableFolderNode;
import resultviewer.tree.SpeciesFolderNode;
import resultviewer.tree.SpeciesNode;

/**
 * 
 * Store all the information for NET files
 *
 */
public class NETFileData extends FileData {
	private String content; // content of the NET file
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
		this.content = "";
		itemList = new ArrayList<String>();
		this.readData();
		if (file.getName().endsWith(".net")) {
			this.readSpeciesFromNETFile(); // read from this.content
			// this.readObservableFromNETFile(file);
		}
	}

	/**
	 * 
	 * @return the content of the NET file
	 */
	public String getFileContent() {
		return this.content;
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
					content += "\n";
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
					// store the offset of length for each begin...end block
					String offset = Integer.toString(content.length());
					String length = Integer.toString(line.length());
					itemList.add(itemName + "\t" + offset + "\t" + length);
				}
				content += line + "\n";
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
			// TODO
			// get species based on pattern, not exact expression

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

		Scanner in = new Scanner(this.content);
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

	public String getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(String selectedItem) {
		this.selectedItem = selectedItem;
	}
	

	/*
	 * Get the expression and composition of observable from netFile.
	 */

	/*
	private boolean readObservableFromNETFile(File file) {
		String fileName = file.getName();
		Scanner in = null;

		
		 // There are two different NET file. The one contains String "end" does
		 // not have information about observable
		 
		if (!fileName.contains("end")) {
			// read from this.content
			in = new Scanner(this.content);
		} else {
			String filePath = file.getAbsolutePath();
			String netFilePath = filePath.replace("_end", "");
			File netFile = new File(netFilePath);
			try {
				// read from other NET file
				in = new Scanner(netFile);
			} catch (FileNotFoundException e) {
				System.out.println("File Not found!");
				e.printStackTrace();
				return false;
			}
		}

		String line;

		// list of observale nodes
		ArrayList<String> info = new ArrayList<String>();
		ArrayList<String>[] componentsList = null;
		ArrayList<String> speciesList = speciesFolder.getComponents();

		while (in.hasNext()) {
			line = in.nextLine().trim();
			if (line.equalsIgnoreCase("begin observables")) {
				//build array of observable info
				line = in.nextLine().trim();
				while (!line.equalsIgnoreCase("end observables")) {
					info.add(line);
					line = in.nextLine().trim();
				}
			}

			else if (line.equalsIgnoreCase("begin groups")) {
				// allocate space for observable components
				componentsList = new ArrayList[info.size()];
				int count = 0;

				line = in.nextLine().trim();

				while (!line.equalsIgnoreCase("end groups")) {

					// format: id name listOfComponent
					String[] tmp = line.split(" ");
					String listOfComponent = tmp[tmp.length - 1];

					// format: number*speciesID,number*speciesID,...
					String[] tmp2 = null;
					if (listOfComponent.contains(",")) {
						tmp2 = listOfComponent.split(",");
					} else {
						// only one species
						tmp2 = new String[1];
						tmp2[0] = listOfComponent;
					}

					ArrayList<String> components = new ArrayList<String>();
					// add speciesID to componentsID
					for (int i = 0; i < tmp2.length; i++) {
						String speciesID = tmp2[i];

						// number*speciesID, remove number, only leave speciesID
						if (speciesID.contains("*")) {
							speciesID = speciesID.substring(
									speciesID.indexOf("*") + 1,
									speciesID.length());
						}

						// add speciesID
						components.add(speciesList.get(Integer
								.parseInt(speciesID) - 1));
					}
					componentsList[count++] = components;
					line = in.nextLine().trim();
				}
				break;
			}

		}

		observableFolder = new ObservableFolderNode("Observables", info,
				componentsList);
		in.close();
		return true;
	}
*/

}
