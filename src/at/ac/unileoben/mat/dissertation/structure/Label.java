package at.ac.unileoben.mat.dissertation.structure;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-30
 * Time: 11:34
 * To change this template use File | Settings | File Templates.
 */
public class Label
{
  private int name;
  private int color;

  public Label(int name, int color)
  {
    this.name = name;
    this.color = color;
  }

  public int getName()
  {
    return name;
  }

  public int getColor()
  {
    return color;
  }

  public void setColor(int color)
  {
    this.color = color;
  }

  @Override
  public String toString()
  {
    return String.format("n:%d, c:%d", name, color);
  }
}
