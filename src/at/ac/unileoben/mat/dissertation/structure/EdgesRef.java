package at.ac.unileoben.mat.dissertation.structure;

import java.util.ArrayList;
import java.util.Arrays;
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

  public int getColorsAmount()
  {
    return colorsAmount;
  }

  public int getAllColorsAmount()
  {
    return colorPositions.size();
  }

  public void setColorAmounts(int... colorAmounts)
  {
    if (colorAmounts.length == 0)
    {
      System.out.println("Empty colorAmounts");
      return;
    }
    colorPositions = new ArrayList<ColorGroupLocation>(colorAmounts.length);

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

  public void setColorsOrderAndAmount(int[] colorAmounts)
  {
    ColorGroupLocation[] colorPositionsArray = new ColorGroupLocation[colorAmounts.length];
    int actualIndex = 0;
    for (int i = 0; i < colorAmounts.length; i++)
    {
      if (colorAmounts[i] > 0)
      {
        ColorGroupLocation colorGroupLocation = new ColorGroupLocation(actualIndex, colorAmounts[i]);
        actualIndex += colorAmounts[i];
        colorPositionsArray[i] = colorGroupLocation;
      }
    }
    colorPositions = Arrays.asList(colorPositionsArray);
  }

  public int getPositionForLabel(Label label)
  {
    ColorGroupLocation colorGroupLocation = colorPositions.get(label.getColor());
    if (colorGroupLocation == null || colorGroupLocation.getLength() <= label.getName())
    {
      return -1;
    }
    return colorGroupLocation.getIndex() + label.getName();
  }

  public List<Integer> getPositionsForColor(int color)
  {
    ColorGroupLocation colorGroupLocation = colorPositions.get(color);
    if (colorGroupLocation == null)
    {
      return new ArrayList<Integer>();
    }
    List<Integer> positionsForColor = new ArrayList<Integer>(colorGroupLocation.getLength());
    for (int i = 0; i < colorGroupLocation.getLength(); i++)
    {
      positionsForColor.add(colorGroupLocation.getIndex() + i);
    }
    return positionsForColor;
  }

  @Override
  public String toString()
  {
    return colorPositions.toString();
  }
}
