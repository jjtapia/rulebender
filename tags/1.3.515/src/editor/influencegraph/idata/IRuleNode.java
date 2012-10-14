package editor.influencegraph.idata;

public class IRuleNode
{
  int index;
  private int iruleindex;
  private String label; // label of the rule, could be empty
  private String name; // expression of rule, including rates
  private boolean forward;
  int[] position = new int[4];// xmin, xmax, ymin, ymax


  public IRuleNode(int in1, int in2, boolean in3, String rulename,
      String rulelabel)
  {
    index = in1;
    setIruleindex(in2);
    setForward(in3);
    setName(rulename);
    setLabel(rulelabel);
  }


  public void setForward(boolean forward)
  {
    this.forward = forward;
  }


  public boolean isForward()
  {
    return forward;
  }


  public void setIruleindex(int iruleindex)
  {
    this.iruleindex = iruleindex;
  }


  public int getIruleindex()
  {
    return iruleindex;
  }


  public void setName(String name)
  {
    this.name = name;
  }


  public String getName()
  {
    return name;
  }


  public String getLabel()
  {
    return label;
  }


  public void setLabel(String label)
  {
    if (label != null)
      this.label = label;
    else
      this.label = "";
  }
}
