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
    if (level == LOG_LEVELS.WARNING 
        || level == LOG_LEVELS.ERROR 
        || level == LOG_LEVELS.SEVERE
        )
    {
      String[] split = className.getName().split("\\.");
      
      System.out.println("[" + level +" | ..." + split[split.length-2] + "." + 
                      split[split.length - 1]+ "]: " + message);
    }
  }
}