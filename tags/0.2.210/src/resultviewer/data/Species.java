package resultviewer.data;

import java.util.ArrayList;

public class Species {
	int id; // species id
	String expression; // species expression
	ArrayList<Molecule> molecules; // list of molecules involved in this species
	ArrayList<Bond> bonds; // list of inside bonds in this species

	/**
	 * 
	 * @param id
	 *            species id
	 * @param expression
	 *            species expression
	 */
	public Species(int id, String expression) {
		this.id = id;
		this.expression = expression;
		this.molecules = new ArrayList<Molecule>();
		this.bonds = new ArrayList<Bond>();
	}

	/**
	 * 
	 * @param id
	 *            species id
	 * @param expression
	 *            species expression
	 * @param molecules
	 *            list of molecules involved in this species
	 * @param bonds
	 *            list of inside bonds in this species
	 */
	public Species(int id, String expression, ArrayList<Molecule> molecules,
			ArrayList<Bond> bonds) {
		this.id = id;
		this.expression = expression;
		this.molecules = molecules;
		this.bonds = bonds;
	}

	/**
	 * 
	 * @return species id
	 */
	public int getId() {
		return id;
	}

	/**
	 * 
	 * @return species expression
	 */
	public String getExpression() {
		return expression;
	}

	/**
	 * 
	 * @return list of molecules involved in this species
	 */
	public ArrayList<Molecule> getMolecules() {
		return molecules;
	}

	/**
	 * 
	 * @param molecules
	 *            list of molecules involved in this species
	 */
	public void setMolecules(ArrayList<Molecule> molecules) {
		this.molecules = molecules;
	}

	/**
	 * 
	 * @return list of inside bonds in this species
	 */
	public ArrayList<Bond> getBonds() {
		return bonds;
	}

	/**
	 * 
	 * @param bonds
	 *            list of inside bonds in this species
	 */
	public void setBonds(ArrayList<Bond> bonds) {
		this.bonds = bonds;
	}

	/**
	 * Add a molecule to the list of molecules
	 * 
	 * @param m
	 *            a molecule involved in this species
	 */
	public void addMolecule(Molecule m) {
		this.molecules.add(m);
	}

	/**
	 * Add a bond to the list of inside bonds
	 * 
	 * @param b
	 *            an inside bond in this species
	 */
	public void addBond(Bond b) {
		this.bonds.add(b);
	}

	/**
	 * Get a Bond object based on its id
	 * 
	 * @param id
	 *            bond id
	 * @return a Bond object
	 */
	public Bond getBondById(int id) {
		for (int i = 0; i < bonds.size(); i++) {
			if (bonds.get(i).getId() == id) {
				return bonds.get(i);
			}
		}
		return null;
	}

}
