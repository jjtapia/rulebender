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
import resultviewer.tree.TreeNode;

public class NETFileData extends FileData {
	private String content;
	private ArrayList<String> itemList;
	
	private NETFolderNode netFolderNode;
	private SpeciesFolderNode speciesFolder;
	private ObservableFolderNode observableFolder;

	private SourceViewer sv; // the SourceViewer attached with the NET file

	public NETFileData(File file) {
		this.fileName = file.getName();
		this.content = "";
		itemList = new ArrayList<String>();
		this.readData(file);
		this.readSpeciesFromNETFile(); // read from this.content
		this.readObservableFromNETFile(file);
	}

	public String getFileContent() {
		return this.content;
	}

	/* NETFolder */
	public NETFolderNode getNETFolderNode() {
		return this.netFolderNode;
	}

	/* speciesFolder */
	public SpeciesFolderNode getSpeciesFolder() {
		return this.speciesFolder;
	}

	/* observableFolder */
	public ObservableFolderNode getObservableFolder() {
		return this.observableFolder;
	}

	@Override
	protected void readData(File file) {
		try {
			Scanner in = new Scanner(file);
			while (in.hasNext()) {
				String line = in.nextLine();
				if (line.startsWith("begin")) {
					content += "\n";
					String itemName = line.trim().substring(
							line.indexOf(" ") + 1);
					String offset = Integer.toString(content.length());
					String length = Integer.toString(line.length());
					itemList.add(itemName + "\t" + offset + "\t" + length);
				}

				content += line + "\n";
			}
			netFolderNode = new NETFolderNode(file.getName(), itemList);
			in.close();
		} catch (FileNotFoundException e) {
			System.out.println("File: " + file.getName() + " not found!");
			e.printStackTrace();
		}

	}

	public SourceViewer getSourceViewer() {
		return this.sv;
	}

	public void setSourceViewer(SourceViewer sv) {
		this.sv = sv;
	}
	
	/*
	 * Return the expression of species based on id
	 */
	public String getSpeciesExpByID(int id) {
		
		Iterator<SpeciesNode> it = speciesFolder.getChildren().iterator();
		while(it.hasNext()) {
			SpeciesNode cur = it.next();
			if (cur.getId() == id) {
				return cur.getExpression();
			}
		}
		
		return null;
	}
	
	/*
	 * Return the id of species based on expression
	 */
	public Integer getSpeciesIDByExp(String exp) {
		
		Iterator<SpeciesNode> it = speciesFolder.getChildren().iterator();
		while(it.hasNext()) {
			SpeciesNode cur = it.next();
			if (cur.getExpression().equalsIgnoreCase(exp)){
				return cur.getId();
			}
		}
		
		return null;
	}

	/*
	 * Get the expression of species from netFile.
	 */
	private void readSpeciesFromNETFile() {

		Scanner in = new Scanner(this.content);
		String line;
		// list of species' name
		ArrayList<String> speciesList = new ArrayList<String>();

		while (in.hasNext()) {
			line = in.nextLine().trim();
			if (line.equalsIgnoreCase("begin species")) {
				/* build array of species */
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

	/*
	 * Get the expression and composition of observable from netFile.
	 */

	private void readObservableFromNETFile(File file) {
		String fileName = file.getName();
		Scanner in = null;

		/*
		 * There are two different NET file. The one contains String "end" does
		 * not have information about observable
		 */
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
				e.printStackTrace();
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
				/* build array of observable info */
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
	}
}
