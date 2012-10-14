package editor.contactmap.cdata;

import java.util.ArrayList;

public class RulePattern {

	private ArrayList<MoleculePattern> molepatterns = new ArrayList<MoleculePattern>();
	private ArrayList<Integer> bonds = new ArrayList<Integer>();

	public void setBonds(ArrayList<Integer> bonds) {
		this.bonds = bonds;
	}

	public ArrayList<Integer> getBonds() {
		return bonds;
	}

	public void setMolepatterns(ArrayList<MoleculePattern> molepatterns) {
		this.molepatterns = molepatterns;
	}

	public ArrayList<MoleculePattern> getMolepatterns() {
		return molepatterns;
	}
}
