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
    colorsMapping = new ArrayList<>(originalColorsAmount);
    actualColors = new LinkedList<>();
    for (int i = 0; i < originalColorsAmount; i++)
    {
      colorsMapping.add(i);
      actualColors.add(i);
    }
  }

  public GraphColoring(GraphColoring graphColoring)
  {
    this.originalColorsAmount = graphColoring.getOriginalColorsAmount();
    this.colorsMapping = new ArrayList<>(graphColoring.getColorsMapping());
    this.actualColors = new LinkedList<>(graphColoring.getActualColors());
  }

  public int getOriginalColorsAmount()
  {
    return originalColorsAmount;
  }

  public List<Integer> getColorsMapping()
  {
    return colorsMapping;
  }

  public List<Integer> getActualColors()
  {
    return actualColors;
  }

  @Override
  public String toString()
  {
    return String.format("%s%s", colorsMapping, actualColors);
  }
}
