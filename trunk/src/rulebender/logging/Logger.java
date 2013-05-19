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
  
    /**
     * Just writing formatted logs to the console for now. Should update
     * with writing to a log file later.   
     * 
     * @param level
     * @param className
     * @param message
     */
  public static void log(LOG_LEVELS level, Class className, String message)
  {
//    if (level == some threshold value set somewhere )
    {
      String[] split = className.getName().split("\\.");
      
      System.out.println("[" + level +" | ..." + split[split.length-2] + "." + 
                      split[split.length - 1]+ "]: " + message);
    }
  }
}