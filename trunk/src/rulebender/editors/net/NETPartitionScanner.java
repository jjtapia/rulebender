package rulebender.editors.net;

import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;

public class NETPartitionScanner extends RuleBasedPartitionScanner{
  
	public final static String NET_COMMENT = "__net_comment";
	
	public NETPartitionScanner() 
	{
		//tokens
		IToken netComment = new Token(NET_COMMENT);
		
		//rules
		IPredicateRule[] rules = new IPredicateRule[1];
		
		// SingleLineRule(String startSequence, String endSequence, IToken token)
		
		rules[0] = new SingleLineRule("#", null, netComment); // parameters		
		
		setPredicateRules(rules);
	}
}
