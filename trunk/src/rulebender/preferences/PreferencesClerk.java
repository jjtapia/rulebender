/**
 * This class is a static way to reference the preferences and other
 */
package rulebender.preferences;

import rulebender.Activator;
import rulebender.core.workspace.PickWorkspaceDialog;
import rulebender.preferences.views.MyFieldEditorPreferencePage;

public class PreferencesClerk 
{
  
  static
  {
    String dir = 
        Activator
        .getDefault()
        .getPreferenceStore()
        .getString(MyFieldEditorPreferencePage.PREF_SIM_PATH);

    if (null == dir || "".equals(dir))
    {
//      Activator
//      .getDefault()
//      .getPreferenceStore()
//      .setDefault(MyFieldEditorPreferencePage.PREF_SIM_PATH, 
//          System.getProperty("user.dir") + MyFieldEditorPreferencePage.DEF_SIM_DIR);
      
      // TODO Check the os and set the default or set the actual value appropriately
    }
  }
	// The name of the main BNG file.
	private static String BNGName = "BNG2.pl";

	// The path from the root directory to the main BNG file.
	private static String BNGPathFromRoot = "";

	// Private constructor for static access only.
	private PreferencesClerk() {
		throw new AssertionError();
	}

	/**
	 * This method returns the path to the 'BNGName' file, but not including that
	 * file.
	 * 
	 * @return String path to 'BNGName'
	 */
	public static String getBNGPath() 
	{
		return Activator.getDefault().getPreferenceStore().getString("SIM_PATH")
		    + System.getProperty("file.separator") + BNGPathFromRoot;
	}

	/**
	 * Returns the name of the main BNG file in 'BNGName'.
	 * 
	 * @return String name of main bng file.
	 */
	public static String getBNGName() {
		return BNGName;
	}

	/**
	 * Returns the user supplied root path to the BioNetGen directory. This does
	 * not include the path from the root to 'BNGName' or 'BNGName' itself.
	 * 
	 * @return String root path to BNG directory.
	 */
	public static String getBNGRoot() {
		return Activator.getDefault().getPreferenceStore().getString("SIM_PATH")
		    + System.getProperty("file.separator");
	}

	/**
	 * Returns the entire path from the root to the 'BNGName' file.
	 * 
	 * @return String path to main bng file.
	 */
	public static String getFullBNGPath() {
		return getBNGRoot() + BNGPathFromRoot + BNGName;
	}

	public static OS getOS() {
		String stemp = System.getProperty("os.name");

		if (stemp.contains("Windows") || stemp.contains("WINDOWS")
		    || stemp.contains("windows")) {
			return OS.WINDOWS;
		} else if (stemp.contains("Mac") || stemp.contains("MAC")
		    || stemp.contains("mac")) {
			return OS.OSX;
		} else {
			return OS.LINUX;
		}

	}

	public static String getWorkspace() {
		return PickWorkspaceDialog.getLastSetWorkspaceDirectory();
	}
}
