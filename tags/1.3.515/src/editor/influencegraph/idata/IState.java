package editor.influencegraph.idata;

import java.awt.Color;

public class IState
{
  private String name;

  int x;
  int y;
  int width;
  int height;
  int textx;
  int texty;
  Color color = null;


  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }
}