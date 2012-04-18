
import java.awt.Color;
import java.util.ArrayList;


class Bond
{
	int molecule1;
	int component1;
	int state1;
	int molecule2;
	int component2;
	int state2;
	boolean CanGenerate;
	
	Color color = null;
	ArrayList<Point> position = new ArrayList<Point>();
	
	Bond()
	{
		molecule1 = -1;
		component1 = -1;
		state1 = -1;
		molecule2 = -1;
		component2 = -1;
		state2 = -1;
		CanGenerate = false;
	}
}
	
class Molecule
{
	String name;
	ArrayList<Component> components = new ArrayList<Component>();
	
	int x;
	int y;
	int width;
	int height;
	int textx;
	int texty;
	Color color = null;
}
	
class Component
{
	String name;
	ArrayList<State> states = new ArrayList<State>();
	
	int x;
	int y;
	int width;
	int height;
	int textx;
	int texty;
	Color color = null;
}

class State
{
	String name;
	
	int x;
	int y;
	int width;
	int height;
	int textx;
	int texty;
	Color color = null;
}
	
class Point
{
	int x;
	int y;
	Point(int inx, int iny)
	{
		x = inx;
		y = iny;
	}
}
	
class Rule
{
	String name;
	boolean bidirection;
	String rate1;
	String rate2;
	ArrayList<RulePattern> reactantpatterns = new ArrayList<RulePattern>();
	ArrayList<RulePattern> productpatterns = new ArrayList<RulePattern>();
	ArrayList<BondAction> bondactions = new ArrayList<BondAction>();
	//TODO: molecule actions, Non-trivial, refer to BNG bible to see different cases
	Rule()
	{
		rate1 = null;
		rate2 = null;
	}
}
	
class BondAction
{
	int bondindex;//start from 0
	int action;// negative means delete, positive means add
}

class RulePattern
{
	ArrayList<MoleculePattern> molepatterns = new ArrayList<MoleculePattern>();
	ArrayList<Integer> bonds = new ArrayList<Integer>();
}
	
class MoleculePattern
{
	int moleindex;
	ArrayList<ComponentPattern> comppatterns = new ArrayList<ComponentPattern>();
}
	
class ComponentPattern
{
	int compindex;
	int stateindex;
	int wildcards; // -1: None 0: ? 1: +
	ComponentPattern()
	{
		stateindex = -1;
		wildcards = -1;
	}
}

class PotentialBond
{
	String name;
	ArrayList<Site> sites = new ArrayList<Site>();
	PotentialBond(String in1, Site in2)
	{
		name = in1;
		sites.add(in2);
	}
}

class Site
{
	int molecule;
	int component;
	int state;
	
	Site(int in1, int in2, int in3)
	{
		molecule = in1;
		component = in2;
		state = in3;
	}
}