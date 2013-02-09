package rulebender.simulationjournaling.comparison;

public class AdjacencyMatrixComparison {

	private static int return_numSimilarVertices;
	private static int return_numSimilarEdges;
	private static double return_percentSimilarVertices;
	private static double return_percentSimilarEdges;
	
	/**
	 * Return the number of similar vertices to the caller
	 * 
	 * @return return_numSimilarVertices
	 */
	public static int getNumSimilarVertices() {
		return return_numSimilarVertices;
	} //getNumSimilarVertices
	
	/**
	 * Return the number of similar edges to the caller
	 * 
	 * @return return_numSimilarEdges
	 */
	public static int getNumSimilarEdges() {
		return return_numSimilarEdges;
	} //getNumSimilarEdges
	
	/**
	 * Return the percentage of vertices in matrix 2 shared by matrix 1
	 * 
	 * @return return_percentSimilarVertices
	 */
	public static double getPercentSimilarVertices() {
		return return_percentSimilarVertices;
	} //getPercentSimilarVertices
	
	/**
	 * Return the percentage of edges in matrix 2 shared by matrix 2
	 * 
	 * @return return_percentSimilarEdges
	 */
	public static double getPercentSimilarEdges() {
		return return_percentSimilarEdges;
	} //getPercentSimilarEdges
	
	/**
	 * Compare two adjacency matrices, and set up return values
	 * 
	 * @param matrix1 - the first matrix
	 * @param matrix2 - the second matrix
	 */
	public static void compareTwoMatrices(AdjacencyMatrix matrix1, AdjacencyMatrix matrix2) {
		
		// Sizes of the larger and smaller matrices
		int largerMatrixSize = 0,
			smallerMatrixSize = 0;
		
		// Labels for the larger and smaller matrices
		String largerMatrixLabels[], smallerMatrixLabels[];
		
		// Counts of similar vertices and similar edges
		int similarVertexCount = 0,
			similarEdgeCount = 0;
		
		// The array that stores the pairs of similar vertices
		VertexPair vertexPairs[];
		
		// Which matrix is larger
		int largerMatrix = 0;			
		
		// Number of edges in matrix 1 (for percentage)
		int numEdgesInMatrix1 = 0;
		
		if (matrix1.getLabels().length > matrix2.getLabels().length) {
			largerMatrixLabels = matrix1.getLabels();
			largerMatrixSize = largerMatrixLabels.length;
			smallerMatrixLabels = matrix2.getLabels();
			smallerMatrixSize = smallerMatrixLabels.length;
			largerMatrix = 1;
		} else {
			largerMatrixLabels = matrix2.getLabels();
			largerMatrixSize = largerMatrixLabels.length;
			smallerMatrixLabels = matrix1.getLabels();
			smallerMatrixSize = smallerMatrixLabels.length;
			largerMatrix = 2;
		} //if-else
		
		
		// Count similar vertices first
		for (int i = 0; i < largerMatrixSize; i++) {
			for (int j = 0; j < smallerMatrixSize; j++) {
				if (largerMatrixLabels[i].equals(smallerMatrixLabels[j])) {
					similarVertexCount++;
				} //if
			} //for
		} //for

		return_numSimilarVertices = similarVertexCount;
		
		if (matrix1.getLabels().length == 0) {
			return_percentSimilarVertices = 0;
		} else {
			return_percentSimilarVertices = (double)similarVertexCount / matrix1.getLabels().length;	
		} //if-else
		
		// Create an array to store the pairs of similar vertices
		vertexPairs = new VertexPair[similarVertexCount];
		
		// Determine the number of edges in matrix1
		for (int i = 0; i < matrix1.getLabels().length; i++) {
			for (int j = i; j < matrix1.getLabels().length; j++) {
				if (matrix1.doesEdgeExist(i, j)) {
					numEdgesInMatrix1++;
				} //if
			} //for
		} //for
		
		int pairIndex = 0;
		// Figure out which vertices are the same between the two graphs
		for (int i = 0; i < largerMatrixSize; i++) {
			for (int j = 0; j < smallerMatrixSize; j++) {
				if (largerMatrixLabels[i].equals(smallerMatrixLabels[j])) {
					vertexPairs[pairIndex] = new VertexPair(i, j);
					pairIndex++;
				} //if
			} //for
		} //for
		
		
		// Now loop through all similar vertex pairs to see if similar edges exist
		for (int i = 0; i < similarVertexCount; i++) {
			for (int j = i; j < similarVertexCount; j++) {
				if (largerMatrix == 1) {
					if ((matrix1.doesEdgeExist(vertexPairs[i].getLargerGraphVertex(), vertexPairs[j].getLargerGraphVertex())) && (matrix2.doesEdgeExist(vertexPairs[i].getSmallerGraphVertex(), vertexPairs[j].getSmallerGraphVertex()))) {
						similarEdgeCount++;
					} //if
					
				} else {
					if ((matrix2.doesEdgeExist(vertexPairs[i].getLargerGraphVertex(), vertexPairs[j].getLargerGraphVertex())) && (matrix1.doesEdgeExist(vertexPairs[i].getSmallerGraphVertex(), vertexPairs[j].getSmallerGraphVertex()))) {
						similarEdgeCount++;
					} //if			
				} //if-else
			} //for
		} //for
		
		return_numSimilarEdges = similarEdgeCount;
		
		if (numEdgesInMatrix1 == 0) {
			return_percentSimilarEdges = 0;
		} else {
			return_percentSimilarEdges = (double)similarEdgeCount / numEdgesInMatrix1;
		} //if-else
		
	} //compareTwoMatrices
	
	
	/** 
	 * Inner class to hold vertex pairs (the same vertex in two graphs)
	 * 
	 * @author John
	 */
	private static class VertexPair {
		
		int largerGraphVertex = 0, 
			smallerGraphVertex = 0;
		
		/**
		 * Constructor to set the matching pair of vertices
		 * 
		 * @param larger - larger adjacency matrix
		 * @param smaller - smaller adjacency matrix
		 */
		public VertexPair(int larger, int smaller) {
			setPair(larger, smaller);
		} //VertexPair (constructor)
		
		/**
		 * Set the matching pair of vertices
		 * 
		 * @param larger - larger adjacency matrix
		 * @param smaller - smaller adjacency matrix
		 */
		public void setPair(int larger, int smaller) {
			largerGraphVertex = larger;
			smallerGraphVertex = smaller;
		} //setPair
		
		/**
		 * Return the vertex from the pair associated with the larger graph
		 * 
		 * @return largerGraphVertex
		 */
		public int getLargerGraphVertex() {
			return largerGraphVertex;
		} //getLargerGraphVertex
		
		/** 
		 * Return the vertex from the pair associated with the smaller graph
		 * 
		 * @return smallerGraphVertex
		 */
		public int getSmallerGraphVertex() {
			return smallerGraphVertex;
		} //getSmallerGRaphVertex
		
	} //VertexPair
	
} //AdjacencyMatrixComparison
