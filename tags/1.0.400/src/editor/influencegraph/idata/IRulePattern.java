package editor.influencegraph.idata;

import java.util.ArrayList;

public class IRulePattern
{
	private ArrayList<IMoleculePattern> molepatterns = new ArrayList<IMoleculePattern>();
	private ArrayList<Integer> bonds = new ArrayList<Integer>();
	public void setBonds(ArrayList<Integer> bonds) {
		this.bonds = bonds;
	}
	public ArrayList<Integer> getBonds() {
		return bonds;
	}
	public void setMolepatterns(ArrayList<IMoleculePattern> molepatterns) {
		this.molepatterns = molepatterns;
	}
	public ArrayList<IMoleculePattern> getMolepatterns() {
		return molepatterns;
	}
}
