package editor;

import java.awt.Color;
import java.util.ArrayList;

class IBond
{
	int molecule1;
	int component1;
	int state1;
	int molecule2;
	int component2;
	int state2;
	boolean CanGenerate;
	
	Color color = null;
	ArrayList<IPoint> position = new ArrayList<IPoint>();
	
	IBond()
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
	
class IMolecule
{
	String name;
	ArrayList<IComponent> components = new ArrayList<IComponent>();
	
	int x;
	int y;
	int width;
	int height;
	int textx;
	int texty;
	Color color = null;
}
	
class IComponent
{
	String name;
	ArrayList<IState> states = new ArrayList<IState>();
	
	int x;
	int y;
	int width;
	int height;
	int textx;
	int texty;
	Color color = null;
}

class IState
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
	
class IPoint
{
	int x;
	int y;
	IPoint(int inx, int iny)
	{
		x = inx;
		y = iny;
	}
}
	
class IRule
{
	String name;
	boolean bidirection;
	String rate1;
	String rate2;
	ArrayList<IRulePattern> reactantpatterns = new ArrayList<IRulePattern>();
	ArrayList<IRulePattern> productpatterns = new ArrayList<IRulePattern>();
	ArrayList<IBondAction> bondactions = new ArrayList<IBondAction>();
	//TODO: molecule actions, Non-trivial, refer to BNG bible to see different cases
	IRule()
	{
		rate1 = null;
		rate2 = null;
	}
}
	
class IBondAction
{
	int bondindex;//start from 0
	int action;// negative means delete, positive means add
}

class IRulePattern
{
	ArrayList<IMoleculePattern> molepatterns = new ArrayList<IMoleculePattern>();
	ArrayList<Integer> bonds = new ArrayList<Integer>();
}
	
class IMoleculePattern
{
	int moleindex;
	boolean matched;
	ArrayList<IComponentPattern> comppatterns = new ArrayList<IComponentPattern>();
}
	
class IComponentPattern
{
	int compindex;
	int stateindex;
	int wildcards; // -1: None 0: ? 1: +
	boolean reactioncenter;
	boolean matched;
	ArrayList<Integer> bondlist = new ArrayList<Integer>();
	ArrayList<String> pbondlist = new ArrayList<String>();
	IComponentPattern()
	{
		stateindex = -1;
		wildcards = -1;
		reactioncenter = false;
	}
}

class IPotentialBond
{
	String name;
	ArrayList<ISite> sites = new ArrayList<ISite>();
	IPotentialBond(String in1, ISite in2)
	{
		name = in1;
		sites.add(in2);
	}
}

class ISite
{
	int molecule;
	int component;
	int state;
	
	ISite(int in1, int in2, int in3)
	{
		molecule = in1;
		component = in2;
		state = in3;
	}
}

class IRuleNode
{
	int index;
	int iruleindex;
	boolean forward;
	int[] position = new int[4];//xmin, xmax, ymin, ymax
	
	IRuleNode(int in1, int in2, boolean in3)
	{
		index = in1;
		iruleindex = in2;
		forward = in3;
	}
}

class Influence
{
	int startrulenodeindex;
	int endrulenodeindex;
	int activation;//-1: None, 0: possible, 1: definite
	int inhibition;//-1: None, 0: possible, 1: definite
	
	Influence(int in1, int in2)
	{
		startrulenodeindex = in1;
		endrulenodeindex = in2;
		activation = -1;
		inhibition = -1;
	}
	
	void setInhibition(boolean definite)
	{
		if(definite)
			inhibition = 1;
		else if(inhibition == -1)
			inhibition = 0;
		else
			;//Do Nothing	
	}
	
	void setActivation(boolean definite)
	{
		if(definite)
			activation = 1;
		else if(activation == -1)
			activation = 0;
		else
			;//Do Nothing	
	}
	
	boolean equals(Influence in)
	{
		if(in.startrulenodeindex == this.startrulenodeindex && in.endrulenodeindex == this.endrulenodeindex)
			return true;
		return false;
	}
}