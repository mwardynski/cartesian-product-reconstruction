package at.ac.unileoben.mat.dissertation.linearfactorization.label;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.FactorizationStepService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 2/1/14
 * Time: 7:44 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class LabelUtils
{
  @Autowired
  Graph graph;

  @Autowired
  FactorizationStepService factorizationStepService;

  @Autowired
  ColoringService coloringService;

  public EdgesRef getEdgesRef(int[] colorsCounter)
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
    coloringService.setColorsOrderAndAmount(edgesRef, colorsCounter);
    return edgesRef;
  }

  public List<Edge> sortEdgesAccordingToLabels(List<Edge> edgesToSort, GraphColoring graphColoring)
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

  public void singleFindPivotSquarePhase(PivotSquareFinderStrategy pivotSquareFinderStrategy, FactorizationStep thisPhase, FactorizationStep nextPhase)
  {
    for (Vertex x : thisPhase.getVerticesInLayer())
    {
      if (x == null)
      {
        continue;
      }
      AdjacencyVector xAdjacencyVector = new AdjacencyVector(graph.getVertices().size(), x);
      findPivotSquareForReferenceVertex(pivotSquareFinderStrategy, x, xAdjacencyVector, thisPhase, nextPhase);
    }
  }

  public void findPivotSquareForReferenceVertex(PivotSquareFinderStrategy pivotSquareFinderStrategy, Vertex x, AdjacencyVector xAdjacencyVector, FactorizationStep thisPhase, FactorizationStep nextPhase)
  {
    List<Vertex> assignedVertices = factorizationStepService.getAssignedVertices(thisPhase, x);
    Iterator<Vertex> assignedVerticesIterator = assignedVertices.iterator();
    while (assignedVerticesIterator.hasNext())
    {
      Vertex u = assignedVerticesIterator.next();
      pivotSquareFinderStrategy.findPivotSquare(u, xAdjacencyVector, nextPhase);
      if (nextPhase != null)
      {
        findPivotSquareForReferenceVertex(pivotSquareFinderStrategy, x, xAdjacencyVector, nextPhase, null);
      }
      assignedVerticesIterator.remove();
    }
  }
}
