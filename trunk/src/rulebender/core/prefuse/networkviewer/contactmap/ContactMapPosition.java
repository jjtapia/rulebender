package rulebender.core.prefuse.networkviewer.contactmap;

import java.awt.geom.Rectangle2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;
import java.util.StringTokenizer;

import prefuse.Visualization;
import prefuse.data.Graph;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;

import rulebender.contactmap.models.NodePosition;

public class ContactMapPosition {
	
	private static String COMPONENT_GRAPH = "component_graph";
	
	public static ArrayList<NodePosition> loadMoleculePositions(String filepath) {

		ArrayList<NodePosition> positionMap = new ArrayList<NodePosition>();
		File positionsFile = new File(modifyFilePath(filepath));
		StringTokenizer st;
		Scanner s;
		String line, molecule, component, id = null, x, y;
				
		// If a positions file exists, generate an ArrayList containing molecule/component names and positions.
		// If it doesn't exist, then return null
		if (positionsFile.exists()) {
			
			try { 
				s = new Scanner(positionsFile);
			
				// Read each line of the file, breaking it up into tokens delimited by "|", and store the positions in the ArrayList
				while (s.hasNext()) {
					line = s.nextLine();
					
					// Ignore the blank line that may exist at the end of the file
					if (!(line.equals(""))) {
						st = new StringTokenizer(line, "|");
						
						// If there are 5 tokens, then this is the special case bounds line
						if (st.countTokens() == 6) {
							molecule = st.nextToken();
							component = line;
							id = "";
							x = "0";
							y = "0";
						} else {
							// The first token is the molecule name, the second is the component name, the third is the x-position, the fourth is the y-position
							molecule = st.nextToken();
							component = st.nextToken();
							id = st.nextToken();
							x = st.nextToken();
							y = st.nextToken();
						} //if-else
						
						// Create a new Position object to add the (x,y) values to
						NodePosition myPosition = new NodePosition(molecule, component, id, Double.parseDouble(x), Double.parseDouble(y));
				
						// Store the <molecule, Position> in the ArrayList
						positionMap.add(myPosition);
					
					} //if
				
				} //while
			
			} catch (Exception e) {
				System.err.println("Error occurred when reading the positions file:");
				System.err.println("Error: " + e.getMessage());
			} //try-catch
			
		} else {
			return null;
		} //if-else
		
		return positionMap;
	} //getSavedMoleculePositions
	
	// Writes node locations to the given filepath
	//public void writeNodeLocations(String filepath, ContactMapVisual visualization) {
	public static void writeNodeLocations(String filepath, Visualization visualization) {
		ArrayList<NodePosition> positionMap = generatePositionList(visualization);
		Rectangle2D bounds = visualization.getBounds(COMPONENT_GRAPH);
		String newFilepath = modifyFilePath(filepath);
		saveMoleculePositions(newFilepath, positionMap, bounds);
	} //writeNodeLocations
	
	public static String modifyFilePath(String bnglFilePath) {
		StringBuilder positionFilePath = new StringBuilder();
		
		try {
			// Get the full filepath of the BNGL file except for the ".bngl" extension
			// Append on the ".pos" extension and return
			positionFilePath.append(bnglFilePath.substring(0, bnglFilePath.length()-5));
			positionFilePath.append(".pos");
		} catch(Exception e) {
			System.err.println("Error occurred when determining the name of the positions file:");
			System.err.println("Error: " + e.getMessage());
		} //try-catch
		
		return positionFilePath.toString();
	} //modifyFilePath
		
	public static void saveMoleculePositions(String filepath, ArrayList<NodePosition> positionMap, Rectangle2D bounds) {
		StringBuilder sb;
		
		try {
			// Open the file and wipe the information that's currently saved
			FileWriter fstream = new FileWriter(filepath, false);
			BufferedWriter out = new BufferedWriter(fstream);
			
			// Iterate through the ArrayList, creating each line and writing each line to the file
			for (NodePosition pos : positionMap) {
				sb = new StringBuilder();
				
				sb.append(pos.getMolecule());
				sb.append("|");
				sb.append(pos.getComponent());
				sb.append("|");
				sb.append(pos.getID());
				sb.append("|");
				sb.append(pos.getX());
				sb.append("|");
				sb.append(pos.getY());
				sb.append("\n");
				
				out.write(sb.toString());
			} //for
			
			sb = new StringBuilder();
			sb.append("BOUNDS");
			sb.append("|");
			sb.append(bounds.getCenterX());
			sb.append("|");
			sb.append(bounds.getCenterY());
			sb.append("|");
			sb.append(bounds.getHeight());
			sb.append("|");
			sb.append(bounds.getWidth());
			sb.append("|");
			sb.append("BOUNDS");
			sb.append("\n");
			
			out.write(sb.toString());
			
			// Close the output stream
			out.close();
			
		} catch (Exception e) {
			System.err.println("Error occured when writing the positions file:");
			System.err.println("Error: " + e.getMessage());
		} //try-catch
		
	} //saveMoleculePositions
	
	//public ArrayList<Position> generatePositionList(ContactMapVisual visualization) {
	public static ArrayList<NodePosition> generatePositionList(Visualization visualization) {	
		// Create the ArrayList that needs to be returned
		ArrayList<NodePosition> positionMap = new ArrayList<NodePosition>();
		NodePosition temp;
		double x, y;
		String molecule, component, id;
		StringTokenizer st;
		
		// Get the collection of all nodes from the visualization
		//Hashtable<String, Node> nodeCollection = visualization.getNodeIndex();
		
		//Visualization m_vis = visualization.getCMAPNetworkViewer().getVisualization();
		//Iterator iter = m_vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
		Iterator iter = visualization.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
		
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			
			id = item.getString("ID");
			molecule = item.getString("molecule");
			component = item.getString(VisualItem.LABEL);
			
			// Correction for the null items
			/*if (molecule == null) {
				//continue;
				molecule = id;
				component = id;
			}*/ //if
			
			x = item.getX();
			y = item.getY();
			
			//temp = new NodePosition(molecule, component, x, y);
			temp = new NodePosition(molecule, component, id, x, y);
			positionMap.add(temp);	
			
		} //while
		
		return positionMap;
		
	} //generatePositionList
	
} //ContactMapPosition