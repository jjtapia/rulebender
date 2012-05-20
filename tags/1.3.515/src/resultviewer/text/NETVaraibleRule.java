package resultviewer.text;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * 
 * Rule to extract parameters and molecule names tokens.
 * 
 */
public class NETVaraibleRule implements IRule {
	private static final int UNDEFINED = -1;
	private IToken variableToken;
	private IToken moleToken;
	private int fColumn = UNDEFINED;

	public NETVaraibleRule(IToken variableToken, IToken moleToken) {
		Assert.isNotNull(variableToken);
		Assert.isNotNull(moleToken);
		this.variableToken = variableToken;
		this.moleToken = moleToken;
	}

	/**
	 * Set the largest column the pattern starts.
	 * 
	 * @param column
	 */
	public void setColumnConstraint(int column) {
		if (column < 0)
			column = UNDEFINED;
		fColumn = column;
	}

	public IToken evaluate(ICharacterScanner scanner) {

		int c = scanner.read();
		if (Character.isLetter((char) c)) {
			if (fColumn == UNDEFINED || fColumn >= scanner.getColumn()) {

				// check if it is an exponential number
				if ((char) c == 'e') {
					boolean isNumber = true;
					do {
						c = scanner.read();
						if (Character.isLetter((char) c) || (char) c == '_') {
							isNumber = false;
						} else if ((char) c == '(') {
							scanner.unread();
							return moleToken;
						}
					} while (Character.isLetterOrDigit((char) c)
							|| (char) c == '_');
					scanner.unread();
					if (isNumber == true)
						return Token.UNDEFINED;
					else
						return variableToken;
				}

				else {
					do {
						c = scanner.read();
						if ((char) c == '(') {
							scanner.unread();
							return moleToken;
						}
					} while (Character.isLetterOrDigit((char) c)
							|| (char) c == '_');
					scanner.unread();
					return variableToken;

				}
			}
		}
		scanner.unread();
		return Token.UNDEFINED;
	}
}
