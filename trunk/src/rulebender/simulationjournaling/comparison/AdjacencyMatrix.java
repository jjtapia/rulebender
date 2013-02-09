package rulebender.simulationjournaling.comparison;

import java.util.Iterator;

import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.util.PrefuseLib;
import prefuse.visual.VisualItem;

public class AdjacencyMatrix {

	private String[] labels;
	private boolean[][] matrix;
	
	private int numLabels;
	private static String COMPONENT_GRAPH = "component_graph";
	
	/**
	 * Constructor that initializes the labels array and the adjacency matrix to null, and the number of labels to 0
	 */
	public AdjacencyMatrix() {
		labels = null;
		matrix = null;		
		numLabels = 0;
	} //AdjacencyMatrix (constructor)
	
	/** 
	 * Generate an adjacency matrix for the given visualization
	 * 
	 * @param vis - The visualization we're creating the matrix for
	 */
	public void generateMatrix(Visualization vis) {
		countLabels(vis);
		addLabels(vis);
		initializeMatrix();
		fillMatrix(vis);		
		//printMatrixToConsole();
	} //generateMatrix
	
	/**
	 * Determine the number of rows and columns the adjacency matrix will have
	 */
	private void countLabels(Visualization vis) { 
		int count = 0;
		
		Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
		
		// Loop through the list of nodes
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			
			// Increment the counter if the node is not null
			if (item.getString("molecule") != null) {
				count++;				
			} //if
			
		} //while
		
		numLabels = count;
	} //countLabels
	
	/**
	 * Add the labels to the array
	 */
	private void addLabels(Visualization vis) {
		// Increase the size of the label list to fit all labels 
		labels = new String[this.numLabels];
		
		int count = 0;
		
		Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.NODES));
		
		// Loop through the list of nodes
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			
			// Add the molecule.component to the list
			// TODO: make these into StringBuilders for increased efficiency
			if (item.getString("molecule") != null) {
				if (item.getString("component") == null) {
					labels[count] = item.getString("molecule") + "(" + item.getString(VisualItem.LABEL) + ")";
				} else {
					labels[count] = item.getString("molecule") + "(" + item.getString("component") + ")." + item.getString(VisualItem.LABEL);
				} //if-else
				
				count++;
			} //if
			
		} //while
		
	} //addLabels
	
	/**
	 * Initialize everything in the adjacency matrix to false - no edge exists
	 */
	private void initializeMatrix() {
		// Increase the size of the matrix to the correct number of rows and columns
		matrix = new boolean[this.numLabels][this.numLabels];
		
		for (int i = 0; i < this.numLabels; i++) {
			for (int j = 0; j < this.numLabels; j++) {
				matrix[i][j] = false;
			} //for
		} //for
	} //initializeMatrix

	/**
	 * Fill the adjacency matrix with edges
	 */
	private void fillMatrix(Visualization vis) {
		VisualItem source = null, target = null;
		String sourceString = null, targetString = null;
		int sourceIndex = 0, targetIndex = 0;
		
		Iterator iter = vis.items(PrefuseLib.getGroupName(COMPONENT_GRAPH, Graph.EDGES));
		
		// Loop through the list of edges
		while (iter.hasNext()) {
			
			// Get the two endpoints of each edge and find their labels
			Edge edge = (Edge) iter.next();
			
			source = (VisualItem) edge.getSourceNode();
			target = (VisualItem) edge.getTargetNode();
			
			if ((source.getString("molecule") == null) || (target.getString("molecule") == null)) {
				continue;
			} //if
			
			// Correction that may not be correct... skip edges in the same molecule unless they bind to themselves
			// TODO: find out if this is valid
			if (source.getString("molecule").equals(target.getString("molecule"))) {
				if (!(source.getString(VisualItem.LABEL).equals(target.getString(VisualItem.LABEL)))) {
					continue;
				} //if
			} //if
			
			
			// TODO: make these into StringBuilders for increased efficiency
			if (source.getString("component") == null) {
				sourceString = source.getString("molecule") + "(" + source.getString(VisualItem.LABEL) + ")";
			} else {
				sourceString = source.getString("molecule") + "(" + source.getString("component") + ")." + source.getString(VisualItem.LABEL);
			} //if-else
			
			if (target.getString("component") == null) {
				targetString = target.getString("molecule") + "(" + target.getString(VisualItem.LABEL) + ")";
			} else {
				targetString = target.getString("molecule") + "(" + target.getString("component") + ")." + target.getString(VisualItem.LABEL);
			} //if-else
			
			sourceIndex = findLabelIndex(sourceString);
			targetIndex = findLabelIndex(targetString);
			
			// Add the edge to the adjacency matrix
			matrix[sourceIndex][targetIndex] = true;
			matrix[targetIndex][sourceIndex] = true;	
			
		} // while
		
	} //fillMatrix
	
	/** 
	 * Retrieve the index of the label
	 * 
	 * @param label - the label we're looking for in the label array
	 * 
	 * @return index
	 */
	private int findLabelIndex(String label) {
		boolean found = false;
		int index = 0;
		
		while (!found) {
			if (label.equals(labels[index])) {
				found = true;
			} else {
				index++;
			} //if
		} //while
		
		return index;
	} //findLabelIndex

	/** 
	 * Determine whether or not an edge exists between a pair of vertices i and j
	 * 
	 * @param i - first index
	 * @param j - second index
	 * @return true/false
	 */
	public boolean doesEdgeExist(int i, int j) {
		return matrix[i][j];		
	} //doesEdgeExist
	
	/**
	 * Provide the caller with the labels for the adjacency matrix
	 * 
	 * @return this.labels
	 */
	public String[] getLabels() {
		return this.labels;
	} //getLabels
	
	/** 
	 * Provide the caller with the adjacency matrix
	 * 
	 * @return this.matrix
	 */
	public boolean[][] getMatrix() {
		return this.matrix;
	} //getMatrix
	
	/**
	 * Output the row/column labels and matrix to the console
	 */
	public void printMatrixToConsole() {
		System.out.println("ROW/COLUMN HEADINGS");
		System.out.println("===================");
		
		for (int i = 0; i < numLabels; i++) {
			System.out.println(labels[i]);
		} //for
		
		System.out.println("");
		System.out.println("ADJACENCY MATRIX");
		System.out.println("================");
		
		for (int i = 0; i < numLabels; i++) {
			for (int j = 0; j < numLabels; j++) {
				if (matrix[i][j] == true) {
					System.out.print("1");
				} else {
					System.out.print("0");
				} //if-else
				
				System.out.print(" ");
			} //for
			System.out.print("\n");
		} //for		
		
		System.out.println("");
		
	} //printMatrixToConsole
	
} //AdjacencyMatrix
