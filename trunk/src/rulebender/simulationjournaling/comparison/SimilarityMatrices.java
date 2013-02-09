package rulebender.simulationjournaling.comparison;

import prefuse.Display;
import prefuse.Visualization;

public class SimilarityMatrices {

	private SimilarityMatrix similarVertices = null;
	private SimilarityMatrix similarEdges = null;
	
	private SimilarityMatrix percentSimilarVertices = null;
	private SimilarityMatrix percentSimilarEdges = null;
	
	private AdjacencyMatrix matrices[];
	private Visualization models[];

	private String[] modelNames;
	
	public SimilarityMatrices(String newModelNames[], Display newModels[]) {
		modelNames = newModelNames;
		models = new Visualization[newModels.length];
		
		for (int i = 0; i < newModels.length; i++) {
			models[i] = newModels[i].getVisualization();
		} //for
		
	} //SimilarityMatrices (constructor)
	
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
	
	public SimilarityMatrix getSimilarVerticesMatrix() {
		return similarVertices;
	} //getSimilarVerticesMatrix
	
	public SimilarityMatrix getSimilarEdgesMatrix() {
		return similarEdges;
	} //getSimilarEdgesMatrix
	
	public SimilarityMatrix getPercentSimilarVerticesMatrix() {
		return percentSimilarVertices;
	} //getPercentSimilarVerticesMatrix
	
	public SimilarityMatrix getPercentSimilarEdgesMatrix() {
		return percentSimilarEdges;
	} //getPercentSimilarEdgesMatrix
	
} //SimilarityMatrices
