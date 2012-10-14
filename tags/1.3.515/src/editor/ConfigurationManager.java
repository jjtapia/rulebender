/**
 * ConfigurationManager.java
 * 
 * February 11, 2011
 * 
 * This class will handle any paths, options, or setup in general that is 
 * required.  
 */
package editor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ConfigurationManager
{
  private String bngFPath;
  private String bngFName;
  private String workspacePath;
  private int osType;

  private String slash = "";

  private static ConfigurationManager configMan;


  /**
   * Singleton
   */
  private ConfigurationManager()
  {
    bngFPath = null;
    bngFName = null;
    workspacePath = null;
  }


  public static synchronized ConfigurationManager getConfigurationManager()
  {
    if (configMan == null)
      configMan = new ConfigurationManager();

    return configMan;
  }


  public void initConfigOptions()
  {
    findOSType();
    readConfigFile();
  }


  public void findOSType()
  {
    String stemp = System.getProperty("os.name");
    if (stemp.contains("Windows") || stemp.contains("WINDOWS")
        || stemp.contains("windows"))
      setOSType(1);
    else if (stemp.contains("Mac") || stemp.contains("MAC")
        || stemp.contains("mac"))
      setOSType(2);
    else
      setOSType(3);

    // Windows
    if (getOSType() == 1)
    {
      slash = "\\";
    }
    // Not Windows
    else
    {
      slash = "/";
    }
  }


  /**
   * Reads the config file located in the user's home directory. This should
   * only be called once when the tool is being loaded because closing the
   * dialogue that lets the user choose a workspace will close the tool.
   */
  public void readConfigFile()
  {
    String userHome = System.getProperty("user.home");
    System.out.println("User Home: " + userHome);

    File configFile = new File(userHome + slash + ".rulebender");

    String possibleBNGFPath = null;
    String possibleBNGFName = null;

    // There is a config file
    if (configFile.exists())
    {
      try
      {
        BufferedReader br1 = new BufferedReader(new FileReader(configFile));
        // setBNGFPath(br1.readLine());
        // setBNGFName(br1.readLine());

        possibleBNGFPath = br1.readLine().trim();
        possibleBNGFName = br1.readLine().trim();
        setWorkspacePath(br1.readLine().trim());
      }

      catch (FileNotFoundException e1)
      {
      }
      catch (IOException e1)
      {
      }

      // Check to make sure that the workspace exists and ask them
      // to choose a new one if it does not.
      // We do not need to do this for bngpath because they will be asked
      // to define a new one if the file does not exists when they try to
      // run
      // a simulation.
      File workspace = new File(workspacePath);

      if (!workspace.exists())
      {
        BNGEditor.displayOutput("Please choose a valid workspace");
        (new SetWorkspaceDialogue(BNGEditor.getMainEditorShell(),
            BNGEditor.getEditor(), false)).show();
      }

      if (!possibleBNGFPath.equals("") && !possibleBNGFName.equals(""))
      {
        setBNGFPath(possibleBNGFPath);
        setBNGFName(possibleBNGFName);
      }
    }
    else
    {
      setDefaultBNGPath();
      (new SetWorkspaceDialogue(BNGEditor.getMainEditorShell(),
          BNGEditor.getEditor(), false)).show();
    }

    if (workspacePath == null || workspacePath == "")
    {
      workspacePath = System.getProperty("user.home") + slash
          + "RuleBenderWorkspace";
      File workspacePathCheck = new File(workspacePath);
      if (!workspacePathCheck.exists())
      {
        workspacePathCheck.mkdir();
      }
    }

    BNGEditor.displayOutput("Workspace set to " + workspacePath);
  }


  public void saveConfigFile()
  {
    String userHome = System.getProperty("user.home");

    File configFile = new File(userHome + slash + ".rulebender");

    if (configFile.exists())
      configFile.delete();

    PrintWriter pw = null;
    try
    {
      pw = new PrintWriter(configFile);
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }

    if (bngFPath != null)
      pw.write(bngFPath);

    pw.write("\n");

    if (bngFName != null)
      pw.write(bngFName);

    pw.write("\n");

    if (workspacePath != null)
      pw.write(workspacePath);

    pw.write("\n");

    pw.close();

    // windows
    if (osType == 1)
    {
      try
      {
        Runtime.getRuntime().exec("attrib +H " + userHome + "\\.rulebender");
      }
      catch (IOException e)
      {
        //
        e.printStackTrace();
      }
    }
  }


  public void setDefaultBNGPath()
  {
    String parentDir = System.getProperty("user.dir");
    String bngpath = "";

    // Mac
    if (getOSType() == 2)
    {
      String appDir = "";
      if (parentDir.indexOf("/Contents/MacOS") != -1)
      {
        appDir = parentDir.substring(0, parentDir.indexOf("/Contents/MacOS"));
        appDir = appDir.substring(0, appDir.lastIndexOf("/"));
        bngpath = appDir + slash + "BioNetGen-2.1.8r597";
      }
      else
      {
        bngpath = parentDir + slash + "BioNetGen-2.1.8r597";
      }

      /*
       * System.out.println("appDir: " + appDir); System.out.println("bngpath: "
       * + bngpath); System.out.println("parentDir: " + parentDir);
       */
    }
    // Not Mac
    else
    {
      bngpath = parentDir + slash + "BioNetGen-2.1.8r597";
    }

    String bngname = "BNG2.pl";

    File bngfile = new File(bngpath + slash + bngname);

    if (bngfile.exists())
    {
      setBNGFPath(bngpath);
      setBNGFName(bngname);
    }
  }


  // ------------------ Getters and Setters ------------------

  public void setBNGFPath(String bngFPath)
  {
    this.bngFPath = bngFPath;
    saveConfigFile();
  }


  public String getBNGFPath()
  {
    return bngFPath;
  }


  public void setWorkspacePath(String workSpacePath)
  {
    this.workspacePath = workSpacePath;
    saveConfigFile();
  }


  public String getWorkspacePath()
  {
    return workspacePath;
  }


  public void setOSType(int osType)
  {
    this.osType = osType;
  }


  public int getOSType()
  {
    return osType;
  }


  public void setBNGFName(String bngFName)
  {
    this.bngFName = bngFName;
    saveConfigFile();
  }


  public String getBNGFName()
  {
    return bngFName;
  }


  public String getSlash()
  {
    return slash;
  }
}
