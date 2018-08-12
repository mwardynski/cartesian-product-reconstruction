package at.ac.unileoben.mat.dissertation.linearfactorization.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
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
public class ColoringServiceImpl implements ColoringService
{
  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Override
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
        for (int i = colorsMapping.size() - 1; i >= 0; i--)
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
  public int getCurrentColorMapping(GraphColoring graphColoring, int colorKey)
  {
    int mappedColor = colorKey;
    if (graphColoring.getColorsMapping().size() > colorKey)
    {
      mappedColor = graphColoring.getColorsMapping().get(colorKey);
    }
    return mappedColor;
  }

  @Override
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

  @Override
  public void setColorsOrderAndAmount(EdgesRef edgesRef, int[] colorAmounts)
  {
    ColorGroupLocation[] colorPositionsArray = new ColorGroupLocation[colorAmounts.length];
    int actualIndex = 0;
    for (int i = 0; i < colorAmounts.length; i++)
    {
      ColorGroupLocation colorGroupLocation = new ColorGroupLocation(actualIndex, colorAmounts[i]);
      actualIndex += colorAmounts[i];
      colorPositionsArray[i] = colorGroupLocation;
    }
    edgesRef.setColorPositions(new ArrayList<>(Arrays.asList(colorPositionsArray)));
  }

  @Override
  public List<Integer> getPositionsForColor(EdgesRef edgesRef, int color)
  {
    ColorGroupLocation colorGroupLocation = edgesRef.getColorPositions().get(color);
    List<Integer> positionsForColor = new ArrayList<Integer>(colorGroupLocation.getLength());
    for (int i = 0; i < colorGroupLocation.getLength(); i++)
    {
      positionsForColor.add(colorGroupLocation.getIndex() + i);
    }
    return positionsForColor;
  }

  @Override
  public int getPositionForLabel(EdgesRef edgesRef, Label label)
  {
    ColorGroupLocation colorGroupLocation = edgesRef.getColorPositions().get(label.getColor());
    if (colorGroupLocation.getLength() <= label.getName())
    {
      return -1;
    }
    return colorGroupLocation.getIndex() + label.getName();
  }

  @Override
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

  @Override
  public boolean mergeColorsForEdges(List<Edge> edges, MergeTagEnum mergeTag)
  {
    MergeOperation mergeOperation = new MergeOperation(edges, mergeTag);
    return mergeColorsForEdges(edges, mergeOperation);
  }

  @Override
  public boolean mergeColorsForEdges(List<Edge> edges, MergeOperation mergeOperation)
  {
    GraphColoring graphColoring = graph.getGraphColoring();
    GraphColoring graphColoringBackup = new GraphColoring(graph.getGraphColoring());
    List<Integer> colorsToMerge = getColorsForEdges(graphColoring, edges);
    boolean colorsMerged = false;
    if (colorsToMerge.size() > 0)
    {
      colorsMerged = mergeColors(graphColoring, colorsToMerge);
    }
    if (colorsMerged)
    {
      reconstructionData.getMergeOperations().add(mergeOperation);
      graph.getAnalyzeData().addMergeOperation(mergeOperation);
      mergeOperation.setMergedColors(colorsToMerge);
      mergeOperation.setMergeGraphColoring(graphColoringBackup);
    }
    return colorsMerged;
  }
}
