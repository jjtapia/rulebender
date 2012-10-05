package rulebender.logging;

public class Logger
{
  public static enum LOG_LEVELS
    {
      INFO,
      WARNING,
      ERROR,
      SEVERE
    };
  
  public static void log(LOG_LEVELS level, Class className, String message)
  {
    String[] split = className.getName().split("\\.");
    
    System.out.println("[" + level +" | ..." + split[split.length-2] + "." + 
                       split[split.length - 1]+ "]: " + message);
  }
}
