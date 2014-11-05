package at.ac.unileoben.mat.dissertation.structure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-30
 * Time: 13:46
 * To change this template use File | Settings | File Templates.
 */
public class GraphColoring
{
  private int originalColorsAmount;
  private List<Integer> colorsMapping;
  private List<Integer> actualColors;

  public GraphColoring(int colorsAmount)
  {
    originalColorsAmount = colorsAmount;
    colorsMapping = new ArrayList<Integer>(originalColorsAmount);
    actualColors = new LinkedList<Integer>();
    for (int i = 0; i < originalColorsAmount; i++)
    {
      colorsMapping.add(i);
      actualColors.add(i);
    }
  }

  public int getOriginalColorsAmount()
  {
    return originalColorsAmount;
  }

  public int getCurrentColorMapping(int colorKey)
  {
    return colorsMapping.get(colorKey);
  }

  public List<Integer> getActualColors()
  {
    return actualColors;
  }

  public boolean mergeColors(List<Integer> colors)
  {
    int minColor = Integer.MAX_VALUE;
    for (int color : colors)
    {
      if (colorsMapping.get(color) < minColor)
      {
        minColor = colorsMapping.get(color);
      }
    }
    boolean colorsMerged = false;
    for (int color : colors)
    {
      if (colorsMapping.get(color) != minColor)
      {
        colorsMerged = true;
        actualColors.remove(colorsMapping.get(color));
        for (int i = 0; i < colorsMapping.size(); i++)
        {
          if (colorsMapping.get(i) == colorsMapping.get(color))
          {
            colorsMapping.set(i, minColor);
          }
        }
      }
    }
    return colorsMerged;
  }

  @Override
  public String toString()
  {
    return String.format("%s%s", colorsMapping, actualColors);
  }
}
