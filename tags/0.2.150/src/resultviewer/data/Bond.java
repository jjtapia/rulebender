package resultviewer.data;

public class Bond {
	int id;
	int leftM; // left molecule
	int leftC; // left component
	int rightM; // right molecule
	int rightC; // right component
	
	/*
	 * Constructor using id, left molecule and left component
	 */
	public Bond(int id, int leftM, int leftC) {
		this.id = id;
		this.leftM = leftM;
		this.leftC = leftC;
	}

	/*
	 * Constructor using id, left molecule, left component, right molecule and right component
	 */
	public Bond(int id, int leftM, int leftC, int rightM, int rightC) {
		this.id = id;
		this.leftM = leftM;
		this.leftC = leftC;
		this.rightM = rightM;
		this.rightC = rightC;
	}

	public int getId() {
		return id;
	}

	public int getLeftM() {
		return leftM;
	}

	public int getLeftC() {
		return leftC;
	}

	public int getRightM() {
		return rightM;
	}
	
	public int getRightC() {
		return rightC;
	}

	public void setRightPart(int rightM, int rightC) {
		this.rightM = rightM;
		this.rightC = rightC;
	}	
}
