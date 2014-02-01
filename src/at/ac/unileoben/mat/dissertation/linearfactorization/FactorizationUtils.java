package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.EdgesRef;
import at.ac.unileoben.mat.dissertation.structure.GraphColoring;
import at.ac.unileoben.mat.dissertation.structure.Label;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 2/1/14
 * Time: 7:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class FactorizationUtils
{
  public static final EdgesRef getEdgesRef(int[] colorsCounter)
  {
    int colorsAmount = 0;
    for (int singleColorCounter : colorsCounter)
    {
      if (singleColorCounter != 0)
      {
        colorsAmount++;
      }
    }
    EdgesRef edgesRef = new EdgesRef(colorsAmount);
    edgesRef.setColorsOrderAndAmount(colorsCounter);
    return edgesRef;
  }

  public static final List<Edge> sortEdgesAccordingToLabels(List<Edge> edgesToSort, GraphColoring graphColoring)
  {
    List<Edge> sortedEdges = new ArrayList<Edge>(edgesToSort.size());
    List<Edge>[] colorOccurrence = (List<Edge>[]) new LinkedList<?>[graphColoring.getOriginalColorsAmount()];
    for (Edge edge : edgesToSort)
    {
      Label label = edge.getLabel();
      int color = label.getColor();
      if (colorOccurrence[color] == null)
      {
        colorOccurrence[color] = new LinkedList<Edge>();
      }
      colorOccurrence[color].add(edge);
    }
    for (List<Edge> edges : colorOccurrence)
    {
      if (edges != null)
      {
        sortedEdges.addAll(edges);
      }
    }
    return sortedEdges;
  }
}
