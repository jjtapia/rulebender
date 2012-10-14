package editor.influencegraph.idata;

import java.util.ArrayList;

public class IPotentialBond
{
  private String name;
  private ArrayList<ISite> sites = new ArrayList<ISite>();


  public IPotentialBond(String in1, ISite in2)
  {
    setName(in1);
    getSites().add(in2);
  }


  public void setSites(ArrayList<ISite> sites)
  {
    this.sites = sites;
  }


  public ArrayList<ISite> getSites()
  {
    return sites;
  }


  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }
}