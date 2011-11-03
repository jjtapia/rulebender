package rulebender.editors.bngl;

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
	
	public BNGLPartitionScanner() 
	{
		//tokens
		IToken bnglComment = new Token(BNGL_COMMENT);
		
		//rules
		IPredicateRule[] rules = new IPredicateRule[1];
		
		// SingleLineRule(String startSequence, String endSequence, IToken token)
		
		rules[0] = new SingleLineRule("#", null, bnglComment); // parameters		
		
		setPredicateRules(rules);
	}
}
