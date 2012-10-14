package editor.contactmap.cdata;

import java.awt.Color;
import java.util.ArrayList;

public class Component {
	private String name;
	private ArrayList<State> states = new ArrayList<State>();

	int x;
	int y;
	int width;
	int height;
	int textx;
	int texty;
	Color color = null;

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setStates(ArrayList<State> states) {
		this.states = states;
	}

	public ArrayList<State> getStates() {
		return states;
	}
}
