package editor.influencegraph.idata;

import java.awt.Color;
import java.util.ArrayList;

public class IMolecule
{
  private String name;
  private ArrayList<IComponent> components = new ArrayList<IComponent>();

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


  public void setComponents(ArrayList<IComponent> components)
  {
    this.components = components;
  }


  public ArrayList<IComponent> getComponents()
  {
    return components;
  }
}