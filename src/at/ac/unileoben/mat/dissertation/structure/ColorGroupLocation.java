package at.ac.unileoben.mat.dissertation.structure;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-30
 * Time: 12:59
 * To change this template use File | Settings | File Templates.
 */
public class ColorGroupLocation
{
  private int index;
  private int length;

  public ColorGroupLocation(int index, int length)
  {
    this.index = index;
    this.length = length;
  }

  public int getIndex()
  {
    return index;
  }

  public int getLength()
  {
    return length;
  }

  @Override
  public String toString()
  {
    return String.format("[%d %d]", index, length);
  }
}
