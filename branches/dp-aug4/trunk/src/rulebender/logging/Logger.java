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
    System.out.println("[" + level +" | " +className.getName() + "]: " + message);
  }
}
