package rulebender.simulationjournaling.comparison;

import prefuse.Display;
import prefuse.Visualization;
import rulebender.simulationjournaling.model.SmallMultiple;

public class SimilarityMatrices {

	private SimilarityMatrix similarVertices = null;
	private SimilarityMatrix similarEdges = null;
	
	private SimilarityMatrix percentSimilarVertices = null;
	private SimilarityMatrix percentSimilarEdges = null;
	
	private AdjacencyMatrix matrices[];
	private Visualization models[];

	private String[] modelNames;
	
	/**
	 * Constructor - initializes the instance variables (model names and models)
	 * 
	 * @param newModelNames - The names of the set of models being compared
	 * @param sm - The set of SmallMultiples storing the Visualization objects being compared
	 */
	public SimilarityMatrices(String newModelNames[], SmallMultiple[] sm) {
		modelNames = newModelNames;
		models = new Visualization[sm.length];
		
		for (int i = 0; i < sm.length; i++) {
			models[i] = sm[i].getDisplay().getVisualization();
		} //for
		
	} //SimilarityMatrices (constructor)
	
	/**
	 * Populates the four similarity matrices from the collection of adjacency matrices
	 */
	public void fillSimilarityMatrices() {
		
		similarVertices = new SimilarityMatrix();
		similarEdges = new SimilarityMatrix();
		percentSimilarVertices = new SimilarityMatrix();
		percentSimilarEdges = new SimilarityMatrix();
		
		// Add labels to each of the similarity matrices
		similarVertices.setLabels(modelNames);
		similarEdges.setLabels(modelNames);
		percentSimilarVertices.setLabels(modelNames);
		percentSimilarEdges.setLabels(modelNames);
		
		// For each model, create the adjacency matrix
		matrices = new AdjacencyMatrix[models.length];
		for (int i = 0; i < models.length; i++) {
			matrices[i] = new AdjacencyMatrix();
			matrices[i].generateMatrix(models[i]);
		} //for
		
		// Loop through each pair of models, comparing them and placing the comparison results in the similarity matrices
		for (int i = 0; i < models.length; i++) {
			for (int j = i+1; j < models.length; j++) {
				AdjacencyMatrixComparison.compareTwoMatrices(matrices[i], matrices[j]);
				similarVertices.setSimilarityValue(i, j, AdjacencyMatrixComparison.getNumSimilarVertices());
				similarEdges.setSimilarityValue(i, j, AdjacencyMatrixComparison.getNumSimilarEdges());
				
				// Because the percent matrices are not diagonally symmetric, place the values, call the comparison function on the reversed pair, and palce the resulting values
				percentSimilarVertices.setSimilarityValue(i, j, AdjacencyMatrixComparison.getPercentSimilarVertices());
				percentSimilarEdges.setSimilarityValue(i, j, AdjacencyMatrixComparison.getPercentSimilarEdges());
				AdjacencyMatrixComparison.compareTwoMatrices(matrices[j], matrices[i]);
				percentSimilarVertices.setSimilarityValue(j, i, AdjacencyMatrixComparison.getPercentSimilarVertices());
				percentSimilarEdges.setSimilarityValue(j, i, AdjacencyMatrixComparison.getPercentSimilarEdges());
			} //for
		} //for
		
	} //fillSimilarityMatrices
	
	/**
	 * Outputs the similarity matrices to the console (for debugging)
	 */
	public void printSimilarityMatrices() {
		System.out.println("\n\nSIMILAR VERTICES MATRIX:");
		System.out.println("------------------------\n");

		similarVertices.printMatrix();
		
		System.out.println("\n\nSIMILAR EDGES MATRIX:");
		System.out.println("---------------------\n");
		
		similarEdges.printMatrix();
		
		System.out.println("\n\nPERCENT SIMILAR VERTICES MATRIX:");
		System.out.println("--------------------------------\n");
		
		percentSimilarVertices.printMatrix();
		
		System.out.println("\n\nPERCENT SIMILAR EDGES MATRIX:");
		System.out.println("-----------------------------\n");
		
		percentSimilarEdges.printMatrix();
		
	} //printSimilarityMatrices
	
	/**
	 * Returns the similar vertices matrix
	 * 
	 * @return - The similar vertices matrix
	 */
	public SimilarityMatrix getSimilarVerticesMatrix() {
		return similarVertices;
	} //getSimilarVerticesMatrix
	
	/**
	 * Returns the similar edges matrix
	 * 
	 * @return - The similar edges matrix
	 */
	public SimilarityMatrix getSimilarEdgesMatrix() {
		return similarEdges;
	} //getSimilarEdgesMatrix
	
	/**
	 * Returns the percent similar vertices matrix
	 * 
	 * @return - The percent similar vertices matrix
	 */
	public SimilarityMatrix getPercentSimilarVerticesMatrix() {
		return percentSimilarVertices;
	} //getPercentSimilarVerticesMatrix
	
	/**
	 * Returns the percent similar edges matrix
	 * 
	 * @return - The percent similar edges matrix
	 */
	public SimilarityMatrix getPercentSimilarEdgesMatrix() {
		return percentSimilarEdges;
	} //getPercentSimilarEdgesMatrix
	
} //SimilarityMatrices
