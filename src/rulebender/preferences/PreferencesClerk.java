/**
 * This class is a static way to reference the preferences and other
 */
package rulebender.preferences;

import java.io.File;

import rulebender.Activator;
import rulebender.core.workspace.PickWorkspaceDialog;
import rulebender.preferences.views.MyFieldEditorPreferencePage;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import rulebender.preferences.PreferencesClerk;
import rulebender.Activator;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import rulebender.preferences.PreferencesClerk;
import rulebender.simulate.BioNetGenUtility;
import rulebender.Activator;

public class PreferencesClerk 
{
  
  static
  {
    String dir = "";
        //Activator
        //.getDefault()
        //.getPreferenceStore()
        //.getString(MyFieldEditorPreferencePage.PREF_SIM_PATH);

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
	private static String BNGPathFromRoot = "BioNetGen-2.2.6";

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

	public static String getDefaultBNGPath() 
	{
//           return Activator.getDefault().getPreferenceStore().getString("SIM_PATH")
//	   + System.getProperty("file.separator") + BNGPathFromRoot;
		return  System.getProperty("user.dir")
		    + System.getProperty("file.separator")
                    + BNGPathFromRoot;
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
//		return Activator.getDefault().getPreferenceStore().getString("SIM_PATH")
//		    + System.getProperty("file.separator");
		// System.out.println("calling getBNGRoot()");
		return  System.getProperty("user.dir");
	}


	
	
	public static String getUpgradeCheck() {
      return Activator.getDefault().getPreferenceStore().getString("UPGRADE_CHECK");
	}
	
	public static String setUpgradeCheck(String ssss) {
      Activator.getDefault().getPreferenceStore().setValue("UPGRADE_CHECK",ssss);
      return "Ret_String";
	}
	

	
	
	public static String getOutputSetting() {
      return Activator.getDefault().getPreferenceStore().getString("OUTPUT_SETTING");
	}
	
	public static String setOutputSetting(String ssss) {
      Activator.getDefault().getPreferenceStore().setValue("OUTPUT_SETTING",ssss);
      return "Ret_String";
	}
	
	
	public static String getMaxGraphDensity() {
	  return Activator.getDefault().getPreferenceStore().getString("MAX_GRAPH_COLUMNS");
	}
		
    public static String setMaxGraphDensity(String ssss) {
	  Activator.getDefault().getPreferenceStore().setValue("MAX_GRAPH_COLUMNS",ssss);
	  return "Ret_String";
    }
		
		
	
	/**
	 * Returns the entire path from the root to the 'BNGName' file.
	 * 
	 * @return String path to main bng file.
	 */
	
	public static String getUserBNGPath() {
		return Activator.getDefault().getPreferenceStore().getString("SIM_PATH");
	}
	public static String getFullUserBNGPath() {
		return getUserBNGPath()  + System.getProperty("file.separator") + BNGName;
	}
	
	public static String getFullDefaultBNGPath() {
		return getDefaultBNGPath() + System.getProperty("file.separator") + BNGName;
	}
	
	
	/*  This returns either a valid directory name with no file.separator, or No_Valid_Path_. */
	public static String getBNGPath() {
		boolean prereq = BioNetGenUtility.checkPreReq();
		if (prereq) {		  
          // Check to see that RuleBender uses the latest BioNetGen for the first run.  The effect of 
          // this check is to make sure that the PreferenceStore does not cause RuleBender to use an 
          // older version. After this, the user can reset the location of BioNetGen. 
          if (getUpgradeCheck() != null) { 
          if (getUpgradeCheck().equals(BNGPathFromRoot)) { 
  		    String     bngPath2  = PreferencesClerk.getFullUserBNGPath();		 
		    boolean bng2 = validateBNGPath(bngPath2);
		    if (bng2) { 
  		      bngPath2  = PreferencesClerk.getUserBNGPath();		 
			  String mm = PickWorkspaceDialog.setLastSetBioNetGenDirectory(PreferencesClerk.getUserBNGPath());
	          Activator.getDefault().getPreferenceStore().setValue("SIM_PATH",PreferencesClerk.getUserBNGPath());
	          return bngPath2; 
	        }
          }
          }
	      
	  	  String     bngPath   = PreferencesClerk.getFullDefaultBNGPath();
	      boolean bng  = validateBNGPath(bngPath);
	      if (bng) { 
  	  	    bngPath = PreferencesClerk.getDefaultBNGPath();
			String mm = PickWorkspaceDialog.setLastSetBioNetGenDirectory(PreferencesClerk.getDefaultBNGPath());
            Activator.getDefault().getPreferenceStore().setValue("SIM_PATH",PreferencesClerk.getDefaultBNGPath());
            mm = setUpgradeCheck(BNGPathFromRoot);
            return bngPath; 
          }
		}

		return "No_Valid_Path_";  //  This is not a good way to handle the situation, but it's
		               //  better than what we had before.
	}
	public static String getFullBNGPath() {
		return  getBNGPath() + System.getProperty("file.separator") + BNGName;
	}

	
	private static boolean validateBNGPath(String path) {
		if ((new File(path)).exists()) {
			return true;
		}
		return false;
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
	public static String getBioNetGen() {
		return PickWorkspaceDialog.getLastSetBioNetGenDirectory();
	}
}
