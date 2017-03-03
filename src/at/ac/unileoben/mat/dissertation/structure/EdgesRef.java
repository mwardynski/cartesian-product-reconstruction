package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-30
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public class EdgesRef
{
  List<ColorGroupLocation> colorPositions;
  int colorsAmount;

  public EdgesRef(int colorsAmount)
  {
    this.colorsAmount = colorsAmount;
  }

  public List<ColorGroupLocation> getColorPositions()
  {
    return colorPositions;
  }

  public void setColorPositions(List<ColorGroupLocation> colorPositions)
  {
    this.colorPositions = colorPositions;
  }

  @Override
  public String toString()
  {
    return colorPositions.toString();
  }
}
