package rulebender.editors.bnga.text;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class BNGAPartitionScanner extends RuleBasedPartitionScanner
{
	public final static String BNGA_COMMENT = "__bngl_comment";
	public final static String BNGA_INIT_BLOCK = "__bngl_init_block";
	public final static String BNGA_REPEAT_BLOCK = "__bngl_repeat_block";
	
	
	public BNGAPartitionScanner() 
	{
		//tokens
		IToken bngaComment = new Token(BNGA_COMMENT);
		IToken bngaInitBlock = new Token(BNGA_INIT_BLOCK);
		IToken bngaRepeatBlock = new Token(BNGA_REPEAT_BLOCK);
		
		//rules
		// SingleLineRule(String startSequence, String endSequence, IToken token)
		// MultipeLineRule(String startSequence, String endSequence, IToken token)
		
		IPredicateRule[] rules = new IPredicateRule[3];
		
		rules[0] = new SingleLineRule("#", null, bngaComment); // parameters
		rules[1] = new MultiLineRule("init{", "}", bngaInitBlock); // parameters		
		rules[2] = new MultiLineRule("repeat{", "}", bngaRepeatBlock); // parameters
		setPredicateRules(rules);
	}
}
