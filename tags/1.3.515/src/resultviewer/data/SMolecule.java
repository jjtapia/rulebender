package resultviewer.data;

import java.util.ArrayList;

public class SMolecule
{
  private int id; // molecule id
  private String name; // molecule name
  private ArrayList<SComponent> components; // list of components inside the


  // molecule

  /**
   * 
   * @param id
   *          molecule id
   * @param name
   *          molecule name
   */
  public SMolecule(int id, String name)
  {
    this.id = id;
    this.name = name;
    this.components = new ArrayList<SComponent>();
  }


  /**
   * 
   * @param id
   *          molecule id
   * @param name
   *          molecule name
   * @param components
   *          list of components inside the molecule
   */
  public SMolecule(int id, String name, ArrayList<SComponent> components)
  {
    this.id = id;
    this.name = name;
    this.components = components;
  }


  /**
   * 
   * @return molecule id
   */
  public int getId()
  {
    return id;
  }


  /**
   * 
   * @return molecule name
   */
  public String getName()
  {
    return name;
  }


  /**
   * 
   * @return list of components inside the molecule
   */
  public ArrayList<SComponent> getComponents()
  {
    return components;
  }


  /**
   * 
   * @param components
   *          list of components inside the molecule
   */
  public void setComponents(ArrayList<SComponent> components)
  {
    this.components = components;
  }


  /**
   * Add a component to the list of components inside the molecule
   * 
   * @param c
   *          a component
   */
  public void addComponent(SComponent c)
  {
    this.components.add(c);
  }

}
