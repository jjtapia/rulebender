import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
//import java.io.Console;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.KeyStroke;

import org.eclipse.swt.SWT;

import prefuse.data.search.RegexSearchTupleSet;

public class KeyStrokeTest {
	public static void main(String[] args) {
		regExpSearch();

		System.exit(0);

	}

	private static void regExpSearch() {
		// The original
		String testRule = "egfr(Y1148~pY) + Shc(PTB,Y317~pY!1).Grb2(SH2!1,SH3) <-> \\ \n egfr(Y1148~pY!2).Shc(PTB!2,Y317~pY!1).Grb2(SH2!1,SH3)  kp18,km18";

		// Start the regular expression as the rule
		String regexp = testRule;

		// put an optional pair of backslashes between every character.
		// This has to happen before the other special characters are escaped.
		regexp = regexp.replace("\n", "");
		regexp = regexp.replace("", "\\s?\\\\?\\s?\\n?");
		// regexp = regexp.replace("", "\\\\\\\\?");

		// Escape the parentheses
		regexp = regexp.replace("(", "\\(");
		regexp = regexp.replace(")", "\\)");

		// Escape the +
		regexp = regexp.replace("+", "\\+");

		// Escape the !
		regexp = regexp.replace("!", "\\!");

		// Escape the ~
		// regexp = regexp.replace("~", "\\~");

		// Escape the .
		regexp = regexp.replace(".", "\\.");

		match(regexp, testRule);

		match("\\s?\\\\?\\s?\\n?e\\s?\\\\?\\s?\\n?g\\s?\\\\?\\s?\\n?f\\s?\\\\?\\s?\\n?r\\s?\\\\?\\s?\\n?\\(\\s?\\\\?\\s?\\n?Y\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?4\\s?\\\\?\\s?\\n?8\\s?\\\\?\\s?\\n?~\\s?\\\\?\\s?\\n?p\\s?\\\\?\\s?\\n?Y\\s?\\\\?\\s?\\n?\\)\\s?\\\\?\\s?\\n? \\s?\\\\?\\s?\\n?\\+\\s?\\\\?\\s?\\n? \\s?\\\\?\\s?\\n?S\\s?\\\\?\\s?\\n?h\\s?\\\\?\\s?\\n?c\\s?\\\\?\\s?\\n?\\(\\s?\\\\?\\s?\\n?P\\s?\\\\?\\s?\\n?T\\s?\\\\?\\s?\\n?B\\s?\\\\?\\s?\\n?,\\s?\\\\?\\s?\\n?Y\\s?\\\\?\\s?\\n?3\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?7\\s?\\\\?\\s?\\n?~\\s?\\\\?\\s?\\n?p\\s?\\\\?\\s?\\n?Y\\s?\\\\?\\s?\\n?\\!\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?\\)\\s?\\\\?\\s?\\n?\\.\\s?\\\\?\\s?\\n?G\\s?\\\\?\\s?\\n?r\\s?\\\\?\\s?\\n?b\\s?\\\\?\\s?\\n?2\\s?\\\\?\\s?\\n?\\(\\s?\\\\?\\s?\\n?S\\s?\\\\?\\s?\\n?H\\s?\\\\?\\s?\\n?2\\s?\\\\?\\s?\\n?\\!\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?,\\s?\\\\?\\s?\\n?S\\s?\\\\?\\s?\\n?H\\s?\\\\?\\s?\\n?3\\s?\\\\?\\s?\\n?\\)\\s?\\\\?\\s?\\n? \\s?\\\\?\\s?\\n?<\\s?\\\\?\\s?\\n?-\\s?\\\\?\\s?\\n?>\\s?\\\\?\\s?\\n? \\s?\\\\?\\s?\\n? \\s?\\\\?\\s?\\n?e\\s?\\\\?\\s?\\n?g\\s?\\\\?\\s?\\n?f\\s?\\\\?\\s?\\n?r\\s?\\\\?\\s?\\n?\\(\\s?\\\\?\\s?\\n?Y\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?4\\s?\\\\?\\s?\\n?8\\s?\\\\?\\s?\\n?~\\s?\\\\?\\s?\\n?p\\s?\\\\?\\s?\\n?Y\\s?\\\\?\\s?\\n?\\!\\s?\\\\?\\s?\\n?2\\s?\\\\?\\s?\\n?\\)\\s?\\\\?\\s?\\n?\\.\\s?\\\\?\\s?\\n?S\\s?\\\\?\\s?\\n?h\\s?\\\\?\\s?\\n?c\\s?\\\\?\\s?\\n?\\(\\s?\\\\?\\s?\\n?P\\s?\\\\?\\s?\\n?T\\s?\\\\?\\s?\\n?B\\s?\\\\?\\s?\\n?\\!\\s?\\\\?\\s?\\n?2\\s?\\\\?\\s?\\n?,\\s?\\\\?\\s?\\n?Y\\s?\\\\?\\s?\\n?3\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?7\\s?\\\\?\\s?\\n?~\\s?\\\\?\\s?\\n?p\\s?\\\\?\\s?\\n?Y\\s?\\\\?\\s?\\n?\\!\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?\\)\\s?\\\\?\\s?\\n?\\.\\s?\\\\?\\s?\\n?G\\s?\\\\?\\s?\\n?r\\s?\\\\?\\s?\\n?b\\s?\\\\?\\s?\\n?2\\s?\\\\?\\s?\\n?\\(\\s?\\\\?\\s?\\n?S\\s?\\\\?\\s?\\n?H\\s?\\\\?\\s?\\n?2\\s?\\\\?\\s?\\n?\\!\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?,\\s?\\\\?\\s?\\n?S\\s?\\\\?\\s?\\n?H\\s?\\\\?\\s?\\n?3\\s?\\\\?\\s?\\n?\\)\\s?\\\\?\\s?\\n? \\s?\\\\?\\s?\\n? \\s?\\\\?\\s?\\n?k\\s?\\\\?\\s?\\n?p\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?8\\s?\\\\?\\s?\\n?,\\s?\\\\?\\s?\\n?k\\s?\\\\?\\s?\\n?m\\s?\\\\?\\s?\\n?1\\s?\\\\?\\s?\\n?8\\s?\\\\?\\s?\\n?",
				"egfr(Y1148~pY) + Shc(PTB,Y317~pY!1).Grb2(SH2!1,SH3) <-> \\ \n egfr(Y1148~pY!2).Shc(PTB!2,Y317~pY!1).Grb2(SH2!1,SH3)  kp18,km18");
	}

	private static void match(String patternString, String string) {
		System.out.println("\nTest String: " + string);
		System.out.println("Built Regexp: " + patternString);

		// Create the regexp
		// Pattern pattern = Pattern.compile(patternString, Pattern.COMMENTS);
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(string);

		boolean found = false;
		while (matcher.find()) {
			System.out.println("\nI found the text " + matcher.group()
					+ " starting at " + "index " + matcher.start()
					+ " and ending at index " + matcher.end());
			found = true;
		}
		if (!found) {
			System.out.println("\nNo match found.");
		}
	}

	private static void keyStrokes() {

		System.out.println("SWT Mod + Q: " + (SWT.MOD1 + 'Q'));
		// System.out.println("AWT KeyStroke: " +
		// KeyStroke.getKeyStroke(KeyEvent.VK_Q,
		// Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()).getKey);

		System.out.println("\\nSWT Mod + C: " + (SWT.MOD1 + 'C'));
		System.out.println("AWT KeyStroke: "
				+ KeyStroke.getKeyStroke(KeyEvent.VK_C,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
						.getKeyCode());

		System.out.println("\\nSWT Mod + V: " + (SWT.MOD1 + 'V'));
		System.out.println("AWT KeyStroke: "
				+ KeyStroke.getKeyStroke(KeyEvent.VK_V,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
						.getKeyCode());

		System.out.println("\\nSWT Mod + Z: " + (SWT.MOD1 + 'Z'));
		System.out.println("AWT KeyStroke: "
				+ KeyStroke.getKeyStroke(KeyEvent.VK_Z,
						Toolkit.getDefaultToolkit().getMenuShortcutKeyMask())
						.getKeyCode());

	}
}