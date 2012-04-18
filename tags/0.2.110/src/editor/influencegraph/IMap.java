package editor.influencegraph;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ToolTipManager;

import editor.BNGEditor;

/******************************************
 * 
 * 1. This version works only with models without duplicate components
 *
 ******************************************/

public class IMap extends JFrame
{
	ArrayList<IMolecule> molecules = new ArrayList<IMolecule>();
	ArrayList<IBond> bonds = new ArrayList<IBond>();
	ArrayList<IRule> rules = new ArrayList<IRule>();
	ArrayList<IRuleNode> rulenodes = new ArrayList<IRuleNode>();
	ArrayList<Influence> influences = new ArrayList<Influence>();
	
	private boolean ruleparsevalid;
	private ArrayList<IPotentialBond> pbonds = new ArrayList<IPotentialBond>();
	private boolean flexiblestate = false;
	
	public IMap(String molestr, String rulestr, boolean moleculetype)
	{
		if(moleculetype)
			parsemoleculetypes(molestr);
		else
		{
			flexiblestate = true;
			parsespecies(molestr);
		}
		parserules(rulestr);
		for(int i=0; i<rules.size(); i++)
			getreactioncenter(rules.get(i));
		generateImap();
		drawImap();
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
	
	void parsemolecule(String molecule)
	{
		boolean leftparenthis = false;
		boolean rightparenthis = false;
		IMolecule tempmole = new IMolecule();
		String component = "";
		if(molecule.trim().length() == 0)
			return;
		if(molecule.indexOf('(')!=-1)
			leftparenthis = true;
		if(molecule.indexOf(')')!=-1)
			rightparenthis = true;
		if(leftparenthis && rightparenthis)
		{
			tempmole.name = molecule.substring(0, molecule.indexOf('(')).trim();
			component = molecule.substring(molecule.indexOf('(')+1, molecule.indexOf(')')).trim();
		}
		if(leftparenthis && !rightparenthis)
		{
			tempmole.name = molecule.substring(0, molecule.indexOf('(')).trim();
			component = molecule.substring(molecule.indexOf('(')+1, molecule.length()).trim();
		}
		if(!leftparenthis && !rightparenthis)
		{
			tempmole.name = molecule;
		}
		if(!leftparenthis && rightparenthis)
		{
			BNGEditor.console.append("\nError parsing molecule : "+molecule+" , molecule not added !");
			return;
		}
		while(component.indexOf(',')!=-1)
		{
			parsecomponent(tempmole,component.substring(0,component.indexOf(',')).trim());
			component = component.substring(component.indexOf(',')+1, component.length()).trim();
		}
		parsecomponent(tempmole,component);
		if(validatemole(tempmole))
			molecules.add(tempmole);
		else
			BNGEditor.console.append("\nError parsing molecule : "+molecule+" , molecule not added !");
	}
	
	void parsecomponent(IMolecule tempmole, String component)
	{
		if(component.trim().length() == 0)
			return;
		IComponent tempcomp = new IComponent();
		IState tempstate;
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
				tempstate = new IState();
				tempstate.name = tempstr1;
				tempcomp.states.add(tempstate);
				state = state.substring(state.indexOf('~')+1, state.length()).trim();
			}
			tempstr1 = state;
			if(tempstr1.indexOf('!')!=-1)
				tempstr1 = tempstr1.substring(0,tempstr1.indexOf('!')).trim();
			tempstate = new IState();
			tempstate.name = tempstr1;
			tempcomp.states.add(tempstate);
			tempmole.components.add(tempcomp);
		}
	}
	
	boolean validatemole(IMolecule tempmole)
	{
		for(int i = 0; i < tempmole.components.size(); i++)//delete the entries with empty string names
		{
			if(tempmole.components.get(i).name.trim().length()==0)
			{
				tempmole.components.remove(i);
				i--;
			}
			else
			{
				for(int j = 0; j < tempmole.components.get(i).states.size(); j++)
					if(tempmole.components.get(i).states.get(j).name.trim().length() == 0)
					{
						tempmole.components.get(i).states.remove(j);
						j--;
					}
			}
		}
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
	
	boolean validatename(String name)
	{
		name = name.trim();
		if(name.indexOf('+')!=-1 || name.indexOf('(')!=-1 || name.indexOf(')')!=-1 || name.indexOf('.')!=-1 || name.indexOf(',')!=-1 || name.indexOf('!')!=-1  || name.indexOf('?')!=-1 || name.indexOf('~')!=-1 || name.indexOf(' ')!=-1 || name.indexOf('\t')!=-1 || name.indexOf('{')!=-1 || name.indexOf('}')!=-1 || name.indexOf('<')!=-1 || name.indexOf('>')!=-1 || name.indexOf('-')!=-1)
			return false;
		return true;
	}
	
	void parserules(String rulestr)
	{
		Scanner scan = new Scanner(rulestr);
		String tempstr1;
		Pattern pattern1 = Pattern.compile("\\{.*\\}");
		Pattern pattern2 = Pattern.compile("\\s+DeleteMolecules\\s+");
		Matcher m;
		IRule temprule;
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
			temprule = new IRule();
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
	
	void parserule(IRule temprule,String rulestr)
	{
		Pattern pattern1 = Pattern.compile("\\-\\s*>");
		Matcher m = pattern1.matcher(rulestr);
		String tempstr1,tempstr2;
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
	
	void parserule2pattern(IRule temprule, String rulestr)
	{
		String reactants,products;
		Pattern pattern1 = Pattern.compile("!\\s*\\+");
		Matcher m = pattern1.matcher(rulestr);
		while(m.find())
		{
			rulestr = rulestr.substring(0, m.start()).trim()+"!@"+rulestr.substring(m.end(), rulestr.length()).trim();
			m = pattern1.matcher(rulestr);
		}
		//System.out.print(rulestr);
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
	
	void add2bondactions(ArrayList<IBondAction> ba, int rule, boolean addbond)
	{
		boolean addnew = true;
		IBondAction tempba;
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
				tempba = new IBondAction();
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
				tempba = new IBondAction();
				tempba.bondindex = rule;
				tempba.action = -1;
				ba.add(tempba);
			}
		}
	}
	
	void parsepatterns(ArrayList<IRulePattern> patternlist, String patternstr)
	{
		IRulePattern temprp = new IRulePattern();
		pbonds.clear();
		while(patternstr.indexOf('.')!=-1)
		{
			parsemolepattern(temprp, patternstr.substring(0, patternstr.indexOf('.')).trim());
			patternstr = patternstr.substring(patternstr.indexOf('.')+1, patternstr.length()).trim();
		}
		parsemolepattern(temprp, patternstr);
		
		//determine bonds
		IBond tempbond;
		int bondindex;
		if(ruleparsevalid)
		{
			for(int i=0; i<pbonds.size(); i++)
				if(pbonds.get(i).sites.size() == 2)
				{
					tempbond = new IBond();
					tempbond.molecule1 = pbonds.get(i).sites.get(0).molecule;
					tempbond.component1 = pbonds.get(i).sites.get(0).component;
					tempbond.state1 = pbonds.get(i).sites.get(0).state;
					tempbond.molecule2 = pbonds.get(i).sites.get(1).molecule;
					tempbond.component2 = pbonds.get(i).sites.get(1).component;
					tempbond.state2 = pbonds.get(i).sites.get(1).state;
					bondindex = add2bonds(tempbond);
					temprp.bonds.add(bondindex);
					for(int j = 0; j<temprp.molepatterns.size(); j++)
						for(int k=0; k<temprp.molepatterns.get(j).comppatterns.size(); k++)
							if(temprp.molepatterns.get(j).comppatterns.get(k).pbondlist.contains(pbonds.get(i).name))
								temprp.molepatterns.get(j).comppatterns.get(k).bondlist.add(bondindex);
				}
		}
		for(int j = 0; j<temprp.molepatterns.size(); j++)
			for(int k=0; k<temprp.molepatterns.get(j).comppatterns.size(); k++)
				temprp.molepatterns.get(j).comppatterns.get(k).pbondlist.clear();
		patternlist.add(temprp);
	}
	
	int add2bonds(IBond tempbond)
	{
		for(int i=0;i<bonds.size();i++)
			if(equalbond(bonds.get(i), tempbond))
				return i;
		bonds.add(tempbond);
		return bonds.size()-1;
	}
	
	boolean equalbond(IBond in1, IBond in2)
	{
		if(in1.molecule1 == in2.molecule1 && in1.molecule2 == in2.molecule2 && in1.component1 == in2.component1 && in1.component2 == in2.component2 && in1.state1 == in2.state1 && in1.state2 == in2.state2)
			return true;
		if(in1.molecule1 == in2.molecule2 && in1.molecule2 == in2.molecule1 && in1.component1 == in2.component2 && in1.component2 == in2.component1 && in1.state1 == in2.state2 && in1.state2 == in2.state1)
			return true;
		return false;
	}
	
	void parsemolepattern(IRulePattern temprp, String molestr)
	{
		boolean leftparenthis = false;
		boolean rightparenthis = false;
		IMoleculePattern tempmp = new IMoleculePattern();
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
	
	void parsecomppattern(IMoleculePattern tempmp, String compstr)
	{
		if(compstr.length() == 0)
			return;
		IComponentPattern tempcp = new IComponentPattern();
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
					IState tempstate = new IState();
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
				{
					add2pbonds(tempstr, new ISite(tempmp.moleindex, tempcp.compindex, tempcp.stateindex));
					tempcp.pbondlist.add(tempstr);
				}
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
			{
				add2pbonds(tempstr, new ISite(tempmp.moleindex, tempcp.compindex, tempcp.stateindex));
				tempcp.pbondlist.add(tempstr);
			}
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
				{
					add2pbonds(tempstr, new ISite(tempmp.moleindex, tempcp.compindex, tempcp.stateindex));
					tempcp.pbondlist.add(tempstr);
				}
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
			{
				add2pbonds(tempstr, new ISite(tempmp.moleindex, tempcp.compindex, tempcp.stateindex));
				tempcp.pbondlist.add(tempstr);
			}
		}
	}
	
	void add2pbonds(String bondname, ISite site)
	{
		for(int i=0;i<pbonds.size();i++)
			if(pbonds.get(i).name.equals(bondname))
			{
				pbonds.get(i).sites.add(site);
				return;
			}
		pbonds.add(new IPotentialBond(bondname, site));		
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
		IMolecule tempmole;
		tempmole = molecules.get(mole);
		for(int i=0; i<tempmole.components.size(); i++)
			if(tempmole.components.get(i).name.equals(comp))
				return i;
		return -1;
	}
	
	int getstateindex(int mole, int comp, String state)
	{
		IComponent tempcomp = molecules.get(mole).components.get(comp);
		for(int i=0; i<tempcomp.states.size(); i++)
			if(tempcomp.states.get(i).name.equals(state))
				return i;
		return -1;
	}
	
	void getreactioncenter(IRule temprule)
	{
		ArrayList<IMoleculePattern> reactantmoles = new ArrayList<IMoleculePattern>();
		ArrayList<IMoleculePattern> productmoles = new ArrayList<IMoleculePattern>();
		for(int i=0; i<temprule.reactantpatterns.size(); i++)
			for(int j=0; j<temprule.reactantpatterns.get(i).molepatterns.size(); j++)
			{
				reactantmoles.add(temprule.reactantpatterns.get(i).molepatterns.get(j));
				for(int k=0; k<temprule.reactantpatterns.get(i).molepatterns.get(j).comppatterns.size(); k++)
					temprule.reactantpatterns.get(i).molepatterns.get(j).comppatterns.get(k).matched = false;
			}
		for(int i=0; i<temprule.productpatterns.size(); i++)
			for(int j=0; j<temprule.productpatterns.get(i).molepatterns.size(); j++)
			{
				productmoles.add(temprule.productpatterns.get(i).molepatterns.get(j));
				for(int k=0; k<temprule.productpatterns.get(i).molepatterns.get(j).comppatterns.size(); k++)
					temprule.productpatterns.get(i).molepatterns.get(j).comppatterns.get(k).matched = false;
			}
		
		for(int i=0; i< reactantmoles.size(); i++)
			reactantmoles.get(i).matched = false;
		for(int i=0; i< productmoles.size(); i++)
			productmoles.get(i).matched = false;
		
		for(int i=0; i< reactantmoles.size(); i++)
			for(int j=0; j<productmoles.size(); j++)
				trymatchmoles(reactantmoles.get(i), productmoles.get(j));
		
		for(int i=0; i< reactantmoles.size(); i++)
			if(!reactantmoles.get(i).matched)
				for(int j=0; j<reactantmoles.get(i).comppatterns.size(); j++)
					reactantmoles.get(i).comppatterns.get(j).reactioncenter = true;
		for(int i=0; i< productmoles.size(); i++)
			if(!productmoles.get(i).matched)
				for(int j=0; j<productmoles.get(i).comppatterns.size(); j++)
					productmoles.get(i).comppatterns.get(j).reactioncenter = true;
	}
	
	void trymatchmoles(IMoleculePattern in1, IMoleculePattern in2)
	{
		boolean result = false;
		if(in1.matched || in2.matched)
			return;
		if(in1.moleindex == in2.moleindex)
		{
			result = true;
			for(int i=0; i<in1.comppatterns.size(); i++)
				for(int j=0; j<in2.comppatterns.size(); j++)
					if(in1.comppatterns.get(i).compindex == in2.comppatterns.get(j).compindex && !in1.comppatterns.get(i).matched && !in2.comppatterns.get(j).matched)
					{
						in1.comppatterns.get(i).matched = true;
						in2.comppatterns.get(j).matched = true;
						if(!comparecomppattern(in1.comppatterns.get(i), in2.comppatterns.get(j)))
						{
							in1.comppatterns.get(i).reactioncenter = true; 
							in2.comppatterns.get(j).reactioncenter = true;
						}
					}
			for(int i=0; i<in1.comppatterns.size(); i++)
				if(!in1.comppatterns.get(i).matched)
				{
					result = false;
					break;
				}
			for(int i=0; i<in2.comppatterns.size(); i++)
				if(!in2.comppatterns.get(i).matched)
				{
					result = false;
					break;
				}
		}
		if(!result)
		{
			for(int i=0; i<in1.comppatterns.size(); i++)
			{
				in1.comppatterns.get(i).matched = false;
				in1.comppatterns.get(i).reactioncenter = false;
			}
			for(int i=0; i<in2.comppatterns.size(); i++)
			{
				in2.comppatterns.get(i).matched = false;
				in2.comppatterns.get(i).reactioncenter = false;
			}
		}
		else
		{
			in1.matched = true;
			in2.matched = true;
		}
	}
	
	boolean comparecomppattern(IComponentPattern in1, IComponentPattern in2)
	{
		Collections.sort(in1.bondlist);
		Collections.sort(in2.bondlist);
		if(in1.stateindex == in2.stateindex && in1.wildcards == in2.wildcards && in1.bondlist.equals(in2.bondlist))
			return true;
		return false;
	}
	
	void generateImap()
	{
		int rulenodeindex = 0;
		for(int i=0; i<rules.size(); i++)
			if(rules.get(i).bidirection)
			{
				rulenodes.add(new IRuleNode(rulenodeindex, i, true));
				rulenodeindex++;
				rulenodes.add(new IRuleNode(rulenodeindex, i, false));
				rulenodeindex++;
			}
			else
			{
				rulenodes.add(new IRuleNode(rulenodeindex, i, true));
				rulenodeindex++;
			}
		
		Influence tempinfluence;
		for(int i=0; i<rulenodes.size(); i++)
			for(int j=0; j<rulenodes.size(); j++)
				if(i != j)
				{
					tempinfluence = new Influence(i, j);
					determineinfluence(tempinfluence, rulenodes.get(i), rulenodes.get(j));
					if(tempinfluence.activation !=-1 || tempinfluence.inhibition != -1)
						influences.add(tempinfluence);
				}
				
		File dotfile = new File("imap.dot");
		File pngfile = new File("imap.png");
		File txtfile = new File("imap.txt");
		if(dotfile.exists())
			dotfile.delete();
		if(pngfile.exists())
			pngfile.delete();
		if(txtfile.exists())
			txtfile.delete();
		PrintWriter outfile;
		try {
			outfile = new PrintWriter(dotfile);
			outfile.println("digraph structs {");
			outfile.println("node [shape=box];");
			for(int i=0; i<rulenodes.size(); i++)
				if(rulenodes.get(i).forward)
					outfile.println("r"+String.valueOf(rulenodes.get(i).iruleindex)+" [label=\"R"+String.valueOf(rulenodes.get(i).iruleindex+1)+"\"];");
				else
					outfile.println("r"+String.valueOf(rulenodes.get(i).iruleindex)+"r [label=\"R"+String.valueOf(rulenodes.get(i).iruleindex+1)+"\'\"];");
			for(int i=0; i<influences.size(); i++)
			{
				if(influences.get(i).activation==0)
				{
					if(rulenodes.get(influences.get(i).startrulenodeindex).forward)
						outfile.print("r"+String.valueOf(rulenodes.get(influences.get(i).startrulenodeindex).iruleindex)+"->");
					else
						outfile.print("r"+String.valueOf(rulenodes.get(influences.get(i).startrulenodeindex).iruleindex)+"r->");
					if(rulenodes.get(influences.get(i).endrulenodeindex).forward)
						outfile.println("r"+String.valueOf(rulenodes.get(influences.get(i).endrulenodeindex).iruleindex)+"[arrowhead=onormal,color=green]");
					else
						outfile.println("r"+String.valueOf(rulenodes.get(influences.get(i).endrulenodeindex).iruleindex)+"r[arrowhead=onormal,color=green]");
				}
				if(influences.get(i).activation==1)
				{
					if(rulenodes.get(influences.get(i).startrulenodeindex).forward)
						outfile.print("r"+String.valueOf(rulenodes.get(influences.get(i).startrulenodeindex).iruleindex)+"->");
					else
						outfile.print("r"+String.valueOf(rulenodes.get(influences.get(i).startrulenodeindex).iruleindex)+"r->");
					if(rulenodes.get(influences.get(i).endrulenodeindex).forward)
						outfile.println("r"+String.valueOf(rulenodes.get(influences.get(i).endrulenodeindex).iruleindex)+"[arrowhead=normal,color=green]");
					else
						outfile.println("r"+String.valueOf(rulenodes.get(influences.get(i).endrulenodeindex).iruleindex)+"r[arrowhead=normal,color=green]");
				}
				if(influences.get(i).inhibition==0)
				{
					if(rulenodes.get(influences.get(i).startrulenodeindex).forward)
						outfile.print("r"+String.valueOf(rulenodes.get(influences.get(i).startrulenodeindex).iruleindex)+"->");
					else
						outfile.print("r"+String.valueOf(rulenodes.get(influences.get(i).startrulenodeindex).iruleindex)+"r->");
					if(rulenodes.get(influences.get(i).endrulenodeindex).forward)
						outfile.println("r"+String.valueOf(rulenodes.get(influences.get(i).endrulenodeindex).iruleindex)+"[arrowhead=onormal,color=red]");
					else
						outfile.println("r"+String.valueOf(rulenodes.get(influences.get(i).endrulenodeindex).iruleindex)+"r[arrowhead=onormal,color=red]");
				}
				if(influences.get(i).inhibition==1)
				{
					if(rulenodes.get(influences.get(i).startrulenodeindex).forward)
						outfile.print("r"+String.valueOf(rulenodes.get(influences.get(i).startrulenodeindex).iruleindex)+"->");
					else
						outfile.print("r"+String.valueOf(rulenodes.get(influences.get(i).startrulenodeindex).iruleindex)+"r->");
					if(rulenodes.get(influences.get(i).endrulenodeindex).forward)
						outfile.println("r"+String.valueOf(rulenodes.get(influences.get(i).endrulenodeindex).iruleindex)+"[arrowhead=normal,color=red]");
					else
						outfile.println("r"+String.valueOf(rulenodes.get(influences.get(i).endrulenodeindex).iruleindex)+"r[arrowhead=normal,color=red]");
				}
			}
			outfile.println("}");
			outfile.close();
		} catch (FileNotFoundException e) {}
		try {//TODO : dot call problem, absolute address GUI
			if(BNGEditor.getOstype()!=1)
			{
				Process p = Runtime.getRuntime().exec(BNGEditor.getDOTfpath()+"/"+BNGEditor.getDOTfname()+" -o imap.txt imap.dot");
				p.waitFor();
				Process p2 = Runtime.getRuntime().exec(BNGEditor.getDOTfpath()+"/"+BNGEditor.getDOTfname()+" -Tpng -o imap.png imap.dot");
				p2.waitFor();
			}
			else
			{
				if(BNGEditor.getDOTfpath() == null || BNGEditor.getDOTfname() == null)
				{
					Process p = Runtime.getRuntime().exec("cmd.exe /c dot -o imap.txt imap.dot");
					p.waitFor();
					Process p2 = Runtime.getRuntime().exec("cmd.exe /c dot -Tpng -o imap.png imap.dot");
					p2.waitFor();
				}
				else
				{
					Process p = Runtime.getRuntime().exec(BNGEditor.getDOTfpath()+"\\"+BNGEditor.getDOTfname()+" -o imap.txt imap.dot");
					p.waitFor();
					Process p2 = Runtime.getRuntime().exec(BNGEditor.getDOTfpath()+"\\"+BNGEditor.getDOTfname()+" -Tpng -o imap.png imap.dot");
					p2.waitFor();
				}
			}
		} catch (IOException e) {} catch (InterruptedException e) {}
	}
	
	void determineinfluence(Influence tempinfluence, IRuleNode rn1, IRuleNode rn2)
	{
		boolean canbreak;
		if(rn2.forward)
		{
			canbreak = false;
			for(int i=0; i<rules.get(rn2.iruleindex).reactantpatterns.size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.iruleindex).reactantpatterns.size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.iruleindex).reactantpatterns.get(i), rules.get(rn1.iruleindex).reactantpatterns.get(j)))
					{
					case 0:
						if(rn1.forward)
							tempinfluence.setInhibition(false);
						else
							tempinfluence.setActivation(false);
						break;
					case 1:
						if(rn1.forward)
							tempinfluence.setInhibition(true);
						else
							tempinfluence.setActivation(true);
						canbreak = true;
						break;
					case -1:
						break;
					}
			canbreak = false;
			for(int i=0; i<rules.get(rn2.iruleindex).reactantpatterns.size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.iruleindex).productpatterns.size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.iruleindex).reactantpatterns.get(i), rules.get(rn1.iruleindex).productpatterns.get(j)))
					{
					case 0:
						if(!rn1.forward)
							tempinfluence.setInhibition(false);
						else
							tempinfluence.setActivation(false);
						break;
					case 1:
						if(!rn1.forward)
							tempinfluence.setInhibition(true);
						else
							tempinfluence.setActivation(true);
						canbreak = true;
						break;
					case -1:
						break;
					}
		}
		else
		{
			canbreak = false;
			for(int i=0; i<rules.get(rn2.iruleindex).productpatterns.size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.iruleindex).reactantpatterns.size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.iruleindex).productpatterns.get(i), rules.get(rn1.iruleindex).reactantpatterns.get(j)))
					{
					case 0:
						if(rn1.forward)
							tempinfluence.setInhibition(false);
						else
							tempinfluence.setActivation(false);
						break;
					case 1:
						if(rn1.forward)
							tempinfluence.setInhibition(true);
						else
							tempinfluence.setActivation(true);
						canbreak = true;
						break;
					case -1:
						break;
					}
			canbreak = false;
			for(int i=0; i<rules.get(rn2.iruleindex).productpatterns.size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.iruleindex).productpatterns.size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.iruleindex).productpatterns.get(i), rules.get(rn1.iruleindex).productpatterns.get(j)))
					{
					case 0:
						if(!rn1.forward)
							tempinfluence.setInhibition(false);
						else
							tempinfluence.setActivation(false);
						break;
					case 1:
						if(!rn1.forward)
							tempinfluence.setInhibition(true);
						else
							tempinfluence.setActivation(true);
						canbreak = true;
						break;
					case -1:
						break;
					}
		}
	}
	
	//TODO
	int patternmatching(IRulePattern in1, IRulePattern in2)
	{
		int result = -1;
		int match = 0, nomatch = 0;
		boolean reactioncenter = false;
		boolean canmatch;
		
		// initialize all to be not matched
		for(int i=0; i<in1.molepatterns.size(); i++)
		{
			in1.molepatterns.get(i).matched = false;
			for(int j=0; j<in1.molepatterns.get(i).comppatterns.size(); j++)
				in1.molepatterns.get(i).comppatterns.get(j).matched = false;
		}
		for(int i=0; i<in2.molepatterns.size(); i++)
		{
			in2.molepatterns.get(i).matched = false;
			for(int j=0; j<in2.molepatterns.get(i).comppatterns.size(); j++)
				in2.molepatterns.get(i).comppatterns.get(j).matched = false;
		}
		
		
		IMoleculePattern tempmp1,tempmp2;
		for(int i=0; i<in1.molepatterns.size(); i++)
			for(int j=0; j<in2.molepatterns.size(); j++)
				
				// try to match molecules
				if(in1.molepatterns.get(i).moleindex == in2.molepatterns.get(j).moleindex && !in2.molepatterns.get(j).matched && !in1.molepatterns.get(i).matched)
				{
					in1.molepatterns.get(i).matched = true;
					in2.molepatterns.get(j).matched = true;
					tempmp1 = in1.molepatterns.get(i);
					tempmp2 = in2.molepatterns.get(j);
					
					// try to match components
					for(int k=0; k<tempmp1.comppatterns.size(); k++)
					{
						canmatch = false;
						for(int l=0; l<tempmp2.comppatterns.size(); l++)
							if(tempmp1.comppatterns.get(k).compindex == tempmp2.comppatterns.get(l).compindex && !tempmp2.comppatterns.get(l).matched && !tempmp1.comppatterns.get(k).matched)
							{
								tempmp2.comppatterns.get(l).matched = true;
								tempmp1.comppatterns.get(k).matched = true;
								Collections.sort(tempmp1.comppatterns.get(k).bondlist);
								Collections.sort(tempmp2.comppatterns.get(l).bondlist);
								
								//TODO
								if((tempmp1.comppatterns.get(k).stateindex!=tempmp2.comppatterns.get(l).stateindex) || !samebondstatus(tempmp1.comppatterns.get(k), tempmp2.comppatterns.get(l)))
								{
									// different states or different bond status
									return -1;
								}
								else
								{
									// same states or same bond status
									canmatch = true;
								}
								if(canmatch && tempmp2.comppatterns.get(l).reactioncenter)
									reactioncenter = true;
							}
						if(canmatch)
							match++;
						else
							nomatch++;
					}
				}
		for(int i=0; i<in1.molepatterns.size(); i++)
			if(!in1.molepatterns.get(i).matched)
				nomatch++;
		if(nomatch>0 && match>0 && reactioncenter)
			result = 0;
		else if(match>0 && nomatch ==0 && reactioncenter)
			result = 1;
		else
			result = -1;
		return result;
	}
	
	boolean samebondstatus(IComponentPattern in1, IComponentPattern in2)
	{
		if(in1.bondlist.equals(in2.bondlist))
		{
			if(in1.bondlist.size()==0)
			{
				//wildcards, -1:none 0:? 1:+
				if(in1.wildcards == -1 && in2.wildcards == 1)
					return false;
				if(in1.wildcards == 1 && in2.wildcards == -1)
					return false;
				return true;
			}
			else
				return true;
		}
		else
		{
			if(in1.bondlist.size()==0)
			{
				if(in1.wildcards==-1)
					return false;
				else
					return true;
			}
			else if(in2.bondlist.size()==0)
			{
				if(in2.wildcards==-1)
					return false;
				else
					return true;
			}
			else
			{
				return true;
			}
		}
	}
	
	class LoadIMap extends JPanel
	{
		BufferedImage img;
		
		LoadIMap()
		{	
			this.setBackground(Color.WHITE);
			try {
				img = ImageIO.read(new File("imap.png"));
			} catch (IOException e) {}
			this.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
			
			File txtfile = new File("imap.txt");
			Pattern pattern;
			Matcher m;
			String TBMatched;
			double widthscale=0,heightscale=0;
			try {
				BufferedReader br = new BufferedReader(new FileReader(txtfile));
				pattern = Pattern.compile("graph.+?\".+?,.+?,(.+?),(.+?)\"");
				while(br.ready())
				{
					TBMatched = br.readLine();
					m = pattern.matcher(TBMatched);
					if(m.find())
					{
						widthscale = Double.parseDouble(m.group(1))*1.0/img.getWidth();
						heightscale = Double.parseDouble(m.group(2))*1.0/img.getHeight();
						break;
					}
				}
				for(int i=0; i<rulenodes.size(); i++)
				{
					if(rulenodes.get(i).forward)
						pattern = Pattern.compile("r"+String.valueOf(rulenodes.get(i).iruleindex)+"\\s*\\[label=R"+String.valueOf(rulenodes.get(i).iruleindex+1)+",\\s*pos=\"(.+?),(.+?)\",\\s*width=\"(.+?)\",\\s*height=\"(.+?)\"\\];");
					else
						pattern = Pattern.compile("r"+String.valueOf(rulenodes.get(i).iruleindex)+"r\\s*\\[label=\"R"+String.valueOf(rulenodes.get(i).iruleindex+1)+"\'\",\\s*pos=\"(.+?),(.+?)\",\\s*width=\"(.+?)\",\\s*height=\"(.+?)\"\\];");
					while(br.ready())
					{
						TBMatched = br.readLine();
						m = pattern.matcher(TBMatched);
						if(m.find())
						{
							rulenodes.get(i).position[0] = (int)Math.round(Double.parseDouble(m.group(1))/widthscale-Float.parseFloat(m.group(3))*100/2);
							rulenodes.get(i).position[2] = (int)Math.round(img.getHeight() - Double.parseDouble(m.group(2))/heightscale - Float.parseFloat(m.group(4))*100/2);
							rulenodes.get(i).position[1] = rulenodes.get(i).position[0]+Math.round(Float.parseFloat(m.group(3))*100);
							rulenodes.get(i).position[3] = rulenodes.get(i).position[2]+Math.round(Float.parseFloat(m.group(4))*100);
							break;
						}
					}
				}
			} catch (IOException e) {}
		}
		
		public void paint(Graphics g)
		{
			g.drawImage(img, 0, 0, null);
		}
		
		public String getToolTipText(MouseEvent e)   
		{
			int x = e.getX(), y = e.getY();
			for(int i=0; i<rulenodes.size(); i++)
				if(x>=rulenodes.get(i).position[0] && x<=rulenodes.get(i).position[1] && y>=rulenodes.get(i).position[2] && y<=rulenodes.get(i).position[3])
					return rules.get(rulenodes.get(i).iruleindex).name;
			return null;
		}
	}
	
	void drawImap()
	{
		this.setTitle("Influence Graph");
		this.setPreferredSize(new Dimension(500,700));
		LoadIMap lm = new LoadIMap();
		lm.setToolTipText("");
		ToolTipManager.sharedInstance().setDismissDelay(100000);
		this.getContentPane().add(new JScrollPane(lm));
		this.pack();
		this.setVisible(true);
	}
}