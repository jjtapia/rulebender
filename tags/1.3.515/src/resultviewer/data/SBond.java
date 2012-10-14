package resultviewer.data;

public class SBond {
	int id; // bond id
	int leftM; // molecule id on the left side of the bond
	int leftC; // component id on the left side of the bond
	int leftS; // state id on the left side of the bond
	int rightM; // molecule id on the right side of the bond
	int rightC; // component id on the right side of the bond
	int rightS; // state id on the right side of the bond

	/**
	 * @param id
	 *            bond id
	 * @param leftM
	 *            molecule id on the left side of the bond
	 * @param leftC
	 *            component id on the left side of the bond
	 * @param leftS
	 *            state id on the left side of the bond
	 */
	public SBond(int id, int leftM, int leftC, int leftS) {
		this.id = id;
		this.leftM = leftM;
		this.leftC = leftC;
		this.leftS = leftS;
	}

	/**
	 * @param id
	 *            bond id
	 * @param leftM
	 *            molecule id on the left side of the bond
	 * @param leftC
	 *            component id on the left side of the bond
	 * @param leftS
	 *            state id on the left side of the bond
	 * @param rightM
	 *            molecule id on the right side of the bond
	 * @param rightC
	 *            component id on the right side of the bond
	 * @param rightS
	 *            state id on the right side of the bond
	 */
	public SBond(int id, int leftM, int leftC, int leftS, int rightM,
			int rightC, int rightS) {
		this.id = id;
		this.leftM = leftM;
		this.leftC = leftC;
		this.leftS = leftS;
		this.rightM = rightM;
		this.rightC = rightC;
		this.rightS = rightS;
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
	 * @return state id on the left side of the bond
	 */
	public int getLeftS() {
		return leftS;
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
	 * @return state id on the right side of the bond
	 */
	public int getRightS() {
		return rightS;
	}

	/**
	 * 
	 * @param rightM
	 *            molecule id on the right side of the bond
	 * @param rightC
	 *            component id on the right side of the bond
	 * @param rightS
	 *            state id on the right side of the bond
	 */
	public void setRightPart(int rightM, int rightC, int rightS) {
		this.rightM = rightM;
		this.rightC = rightC;
		this.rightS = rightS;
	}
}
