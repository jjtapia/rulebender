package editor.influencegraph.idata;

import java.awt.Color;
import java.util.ArrayList;

public class IComponent
{
  private String name;
  private ArrayList<IState> states = new ArrayList<IState>();

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


  public void setStates(ArrayList<IState> states)
  {
    this.states = states;
  }


  public ArrayList<IState> getStates()
  {
    return states;
  }
}