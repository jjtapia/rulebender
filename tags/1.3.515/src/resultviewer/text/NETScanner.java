package resultviewer.text;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.swt.graphics.Color;

public class NETScanner extends RuleBasedScanner {
	private static Color color_Blue = new Color(null, 0, 0, 255);
	private static Color color_PaleGreen4 = new Color(null, 84, 139, 84);
	private static Color color_purple = new Color(null, 128, 0, 128);
	private static Color color_grey = new Color(null, 190, 190, 190);
	private static Color color_grey34 = new Color(null, 87, 87, 87);
	private static Color color_brown4 = new Color(null, 139, 35, 35);

	
	private static String[] words = { "begin", "end", "parameters", "molecule",
			"Molecules", "types", "observables", "species", "Species", "reaction", "reactions",
			"rules", "reaction_rules", "groups", "rates", "reaction_rates", "MatchOnce", "substanceUnits", "setOption",
			"model", "generate_network", "setConcentration", "resetConcentrations", "saveConcentrations",
			"writeSBML", "writeMfile", "simulate_ode", "simulate_ssa", "compartments"};

	public static RuleBasedScanner getScanner() {
		RuleBasedScanner scanner = new RuleBasedScanner();

		IRule[] rules = new IRule[5];

		// whitespace
		IWhitespaceDetector whitespaceDetector = new IWhitespaceDetector() {
			public boolean isWhitespace(char c) {
				return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
			}
		};

		rules[0] = new WhitespaceRule(whitespaceDetector);

		//keywords
		IToken keyword = new Token(new TextAttribute(color_purple));
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
		
		//index number
		NETIndexNumberRule indexNumberRule = new NETIndexNumberRule(new Token(new TextAttribute(color_grey)));
		// set the largest column the pattern starts
		indexNumberRule.setColumnConstraint(5); 
		rules[2] = indexNumberRule;		
		
		//variable
		IToken variableToken = new Token(new TextAttribute(color_brown4));
		IToken moleToken = new Token(new TextAttribute(color_Blue));
		NETVaraibleRule variableRule = new NETVaraibleRule(variableToken, moleToken);
		// set the largest column the pattern starts
		//parameterRule.setColumnConstraint(7);
		rules[3] = variableRule;
		
		//site
		NETSiteRule siteRule = new NETSiteRule(new Token(new TextAttribute(color_grey34)));
		rules[4] = siteRule;
		
		scanner.setRules(rules);
		return scanner;
	}

	//comment
	public static RuleBasedScanner getCommentScanner() {
		RuleBasedScanner scanner = new RuleBasedScanner();
		scanner.setDefaultReturnToken(new Token(new TextAttribute(
				color_PaleGreen4)));
		return scanner;
	}
}
