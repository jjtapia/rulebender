package resultviewer.text;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * 
 * Rule to extract index of rule tokens.
 * 
 */
public class NETIndexNumberRule implements IRule
{
  private static final int UNDEFINED = -1;
  private IToken fToken;
  private int fColumn = UNDEFINED;


  public NETIndexNumberRule(IToken token)
  {
    Assert.isNotNull(token);
    fToken = token;
  }


  /**
   * Set the largest column the pattern starts.
   * 
   * @param column
   */
  public void setColumnConstraint(int column)
  {
    if (column < 0)
      column = UNDEFINED;
    fColumn = column;
  }


  public IToken evaluate(ICharacterScanner scanner)
  {
    int c = scanner.read();
    if (Character.isDigit((char) c))
    {
      if (fColumn == UNDEFINED || fColumn >= scanner.getColumn())
      {
        do
        {
          c = scanner.read();
        } while (Character.isDigit((char) c));
        scanner.unread();
        if (Character.isWhitespace((char) c))
          return fToken;
        else
          return Token.UNDEFINED;
      }
    }
    scanner.unread();
    return Token.UNDEFINED;
  }
}
