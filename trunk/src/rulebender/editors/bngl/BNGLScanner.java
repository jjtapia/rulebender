package rulebender.editors.bngl;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

//import rulebender.editors.common.SiteRule;
import rulebender.editors.common.VariableRule;


public class BNGLScanner extends RuleBasedScanner
{
	// Define the keywords.
	private static String[] words = { 
		"begin", 
		"end", 
		"model", 
		"parameters", 
		"compartments", 
		"molecule", 
		"types", 
		"seed", 
		"species", 
		"observables", 
		"functions",
		"reaction", 
		"rules", 
//		"reaction_rules", 
		"population", 
		"maps",
		"actions",
		"reactions", // NET file block
		"groups",    // NET file block
//		"rates", 
//		"reaction_rates", 
		"Molecules", // Observable type
		"Species",   // Observable type
		"MatchOnce", 
		"DeleteMolecules",
		"MoveConnected",
		"include_reactants",
		"exclude_reactants",
		"include_products",
		"exclude_products"
//		"substanceUnits", 
//		"setOption",
//		"generate_network", 
//		"setConcentration", 
//		"resetConcentrations", 
//		"saveConcentrations",
//		"writeSBML", 
//		"writeMfile", 
//		"writeMexfile",
//		"simulate_ode", 
//		"simulate_ssa", 
//		"simulate_pla", 
//		"simulate_nf",
//		"simulate"
		};
	
	
	/**
	 * Constructor accepts the color manager.
	 * 
	 *  The basic idea is to create an IToken for each color, and then
	 *  make a rule that matches the text.
	 * 
	 * Sets up all of the rules. 
	 * @param manager
	 */
	public BNGLScanner(BNGLColorManager manager) {
		
		// Make an array of rules that will be used. 
		IRule[] rules = new IRule[3];
		//Add rule for white space
		rules[0] = new WhitespaceRule(new BNGLWhitespaceDetector());

		//keywords
		IToken keyword = new Token(new TextAttribute(manager.getColor(IBNGLColorConstants.KEY_WORD)));
		
		WordRule keywordRule = new WordRule(new IWordDetector() {
			public boolean isWordStart(char ch) {
				if (Character.isLetter(ch) || ch == '_')
					return true;
				else
					return false;
			}

			public boolean isWordPart(char ch) {
				if (Character.isLetter(ch) || Character.isDigit(ch)
						|| ch == '_')
					return true;
				else
					return false;
			}
		});

		for (int i = 0; i < words.length; i++) {
			keywordRule.addWord(words[i], keyword);
		}

		rules[1] = keywordRule;
		
		//variable
		IToken variableToken = new Token(new TextAttribute(manager.getColor(IBNGLColorConstants.VARIABLE)));
		IToken moleToken = new Token(new TextAttribute(manager.getColor(IBNGLColorConstants.MOLECULE)));
		VariableRule variableRule = new VariableRule(variableToken, moleToken);
		rules[2] = variableRule;
		
		//site
		//SiteRule siteRule = new SiteRule(new Token(new TextAttribute(manager.getColor(IBNGLColorConstants.SITE_RULE))));
		//rules[3] = siteRule;
		
		setRules(rules);
	}
}
