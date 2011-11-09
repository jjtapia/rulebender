package rulebender.contactmap.models;

import java.util.ArrayList;
import java.util.List;

public class Compartment 
{
	private String name;
	private Compartment parent;
	private List<Compartment> children;
	
	public Compartment(String name, Compartment parent) 
	{
		this.name = name;
		this.parent = parent;
		
		this.children = new ArrayList<Compartment>();
	
		if (parent != null) {
			parent.addChild(this);
		}
	}
	
	public String getName() {
		return name;
	}
	
	public Compartment getParent() {
		return parent;
	}
	
	public void setParent(Compartment parent) {
		this.parent = parent;
	}
	
	public List<Compartment> getChildren() {
		return children;
	}
	
	public void addChild(Compartment child) {
		if (!this.children.contains(child)) {
			this.children.add(child);
		}
	}
}


