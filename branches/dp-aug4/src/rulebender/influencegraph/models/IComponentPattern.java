package rulebender.influencegraph.models;

import java.util.ArrayList;

public class IComponentPattern
{
	private int compindex;
	private int stateindex;
	private int wildcards; // -1: None 0: ? 1: +
	private boolean reactioncenter;
	private boolean matched;
	private ArrayList<Integer> bondlist = new ArrayList<Integer>();
	private ArrayList<String> pbondlist = new ArrayList<String>();
	public IComponentPattern()
	{
		setStateindex(-1);
		setWildcards(-1);
		setReactioncenter(false);
	}
	public void setPbondlist(ArrayList<String> pbondlist) {
		this.pbondlist = pbondlist;
	}
	public ArrayList<String> getPbondlist() {
		return pbondlist;
	}
	public void setBondlist(ArrayList<Integer> bondlist) {
		this.bondlist = bondlist;
	}
	public ArrayList<Integer> getBondlist() {
		return bondlist;
	}
	public void setCompindex(int compindex) {
		this.compindex = compindex;
	}
	public int getCompindex() {
		return compindex;
	}
	public void setStateindex(int stateindex) {
		this.stateindex = stateindex;
	}
	public int getStateindex() {
		return stateindex;
	}
	public void setWildcards(int wildcards) {
		this.wildcards = wildcards;
	}
	public int getWildcards() {
		return wildcards;
	}
	public void setMatched(boolean matched) {
		this.matched = matched;
	}
	public boolean isMatched() {
		return matched;
	}
	public void setReactioncenter(boolean reactioncenter) {
		this.reactioncenter = reactioncenter;
	}
	public boolean isReactioncenter() {
		return reactioncenter;
	}
}