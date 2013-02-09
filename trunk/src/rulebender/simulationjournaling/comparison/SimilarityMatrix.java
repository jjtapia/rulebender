package rulebender.simulationjournaling.comparison;

public class SimilarityMatrix {

	private String[] labels;
	private double[][] matrix;
	
	private int numLabels;
	
	/**
	 * Constructor that initializes the labels array and the similarity matrix to null, and the number of labels to 0
	 */
	public SimilarityMatrix() {
		labels = null;
		matrix = null;		
		numLabels = 0;		
	} //SimilarityMatrix (constructor)
	
	/**
	 * Label the rows/columns of the similarity matrix with the collection of models passed in
	 * 
	 * @param modelNames - collection of model names
	 */
	public void setLabels(String[] modelNames) {
		// Determine the number of models being compared and initialize the arrays for the matrix
		numLabels = modelNames.length;
		
		labels = new String[numLabels];
		matrix = new double[numLabels][numLabels];
		
		initializeMatrix();
		
		// Copy the model names into the labels array
		for (int i = 0; i < numLabels; i++) {
			labels[i] = modelNames[i];
		} //for
		
	} //setLabels
	
	/**
	 * Initialize the similarity matrix
	 */
	public void initializeMatrix() {
		for (int i = 0; i < numLabels; i++) {
			for (int j = 0; j < numLabels; j++) {
				matrix[i][j] = 0;
			} //for
		} //for
	} //initializeMatrix
	
	/**
	 * Set the similarity value of the row/column pair to the value provided 
	 * 
	 * @param i - row (and column)
	 * @param j - column (and now)
	 * @param val - similarity value provided for the cell
	 */
	public void setSimilarityValue(int i, int j, double val) {
		matrix[i][j] = val;		
		matrix[j][i] = val;
	} //setSimilarityValue
	
	public double getSimilarityValue(int i, int j) {
		return matrix[i][j];
	} //getSimilarityValue
	
	public void printMatrix() {
		System.out.println("Labels:\n");
		
		for (int i = 0; i < numLabels; i++) {
			System.out.println(labels[i]);
		} //for
		
		System.out.println("\nMatrix:\n");
		
		for (int i = 0; i < numLabels; i++) {
			for (int j = 0; j < numLabels; j++) {
				if (i == j) {
					System.out.print("-----\t");
				} else {
					System.out.printf("%.3f", matrix[i][j]);
					System.out.print("\t");
				} //if-else
			} //for
			System.out.println("");
		} //for
		
	} //printMatrix
	
} //SimilarityMatrix
