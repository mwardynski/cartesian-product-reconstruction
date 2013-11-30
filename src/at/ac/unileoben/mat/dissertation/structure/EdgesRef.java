package at.ac.unileoben.mat.dissertation.structure;

import java.util.ArrayList;
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
    colorPositions = new ArrayList<ColorGroupLocation>(colorsAmount);
    this.colorsAmount = colorsAmount;
  }

  public void setColorAmounts(int... colorAmounts)
  {
    if (colorAmounts.length == 0)
    {
      System.out.println("Empty colorAmounts");
      return;
    }

    ColorGroupLocation firstColorGroupLocation = new ColorGroupLocation(0, colorAmounts[0]);
    colorPositions.add(firstColorGroupLocation);
    int lastColorEnd = colorAmounts[0];

    for (int i = 1; i < colorAmounts.length; i++)
    {
      ColorGroupLocation colorGroupLocation = new ColorGroupLocation(lastColorEnd, colorAmounts[i]);
      colorPositions.add(colorGroupLocation);
      lastColorEnd += colorAmounts[i];
    }
  }

  @Override
  public String toString()
  {
    return colorPositions.toString();
  }
}
