package at.ac.unileoben.mat.dissertation.linearfactorization.services;

import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 18.10.15
 * Time: 18:47
 * To change this template use File | Settings | File Templates.
 */
@Component
public class ColoringService
{
  @Autowired
  Graph graph;

  public boolean mergeColors(GraphColoring graphColoring, List<Integer> colors)
  {
    int minColor = Integer.MAX_VALUE;
    List<Integer> colorsMapping = graphColoring.getColorsMapping();
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
        graphColoring.getActualColors().remove(colorsMapping.get(color));
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

  public int getCurrentColorMapping(GraphColoring graphColoring, int colorKey)
  {
    return graphColoring.getColorsMapping().get(colorKey);
  }

  public void setColorAmounts(EdgesRef edgesRef, int... colorAmounts)
  {
    if (colorAmounts.length == 0)
    {
      System.out.println("Empty colorAmounts");
      return;
    }

    ArrayList<ColorGroupLocation> colorPositions = new ArrayList<>(colorAmounts.length);

    ColorGroupLocation firstColorGroupLocation = new ColorGroupLocation(0, colorAmounts[0]);
    colorPositions.add(firstColorGroupLocation);
    int lastColorEnd = colorAmounts[0];

    for (int i = 1; i < colorAmounts.length; i++)
    {
      ColorGroupLocation colorGroupLocation = new ColorGroupLocation(lastColorEnd, colorAmounts[i]);
      colorPositions.add(colorGroupLocation);
      lastColorEnd += colorAmounts[i];
    }
    edgesRef.setColorPositions(colorPositions);
  }

  public void setColorsOrderAndAmount(EdgesRef edgesRef, int[] colorAmounts)
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
    edgesRef.setColorPositions(Arrays.asList(colorPositionsArray));
  }

  public List<Integer> getPositionsForColor(EdgesRef edgesRef, int color)
  {
    ColorGroupLocation colorGroupLocation = edgesRef.getColorPositions().get(color);
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

  public int getPositionForLabel(EdgesRef edgesRef, Label label)
  {
    ColorGroupLocation colorGroupLocation = edgesRef.getColorPositions().get(label.getColor());
    if (colorGroupLocation == null || colorGroupLocation.getLength() <= label.getName())
    {
      return -1;
    }
    return colorGroupLocation.getIndex() + label.getName();
  }

  public List<Integer> getColorsForEdges(GraphColoring graphColoring, List<Edge> edges)
  {
    boolean[] colorPresence = new boolean[graphColoring.getOriginalColorsAmount()];
    for (Edge e : edges)
    {
      Label label = e.getLabel();
      if (label != null)
      {
        int currentLabelColor = getCurrentColorMapping(graphColoring, label.getColor());
        colorPresence[currentLabelColor] = true;
      }
    }
    List<Integer> colors = new ArrayList<Integer>(colorPresence.length);
    for (int i = 0; i < colorPresence.length; i++)
    {
      if (colorPresence[i])
      {
        colors.add(i);
      }
    }
    return colors;
  }

  public boolean mergeColorsForEdges(List<Edge> edges, MergeTagEnum mergeTag)
  {
    GraphColoring graphColoring = graph.getGraphColoring();
    List<Integer> colorsToMerge = getColorsForEdges(graphColoring, edges);
    boolean colorsMerged = false;
    if (colorsToMerge.size() > 0)
    {
      colorsMerged = mergeColors(graphColoring, colorsToMerge);
    }
    if (colorsMerged)
    {
      graph.getAnalyzeData().addMergeOperation(graphColoring.getActualColors().size(), edges, mergeTag);
    }
    return colorsMerged;
  }
}
