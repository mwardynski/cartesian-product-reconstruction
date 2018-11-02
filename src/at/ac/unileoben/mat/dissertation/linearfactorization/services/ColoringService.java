package at.ac.unileoben.mat.dissertation.linearfactorization.services;

import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 20.12.15
 * Time: 19:36
 * To change this template use File | Settings | File Templates.
 */
public interface ColoringService
{
  boolean mergeColors(GraphColoring graphColoring, List<Integer> colors);

  int getCurrentColorMapping(GraphColoring graphColoring, int colorKey);

  void setColorAmounts(EdgesRef edgesRef, int... colorAmounts);

  void setColorsOrderAndAmount(EdgesRef edgesRef, int[] colorAmounts);

  List<Integer> getPositionsForColor(EdgesRef edgesRef, int color);

  int getPositionForLabel(EdgesRef edgesRef, Label label);

  List<Integer> getColorsForEdges(GraphColoring graphColoring, List<Edge> edges);

  boolean mergeColorsForEdges(List<Edge> edges, MergeTagEnum mergeTag);

  boolean mergeColorsForEdges(List<Edge> edges, MergeOperation mergeOperation);
}
