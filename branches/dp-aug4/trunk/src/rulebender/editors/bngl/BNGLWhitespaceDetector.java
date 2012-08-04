package rulebender.editors.bngl;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class BNGLWhitespaceDetector implements IWhitespaceDetector 
{
	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
}
