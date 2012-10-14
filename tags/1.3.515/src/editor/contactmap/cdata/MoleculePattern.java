package editor.contactmap.cdata;

import java.util.ArrayList;

public class MoleculePattern {

	private int moleindex;
	private ArrayList<ComponentPattern> comppatterns = new ArrayList<ComponentPattern>();

	public void setMoleindex(int moleindex) {
		this.moleindex = moleindex;
	}

	public int getMoleindex() {
		return moleindex;
	}

	public void setComppatterns(ArrayList<ComponentPattern> comppatterns) {
		this.comppatterns = comppatterns;
	}

	public ArrayList<ComponentPattern> getComppatterns() {
		return comppatterns;
	}
}
