package editor.contactmap;
/**
 * CMap.java
 * 
 * This file defines the CMap class.  The ContactMap is a visualization
 * of molecules and their potential interactions.
 * 
 * @author Yao Sun - Original code.
 * @author Adam M. Smith - Additions and documentation marked with '-ams <date>' 
 */

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import networkviewer.NetworkViewer;
import networkviewer.VisualRule;

import editor.BNGEditor;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.visual.AggregateItem;
import prefuse.visual.AggregateTable;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import bubbleset.BubbleModel;
import bubbleset.VirtualNode;

/******************************************
 * 
 * 1. Some bugs found with duplicate components models, ex: A(a,a)->A(a!1, a!1)
 * 2. Duplicate component propagation in bonds
 *
 ******************************************/

public class CMap extends JFrame
{
	// ArrayLists to hold the necessary CMap data.
	ArrayList<Molecule> molecules = new ArrayList<Molecule>();
	ArrayList<Bond> bonds = new ArrayList<Bond>();
	ArrayList<Rule> rules = new ArrayList<Rule>();
	
	private boolean ruleparsevalid;
	private ArrayList<PotentialBond> pbonds = new ArrayList<PotentialBond>();
	private boolean flexiblestate = false;
	
	public CMap(String molestr, String rulestr, boolean moleculetype)
	{
	
		if(moleculetype)
			parsemoleculetypes(molestr);
		else
		{
			flexiblestate = true;
			parsespecies(molestr);
		}
		parserules(rulestr);
		//generateCMapDOT();
		drawcmap();
	}
	
	void parsemoleculetypes(String molestr)
	{
		Scanner scan = new Scanner(molestr);
		String tempstr1;
		while(scan.hasNext())
		{
			String TBProcessed = scan.nextLine().trim();
			if(TBProcessed.length()==0)
				continue;
			if(TBProcessed.charAt(0)=='#')
				continue;
			if(TBProcessed.indexOf('#')!=-1)
				TBProcessed = TBProcessed.substring(0, TBProcessed.indexOf('#')).trim();
			while (TBProcessed.charAt(TBProcessed.length()-1) == '\\')
			{
				if(scan.hasNext())
					TBProcessed = TBProcessed.substring(0, TBProcessed.length()-1) + ' ' + scan.nextLine().trim();
				else
					TBProcessed = TBProcessed.substring(0, TBProcessed.length()-1);
		        if(TBProcessed.indexOf('#')>=0)
		        	TBProcessed=TBProcessed.substring(0, TBProcessed.indexOf('#')).trim();
			}
			if(TBProcessed.indexOf(' ')!=-1)
			{
				tempstr1 = TBProcessed.substring(0,TBProcessed.indexOf(' ')).trim();
				try
				{Integer.parseInt(tempstr1);
				parsemolecule(TBProcessed.substring(TBProcessed.indexOf(' ')+1,TBProcessed.length()).trim());}
				catch(NumberFormatException e){}	
			}
			parsemolecule(TBProcessed);
		}
	}
	
	void parsespecies(String molestr)
	{
		Scanner scan = new Scanner(molestr);
		String tempstr1;
		while(scan.hasNext())
		{
			String TBProcessed = scan.nextLine().trim();
			if(TBProcessed.length()==0)
				continue;
			if(TBProcessed.charAt(0)=='#')
				continue;
			if(TBProcessed.indexOf('#')!=-1)
				TBProcessed = TBProcessed.substring(0, TBProcessed.indexOf('#')).trim();
			while (TBProcessed.charAt(TBProcessed.length()-1) == '\\')
			{
				if(scan.hasNext())
					TBProcessed = TBProcessed.substring(0, TBProcessed.length()-1) + ' ' + scan.nextLine().trim();
				else
					TBProcessed = TBProcessed.substring(0, TBProcessed.length()-1);
		        if(TBProcessed.indexOf('#')>=0)
		        	TBProcessed=TBProcessed.substring(0, TBProcessed.indexOf('#')).trim();
			}
			if(TBProcessed.indexOf(' ')!=-1)
			{
				tempstr1 = TBProcessed.substring(0,TBProcessed.indexOf(' ')).trim();
				try
				{Integer.parseInt(tempstr1);
				TBProcessed = TBProcessed.substring(TBProcessed.indexOf(' ')+1,TBProcessed.length()).trim();}
				catch(NumberFormatException e){}
			}
			while(TBProcessed.lastIndexOf(' ')!=-1)
			{
				tempstr1 = TBProcessed.substring(TBProcessed.lastIndexOf(' ')+1, TBProcessed.length()).trim();
				if(tempstr1.indexOf('(')==-1 && tempstr1.indexOf(')')==-1 && tempstr1.indexOf(',')==-1 && tempstr1.indexOf('.')==-1 && tempstr1.indexOf('!')==-1 && tempstr1.indexOf('+')==-1 && tempstr1.indexOf('?')==-1 && tempstr1.indexOf('~')==-1 && tempstr1.indexOf('{')==-1 && tempstr1.indexOf('}')==-1)
					TBProcessed = TBProcessed.substring(0, TBProcessed.lastIndexOf(' ')).trim();
				else
					break;
			}
			while(TBProcessed.indexOf('.')!=-1)
			{
				parsemolecule(TBProcessed.substring(0, TBProcessed.indexOf('.')).trim());
				TBProcessed = TBProcessed.substring(TBProcessed.indexOf('.')+1, TBProcessed.length()).trim();
			}
			parsemolecule(TBProcessed);
		}
	}
	
	/**
	 * This is where each molecule is parsed and the added to the arrayList.
	 * 
	 * -ams292 5/1/10
	 * @param molecule
	 */
	void parsemolecule(String molecule)
	{
		boolean leftparenthis = false;
		boolean rightparenthis = false;
		Molecule tempmole = new Molecule();
		String component = "";
		if(molecule.trim().length() == 0)
			return;
		if(molecule.indexOf('(')!=-1)
			leftparenthis = true;
		if(molecule.indexOf(')')!=-1)
			rightparenthis = true;
		
		// IF there are ( and ) then get the name, and the component list.
		if(leftparenthis && rightparenthis)
		{
			tempmole.name = molecule.substring(0, molecule.indexOf('(')).trim();
			component = molecule.substring(molecule.indexOf('(')+1, molecule.indexOf(')')).trim();
		}
		
		// If there is no ) then pull the name out and get the components from ( to end.
		if(leftparenthis && !rightparenthis)
		{
			tempmole.name = molecule.substring(0, molecule.indexOf('(')).trim();
			component = molecule.substring(molecule.indexOf('(')+1, molecule.length()).trim();
		}
		
		// If there are no parens, then just get the name. 
		if(!leftparenthis && !rightparenthis)
		{
			tempmole.name = molecule;
		}
		
		// Error on ) and no (.
		if(!leftparenthis && rightparenthis)
		{
			BNGEditor.console.append("\nError parsing molecule : "+molecule+" , molecule not added !");
			return;
		}
		
		// Handle the components.
		while(component.indexOf(',')!=-1)
		{
			parsecomponent(tempmole,component.substring(0,component.indexOf(',')).trim());
			component = component.substring(component.indexOf(',')+1, component.length()).trim();
		}
		parsecomponent(tempmole,component);
		if(validatemole(tempmole))
		{
			molecules.add(tempmole);
		}
		else
			BNGEditor.console.append("\nError parsing molecule : "+molecule+" , molecule not added !");
	}
	
	/**
	 * This is where components of molecules are parsed and added to the 
	 * Molecule data structure.
	 * 
	 *  -ams292 5/1/10
	 * @param tempmole
	 * @param component
	 */
	void parsecomponent(Molecule tempmole, String component)
	{
		if(component.trim().length() == 0)
			return;
		Component tempcomp = new Component();
		State tempstate;
		String state;
		String tempstr1;
		if(component.indexOf('~')==-1)
		{
			if(component.indexOf('!')!=-1)
				component = component.substring(0,component.indexOf('!')).trim();
			tempcomp.name = component;
			tempmole.components.add(tempcomp);
			return;
		}
		else
		{
			state = component.substring(component.indexOf('~')+1, component.length()).trim();
			component = component.substring(0,component.indexOf('~'));
			if(component.indexOf('!')!=-1)
				component = component.substring(0,component.indexOf('!')).trim();
			tempcomp.name = component;
			while(state.indexOf('~')!=-1)
			{
				tempstr1 = state.substring(0, state.indexOf('~'));
				if(tempstr1.indexOf('!')!=-1)
					tempstr1 = tempstr1.substring(0,tempstr1.indexOf('!')).trim();
				tempstate = new State();
				tempstate.name = tempstr1;
				tempcomp.states.add(tempstate);
				state = state.substring(state.indexOf('~')+1, state.length()).trim();
			}
			tempstr1 = state;
			if(tempstr1.indexOf('!')!=-1)
				tempstr1 = tempstr1.substring(0,tempstr1.indexOf('!')).trim();
			tempstate = new State();
			tempstate.name = tempstr1;
			tempcomp.states.add(tempstate);
			tempmole.components.add(tempcomp);
		}
	}
	
	/** 
	 * This method runs a few checks on the Molecule data structure 
	 * to determine its validity
	 * 
	 * -ams292 5/1/10
	 * 
	 * @param tempmole
	 * @return
	 */
	boolean validatemole(Molecule tempmole)
	{
		for(int i = 0; i < tempmole.components.size(); i++)//delete the entries with empty string names
		{
			if(tempmole.components.get(i).name.trim().length()==0)
			{
				tempmole.components.remove(i);
				i--;
			}
			else // Remove states that are empty
			{
				for(int j = 0; j < tempmole.components.get(i).states.size(); j++)
					if(tempmole.components.get(i).states.get(j).name.trim().length() == 0)
					{
						tempmole.components.get(i).states.remove(j);
						j--;
					}
			}
		}
		// no garbage in the names
		if(!validatename(tempmole.name))
			return false;
		for(int i = 0; i < tempmole.components.size(); i++)
			if(!validatename(tempmole.components.get(i).name))
				return false;
			else
				for(int j = 0; j < tempmole.components.get(i).states.size(); j++)
					if(!validatename(tempmole.components.get(i).states.get(j).name))
						return false;
		return true;
	}
	
	/**
	 * Check for a valid name that does not contain any special characters.
	 * 
	 * -ams292 5/1/10
	 *
	 * @param name
	 * @return
	 */
	boolean validatename(String name)
	{
		name = name.trim();
		if(name.indexOf('+')!=-1 || name.indexOf('(')!=-1 || name.indexOf(')')!=-1 || name.indexOf('.')!=-1 || name.indexOf(',')!=-1 || name.indexOf('!')!=-1  || name.indexOf('?')!=-1 || name.indexOf('~')!=-1 || name.indexOf(' ')!=-1 || name.indexOf('\t')!=-1 || name.indexOf('{')!=-1 || name.indexOf('}')!=-1 || name.indexOf('<')!=-1 || name.indexOf('>')!=-1 || name.indexOf('-')!=-1)
			return false;
		return true;
	}
	
	
	/**
	 * This method parses the rules between molecules.  If the rule is valid 
	 * then it is added to the rules arraylist. 
	 * 
	 * -ams292 5/1/10
	 * 
	 * @param rulestr
	 */
	void parserules(String rulestr)
	{
		Scanner scan = new Scanner(rulestr);
		String tempstr1;
		Pattern pattern1 = Pattern.compile("\\{.*\\}");
		Pattern pattern2 = Pattern.compile("\\s+DeleteMolecules\\s+");
		Matcher m;
		Rule temprule;
		while(scan.hasNext())
		{
			String TBProcessed = scan.nextLine().trim();
			if(TBProcessed.length()==0)
				continue;
			if(TBProcessed.charAt(0)=='#')
				continue;
			if(TBProcessed.indexOf('#')!=-1)
				TBProcessed = TBProcessed.substring(0, TBProcessed.indexOf('#')).trim();
			while (TBProcessed.charAt(TBProcessed.length()-1) == '\\')
			{
				if(scan.hasNext())
					TBProcessed = TBProcessed.substring(0, TBProcessed.length()-1) + ' ' + scan.nextLine().trim();
				else
					TBProcessed = TBProcessed.substring(0, TBProcessed.length()-1);
		        if(TBProcessed.indexOf('#')>=0)
		        	TBProcessed=TBProcessed.substring(0, TBProcessed.indexOf('#')).trim();
			}
			if(TBProcessed.indexOf(' ')!=-1)
			{
				tempstr1 = TBProcessed.substring(0,TBProcessed.indexOf(' ')).trim();
				try
				{Integer.parseInt(tempstr1);
				TBProcessed = TBProcessed.substring(TBProcessed.indexOf(' ')+1,TBProcessed.length()).trim();}
				catch(NumberFormatException e){}	
			}	
			ruleparsevalid = true;
			temprule = new Rule();
			temprule.name = TBProcessed;
			m = pattern1.matcher(TBProcessed);//delete
			while(m.find())
			{
				TBProcessed = TBProcessed.substring(0,m.start()).trim()+" "+TBProcessed.substring(m.end(), TBProcessed.length()).trim();
				m = pattern1.matcher(TBProcessed);
			}
			TBProcessed = TBProcessed + " ";
			m = pattern2.matcher(TBProcessed);
			while(m.find())
			{
				TBProcessed = TBProcessed.substring(0,m.start()).trim()+ " " + TBProcessed.substring(m.end(),TBProcessed.length()).trim() + " ";
				m = pattern2.matcher(TBProcessed);
			}
			TBProcessed = TBProcessed.trim();
			if(TBProcessed.contains("include_reactants"))
				TBProcessed = TBProcessed.substring(0,TBProcessed.indexOf("include_reactants")).trim();
			if(TBProcessed.contains("exclude_reactants"))
				TBProcessed = TBProcessed.substring(0,TBProcessed.indexOf("exclude_reactants")).trim();
			if(TBProcessed.contains("include_products"))
				TBProcessed = TBProcessed.substring(0,TBProcessed.indexOf("include_products")).trim();
			if(TBProcessed.contains("exclude_products"))
				TBProcessed = TBProcessed.substring(0,TBProcessed.indexOf("exclude_products")).trim();
			
			parserule(temprule,TBProcessed);	
			if(ruleparsevalid)
				rules.add(temprule);
			else
				BNGEditor.console.append("\nError parsing rule : "+TBProcessed+" , rule not added !");
		}
	}
	
	
	/**
	 * Parses each individual rule.
	 * 
	 * -ams292 5/1/10
	 * 
	 * @param temprule
	 * @param rulestr
	 */
	void parserule(Rule temprule, String rulestr)
	{
		Pattern pattern1 = Pattern.compile("\\-\\s*>");
		Matcher m = pattern1.matcher(rulestr);
		String tempstr1, tempstr2;
		if(!m.find())
		{
			ruleparsevalid = false;
			return;
		}
		
		if(rulestr.indexOf('<')!=-1)
		{
			temprule.bidirection = true;
			while(rulestr.lastIndexOf(' ')!=-1)
			{
				tempstr1 = rulestr.substring(rulestr.lastIndexOf(' '), rulestr.length()).trim();
				if(tempstr1.indexOf('(')==-1 && tempstr1.indexOf(')')==-1 && tempstr1.indexOf('>')==-1 && tempstr1.indexOf('.')==-1 && tempstr1.indexOf('!')==-1 && tempstr1.indexOf('+')==-1 && tempstr1.indexOf('?')==-1 && tempstr1.indexOf('~')==-1 && tempstr1.indexOf('{')==-1 && tempstr1.indexOf('}')==-1)
				{
					if(tempstr1.indexOf(',')!=-1)
					{
						tempstr2 = tempstr1.substring(tempstr1.indexOf(',')+1, tempstr1.length()).trim();
						tempstr1 = tempstr1.substring(0,tempstr1.indexOf(',')).trim();
						if(validaterate(tempstr1) && validaterate(tempstr2))
						{
							if(tempstr2.length()!=0)
								temprule.rate2 = tempstr2;
							else
								temprule.rate2 = temprule.rate1;
							temprule.rate1 = tempstr1;
							rulestr = rulestr.substring(0, rulestr.lastIndexOf(' ')).trim();
						}
						else 
							break;
					}
					else
					{
						if(validaterate(tempstr1))
						{
							if(temprule.rate1!=null && !(temprule.rate1.length()==0))
								temprule.rate2 = temprule.rate1;				
							temprule.rate1 = tempstr1;
							rulestr = rulestr.substring(0, rulestr.lastIndexOf(' ')).trim();
						}
						else
							break;
					}
				}
				else
					break;
			}
			parserule2pattern(temprule,rulestr);
		}
		else
		{
			temprule.bidirection = false;
			while(rulestr.lastIndexOf(' ')!=-1)
			{
				tempstr1 = rulestr.substring(rulestr.lastIndexOf(' '), rulestr.length()).trim();
				if(tempstr1.indexOf('(')==-1 && tempstr1.indexOf(')')==-1 && tempstr1.indexOf('>')==-1 && tempstr1.indexOf('.')==-1 && tempstr1.indexOf('!')==-1 && tempstr1.indexOf('+')==-1 && tempstr1.indexOf('?')==-1 && tempstr1.indexOf('~')==-1 && tempstr1.indexOf('{')==-1 && tempstr1.indexOf('}')==-1)
				{
					if(tempstr1.indexOf(',')!=-1)
					{
						tempstr2 = tempstr1.substring(tempstr1.indexOf(',')+1, tempstr1.length()).trim();
						tempstr1 = tempstr1.substring(0,tempstr1.indexOf(',')).trim();
						if(validaterate(tempstr1) && validaterate(tempstr2))
						{
							if(temprule.rate1 == null || tempstr1.length()!=0)
								temprule.rate1 = tempstr1;
							else
								if(tempstr2.length()!=0)
									temprule.rate1 = tempstr2;									
							rulestr = rulestr.substring(0, rulestr.lastIndexOf(' ')).trim();
						}
						else 
							break;
					}
					else
					{
						if(validaterate(tempstr1))
						{				
							temprule.rate1 = tempstr1;
							rulestr = rulestr.substring(0, rulestr.lastIndexOf(' ')).trim();
						}
						else
							break;
					}
				}
				else
					break;
			}
			parserule2pattern(temprule,rulestr);
		}
	}
	
	boolean validaterate(String rate)
	{
		rate = rate.trim();
		if(rate.length()==0)
			return true;
		for(int i = 0; i < molecules.size(); i++)
			if(molecules.get(i).name.equals(rate))
				return false;
			else
				for(int j = 0; j<molecules.get(i).components.size(); j++)
					if(molecules.get(i).components.get(j).name.equals(rate))
						return false;
		return true;
	}
	
	void parserule2pattern(Rule temprule, String rulestr)
	{
		String reactants,products;
		Pattern pattern1 = Pattern.compile("!\\s*\\+");
		Matcher m = pattern1.matcher(rulestr);
		while(m.find())
		{
			rulestr = rulestr.substring(0, m.start()).trim()+"!@"+rulestr.substring(m.end(), rulestr.length()).trim();
			m = pattern1.matcher(rulestr);
		}

		if(temprule.bidirection)
			reactants = rulestr.substring(0, rulestr.indexOf('<')).trim();
		else
			reactants = rulestr.substring(0, rulestr.indexOf('-')).trim();
		products = rulestr.substring(rulestr.indexOf('>')+1, rulestr.length()).trim();
		while(reactants.indexOf('+')!=-1)
		{
			parsepatterns(temprule.reactantpatterns, reactants.substring(0, reactants.indexOf('+')).trim());
			reactants = reactants.substring(reactants.indexOf('+')+1, reactants.length()).trim();			
		}
		parsepatterns(temprule.reactantpatterns, reactants);
		while(products.indexOf('+')!=-1)
		{
			parsepatterns(temprule.productpatterns, products.substring(0, products.indexOf('+')).trim());
			products = products.substring(products.indexOf('+')+1, products.length()).trim();			
		}
		parsepatterns(temprule.productpatterns, products);
		//determine bondaction & bond Cangenerate
		if(ruleparsevalid)
		{
			for(int i=0; i<temprule.reactantpatterns.size(); i++)
				for(int j=0; j<temprule.reactantpatterns.get(i).bonds.size(); j++)
					add2bondactions(temprule.bondactions, temprule.reactantpatterns.get(i).bonds.get(j), false);
			for(int i=0; i<temprule.productpatterns.size(); i++)
				for(int j=0; j<temprule.productpatterns.get(i).bonds.size(); j++)
					add2bondactions(temprule.bondactions, temprule.productpatterns.get(i).bonds.get(j), true);
			for(int i=0; i<temprule.bondactions.size(); i++)
				if(temprule.bondactions.get(i).action == 0)
				{
					temprule.bondactions.remove(i);
					i--;
				}
			if(temprule.bidirection)
				for(int i=0; i<temprule.bondactions.size(); i++)
					bonds.get(temprule.bondactions.get(i).bondindex).CanGenerate = true;
			else
				for(int i=0; i<temprule.bondactions.size(); i++)
					if(temprule.bondactions.get(i).action>0)
						bonds.get(temprule.bondactions.get(i).bondindex).CanGenerate = true;
		}
	}
	
	void add2bondactions(ArrayList<BondAction> ba, int rule, boolean addbond)
	{
		boolean addnew = true;
		BondAction tempba;
		if(addbond)
		{
			for(int i=0; i<ba.size(); i++)
				if(ba.get(i).bondindex == rule)
				{
					addnew = false;
					ba.get(i).action++;
				}
			if(addnew)
			{
				tempba = new BondAction();
				tempba.bondindex = rule;
				tempba.action = 1;
				ba.add(tempba);
			}
		}
		else
		{
			for(int i=0; i<ba.size(); i++)
				if(ba.get(i).bondindex == rule)
				{
					addnew = false;
					ba.get(i).action--;
				}
			if(addnew)
			{
				tempba = new BondAction();
				tempba.bondindex = rule;
				tempba.action = -1;
				ba.add(tempba);
			}
		}
	}
	
	void parsepatterns(ArrayList<RulePattern> patternlist, String patternstr)
	{
		RulePattern temprp = new RulePattern();
		pbonds.clear();
		while(patternstr.indexOf('.')!=-1)
		{
			parsemolepattern(temprp, patternstr.substring(0, patternstr.indexOf('.')).trim());
			patternstr = patternstr.substring(patternstr.indexOf('.')+1, patternstr.length()).trim();
		}
		parsemolepattern(temprp, patternstr);
		
		//determine bonds
		Bond tempbond;
		if(ruleparsevalid)
		{
			for(int i=0; i<pbonds.size(); i++)
				if(pbonds.get(i).sites.size() == 2)
				{
					tempbond = new Bond();
					tempbond.molecule1 = pbonds.get(i).sites.get(0).molecule;
					tempbond.component1 = pbonds.get(i).sites.get(0).component;
					tempbond.state1 = pbonds.get(i).sites.get(0).state;
					tempbond.molecule2 = pbonds.get(i).sites.get(1).molecule;
					tempbond.component2 = pbonds.get(i).sites.get(1).component;
					tempbond.state2 = pbonds.get(i).sites.get(1).state;
					temprp.bonds.add(add2bonds(tempbond));
				}
		}
		patternlist.add(temprp);
	}
	
	int add2bonds(Bond tempbond)
	{
		for(int i=0;i<bonds.size();i++)
			if(equalbond(bonds.get(i), tempbond))
				return i;
		bonds.add(tempbond);
		return bonds.size()-1;
	}
	
	boolean equalbond(Bond in1, Bond in2)
	{
		if(in1.molecule1 == in2.molecule1 && in1.molecule2 == in2.molecule2 && in1.component1 == in2.component1 && in1.component2 == in2.component2 && in1.state1 == in2.state1 && in1.state2 == in2.state2)
			return true;
		if(in1.molecule1 == in2.molecule2 && in1.molecule2 == in2.molecule1 && in1.component1 == in2.component2 && in1.component2 == in2.component1 && in1.state1 == in2.state2 && in1.state2 == in2.state1)
			return true;
		return false;
	}
	
	void parsemolepattern(RulePattern temprp, String molestr)
	{
		boolean leftparenthis = false;
		boolean rightparenthis = false;
		MoleculePattern tempmp = new MoleculePattern();
		String molename = "";
		String component = "";
		if(molestr.trim().length() == 0)
		{
			ruleparsevalid = false;
			return;
		}
		if(molestr.indexOf('(')!=-1)
			leftparenthis = true;
		if(molestr.indexOf(')')!=-1)
			rightparenthis = true;
		if(leftparenthis && rightparenthis)
		{
			molename = molestr.substring(0, molestr.indexOf('(')).trim();
			component = molestr.substring(molestr.indexOf('(')+1, molestr.indexOf(')')).trim();
		}
		if(leftparenthis && !rightparenthis)
		{
			molename = molestr.substring(0, molestr.indexOf('(')).trim();
			component = molestr.substring(molestr.indexOf('(')+1, molestr.length()).trim();
		}
		if(!leftparenthis && !rightparenthis)
		{
			molename = molestr;
		}
		if(!leftparenthis && rightparenthis)
		{
			ruleparsevalid = false;
			return;
		}
		tempmp.moleindex = getmoleindex(molename);
		
		if(tempmp.moleindex == -1)
		{
			BNGEditor.console.append("\nError! Molecule: " + molename + " NOT declared!");
			ruleparsevalid = false;
			return;
		}
		
		while(component.indexOf(',')!=-1)
		{
			parsecomppattern(tempmp, component.substring(0, component.indexOf(',')).trim());
			component = component.substring(component.indexOf(',')+1, component.length()).trim();
		}
		parsecomppattern(tempmp, component);
		temprp.molepatterns.add(tempmp);
	}
	
	void parsecomppattern(MoleculePattern tempmp, String compstr)
	{
		if(compstr.length() == 0)
			return;
		ComponentPattern tempcp = new ComponentPattern();
		String compbond, statebond = "", tempstr;
		String statestr = "";
		if(compstr.indexOf('~')!=-1)
		{
			statestr = compstr.substring(compstr.indexOf('~')+1, compstr.length()).trim();
			compstr = compstr.substring(0, compstr.indexOf('~')).trim();
		}
		if(statestr.indexOf('~')!=-1)
		{
			ruleparsevalid = false;
			return;
		}
		if(compstr.indexOf('!')!=-1)
		{
			compbond = compstr.substring(compstr.indexOf('!')+1, compstr.length()).trim();
			compstr = compstr.substring(0, compstr.indexOf('!')).trim();
			if(compstr.indexOf('%')!=-1)
				compstr = compstr.substring(0,compstr.indexOf('%')).trim();
			tempcp.compindex = getcompindex(tempmp.moleindex, compstr);
		}
		else
		{
			if(compstr.indexOf('%')!=-1)
				compstr = compstr.substring(0,compstr.indexOf('%')).trim();
			tempcp.compindex = getcompindex(tempmp.moleindex, compstr);
			compbond = "";
		}
		if(tempcp.compindex == -1)
		{
			BNGEditor.console.append("\nError! Molecule: " + molecules.get(tempmp.moleindex).name + ", Component: "+ compstr + " NOT declared!");
			ruleparsevalid = false;
			return;
		}
		if(statestr.length() != 0)
		{
			if(statestr.indexOf('!')!=-1)
			{
				statebond = statestr.substring(statestr.indexOf('!')+1, statestr.length()).trim();
				statestr = statestr.substring(0, statestr.indexOf('!')).trim();
				if(statestr.indexOf('%')!=-1)
					statestr = statestr.substring(0, statestr.indexOf('%')).trim();
				tempcp.stateindex = getstateindex(tempmp.moleindex, tempcp.compindex, statestr);
			}
			else
			{
				if(statestr.indexOf('%')!=-1)
					statestr = statestr.substring(0, statestr.indexOf('%')).trim();
				tempcp.stateindex = getstateindex(tempmp.moleindex, tempcp.compindex, statestr);
				statebond = "";
			}
			if(tempcp.stateindex == -1 && !statestr.equals("*"))
			{
				if(!flexiblestate)
				{
					BNGEditor.console.append("\nError! Molecule: " + molecules.get(tempmp.moleindex).name + ", Component: "+ compstr + ", State: " + statestr + " NOT declared!");
					ruleparsevalid = false;
					return;
				}
				else
				{
					tempcp.stateindex = molecules.get(tempmp.moleindex).components.get(tempcp.compindex).states.size(); 
					State tempstate = new State();
					tempstate.name = statestr;
					molecules.get(tempmp.moleindex).components.get(tempcp.compindex).states.add(tempstate); 
				}
			}
		}
		tempmp.comppatterns.add(tempcp);
		if(compbond.length() != 0)
		{
			while(compbond.indexOf('!')!=-1)
			{
				tempstr = compbond.substring(0, compbond.indexOf('!')).trim();
				if(tempstr.indexOf('%')!=-1)
					tempstr = tempstr.substring(0, tempstr.indexOf('%')).trim();
				compbond = compbond.substring(compbond.indexOf('!')+1, compbond.length()).trim();
				if(tempstr.equals("@"))
				{
					if(tempcp.wildcards != 0)
						tempcp.wildcards = 1;
					else
					{
						BNGEditor.console.append("\nError! Molecule: " + molecules.get(tempmp.moleindex).name + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
						ruleparsevalid = false;
						return;
					}
				}
				else if(tempstr.equals("?"))
				{
					if(tempcp.wildcards != 1)
						tempcp.wildcards = 0;
					else
					{
						BNGEditor.console.append("\nError! Molecule: " + molecules.get(tempmp.moleindex).name + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
						ruleparsevalid = false;
						return;
					}
				}
				else
					add2pbonds(tempstr, new Site(tempmp.moleindex, tempcp.compindex, tempcp.stateindex));
			}
			tempstr = compbond;
			if(tempstr.indexOf('%')!=-1)
				tempstr = tempstr.substring(0, tempstr.indexOf('%')).trim();
			if(tempstr.equals("@"))
			{
				if(tempcp.wildcards != 0)
					tempcp.wildcards = 1;
				else
				{
					BNGEditor.console.append("\nError! Molecule: " + molecules.get(tempmp.moleindex).name + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
					ruleparsevalid = false;
					return;
				}
			}
			else if(tempstr.equals("?"))
			{
				if(tempcp.wildcards != 1)
					tempcp.wildcards = 0;
				else
				{
					BNGEditor.console.append("\nError! Molecule: " + molecules.get(tempmp.moleindex).name + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
					ruleparsevalid = false;
					return;
				}
			}
			else
				add2pbonds(tempstr, new Site(tempmp.moleindex, tempcp.compindex, tempcp.stateindex));
		}
		if(statebond.length() != 0)
		{
			while(statebond.indexOf('!')!=-1)
			{
				tempstr = statebond.substring(0, statebond.indexOf('!')).trim();
				if(tempstr.indexOf('%')!=-1)
					tempstr = tempstr.substring(0, tempstr.indexOf('%')).trim();
				statebond = statebond.substring(statebond.indexOf('!')+1, statebond.length()).trim();
				if(tempstr.equals("@"))
				{
					if(tempcp.wildcards != 0)
						tempcp.wildcards = 1;
					else
					{
						BNGEditor.console.append("\nError! Molecule: " + molecules.get(tempmp.moleindex).name + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
						ruleparsevalid = false;
						return;
					}
				}
				else if(tempstr.equals("?"))
				{
					if(tempcp.wildcards != 1)
						tempcp.wildcards = 0;
					else
					{
						BNGEditor.console.append("\nError! Molecule: " + molecules.get(tempmp.moleindex).name + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
						ruleparsevalid = false;
						return;
					}
				}
				else
					add2pbonds(tempstr, new Site(tempmp.moleindex, tempcp.compindex, tempcp.stateindex));
			}
			tempstr = statebond;
			if(tempstr.indexOf('%')!=-1)
				tempstr = tempstr.substring(0, tempstr.indexOf('%')).trim();
			if(tempstr.equals("@"))
			{
				if(tempcp.wildcards != 0)
					tempcp.wildcards = 1;
				else
				{
					BNGEditor.console.append("\nError! Molecule: " + molecules.get(tempmp.moleindex).name + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
					ruleparsevalid = false;
					return;
				}
			}
			else if(tempstr.equals("?"))
			{
				if(tempcp.wildcards != 1)
					tempcp.wildcards = 0;
				else
				{
					BNGEditor.console.append("\nError! Molecule: " + molecules.get(tempmp.moleindex).name + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
					ruleparsevalid = false;
					return;
				}
			}
			else
				add2pbonds(tempstr, new Site(tempmp.moleindex, tempcp.compindex, tempcp.stateindex));
		}
	}
	
	void add2pbonds(String bondname, Site site)
	{
		for(int i=0;i<pbonds.size();i++)
			if(pbonds.get(i).name.equals(bondname))
			{
				pbonds.get(i).sites.add(site);
				return;
			}
		pbonds.add(new PotentialBond(bondname, site));		
	}
	
	int getmoleindex(String mole)
	{
		for(int i=0; i<molecules.size(); i++)
			if(molecules.get(i).name.equals(mole))
				return i;
		return -1;
	}
	
	int getcompindex(int mole, String comp)
	{
		Molecule tempmole;
		tempmole = molecules.get(mole);
		for(int i=0; i<tempmole.components.size(); i++)
			if(tempmole.components.get(i).name.equals(comp))
				return i;
		return -1;
	}
	
	int getstateindex(int mole, int comp, String state)
	{
		Component tempcomp = molecules.get(mole).components.get(comp);
		for(int i=0; i<tempcomp.states.size(); i++)
			if(tempcomp.states.get(i).name.equals(state))
				return i;
		return -1;
	}
	
	
	/**
	 * This class creates the data structure required for prefuse and 
	 * interacts with the NetworkView class in order to show 
	 * a visualization of the contact map data structure constructed 
	 * by the parser. 
	 * 
	 * @author ams
	 *
	 */
	class LoadCMap
	{
		
		// To use Prefuse, you first have to construct the data structures that it
		// uses for visualization e.g. the Graph object. This Graph object will 
		// hold the nodes and edges for our graph visualization.
		//
		Graph comp_graph;
	
		// Prefuse uses string identifiers for data structures and 
		// their data fields.  These Strings variables are all used 
		// as identifiers.
		
		// Each node stores a string label for the molecule it belongs to.
		String COMP_PARENT_LABEL = "molecule";
		// This is the identifier for our primary Graph object
		String COMPONENT_GRAPH = "component_graph";
		// This is a label used with Aggregate objects.  They are used for visually grouping nodes.
		// In our case, they make the molecules
		String AGG_CAT_LABEL = "molecule";
		// This is an identifier for the aggregate labels
		String AGG_DEC = "aggregate_decorators";
		// This is a label for the aggregates themselves.
		String AGG = "aggregates";
		
		// The visualization object is a primary data structure for prefuse.
		// We will give our Graph object to it, and also use it to access
		// and change properties of the visualization.
		Visualization vis;
		
		// The NetworkViewer is an object I created to encapsulate most of 
		// prefuse nastiness.  You will interact with this mostly.  I plan
		// on making it much easier to use once I get back.
		// NetworkViewer is a subclass of prefuse.Display which is a 
		// Component and can be added to a JPanel.
		NetworkViewer nv;
		
		
		/**
		 * This constructor builds all of the data and sets up the visualization.
		 */
		LoadCMap()
		{
			// Instantiate the NetworkViewer object.
			nv = new NetworkViewer(); 
			
			// Instantiate the Graph
			/*
			 *  VisualItem.LABEL   [Integer]   |   COMP_PARENT_LABEL ('molecule') [String]   |   'rules'   [ArrayList<Rule>]  |
			 *  |   'states'     |
			 *
			 * 
			 */
			comp_graph = new Graph();
			
			// Ideally I wanted to remove all interaction with the Visualization object,
			// but I didn't quite finish it yet.  
			vis = nv.getVisualization();
			
			// Graphs (and all other data structures in prefuse) are table-based data structures.  Each node is a row in the
			// table and the columns hold data about the node.  Here we add a 
			// column for the label of the node, and then a column for the
			// molecule of which the node is a member.
	        
			// The Label for a node.  Constant value int from VisualItem
			comp_graph.addColumn(VisualItem.LABEL, String.class);
			
			// The parent (molecule) for a node.  String.
	        comp_graph.addColumn(COMP_PARENT_LABEL, String.class);
	        
	        // The Rule object that can create an edge.  Arraylist of Rule objects.
	        comp_graph.addColumn("rules", ArrayList.class);
	        
	        // The states that the component can be in.  ArrayList of Strings.
	        comp_graph.addColumn("states", ArrayList.class);
	        
	        
			// Aggregate tables are created by adding an aggregator group to the 
	        // NetworkViewer.  This is also a table data structure and will be 
	        // used to keep track of the shape of the molecules.   
			AggregateTable at = nv.addAggregateTable("aggregates");
			
			// Add the graph to the visualization
			// Pass the Graph object, and the string label for it.
	        nv.addGraph(comp_graph, COMPONENT_GRAPH);
	    	
	        // Add the decorators to the visualization
	        // Decorators are a way to add labels to objects.
	        // Pass the string label for the decorators, and
	        // the string value for the objects that they decorate.
			nv.addDecorators(AGG_DEC, AGG);
			
			// This sets the decorator objects as not interactive.
			vis.setInteractive(AGG_DEC, null, false);
			vis.setInteractive(COMPONENT_GRAPH+".edges", null, false);
			
	        // Create the aggregate table and add it to the visualization
	        //at = vis.addAggregates(AGG);
			
	        // Add a column to the table to hold the polygon information
	        // that will be used to render the aggregates.  This float array
			// will hold the points that create the molecule boundary.
	        at.addColumn(VisualItem.POLYGON, float[].class);

	        // Add a column that will keep track of the types of aggregates.
	        at.addColumn(AGG_CAT_LABEL, String.class);
	        
	        // Set how the clicks will be handled.
	        // Here you can create your own interactions for the Graph.
	        // If you do not want interactions, simply do not add a
	        // clickcontroldelegate.  See the editor.contactmap.CMapClickControlDelegate
	        // for how to implement it.
	        nv.setClickControl(new CMapClickControlDelegate(nv.getVisualization()));

	        // This is an index to the Node objects so that I can retrieve them 
	        // based on the string value "<parent molecule index>.<component index>".
	        Hashtable<String, Node> nodes = new Hashtable<String, Node>();
	        
	        // This is so I can get an edge, based on an integer value.
	        Hashtable<Integer, Edge> edges = new Hashtable<Integer, Edge>();
	        
	        // Now begins the construction of the data structure.  I am not sure
	        // if you will be able to use Yao's parser and get the same structure,
	        // but if you can, then you will be able to use this code below to create 
	        // the visualizaiton.
			Molecule tmole;
			Component tcomp;
			State tstate;
			Bond tbond;
			
			// This is so I can add invisible edges between all of the components in the 
			// molecule.  This is for the force directed layout.
			ArrayList<Node> otherCompsInMol;
			
			// In general, we construct the graph by making a node for each component
			// and then group the components in the same molecule into an aggregator. 
			// There are visible edges representing bonds, and then invisible edges
			// between components in the same molecule. 
			
			// For each molecule.
			for(int i=0; i<molecules.size(); i++)
			{
				// Get the molecule
				tmole = molecules.get(i);
				
				// If there are no components in the molecule, then the molecule
				// is rendered as a component.
				if(tmole.components.size()==0)
				{
					Node n = comp_graph.addNode();
					n.setString(VisualItem.LABEL, "       ");
					AggregateItem agg = (AggregateItem) at.addItem(); 
					agg.setString(AGG_CAT_LABEL, tmole.name);
					agg.addItem(vis.getVisualItem(COMPONENT_GRAPH, n));
				}
				
				// It has components
				else
				{	
					// for making invisible edges between components in the same molecule.
					otherCompsInMol = new ArrayList<Node>();
					
					// Get the aggregate so you can add nodes to it.
					AggregateItem aggregateForMolecule = (AggregateItem) at.addItem();
					aggregateForMolecule.setString(AGG_CAT_LABEL, tmole.name);
					
					// For each component
					for(int j=0;j<tmole.components.size();j++)
					{	
						// Get the component
						tcomp = tmole.components.get(j);
						
					
						// Make a new node for it
						Node n = comp_graph.addNode();
						// Set its name
					    n.setString(VisualItem.LABEL, tcomp.name);
					    // Set its parent
					    n.setString(COMP_PARENT_LABEL,tmole.name); 
					    // Add it to the hashtable
					    nodes.put(i+"."+j , n);
					    // Add it to the aggregate
					    aggregateForMolecule.addItem(vis.getVisualItem(COMPONENT_GRAPH,n));
					
						     
						// If it has states
						if(tcomp.states.size() != 0)	
						{
							n.set("states", new ArrayList<String>());
							
							for(int k = 0; k < tcomp.states.size(); k++)
							{
								tstate = tcomp.states.get(k);
								
								((ArrayList<String>) n.get("states")).add(tstate.name);
							}  
						}  

					    // Add invisible edges for the force directed layout.
					    for(Node on : otherCompsInMol)
					    {
					    	Edge te = comp_graph.addEdge(n, on);
					    	EdgeItem ei = (EdgeItem) vis.getVisualItem(COMPONENT_GRAPH+".edges", te);
								
					    	ei.setVisible(false);
					    }
					     
					    otherCompsInMol.add(n);
					}
				}
			} // Close for each molecule
			
			
			// Create an edge for each bond.
			for(int b = 0; b < bonds.size(); b++)
			{
				// Declare an Edge.
				Edge e;
				
				// Get the bond
				tbond = bonds.get(b);
				
				// Create the edge
				e = comp_graph.addEdge(nodes.get(tbond.molecule1+"."+tbond.component1), nodes.get(tbond.molecule2+"."+tbond.component2));
				
				// Put the edge into the hashtable.
				edges.put(b, e);
				
				// instantiate the ArrayList of Rule objects that will be stored with the edge.
				e.set("rules", new ArrayList<Rule>());
			}
			
			
			// For all of the rules
			for(int l = 0; l<rules.size();l++)
			{	
				// Get a reference to the rule
				Rule thisRule = rules.get(l);
				
				// DEBUG
				System.out.println("Rule: "+ thisRule.name);
				
				// Create a visual rule.  Used for interaction.  (See editor.contactmap.CMapClickControlDelegate.java)
				VisualRule r = new VisualRule(rules.get(l).name);
				
				// For the bonds created or destroyed by the rule
				for(BondAction ba : thisRule.bondactions)
				{
					//DEBUG
					System.out.println("\t" + ba.bondindex + " " + ba.action);
					
					// Get the edge for this bond.
					Edge e = edges.get(ba.bondindex);
				
					// Add the bond to the rule as either created or 
					// destroyed by the rule.
					if(ba.action > 0)
						r.addAddBond(e);
					else
						r.addRemoveBond(e);

					// Add the Visual rule to the Edge.
					((ArrayList<VisualRule>) e.get("rules")).add(r);					
				}
				
				// For all of the reactant patterns
				// TODO add states
				for(RulePattern rp : thisRule.reactantpatterns)
				{
					for(Integer i : rp.bonds)
					{
						System.out.println("\t"+i);
						r.addReactantBond(edges.get(i));
					}
					
					for(MoleculePattern mp : rp.molepatterns)
						for(ComponentPattern cp : mp.comppatterns)
							r.addReactantNode(nodes.get(mp.moleindex+"."+cp.compindex));
				}
				
				// For all of the product patterns.
				for(RulePattern pp : thisRule.productpatterns)
				{
					for(Integer i : pp.bonds)
					{
						System.out.println("\t"+i);
						r.addProductBond(edges.get(i));
					}
					
					for(MoleculePattern mp : pp.molepatterns)
						for(ComponentPattern cp : mp.comppatterns)
							r.addProductNode(nodes.get(mp.moleindex+"."+cp.compindex));
				}
				
				
				
				//TODO  I'm not sure what this is for, but I'm going to ignore it for now.
				//if(tbond.CanGenerate == true)
				//{
					/*if(tbond.state1 != -1)
					{
						outfile.print("struct"+molecules.get(tbond.molecule1).name+"_"+molecules.get(tbond.molecule1).components.get(tbond.component1).name+tbond.component1+":"+molecules.get(tbond.molecule1).components.get(tbond.component1).states.get(tbond.state1).name+"->");
					}
					else
					{
						outfile.print("struct"+molecules.get(tbond.molecule1).name+"_"+molecules.get(tbond.molecule1).components.get(tbond.component1).name+tbond.component1+"->");
					}
					if(tbond.state2 != -1)
					{
						outfile.println("struct"+molecules.get(tbond.molecule2).name+"_"+molecules.get(tbond.molecule2).components.get(tbond.component2).name+tbond.component2+":"+molecules.get(tbond.molecule2).components.get(tbond.component2).states.get(tbond.state2).name+"[arrowhead=none];");
					}
					else
					{
						outfile.println("struct"+molecules.get(tbond.molecule2).name+"_"+molecules.get(tbond.molecule2).components.get(tbond.component2).name+tbond.component2+"[arrowhead=none];");
					}*/
				
				// Add an edge between the nodes in the bond.
				
			//	comp_graph.addEdge(arg0, arg1)
				
			/*	}
				else
				{
					if(tbond.state1 != -1)
					{
						outfile.print("struct"+molecules.get(tbond.molecule1).name+"_"+molecules.get(tbond.molecule1).components.get(tbond.component1).name+tbond.component1+":"+molecules.get(tbond.molecule1).components.get(tbond.component1).states.get(tbond.state1).name+"->");
					}
					else
					{
						outfile.print("struct"+molecules.get(tbond.molecule1).name+"_"+molecules.get(tbond.molecule1).components.get(tbond.component1).name+tbond.component1+"->");
					}
					if(tbond.state2 != -1)
					{
						outfile.println("struct"+molecules.get(tbond.molecule2).name+"_"+molecules.get(tbond.molecule2).components.get(tbond.component2).name+tbond.component2+":"+molecules.get(tbond.molecule2).components.get(tbond.component2).states.get(tbond.state2).name+"[arrowhead=none,color=grey];");
					}
					else
					{
						outfile.println("struct"+molecules.get(tbond.molecule2).name+"_"+molecules.get(tbond.molecule2).components.get(tbond.component2).name+tbond.component2+"[arrowhead=none,color=grey];");
					}
				}*/
			}  // Close for rules.
		}	
	
		public Display getDisplay()
		{
			return nv.getDisplay();
		}
	}
	
	void drawcmap()
	{
		// TODO
		// There is a small horizontal band just below the menu bar.
		// I suspect this has something to do with osx vs win.
		// I'll just change the background color to hide it for now.
		this.setBackground(Color.white);
		this.setTitle("Contact Map");
		this.setPreferredSize(new Dimension(500,600));
		this.getContentPane().add(new LoadCMap().getDisplay());
		this.pack();
		this.setVisible(true);
	}
}