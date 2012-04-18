package resultviewer.data;

import java.util.ArrayList;

public class Species {
	int id;
	String expression;
	ArrayList<Molecule> molecules;
	ArrayList<Bond> bonds;
	
	/*
	 * Constructor using id and expression
	 */
	public Species(int id, String expression) {
		this.id = id;
		this.expression = expression;
		this.molecules = new ArrayList<Molecule>();
		this.bonds = new ArrayList<Bond>();
	}
	
	/*
	 * Constructor using id, expression, list of molecules, and list of bonds
	 */
	public Species(int id, String expression, ArrayList<Molecule> molecules,
			ArrayList<Bond> bonds) {
		this.id = id;
		this.expression = expression;
		this.molecules = molecules;
		this.bonds = bonds;
	}

	public int getId() {
		return id;
	}

	public String getExpression() {
		return expression;
	}

	public ArrayList<Molecule> getMolecules() {
		return molecules;
	}

	public void setMolecules(ArrayList<Molecule> molecules) {
		this.molecules = molecules;
	}

	public ArrayList<Bond> getBonds() {
		return bonds;
	}

	public void setBonds(ArrayList<Bond> bonds) {
		this.bonds = bonds;
	}
	
	public void addMolecule(Molecule m) {
		this.molecules.add(m);
	}
	
	public void addBond(Bond b) {
		this.bonds.add(b);
	}
	
	public Bond getBondById(int id) {
		for (int i = 0; i < bonds.size(); i++) {
			if (bonds.get(i).getId() == id) {
				return bonds.get(i);
			}
		}
		return null;
	}
	
}
