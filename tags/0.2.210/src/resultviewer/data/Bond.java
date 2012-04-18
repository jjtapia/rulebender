package resultviewer.data;

public class Bond {
	int id; // bond id
	int leftM; // molecule id on the left side of the bond
	int leftC; // component id on the left side of the bond
	int rightM; // molecule id on the right side of the bond
	int rightC; // component id on the right side of the bond

	/**
	 * @param id
	 *            bond id
	 * @param leftM
	 *            molecule id on the left side of the bond
	 * @param leftC
	 *            component id on the left side of the bond
	 */
	public Bond(int id, int leftM, int leftC) {
		this.id = id;
		this.leftM = leftM;
		this.leftC = leftC;
	}

	/**
	 * @param id
	 *            bond id
	 * @param leftM
	 *            molecule id on the left side of the bond
	 * @param leftC
	 *            component id on the left side of the bond
	 * @param rightM
	 *            molecule id on the right side of the bond
	 * @param rightC
	 *            component id on the right side of the bond
	 */
	public Bond(int id, int leftM, int leftC, int rightM, int rightC) {
		this.id = id;
		this.leftM = leftM;
		this.leftC = leftC;
		this.rightM = rightM;
		this.rightC = rightC;
	}

	/**
	 * 
	 * @return bond id
	 */
	public int getId() {
		return id;
	}

	/**
	 * 
	 * @return molecule id on the left side of the bond
	 */
	public int getLeftM() {
		return leftM;
	}

	/**
	 * 
	 * @return component id on the left side of the bond
	 */
	public int getLeftC() {
		return leftC;
	}

	/**
	 * 
	 * @return molecule id on the right side of the bond
	 */
	public int getRightM() {
		return rightM;
	}

	/**
	 * 
	 * @return component id on the right side of the bond
	 */
	public int getRightC() {
		return rightC;
	}

	/**
	 * 
	 * @param rightM
	 *            molecule id on the right side of the bond
	 * @param rightC
	 *            component id on the right side of the bond
	 */
	public void setRightPart(int rightM, int rightC) {
		this.rightM = rightM;
		this.rightC = rightC;
	}
}
