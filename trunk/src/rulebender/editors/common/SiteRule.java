package rulebender.editors.common;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * 
 * Rule to extract bonding sites tokens.
 * 
 */
public class SiteRule implements IRule {
	private static final int UNDEFINED = -1;
	private IToken fToken;

	public SiteRule(IToken token) {
		Assert.isNotNull(token);
		fToken = token;
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int c = scanner.read();
		if ((char) c == '(') {

			do {
				c = scanner.read();
			} while ((char) c != ')');
			c = scanner.read();
			scanner.unread();
			return fToken;
		}
		scanner.unread();
		return Token.UNDEFINED;
	}
}
