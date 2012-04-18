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

import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import editor.BNGEditor;
import editor.contactmap.cdata.Bond;
import editor.contactmap.cdata.BondAction;
import editor.contactmap.cdata.Compartment;
import editor.contactmap.cdata.CompartmentTable;
import editor.contactmap.cdata.Component;
import editor.contactmap.cdata.ComponentPattern;
import editor.contactmap.cdata.Molecule;
import editor.contactmap.cdata.MoleculePattern;
import editor.contactmap.cdata.PotentialBond;
import editor.contactmap.cdata.Rule;
import editor.contactmap.cdata.RulePattern;
import editor.contactmap.cdata.Site;
import editor.contactmap.cdata.State;


/******************************************
 * 
 * 1. Some bugs found with duplicate components models, ex: A(a,a)->A(a!1, a!1)
 * 2. Duplicate component propagation in bonds
 *
 ******************************************/

public class CMapModel
{
	// ArrayLists to hold the necessary CMap data.
	ArrayList<Molecule> molecules = new ArrayList<Molecule>();
	ArrayList<Bond> bonds = new ArrayList<Bond>();
	ArrayList<Rule> rules = new ArrayList<Rule>();
	
	private boolean ruleparsevalid;
	private ArrayList<PotentialBond> pbonds = new ArrayList<PotentialBond>();
	private boolean flexiblestate = false;
	
	private CompartmentTable cmptTable = new CompartmentTable();
	
	public CMapModel(String molestr, boolean moleculetype)
	{
		if(moleculetype)
			parsemoleculetypes(molestr);
		else
		{
			flexiblestate = true;
			parsespecies(molestr);
		}
	}
	
	public void addCompartmentsInfo(String compartmentsstr) {
		parsecompartments(compartmentsstr);
	}
	
	public void addSeedSpeciesInfo(String speciesstr) {
		parsespecies(speciesstr);
	}
	
	public void addRulesInfo(String rulestr) {
		parserules(rulestr);
		//generateCMapDOT();
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
			tempmole.setName(molecule.substring(0, molecule.indexOf('(')).trim());
			component = molecule.substring(molecule.indexOf('(')+1, molecule.indexOf(')')).trim();
		}
		
		// If there is no ) then pull the name out and get the components from ( to end.
		if(leftparenthis && !rightparenthis)
		{
			tempmole.setName(molecule.substring(0, molecule.indexOf('(')).trim());
			component = molecule.substring(molecule.indexOf('(')+1, molecule.length()).trim();
		}
		
		// If there are no parens, then just get the name. 
		if(!leftparenthis && !rightparenthis)
		{
			tempmole.setName(molecule);
		}
		
		// Error on ) and no (.
		if(!leftparenthis && rightparenthis)
		{
			BNGEditor.displayOutput("\nError parsing molecule : "+molecule+" , molecule not added !");
			return;
		}
		
		// Handle compartment
		if (molecule.indexOf("@") != -1) {
			String compartment = molecule.substring(molecule.indexOf("@") + 1);
			tempmole.addCompartment(compartment);
			if (cmptTable.getCompartment(compartment) == null) {
				BNGEditor.displayOutput("\nError! compartment : " + compartment + " NOT declared");
			}
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
			// if not exists
			if (this.getmoleindex(tempmole.getName()) == -1) {
				molecules.add(tempmole);
			}
			else {
				int index = this.getmoleindex(tempmole.getName());
				// Handle compartment
				if (molecule.indexOf("@") != -1) {
					String compartment = molecule.substring(molecule.indexOf("@") + 1);
					molecules.get(index).addCompartment(compartment);
				}
			}
		}
		else
			BNGEditor.displayOutput("\nError parsing molecule : "+molecule+" , molecule not added !");
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
			tempcomp.setName(component);
			tempmole.getComponents().add(tempcomp);
			return;
		}
		else
		{
			state = component.substring(component.indexOf('~')+1, component.length()).trim();
			component = component.substring(0,component.indexOf('~'));
			if(component.indexOf('!')!=-1)
				component = component.substring(0,component.indexOf('!')).trim();
			tempcomp.setName(component);
			while(state.indexOf('~')!=-1)
			{
				tempstr1 = state.substring(0, state.indexOf('~'));
				if(tempstr1.indexOf('!')!=-1)
					tempstr1 = tempstr1.substring(0,tempstr1.indexOf('!')).trim();
				tempstate = new State();
				tempstate.setName(tempstr1);
				tempcomp.getStates().add(tempstate);
				state = state.substring(state.indexOf('~')+1, state.length()).trim();
			}
			tempstr1 = state;
			if(tempstr1.indexOf('!')!=-1)
				tempstr1 = tempstr1.substring(0,tempstr1.indexOf('!')).trim();
			tempstate = new State();
			tempstate.setName(tempstr1);
			tempcomp.getStates().add(tempstate);
			tempmole.getComponents().add(tempcomp);
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
		for(int i = 0; i < tempmole.getComponents().size(); i++)//delete the entries with empty string names
		{
			if(tempmole.getComponents().get(i).getName().trim().length()==0)
			{
				tempmole.getComponents().remove(i);
				i--;
			}
			else // Remove states that are empty
			{
				for(int j = 0; j < tempmole.getComponents().get(i).getStates().size(); j++)
					if(tempmole.getComponents().get(i).getStates().get(j).getName().trim().length() == 0)
					{
						tempmole.getComponents().get(i).getStates().remove(j);
						j--;
					}
			}
		}
		// no garbage in the names
		if(!validatename(tempmole.getName()))
			return false;
		for(int i = 0; i < tempmole.getComponents().size(); i++)
			if(!validatename(tempmole.getComponents().get(i).getName()))
				return false;
			else
				for(int j = 0; j < tempmole.getComponents().get(i).getStates().size(); j++)
					if(!validatename(tempmole.getComponents().get(i).getStates().get(j).getName()))
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
			temprule.setName(TBProcessed);
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
				BNGEditor.displayOutput("\nError parsing rule : "+TBProcessed+" , rule not added !");
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
		
		// Pull out the name
		if(rulestr.indexOf(":") != -1)
		{
			temprule.setLabel(rulestr.substring(0, rulestr.indexOf(":")));
			rulestr = rulestr.substring(rulestr.indexOf(":")+1, rulestr.length()).trim();
			temprule.setName(rulestr);
			System.out.println("\t\t Found name: " + temprule.getLabel());
		}
		
		if(rulestr.indexOf('<')!=-1)
		{
			temprule.setBidirection(true);
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
								temprule.setRate2(tempstr2);
							else
								temprule.setRate2(temprule.getRate1());
							temprule.setRate1(tempstr1);
							rulestr = rulestr.substring(0, rulestr.lastIndexOf(' ')).trim();
						}
						else 
							break;
					}
					else
					{
						if(validaterate(tempstr1))
						{
							if(temprule.getRate1()!=null && !(temprule.getRate1().length()==0))
								temprule.setRate2(temprule.getRate1());				
							temprule.setRate1(tempstr1);
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
			temprule.setBidirection(false);
			while(rulestr.lastIndexOf(' ')!=-1)
			{
				tempstr1 = rulestr.substring(rulestr.lastIndexOf(' '), rulestr.length()).trim();
				if (tempstr1.indexOf('(') == -1 && tempstr1.indexOf(')') == -1
						&& tempstr1.indexOf('>') == -1
						&& tempstr1.indexOf('.') == -1
						&& tempstr1.indexOf('!') == -1
						&& tempstr1.indexOf('+') == -1
						&& tempstr1.indexOf('?') == -1
						&& tempstr1.indexOf('~') == -1
						&& tempstr1.indexOf('{') == -1
						&& tempstr1.indexOf('}') == -1
						&& tempstr1.indexOf('@') == -1) {
				if(tempstr1.indexOf(',')!=-1)
					{
						tempstr2 = tempstr1.substring(tempstr1.indexOf(',')+1, tempstr1.length()).trim();
						tempstr1 = tempstr1.substring(0,tempstr1.indexOf(',')).trim();
						if(validaterate(tempstr1) && validaterate(tempstr2))
						{
							if(temprule.getRate1() == null || tempstr1.length()!=0)
								temprule.setRate1(tempstr1);
							else
								if(tempstr2.length()!=0)
									temprule.setRate1(tempstr2);									
							rulestr = rulestr.substring(0, rulestr.lastIndexOf(' ')).trim();
						}
						else 
							break;
					}
					else
					{
						if(validaterate(tempstr1))
						{				
							temprule.setRate1(tempstr1);
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
			if(molecules.get(i).getName().equals(rate))
				return false;
			else
				for(int j = 0; j<molecules.get(i).getComponents().size(); j++)
					if(molecules.get(i).getComponents().get(j).getName().equals(rate))
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
			// modified by Wen on Nov 7 2010
			rulestr = rulestr.substring(0, m.start()).trim()+"!$"+rulestr.substring(m.end(), rulestr.length()).trim();
			m = pattern1.matcher(rulestr);
		}

		if(temprule.isBidirection())
			reactants = rulestr.substring(0, rulestr.indexOf('<')).trim();
		else
			reactants = rulestr.substring(0, rulestr.indexOf('-')).trim();
		products = rulestr.substring(rulestr.indexOf('>')+1, rulestr.length()).trim();
		while(reactants.indexOf('+')!=-1)
		{
			parsepatterns(temprule.getReactantpatterns(), reactants.substring(0, reactants.indexOf('+')).trim());
			reactants = reactants.substring(reactants.indexOf('+')+1, reactants.length()).trim();			
		}
		parsepatterns(temprule.getReactantpatterns(), reactants);
		while(products.indexOf('+')!=-1)
		{
			parsepatterns(temprule.getProductpatterns(), products.substring(0, products.indexOf('+')).trim());
			products = products.substring(products.indexOf('+')+1, products.length()).trim();			
		}
		parsepatterns(temprule.getProductpatterns(), products);
		//determine bondaction & bond Cangenerate
		if(ruleparsevalid)
		{
			for(int i=0; i<temprule.getReactantpatterns().size(); i++)
				for(int j=0; j<temprule.getReactantpatterns().get(i).getBonds().size(); j++)
					add2bondactions(temprule.getBondactions(), temprule.getReactantpatterns().get(i).getBonds().get(j), false);
			for(int i=0; i<temprule.getProductpatterns().size(); i++)
				for(int j=0; j<temprule.getProductpatterns().get(i).getBonds().size(); j++)
					add2bondactions(temprule.getBondactions(), temprule.getProductpatterns().get(i).getBonds().get(j), true);
			for(int i=0; i<temprule.getBondactions().size(); i++)
				if(temprule.getBondactions().get(i).getAction() == 0)
				{
					temprule.getBondactions().remove(i);
					i--;
				}
			if(temprule.isBidirection())
				for(int i=0; i<temprule.getBondactions().size(); i++)
					bonds.get(temprule.getBondactions().get(i).getBondindex()).setCanGenerate(true);
			else
				for(int i=0; i<temprule.getBondactions().size(); i++)
					if(temprule.getBondactions().get(i).getAction()>0)
						bonds.get(temprule.getBondactions().get(i).getBondindex()).setCanGenerate(true);
		}
	}
	
	void add2bondactions(ArrayList<BondAction> ba, int rule, boolean addbond)
	{
		boolean addnew = true;
		BondAction tempba;
		if(addbond)
		{
			for(int i=0; i<ba.size(); i++)
				if(ba.get(i).getBondindex() == rule)
				{
					addnew = false;
					ba.get(i).setAction(ba.get(i).getAction() + 1);
				}
			if(addnew)
			{
				tempba = new BondAction();
				tempba.setBondindex(rule);
				tempba.setAction(1);
				ba.add(tempba);
			}
		}
		else
		{
			for(int i=0; i<ba.size(); i++)
				if(ba.get(i).getBondindex() == rule)
				{
					addnew = false;
					ba.get(i).setAction(ba.get(i).getAction() - 1);
				}
			if(addnew)
			{
				tempba = new BondAction();
				tempba.setBondindex(rule);
				tempba.setAction(-1);
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
				if(pbonds.get(i).getSites().size() == 2)
				{
					tempbond = new Bond();
					tempbond.setMolecule1(pbonds.get(i).getSites().get(0).getMolecule());
					tempbond.setComponent1(pbonds.get(i).getSites().get(0).getComponent());
					tempbond.setState1(pbonds.get(i).getSites().get(0).getState());
					tempbond.setMolecule2(pbonds.get(i).getSites().get(1).getMolecule());
					tempbond.setComponent2(pbonds.get(i).getSites().get(1).getComponent());
					tempbond.setState2(pbonds.get(i).getSites().get(1).getState());
					temprp.getBonds().add(add2bonds(tempbond));
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
		if(in1.getMolecule1() == in2.getMolecule1() && in1.getMolecule2() == in2.getMolecule2() && in1.getComponent1() == in2.getComponent1() && in1.getComponent2() == in2.getComponent2() && in1.getState1() == in2.getState1() && in1.getState2() == in2.getState2())
			return true;
		if(in1.getMolecule1() == in2.getMolecule2() && in1.getMolecule2() == in2.getMolecule1() && in1.getComponent1() == in2.getComponent2() && in1.getComponent2() == in2.getComponent1() && in1.getState1() == in2.getState2() && in1.getState2() == in2.getState1())
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
		String compartment = "";
		
		if(molestr.trim().length() == 0)
		{
			ruleparsevalid = false;
			return;
		}
		if(molestr.indexOf('(')!=-1)
			leftparenthis = true;
		if(molestr.indexOf(')')!=-1)
			rightparenthis = true;
		
		// parse compartment info
		if (molestr.indexOf("@") != -1) {
			if( molestr.indexOf(":") > molestr.indexOf("@")) {
				compartment = molestr.substring(molestr.indexOf("@")+1, molestr.indexOf(":"));
				molestr = molestr.substring(molestr.indexOf(":") + 1);
			}
			else {
				compartment = molestr.substring(molestr.indexOf("@")+1);
				molestr = molestr.substring(0, molestr.indexOf("@"));
			}
		}
		
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
		tempmp.setMoleindex(getmoleindex(molename));
		
		// add compartment info to molecule
		if (!compartment.equals("")) {
			molecules.get(getmoleindex(molename)).addCompartment(compartment);
		}
		
		if(tempmp.getMoleindex() == -1)
		{
			BNGEditor.displayOutput("\nError! Molecule: " + molename + " NOT declared!");
			ruleparsevalid = false;
			return;
		}
		
		while(component.indexOf(',')!=-1)
		{
			parsecomppattern(tempmp, component.substring(0, component.indexOf(',')).trim());
			component = component.substring(component.indexOf(',')+1, component.length()).trim();
		}
		parsecomppattern(tempmp, component);
		temprp.getMolepatterns().add(tempmp);
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
			tempcp.setCompindex(getcompindex(tempmp.getMoleindex(), compstr));
		}
		else
		{
			if(compstr.indexOf('%')!=-1)
				compstr = compstr.substring(0,compstr.indexOf('%')).trim();
			tempcp.setCompindex(getcompindex(tempmp.getMoleindex(), compstr));
			compbond = "";
		}
		if(tempcp.getCompindex() == -1)
		{
			BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + " NOT declared!");
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
				tempcp.setStateindex(getstateindex(tempmp.getMoleindex(), tempcp.getCompindex(), statestr));
			}
			else
			{
				if(statestr.indexOf('%')!=-1)
					statestr = statestr.substring(0, statestr.indexOf('%')).trim();
				tempcp.setStateindex(getstateindex(tempmp.getMoleindex(), tempcp.getCompindex(), statestr));
				statebond = "";
			}
			if(tempcp.getStateindex() == -1 && !statestr.equals("*"))
			{
				if(!flexiblestate)
				{
					BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " NOT declared!");
					ruleparsevalid = false;
					return;
				}
				else
				{
					tempcp.setStateindex(molecules.get(tempmp.getMoleindex()).getComponents().get(tempcp.getCompindex()).getStates().size()); 
					State tempstate = new State();
					tempstate.setName(statestr);
					molecules.get(tempmp.getMoleindex()).getComponents().get(tempcp.getCompindex()).getStates().add(tempstate); 
				}
			}
		}
		tempmp.getComppatterns().add(tempcp);
		if(compbond.length() != 0)
		{
			while(compbond.indexOf('!')!=-1)
			{
				tempstr = compbond.substring(0, compbond.indexOf('!')).trim();
				if(tempstr.indexOf('%')!=-1)
					tempstr = tempstr.substring(0, tempstr.indexOf('%')).trim();
				compbond = compbond.substring(compbond.indexOf('!')+1, compbond.length()).trim();
				if(tempstr.equals("$"))
				{
					if(tempcp.getWildcards() != 0)
						tempcp.setWildcards(1);
					else
					{
						BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
						ruleparsevalid = false;
						return;
					}
				}
				else if(tempstr.equals("?"))
				{
					if(tempcp.getWildcards() != 1)
						tempcp.setWildcards(0);
					else
					{
						BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
						ruleparsevalid = false;
						return;
					}
				}
				else
					add2pbonds(tempstr, new Site(tempmp.getMoleindex(), tempcp.getCompindex(), tempcp.getStateindex()));
			}
			tempstr = compbond;
			if(tempstr.indexOf('%')!=-1)
				tempstr = tempstr.substring(0, tempstr.indexOf('%')).trim();
			if(tempstr.equals("$"))
			{
				if(tempcp.getWildcards() != 0)
					tempcp.setWildcards(1);
				else
				{
					BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
					ruleparsevalid = false;
					return;
				}
			}
			else if(tempstr.equals("?"))
			{
				if(tempcp.getWildcards() != 1)
					tempcp.setWildcards(0);
				else
				{
					BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
					ruleparsevalid = false;
					return;
				}
			}
			else
				add2pbonds(tempstr, new Site(tempmp.getMoleindex(), tempcp.getCompindex(), tempcp.getStateindex()));
		}
		if(statebond.length() != 0)
		{
			while(statebond.indexOf('!')!=-1)
			{
				tempstr = statebond.substring(0, statebond.indexOf('!')).trim();
				if(tempstr.indexOf('%')!=-1)
					tempstr = tempstr.substring(0, tempstr.indexOf('%')).trim();
				statebond = statebond.substring(statebond.indexOf('!')+1, statebond.length()).trim();
				if(tempstr.equals("$"))
				{
					if(tempcp.getWildcards() != 0)
						tempcp.setWildcards(1);
					else
					{
						BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
						ruleparsevalid = false;
						return;
					}
				}
				else if(tempstr.equals("?"))
				{
					if(tempcp.getWildcards() != 1)
						tempcp.setWildcards(0);
					else
					{
						BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
						ruleparsevalid = false;
						return;
					}
				}
				else
					add2pbonds(tempstr, new Site(tempmp.getMoleindex(), tempcp.getCompindex(), tempcp.getStateindex()));
			}
			tempstr = statebond;
			if(tempstr.indexOf('%')!=-1)
				tempstr = tempstr.substring(0, tempstr.indexOf('%')).trim();
			if(tempstr.equals("$"))
			{
				if(tempcp.getWildcards() != 0)
					tempcp.setWildcards(1);
				else
				{
					BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
					ruleparsevalid = false;
					return;
				}
			}
			else if(tempstr.equals("?"))
			{
				if(tempcp.getWildcards() != 1)
					tempcp.setWildcards(0);
				else
				{
					BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
					ruleparsevalid = false;
					return;
				}
			}
			else
				add2pbonds(tempstr, new Site(tempmp.getMoleindex(), tempcp.getCompindex(), tempcp.getStateindex()));
		}
	}
	
	void add2pbonds(String bondname, Site site)
	{
		for(int i=0;i<pbonds.size();i++)
			if(pbonds.get(i).getName().equals(bondname))
			{
				pbonds.get(i).getSites().add(site);
				return;
			}
		pbonds.add(new PotentialBond(bondname, site));		
	}
	
	int getmoleindex(String mole)
	{
		for(int i=0; i<molecules.size(); i++)
			if(molecules.get(i).getName().equals(mole))
				return i;
		return -1;
	}
	
	int getcompindex(int mole, String comp)
	{
		Molecule tempmole;
		tempmole = molecules.get(mole);
		for(int i=0; i<tempmole.getComponents().size(); i++)
			if(tempmole.getComponents().get(i).getName().equals(comp))
				return i;
		return -1;
	}
	
	int getstateindex(int mole, int comp, String state)
	{
		Component tempcomp = molecules.get(mole).getComponents().get(comp);
		for(int i=0; i<tempcomp.getStates().size(); i++)
			if(tempcomp.getStates().get(i).getName().equals(state))
				return i;
		return -1;
	}
	
	public ArrayList<Molecule> getMolecules()
	{
		return molecules;
	}
	
	public ArrayList<Bond> getBonds()
	{
		return bonds;
	}
	
	public ArrayList<Rule> getRules()
	{
		return rules;
	}
	
	public CompartmentTable getCompartments() {
		return cmptTable;
	}
	
	private void parsecompartments(String compartmentsstr) {
		Scanner in = new Scanner(compartmentsstr);
		while (in.hasNext()) {
			String line = in.nextLine().trim();
			
			// empty line
			if(line.length()==0)
				continue;
			
			// comments
			if(line.charAt(0)=='#')
				continue;
			if(line.indexOf('#')!=-1)
				line = line.substring(0, line.indexOf('#')).trim();
			
			// multiple lines
			while (line.charAt(line.length()-1) == '\\')
			{
				if(in.hasNext())
					line = line.substring(0, line.length()-1) + ' ' + in.nextLine().trim();
				else
					line = line.substring(0, line.length()-1);
		        if(line.indexOf('#')>=0)
		        	line=line.substring(0, line.indexOf('#')).trim();
			}
			
			// block begin & end
			if (line.startsWith("begin") || line.startsWith("end")) {
				continue;
			}
			
			// compartment
			String[] tmplist = line.split(" ");
			
			// root
			boolean isRoot = false;
			if (!line.contains("*") && tmplist.length == 3) {
				isRoot = true;
			}
			if (line.contains("*") && tmplist.length == 5) {
				isRoot = true;
			}
			
			// current compartment name
			String name = tmplist[0];
			
			if (isRoot) {
				Compartment root = new Compartment(name, null);
				cmptTable.addCompartment(root);
				cmptTable.setRoot(root);
			}
			else {
				String parentName = tmplist[tmplist.length - 1];
				Compartment parent = cmptTable.getCompartment(parentName);
				Compartment cur = new Compartment(name, parent);
				cmptTable.addCompartment(cur);
			}
		}
		
//		cmptTable.print();
	}
}

