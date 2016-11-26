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
  private int color;
  private int name;

  public Label(int color, int name)
  {
    this.color = color;
    this.name = name;
  }

  public int getName()
  {
    return name;
  }

  public void setName(int name)
  {
    this.name = name;
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
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    Label label = (Label) o;

    if (color != label.color)
    {
      return false;
    }
    if (name != label.name)
    {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode()
  {
    int result = name;
    result = 31 * result + color;
    return result;
  }

  @Override
  public String toString()
  {
    return String.format("c:%d, n:%d", color, name);
  }
}
