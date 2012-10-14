package org.bng.simulate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import editor.BNGEditor;

public class StreamDisplayThread extends Thread
{
  private InputStream m_stream;
  private boolean m_printNow;
  private String m_log = "";
  // private String m_name;
  private int pid = -1;


  public StreamDisplayThread(String name, InputStream stream, boolean printNow)
  {
    m_stream = stream;
    m_printNow = printNow;
    // m_name = name;
  }


  public void run()
  {
    String line = "";

    BufferedReader buffer = new BufferedReader(new InputStreamReader(m_stream));

    try
    {
      while ((line = buffer.readLine()) != null)
      {
        if (m_printNow)
          BNGEditor.displayOutput(line);

        if (line.startsWith("[simulation PID is:"))
        {
          String[] split = line.split("\\s+");
          System.out.println("split[3]: " + split[3]);
          pid = Integer.parseInt(split[3].substring(0, split[3].indexOf("]"))
              .trim());
        }
        m_log += line + BNGEditor.getConsoleLineDelimeter();
      }
    }
    catch (IOException e)
    {
      // This happens if the simulation/scan is cancelled.
      // e.printStackTrace();
    }
  }


  public String getLog()
  {
    return m_log;
  }


  public int getPID()
  {
    return pid;
  }
}
