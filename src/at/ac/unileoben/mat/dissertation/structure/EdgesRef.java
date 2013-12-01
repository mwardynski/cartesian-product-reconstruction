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

  public void setColorsOrderAndAmount(List<Integer> colorsOrder, int[] colorAmounts)
  {
    ColorGroupLocation[] colorPositionsArray = new ColorGroupLocation[colorAmounts.length];
    int actualIndex = 0;
    for (Integer color : colorsOrder)
    {
      ColorGroupLocation colorGroupLocation = new ColorGroupLocation(actualIndex, colorAmounts[color]);
      actualIndex += colorAmounts[color];
      colorPositionsArray[color] = colorGroupLocation;
    }
    colorPositions = Arrays.asList(colorPositionsArray);
  }

  public int getPositionForLabel(Label label)
  {
    ColorGroupLocation colorGroupLocation = colorPositions.get(label.getColor());
    if (colorGroupLocation.getLength() < label.getName())
    {
      System.err.println(String.format("no entry in ColorGroupLocation for given label(c:%d,n:%d) - the size of color: %d ",
              label.getColor(), label.getName(), colorGroupLocation.getLength()));
      return -1;
    }
    return colorGroupLocation.getIndex() + label.getName();
  }

  @Override
  public String toString()
  {
    return colorPositions.toString();
  }
}
