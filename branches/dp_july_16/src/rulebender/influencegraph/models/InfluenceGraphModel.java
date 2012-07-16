package rulebender.influencegraph.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/******************************************
 * 
 * 1. This version works only with models without duplicate components
 *
 ******************************************/

public class InfluenceGraphModel
{	
	// An ArrayList of molecules that are in the model.  
	ArrayList<IMolecule> molecules = new ArrayList<IMolecule>();
	
	// An ArrayList of bonds that are in the model, but not from a rule?
	ArrayList<IBond> bonds = new ArrayList<IBond>();
	
	// Arraylist of IRule information
	ArrayList<IRule> rules = new ArrayList<IRule>();
	ArrayList<IRuleNode> rulenodes = new ArrayList<IRuleNode>();
	ArrayList<Influence> influences = new ArrayList<Influence>();
	
	private boolean ruleparsevalid;
	private ArrayList<IPotentialBond> pbonds = new ArrayList<IPotentialBond>();
	private boolean flexiblestate = false;
	
	public InfluenceGraphModel(String molestr, String rulestr, boolean moleculetype)
	{
		/*
		parserules(rulestr);
		
		for(int i=0; i<rules.size(); i++)
			getreactioncenter(rules.get(i));
		
		generateImap();
	// */
	}
	
	
	void parserules(String rulestr)
	{
		Scanner scan = new Scanner(rulestr);
		String tempstr1;
		
		// regular expression for anything in curly braces?
		Pattern curlyBracePattern = Pattern.compile("\\{.*\\}");
		
		// regular expression for 'DeleteMolecules' with any spaces on the outside
		Pattern deleteMoleculesPattern = Pattern.compile("\\s+DeleteMolecules\\s+");
		Matcher matcher;
		IRule temprule;
		while(scan.hasNext())
		{
			String TBProcessed = scan.nextLine().trim();
			// Empty line
			if(TBProcessed.length()==0)
				continue;
			
			// Comment
			if(TBProcessed.charAt(0)=='#')
				continue;
			
			// In-line Comment
			if(TBProcessed.indexOf('#')!=-1)
				TBProcessed = TBProcessed.substring(0, TBProcessed.indexOf('#')).trim();
			
			// Multiline rule
			while (TBProcessed.charAt(TBProcessed.length()-1) == '\\')
			{
				if(scan.hasNext())
					TBProcessed = TBProcessed.substring(0, TBProcessed.length()-1) + ' ' + scan.nextLine().trim();
				else
					TBProcessed = TBProcessed.substring(0, TBProcessed.length()-1);
		        if(TBProcessed.indexOf('#')>=0)
		        	TBProcessed=TBProcessed.substring(0, TBProcessed.indexOf('#')).trim();
			}
			
			// if there is white space in the rule string
			if(TBProcessed.indexOf(' ')!=-1)
			{
				// Create a temp string from the beginning to the white space.
				tempstr1 = TBProcessed.substring(0,TBProcessed.indexOf(' ')).trim();
				try	
				{
					// Get an integer from the temp string ... but do nothing with it.  I think
					// this must be for labelling the rules, but is never used.
					Integer.parseInt(tempstr1);
					//
					TBProcessed = TBProcessed.substring(TBProcessed.indexOf(' ')+1,TBProcessed.length()).trim();
				}
			
				catch(NumberFormatException e)
				{
					
				}	
			}	
			
			// if there is white space in the rule string
			if(TBProcessed.indexOf(' ')!=-1)
			{
				// Create a temp string from the beginning to the white space.
				tempstr1 = TBProcessed.substring(0,TBProcessed.indexOf(' ')).trim();
				try	
				{
					// Get an integer from the temp string ... but do nothing with it.  I think
					// this must be for labelling the rules, but is never used.
					Integer.parseInt(tempstr1);
					//
					TBProcessed = TBProcessed.substring(TBProcessed.indexOf(' ')+1,TBProcessed.length()).trim();
				}
			
				catch(NumberFormatException e)
				{
					
				}	
			}
			
			// TBProcessed is now probably without a space and does not contain comments.
			
			ruleparsevalid = true;
			
			//create a rule and set the name
			temprule = new IRule();
			temprule.setName(TBProcessed);
			matcher = curlyBracePattern.matcher(TBProcessed);//delete
			
			// removing the text in curly braces.
			while(matcher.find())
			{
				TBProcessed = TBProcessed.substring(0,matcher.start()).trim()+" "+TBProcessed.substring(matcher.end(), TBProcessed.length()).trim();
				matcher = curlyBracePattern.matcher(TBProcessed);
			}
			
			// Add a space
			TBProcessed = TBProcessed + " ";
			matcher = deleteMoleculesPattern.matcher(TBProcessed);
			
			// removing the 'DeleteMolecule'. pattern
			while(matcher.find())
			{
				TBProcessed = TBProcessed.substring(0,matcher.start()).trim()+ " " + TBProcessed.substring(matcher.end(),TBProcessed.length()).trim() + " ";
				matcher = deleteMoleculesPattern.matcher(TBProcessed);
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
			
			
			// Everything up to here is just text processing.
			
			parserule(temprule,TBProcessed);
			
			if(ruleparsevalid)
				rules.add(temprule);
			//else
				//BNGEditor.displayOutput("\nError parsing rule : "+TBProcessed+" , rule not added !");
		}
	}
	
	void parserule(IRule temprule,String rulestr)
	{
		
		Pattern rightarrowpattern = Pattern.compile("\\-\\s*>");
		Matcher m = rightarrowpattern.matcher(rulestr);
		String tempstr1,tempstr2;
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
		}
		
		// Check to see if the rule is bidirectional.
		if(rulestr.indexOf('<')!=-1)
		{
			// Set the bidirectional boolean to true
			temprule.setBidirection(true);
			
			// it looks like this block gets the rates
			while(rulestr.lastIndexOf(' ')!=-1)
			{ 
				// Get the substring from the last space to the end.  This should contain the rates.
				tempstr1 = rulestr.substring(rulestr.lastIndexOf(' '), rulestr.length()).trim();
				
				// If there is not a special character (, ), >, ., !, +, ?, ~, {, } 
				if(		   tempstr1.indexOf('(') == -1 && tempstr1.indexOf(')') == -1 
						&& tempstr1.indexOf('>') == -1 && tempstr1.indexOf('.') == -1 
						&& tempstr1.indexOf('!') == -1 && tempstr1.indexOf('+') == -1
						&& tempstr1.indexOf('?') == -1 && tempstr1.indexOf('~') == -1
						&& tempstr1.indexOf('{') == -1 && tempstr1.indexOf('}') == -1)
				{
					// If there is not a comma
					if(tempstr1.indexOf(',') != -1)
					{
						// Set a temp string to the substring from after the comma to the end. For reverse rate.
						tempstr2 = tempstr1.substring(tempstr1.indexOf(',')+1, tempstr1.length()).trim();
						
						// Set a temp string to the substring from the beginning to the comma. For forward rate.
						tempstr1 = tempstr1.substring(0,tempstr1.indexOf(',')).trim();
						
						// Validate both of the strings (either empty or not a parsed name so far).
						// If they are valid...
						if(validaterate(tempstr1) && validaterate(tempstr2))
						{
							// Set the reverse rate if the string is not empty.
							if(tempstr2.length()!=0)
								temprule.setRate2(tempstr2);
							
							// Set the reverse rate as the forward rate if the reverse rate is empty.
							else
								temprule.setRate2(temprule.getRate1());
							
							// Set the forward rate
							//TODO Shouldn't this be above the previous if blocks?
							temprule.setRate1(tempstr1);
							
							// Remove the rates from the rule string.
							rulestr = rulestr.substring(0, rulestr.lastIndexOf(' ')).trim();
						}
						// If they are invalid then stop parsing this rule.
						else 
							break;
					}
					
					// If there was a special character in the rulestring between the last space and the end.
					else
					{
						// If the first temp string is valid.
						if(validaterate(tempstr1))
						{
							// If the forward rate for the temp rule is not null and not empty,
							// then set the reverse rate as the forward rate.
							if(temprule.getRate1()!=null && !(temprule.getRate1().length()==0))
								temprule.setRate2(temprule.getRate1());		
							
							// Set the forward rate as the temp string 1
							temprule.setRate1(tempstr1);
							
							// remove the rates from the rulestring.
							rulestr = rulestr.substring(0, rulestr.lastIndexOf(' ')).trim();
						}
						// Stop parsing if the forward rate is invalid.
						else
							break;
					}
				}
				
				// If there is a special character (, ), >, ., !, +, ?, ~, {, }
				// between the last space and the end of the rule string then
				// stop parsing the rule.
				else
					break;
			}
			
			// Send the rule string and the partially completed IRule object to the 
			// parserule2pattern method for further processing.
			parserule2pattern(temprule,rulestr);
		}
		
		// If there is no "<" in the rule string then it is not bidirectional.
		else
		{
			// Set the bidirectional bool to false.
			temprule.setBidirection(false);
			
			// While there are spaces in the rule string.
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
	
	/**
	 * Validates a rate string.  The string can be empty, or if the string
	 * is not empty it must not match the name of a molecule or component
	 * that has been previously parsed.
	 * 
	 * @param rate
	 * @return
	 */
	
	
	boolean validaterate(String rate)
	{
		// Removes white space from beginning and end.
		rate = rate.trim();
		
		// empty and non-null rates are valid. 
		if(rate.length()==0)
			return true;
		
		// Check all of the molecules seen so far.
		for(int i = 0; i < molecules.size(); i++)
		{
			// If the name of the molecule matches the rate that is being 
			// checked then the rate is invalid.
			if(molecules.get(i).getName().equals(rate))
			{
				return false;
			}
			
			// If the name of the molecule does not match the rate
			else
			{
				// For all of the components in the current molecule
				for(int j = 0; j<molecules.get(i).getComponents().size(); j++)
				{
					// If the component name matches the rate string then the 
					// rate is invalid.
					if(molecules.get(i).getComponents().get(j).getName().equals(rate))
						return false;
				}
			}
		}
		
		// The molecule is valid if flow reaches here.
		return true;
	}
	
	/**
	 * 
	 * @param temprule
	 * @param rulestr
	 */
	
	void parserule2pattern(IRule temprule, String rulestr)
	{
		// Declare reactants and products strings.
		String reactants,products;
		
		// Create a regular expression that matches !+  
		Pattern wildcardPattern = Pattern.compile("!\\s*\\+");
		
		// Match the regexp to the rule string.
		Matcher m = wildcardPattern.matcher(rulestr);
		
		// Replace all strings that match the regexp with !$
		while(m.find())
		{
			// Remove the match and replace it with '!$'
			rulestr = rulestr.substring(0, m.start()).trim()+"!$"+rulestr.substring(m.end(), rulestr.length()).trim();
			
			// match the regexp to the string again.
			m = wildcardPattern.matcher(rulestr);
		}
		
		// Set the reactants for bidirectional rules as the text from the 
		// beginning to the '<'.
		if(temprule.isBidirection())
			reactants = rulestr.substring(0, rulestr.indexOf('<')).trim();
		
		// Set the reactants for a single directional rule as the text from
		// the beginning to the '-'.  
		else
			reactants = rulestr.substring(0, rulestr.indexOf('-')).trim();
		
		// Set the products to the string from after '>' to the end.
		products = rulestr.substring(rulestr.indexOf('>')+1, rulestr.length()).trim();
		
		// While the reactants string contains a '+'
		while(reactants.indexOf('+')!=-1)
		{
			// Parse the reactant patterns out of the reactants
			parsepatterns(temprule.getReactantpatterns(), reactants.substring(0, reactants.indexOf('+')).trim());
			
			// Remove from the beginning until after the first '+'.
			reactants = reactants.substring(reactants.indexOf('+')+1, reactants.length()).trim();			
		}
		
		// parse the last of the reactants string.
		parsepatterns(temprule.getReactantpatterns(), reactants);
		
		// While the products string contains a '+'
		while(products.indexOf('+')!=-1)
		{
			// parse out the products paterns. 
			parsepatterns(temprule.getProductpatterns(), products.substring(0, products.indexOf('+')).trim());
			
			// Remove from the beginning until after the first '+'.
			products = products.substring(products.indexOf('+')+1, products.length()).trim();			
		}
		
		parsepatterns(temprule.getProductpatterns(), products);
		//determine bondaction & bond Can generate
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
	
	void add2bondactions(ArrayList<IBondAction> ba, int rule, boolean addbond)
	{
		boolean addnew = true;
		IBondAction tempba;
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
				tempba = new IBondAction();
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
				tempba = new IBondAction();
				tempba.setBondindex(rule);
				tempba.setAction(-1);
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
				// two sites, actual integer bonds
				if(pbonds.get(i).getSites().size() == 2)
				{
					tempbond = new IBond();
					tempbond.setMolecule1(pbonds.get(i).getSites().get(0).getMolecule());
					tempbond.setComponent1(pbonds.get(i).getSites().get(0).getComponent());
					tempbond.setState1(pbonds.get(i).getSites().get(0).getState());
					tempbond.setMolecule2(pbonds.get(i).getSites().get(1).getMolecule());
					tempbond.setComponent2(pbonds.get(i).getSites().get(1).getComponent());
					tempbond.setState2(pbonds.get(i).getSites().get(1).getState());
					bondindex = add2bonds(tempbond);
					temprp.getBonds().add(bondindex);
					for(int j = 0; j<temprp.getMolepatterns().size(); j++)
						for(int k=0; k<temprp.getMolepatterns().get(j).getComppatterns().size(); k++)
							if(temprp.getMolepatterns().get(j).getComppatterns().get(k).getPbondlist().contains(pbonds.get(i).getName())) {
								temprp.getMolepatterns().get(j).getComppatterns().get(k).getBondlist().add(bondindex);
							}
				}
		}
		
		//for(int j = 0; j<temprp.getMolepatterns().size(); j++)
			//for(int k=0; k<temprp.getMolepatterns().get(j).getComppatterns().size(); k++)
			//	temprp.getMolepatterns().get(j).getComppatterns().get(k).getPbondlist().clear();
			
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
		if(in1.getMolecule1() == in2.getMolecule1() && in1.getMolecule2() == in2.getMolecule2() && in1.getComponent1() == in2.getComponent1() && in1.getComponent2() == in2.getComponent2() && in1.getState1() == in2.getState1() && in1.getState2() == in2.getState2())
			return true;
		if(in1.getMolecule1() == in2.getMolecule2() && in1.getMolecule2() == in2.getMolecule1() && in1.getComponent1() == in2.getComponent2() && in1.getComponent2() == in2.getComponent1() && in1.getState1() == in2.getState2() && in1.getState2() == in2.getState1())
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
		// This value is set later, but never actually used. 
		//String compartment = "";
		
		if(molestr.trim().length() == 0)
		{
			ruleparsevalid = false;
			return;
		}
		if(molestr.indexOf('(')!=-1)
			leftparenthis = true;
		if(molestr.indexOf(')')!=-1)
			rightparenthis = true;
		
		// delete compartment info
		if (molestr.indexOf("@") != -1) {
			if( molestr.indexOf(":") > molestr.indexOf("@")) {
				//compartment = molestr.substring(molestr.indexOf("@")+1, molestr.indexOf(":"));
				molestr = molestr.substring(molestr.indexOf(":") + 1);
			}
			else {
				//compartment = molestr.substring(molestr.indexOf("@")+1);
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
		
		if(tempmp.getMoleindex() == -1)
		{
			//BNGEditor.displayOutput("\nError! Molecule: " + molename + " NOT declared!");
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
			//BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + " NOT declared!");
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
					//BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " NOT declared!");
					ruleparsevalid = false;
					return;
				}
				else
				{
					tempcp.setStateindex(molecules.get(tempmp.getMoleindex()).getComponents().get(tempcp.getCompindex()).getStates().size()); 
					IState tempstate = new IState();
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
				// ?+, + has been replaced with $
				if(tempstr.equals("$"))
				{
					if(tempcp.getWildcards() != 0)
						tempcp.setWildcards(1);
					else
					{
						//BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
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
						//BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
						ruleparsevalid = false;
						return;
					}
				}
				else
				{
					add2pbonds(tempstr, new ISite(tempmp.getMoleindex(), tempcp.getCompindex(), tempcp.getStateindex()));
					tempcp.getPbondlist().add(tempstr);
				}
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
					//BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
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
					//BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
					ruleparsevalid = false;
					return;
				}
			}
			else
			{
				add2pbonds(tempstr, new ISite(tempmp.getMoleindex(), tempcp.getCompindex(), tempcp.getStateindex()));
				tempcp.getPbondlist().add(tempstr);
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
				if(tempstr.equals("$"))
				{
					if(tempcp.getWildcards() != 0)
						tempcp.setWildcards(1);
					else
					{
						//BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
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
						//BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
						ruleparsevalid = false;
						return;
					}
				}
				else
				{
					add2pbonds(tempstr, new ISite(tempmp.getMoleindex(), tempcp.getCompindex(), tempcp.getStateindex()));
					tempcp.getPbondlist().add(tempstr);
				}
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
					//BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
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
					//BNGEditor.displayOutput("\nError! Molecule: " + molecules.get(tempmp.getMoleindex()).getName() + ", Component: "+ compstr + ", State: " + statestr + " has multiple bond status!");
					ruleparsevalid = false;
					return;
				}
			}
			else
			{
				add2pbonds(tempstr, new ISite(tempmp.getMoleindex(), tempcp.getCompindex(), tempcp.getStateindex()));
				tempcp.getPbondlist().add(tempstr);
			}
		}
	}
	
	void add2pbonds(String bondname, ISite site)
	{
		for(int i=0;i<pbonds.size();i++)
			if(pbonds.get(i).getName().equals(bondname))
			{
				pbonds.get(i).getSites().add(site);
				return;
			}
		pbonds.add(new IPotentialBond(bondname, site));		
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
		IMolecule tempmole;
		tempmole = molecules.get(mole);
		for(int i=0; i<tempmole.getComponents().size(); i++)
			if(tempmole.getComponents().get(i).getName().equals(comp))
				return i;
		return -1;
	}
	
	int getstateindex(int mole, int comp, String state)
	{
		IComponent tempcomp = molecules.get(mole).getComponents().get(comp);
		for(int i=0; i<tempcomp.getStates().size(); i++)
			if(tempcomp.getStates().get(i).getName().equals(state))
				return i;
		return -1;
	}
	
	void getreactioncenter(IRule temprule)
	{
		ArrayList<IMoleculePattern> reactantmoles = new ArrayList<IMoleculePattern>();
		ArrayList<IMoleculePattern> productmoles = new ArrayList<IMoleculePattern>();
		for(int i=0; i<temprule.getReactantpatterns().size(); i++)
			for(int j=0; j<temprule.getReactantpatterns().get(i).getMolepatterns().size(); j++)
			{
				reactantmoles.add(temprule.getReactantpatterns().get(i).getMolepatterns().get(j));
				for(int k=0; k<temprule.getReactantpatterns().get(i).getMolepatterns().get(j).getComppatterns().size(); k++)
					temprule.getReactantpatterns().get(i).getMolepatterns().get(j).getComppatterns().get(k).setMatched(false);
			}
		for(int i=0; i<temprule.getProductpatterns().size(); i++)
			for(int j=0; j<temprule.getProductpatterns().get(i).getMolepatterns().size(); j++)
			{
				productmoles.add(temprule.getProductpatterns().get(i).getMolepatterns().get(j));
				for(int k=0; k<temprule.getProductpatterns().get(i).getMolepatterns().get(j).getComppatterns().size(); k++)
					temprule.getProductpatterns().get(i).getMolepatterns().get(j).getComppatterns().get(k).setMatched(false);
			}
		
		for(int i=0; i< reactantmoles.size(); i++)
			reactantmoles.get(i).setMatched(false);
		for(int i=0; i< productmoles.size(); i++)
			productmoles.get(i).setMatched(false);
		
		for(int i=0; i< reactantmoles.size(); i++)
			for(int j=0; j<productmoles.size(); j++)
				trymatchmoles(reactantmoles.get(i), productmoles.get(j));
		
		for(int i=0; i< reactantmoles.size(); i++)
			if(!reactantmoles.get(i).isMatched())
				for(int j=0; j<reactantmoles.get(i).getComppatterns().size(); j++)
					reactantmoles.get(i).getComppatterns().get(j).setReactioncenter(true);
		for(int i=0; i< productmoles.size(); i++)
			if(!productmoles.get(i).isMatched())
				for(int j=0; j<productmoles.get(i).getComppatterns().size(); j++)
					productmoles.get(i).getComppatterns().get(j).setReactioncenter(true);
	}
	
	void trymatchmoles(IMoleculePattern in1, IMoleculePattern in2)
	{
		boolean result = false;
		if(in1.isMatched() || in2.isMatched())
			return;
		if(in1.getMoleindex() == in2.getMoleindex())
		{
			result = true;
			for(int i=0; i<in1.getComppatterns().size(); i++)
				for(int j=0; j<in2.getComppatterns().size(); j++)
					if(in1.getComppatterns().get(i).getCompindex() == in2.getComppatterns().get(j).getCompindex() && !in1.getComppatterns().get(i).isMatched() && !in2.getComppatterns().get(j).isMatched())
					{
						in1.getComppatterns().get(i).setMatched(true);
						in2.getComppatterns().get(j).setMatched(true);
						if(!comparecomppattern(in1.getComppatterns().get(i), in2.getComppatterns().get(j)))
						{
							in1.getComppatterns().get(i).setReactioncenter(true); 
							in2.getComppatterns().get(j).setReactioncenter(true);
						}
					}
			for(int i=0; i<in1.getComppatterns().size(); i++)
				if(!in1.getComppatterns().get(i).isMatched())
				{
					result = false;
					break;
				}
			for(int i=0; i<in2.getComppatterns().size(); i++)
				if(!in2.getComppatterns().get(i).isMatched())
				{
					result = false;
					break;
				}
		}
		if(!result)
		{
			for(int i=0; i<in1.getComppatterns().size(); i++)
			{
				in1.getComppatterns().get(i).setMatched(false);
				in1.getComppatterns().get(i).setReactioncenter(false);
			}
			for(int i=0; i<in2.getComppatterns().size(); i++)
			{
				in2.getComppatterns().get(i).setMatched(false);
				in2.getComppatterns().get(i).setReactioncenter(false);
			}
		}
		else
		{
			in1.setMatched(true);
			in2.setMatched(true);
		}
	}
	
	boolean comparecomppattern(IComponentPattern in1, IComponentPattern in2)
	{
		Collections.sort(in1.getBondlist());
		Collections.sort(in2.getBondlist());
		if(in1.getStateindex() == in2.getStateindex() && in1.getWildcards() == in2.getWildcards() && in1.getBondlist().equals(in2.getBondlist()))
			return true;
		return false;
	}
	
	void generateImap()
	{
		int rulenodeindex = 0;
		for(int i=0; i<rules.size(); i++)
			if(rules.get(i).isBidirection())
			{
				rulenodes.add(new IRuleNode(rulenodeindex, i, true, rules.get(i).getName(), rules.get(i).getLabel()));
				rulenodeindex++;
				rulenodes.add(new IRuleNode(rulenodeindex, i, false, rules.get(i).getName(), rules.get(i).getLabel()));
				rulenodeindex++;
			}
			else
			{
				rulenodes.add(new IRuleNode(rulenodeindex, i, true, rules.get(i).getName(), rules.get(i).getLabel()));
				rulenodeindex++;
			}
		
		Influence tempinfluence;
		for(int i=0; i<rulenodes.size(); i++)
			for(int j=0; j<rulenodes.size(); j++)
				if(i != j)
				{
					tempinfluence = new Influence(i, j);
					determineinfluence(tempinfluence, rulenodes.get(i), rulenodes.get(j));
					if(tempinfluence.getActivation() !=-1 || tempinfluence.getInhibition() != -1)
						influences.add(tempinfluence);
				}
	}
	
	void determineinfluence(Influence tempinfluence, IRuleNode rn1, IRuleNode rn2)
	{

		boolean canbreak;
		if (rn2.isForward() && rn1.isForward()) {
			canbreak = false;
			// pattern matching from all first half of reaction2 to all first half of reaction1
			for(int i=0; i<rules.get(rn2.getIruleindex()).getReactantpatterns().size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.getIruleindex()).getReactantpatterns().size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.getIruleindex()).getReactantpatterns().get(i), rules.get(rn1.getIruleindex()).getReactantpatterns().get(j)))
					{
					// partial match and r1 reactioncenter
					case 0:
						// possible inhibition
						tempinfluence.setInhibition(false);
						break;
					// full match and r1 reactioncenter
					case 1:
						// definite inhibition
						tempinfluence.setInhibition(true);
						canbreak = true;
						break;
					case -1:
						break;
					}

			canbreak = false;
			for(int i=0; i<rules.get(rn2.getIruleindex()).getReactantpatterns().size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.getIruleindex()).getProductpatterns().size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.getIruleindex()).getReactantpatterns().get(i), rules.get(rn1.getIruleindex()).getProductpatterns().get(j)))
					{
					// partial match and r1 reactioncenter
					case 0:
						// possible activation
						tempinfluence.setActivation(false);
						break;
					// full match and r1 reactioncenter
					case 1:
						// definite activation
						tempinfluence.setActivation(true);
						canbreak = true;
						break;
					case -1:
						break;
					}
		}
		else if (rn2.isForward() && !rn1.isForward()) {
			canbreak = false;
			for(int i=0; i<rules.get(rn2.getIruleindex()).getReactantpatterns().size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.getIruleindex()).getProductpatterns().size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.getIruleindex()).getReactantpatterns().get(i), rules.get(rn1.getIruleindex()).getProductpatterns().get(j)))
					{
					
						// partial match and r1 reactioncenter
					case 0:
						// possible inhibition
						tempinfluence.setInhibition(false);
						break;
					// full match and r1 reactioncenter
					case 1:
						// definite inhibition
						tempinfluence.setInhibition(true);
						canbreak = true;
						break;
					case -1:
						break;
					}
			
			canbreak = false;
			// pattern matching from all first half of reaction2 to all first half of reaction1
			for(int i=0; i<rules.get(rn2.getIruleindex()).getReactantpatterns().size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.getIruleindex()).getReactantpatterns().size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.getIruleindex()).getReactantpatterns().get(i), rules.get(rn1.getIruleindex()).getReactantpatterns().get(j)))
					{
					// partial match and r1 reactioncenter
					case 0:
						// possible activation
						tempinfluence.setActivation(false);
						break;
					// full match and r1 reactioncenter
					case 1:
						// definite activation
						tempinfluence.setActivation(true);
						canbreak = true;
						break;
					case -1:
						break;
					}
			
			
		}
		else if (!rn2.isForward() && rn1.isForward()) {
			canbreak = false;
			for(int i=0; i<rules.get(rn2.getIruleindex()).getProductpatterns().size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.getIruleindex()).getReactantpatterns().size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.getIruleindex()).getProductpatterns().get(i), rules.get(rn1.getIruleindex()).getReactantpatterns().get(j)))
					{
					case 0:
						// possible inhibition
						tempinfluence.setInhibition(false);
						break;
					case 1:
						// definite inhibition
						tempinfluence.setInhibition(true);
						canbreak = true;
						break;
					case -1:
						break;
					}

			canbreak = false;
			for(int i=0; i<rules.get(rn2.getIruleindex()).getProductpatterns().size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.getIruleindex()).getProductpatterns().size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.getIruleindex()).getProductpatterns().get(i), rules.get(rn1.getIruleindex()).getProductpatterns().get(j)))
					{
					case 0:
						// possible activation
						tempinfluence.setActivation(false);
						break;
					case 1:
						// definite activation
						tempinfluence.setActivation(true);
						canbreak = true;
						break;
					case -1:
						break;
					}
		}
		else if (!rn2.isForward() && !rn1.isForward()) {
			canbreak = false;
			for(int i=0; i<rules.get(rn2.getIruleindex()).getProductpatterns().size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.getIruleindex()).getProductpatterns().size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.getIruleindex()).getProductpatterns().get(i), rules.get(rn1.getIruleindex()).getProductpatterns().get(j)))
					{
					
					case 0:
						// possible inhibition
						tempinfluence.setInhibition(false);
						break;
					case 1:
						// definite inhibition
						tempinfluence.setInhibition(true);
						canbreak = true;
						break;
					case -1:
						break;
					}
			
			canbreak = false;
			for(int i=0; i<rules.get(rn2.getIruleindex()).getProductpatterns().size() && !canbreak; i++)
				for(int j=0; j<rules.get(rn1.getIruleindex()).getReactantpatterns().size() && !canbreak; j++)
					switch(patternmatching(rules.get(rn2.getIruleindex()).getProductpatterns().get(i), rules.get(rn1.getIruleindex()).getReactantpatterns().get(j)))
					{
					case 0:
						// possible activation
						tempinfluence.setActivation(false);
						break;
					case 1:
						// definite activation
						tempinfluence.setActivation(true);
						canbreak = true;
						break;
					case -1:
						break;
					}
			
			
		}
	}
	
	// match pattern1 to pattern2
	// result: conflict, full match, partial match, no match
	int patternmatching(IRulePattern in1, IRulePattern in2)
	{
		int result = -1;
		int match = 0, nomatch = 0;
		boolean reactioncenter = false;
		boolean canmatch;
		
		// initialize all to be not matched
		for(int i=0; i<in1.getMolepatterns().size(); i++)
		{
			in1.getMolepatterns().get(i).setMatched(false);
			for(int j=0; j<in1.getMolepatterns().get(i).getComppatterns().size(); j++)
				in1.getMolepatterns().get(i).getComppatterns().get(j).setMatched(false);
		}
		for(int i=0; i<in2.getMolepatterns().size(); i++)
		{
			in2.getMolepatterns().get(i).setMatched(false);
			for(int j=0; j<in2.getMolepatterns().get(i).getComppatterns().size(); j++)
				in2.getMolepatterns().get(i).getComppatterns().get(j).setMatched(false);
		}
		
		
		IMoleculePattern tempmp1,tempmp2;
		for(int i=0; i<in1.getMolepatterns().size(); i++)
			for(int j=0; j<in2.getMolepatterns().size(); j++)
				
				// try to match molecules
				if(in1.getMolepatterns().get(i).getMoleindex() == in2.getMolepatterns().get(j).getMoleindex() && !in2.getMolepatterns().get(j).isMatched() && !in1.getMolepatterns().get(i).isMatched())
				{
					in1.getMolepatterns().get(i).setMatched(true);
					in2.getMolepatterns().get(j).setMatched(true);
					tempmp1 = in1.getMolepatterns().get(i);
					tempmp2 = in2.getMolepatterns().get(j);
					
					
					for(int k=0; k<tempmp1.getComppatterns().size(); k++)
					{
						canmatch = false;
						for(int l=0; l<tempmp2.getComppatterns().size(); l++)
							
							// try to match components
							if(tempmp1.getComppatterns().get(k).getCompindex() == tempmp2.getComppatterns().get(l).getCompindex() && !tempmp2.getComppatterns().get(l).isMatched() && !tempmp1.getComppatterns().get(k).isMatched())
							{
								tempmp2.getComppatterns().get(l).setMatched(true);
								tempmp1.getComppatterns().get(k).setMatched(true);
								Collections.sort(tempmp1.getComppatterns().get(k).getPbondlist());
								Collections.sort(tempmp2.getComppatterns().get(l).getPbondlist());
								
								
								// different states or different bond status
								if((tempmp1.getComppatterns().get(k).getStateindex()!=tempmp2.getComppatterns().get(l).getStateindex()) || !samebondstatus(tempmp1.getComppatterns().get(k), tempmp2.getComppatterns().get(l)))
								{
									// conflict
									return -1;
								}
								else
								{
									// same states and same bond status
									canmatch = true;
								}
								if(canmatch && tempmp2.getComppatterns().get(l).isReactioncenter())
									reactioncenter = true;
							}
						if(canmatch)
							// match or conflict
							match++;
						else
							// no match
							nomatch++;
					}
				}
		for(int i=0; i<in1.getMolepatterns().size(); i++)
			if(!in1.getMolepatterns().get(i).isMatched())
				nomatch++;
		if(nomatch>0 && match>0 && reactioncenter)
			// partial match and reactioncenter
			result = 0;
		else if(match>0 && nomatch ==0 && reactioncenter)
			// full match and reactioncenter
			result = 1;
		else
			// conflict or no match or not reactioncenter
			result = -1;
		return result;
	}
	
	boolean samebondstatus(IComponentPattern in1, IComponentPattern in2)
	{
		
		// in1 and in2 have same bondlist (ArrayList<Integer>)
		if(in1.getPbondlist().equals(in2.getPbondlist()))
		{
			// no integer bond
			if(in1.getPbondlist().size()==0)
			{
				//wildcards, -1:none 0:? 1:+
				
				// a -> a , a!? -> a!?, a!+ -> a!+, match
				if (in1.getWildcards() == in2.getWildcards())
					return true;
				// a!? -> a match
				if (in1.getWildcards() == 0 && in2.getWildcards() == -1)
					return true;
				// a!+ -> a conflict
				if(in1.getWildcards() == 1 && in2.getWildcards() == -1)
					return false;
				// a -> a!+ conflict, but can't be reaction center
				if(in1.getWildcards() == -1 && in2.getWildcards() == 1)
					return false;
				// a -> a!?, a!? -> a!+ match, a!+ -> a!? match
				// but can't be reaction center
				else
					return true;
			}
			// has same integer bonds, match
			else
				return true;
		}
		// in1 and in2 have different bondlist
		else
		{
			// in1 no integer bond
			if(in1.getPbondlist().size()==0)
			{
				// a -> a!1 conflict
				if(in1.getWildcards()==-1)
					return false;
				// a!? -> a!1, a!+ -> a!1 match
				else
					return true;
			}
			else if(in2.getPbondlist().size()==0)
			{
				// a!1 -> a conflict
				if(in2.getWildcards()==-1)
					return false;
				// a!1 -> a!+, a!1 -> a!? match, but can be reaction center
				else
					return true;
			}
			else
			{
				// has different integer bond, match
				return true;
			}
		}
	}
	
	
	public ArrayList<IRuleNode> getRuleNodes()
	{
		return rulenodes;
	}

	public ArrayList<Influence> getInfluences() 
	{
		return influences;
	}
}