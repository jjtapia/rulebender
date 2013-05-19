package rulebender.editors.bngl;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

/**
 * The partitioner splits the text of the file into categories. 
 * For now, we are just using this for comments. 
 * 
 * @author mr_smith22586
 */
public class BNGLPartitionScanner extends RuleBasedPartitionScanner
{
	public final static String BNGL_COMMENT = "__bngl_comment";
	public final static String BNGL_PARAMETERS_BLOCK = "__bngl_parameters_block";
	public final static String BNGL_MOLECULE_TYPES_BLOCK = "__bngl_parameters_block";
	public final static String BNGL_SPECIES_BLOCK = "__bngl_parameters_block";
	public final static String BNGL_REACTION_RULES_BLOCK = "__bngl_parameters_block";
	
	
	public BNGLPartitionScanner() 
	{
		//tokens
		IToken bnglComment = new Token(BNGL_COMMENT);
		//IToken bnglParametersBlock = new Token(BNGL_PARAMETERS_BLOCK);
		//IToken bnglMoleculeTypesBlock = new Token(BNGL_MOLECULE_TYPES_BLOCK);
		//IToken bnglSpeciesBlock = new Token(BNGL_SPECIES_BLOCK);
		//IToken bnglReactionRulesBlock = new Token(BNGL_REACTION_RULES_BLOCK);
		
		//rules
		// SingleLineRule(String startSequence, String endSequence, IToken token)
		// MultipeLineRule(String startSequence, String endSequence, IToken token)
		
		IPredicateRule[] rules = new IPredicateRule[1];
		
		rules[0] = new EndOfLineRule("#", bnglComment); // parameters
		//rules[1] = new MultiLineRule("begin parameters", "end parameters", bnglParametersBlock); // parameters		
		//rules[2] = new MultiLineRule("begin molecule types", "end molecule types", bnglMoleculeTypesBlock); // parameters
		//rules[3] = new MultiLineRule("begin species", "end species", bnglSpeciesBlock); // parameters
		//rules[4] = new MultiLineRule("begin reaction rules", "end reaction rules", bnglReactionRulesBlock); // parameters
		setPredicateRules(rules);
	}
}
