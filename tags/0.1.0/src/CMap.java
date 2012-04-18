import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/******************************************
 * 
 * 1. Some bugs found with duplicate components models, ex: A(a,a)->A(a!1, a!1)
 * 2. Duplicate component propagation in bonds
 *
 ******************************************/

public class CMap extends JFrame
{
	ArrayList<Molecule> molecules = new ArrayList<Molecule>();
	ArrayList<Bond> bonds = new ArrayList<Bond>();
	ArrayList<Rule> rules = new ArrayList<Rule>();
	
	private boolean ruleparsevalid;
	private ArrayList<PotentialBond> pbonds = new ArrayList<PotentialBond>();
	private boolean flexiblestate = false;
	
	CMap(String molestr, String rulestr, boolean moleculetype)
	{
		if(moleculetype)
			parsemoleculetypes(molestr);
		else
		{
			flexiblestate = true;
			parsespecies(molestr);
		}
		parserules(rulestr);
		generatecmap();
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
	
	boolean validatemole(Molecule tempmole)
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
	
	void parserule(Rule temprule,String rulestr)
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
	
	void generatecmap()
	{
		Molecule tmole;
		Component tcomp;
		State tstate;
		Bond tbond;
		File dotfile = new File("cmap.dot");
		File pngfile = new File("cmap.png");
		if(dotfile.exists())
			dotfile.delete();
		if(pngfile.exists())
			pngfile.delete();
		PrintWriter outfile;
		try {
			outfile = new PrintWriter(dotfile);
			outfile.println("digraph G{ranksep=2;node [shape=record];");
			for(int i=0; i<molecules.size(); i++)
			{
				tmole = molecules.get(i);
				if(tmole.components.size()==0)
					outfile.println(tmole.name+"[shape=box]");
				else
				{
					outfile.println("subgraph cluster"+tmole.name+" {label = \"" + tmole.name+"\";");
					outfile.println("{");
					for(int j=0;j<tmole.components.size();j++)
					{
						tcomp = tmole.components.get(j);
						if(tcomp.states.size() == 0)
							outfile.println("struct"+tmole.name+"_"+tcomp.name+j+"[label= \""+tcomp.name+"\"];");
						else
						{
							outfile.print("struct"+tmole.name+"_"+tcomp.name+j+"[label= \"{"+tcomp.name+"|{");
							for(int k=0;k<tcomp.states.size();k++)
							{
								tstate = tcomp.states.get(k);
								if(k == tcomp.states.size()-1)
								{
									outfile.print("<"+tstate.name+">"+tstate.name);
								}
								else
								{
									outfile.print("<"+tstate.name+">"+tstate.name+"|");
								}
							}
							outfile.println("} }\"];");
						}
					}
					outfile.println("}");
					outfile.println("}");
				}
			}
			for(int l = 0; l<bonds.size();l++)
			{
				tbond = bonds.get(l);
				if(tbond.CanGenerate == true)
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
						outfile.println("struct"+molecules.get(tbond.molecule2).name+"_"+molecules.get(tbond.molecule2).components.get(tbond.component2).name+tbond.component2+":"+molecules.get(tbond.molecule2).components.get(tbond.component2).states.get(tbond.state2).name+"[arrowhead=none];");
					}
					else
					{
						outfile.println("struct"+molecules.get(tbond.molecule2).name+"_"+molecules.get(tbond.molecule2).components.get(tbond.component2).name+tbond.component2+"[arrowhead=none];");
					}
				}
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
				}
			}
			outfile.println("}");
			outfile.close();
		} catch (FileNotFoundException e) {}
		try {
			Process p;
			if(BNGEditor.ostype !=1)
				p = Runtime.getRuntime().exec(BNGEditor.DOTfpath+"/"+BNGEditor.DOTfname+" -Tpng -o cmap.png cmap.dot");
			else
			{
				if(BNGEditor.DOTfpath == null || BNGEditor.DOTfname == null)
					p = Runtime.getRuntime().exec("cmd.exe /c dot -Tpng -o cmap.png cmap.dot");
				else
					p = Runtime.getRuntime().exec(BNGEditor.DOTfpath+"\\"+BNGEditor.DOTfname+" -Tpng -o cmap.png cmap.dot");
			}
			p.waitFor();
		} catch (IOException e) {} catch (InterruptedException e) {}
	}
	
	class LoadCMap extends JPanel
	{
		BufferedImage img;
		
		LoadCMap()
		{
			this.setBackground(Color.WHITE);
			try {
				img = ImageIO.read(new File("cmap.png"));
			} catch (IOException e) {}
			this.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
		}
		
		public void paint(Graphics g)
		{
			g.drawImage(img, 0, 0, null);
		}
	}
	
	void drawcmap()
	{
		this.setTitle("Contact Map");
		this.setPreferredSize(new Dimension(500,700));
		this.getContentPane().add(new JScrollPane(new LoadCMap()));
		this.pack();
		this.setVisible(true);
	}
}