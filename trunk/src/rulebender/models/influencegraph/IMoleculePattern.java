package rulebender.models.influencegraph;

import java.util.ArrayList;

public class IMoleculePattern
{
	private int moleindex;
	private boolean matched;
	private ArrayList<IComponentPattern> comppatterns = new ArrayList<IComponentPattern>();
	public void setComppatterns(ArrayList<IComponentPattern> comppatterns) {
		this.comppatterns = comppatterns;
	}
	public ArrayList<IComponentPattern> getComppatterns() {
		return comppatterns;
	}
	public void setMoleindex(int moleindex) {
		this.moleindex = moleindex;
	}
	public int getMoleindex() {
		return moleindex;
	}
	public void setMatched(boolean matched) {
		this.matched = matched;
	}
	public boolean isMatched() {
		return matched;
	}
}
