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
  private int actualColorsAmount;
  private int originalColorsAmount;
  private List<Integer> colorsMapping;
  private List<Integer> actualColors;

  public GraphColoring(int colorsAmount)
  {
    originalColorsAmount = actualColorsAmount = colorsAmount;
    colorsMapping = new ArrayList<Integer>(actualColorsAmount);
    actualColors = new LinkedList<Integer>();
    for (int i = 0; i < actualColorsAmount; i++)
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

  public void mergeColors(List<Integer> colors)
  {
    int minColor = Integer.MAX_VALUE;
    for (int color : colors)
    {
      if (colorsMapping.get(color) < minColor)
      {
        minColor = colorsMapping.get(color);
      }
    }
    for (int color : colors)
    {
      if (colorsMapping.get(color) != minColor)
      {
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
  }

  @Override
  public String toString()
  {
    return String.format("(%d)%s%s", actualColorsAmount, colorsMapping, actualColors);
  }
}
