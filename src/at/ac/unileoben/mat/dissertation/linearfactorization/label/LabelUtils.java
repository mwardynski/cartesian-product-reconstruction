package at.ac.unileoben.mat.dissertation.linearfactorization.label;

import at.ac.unileoben.mat.dissertation.common.impl.GraphReaderImpl;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.LayerLabelingData;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.FactorizationStepService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
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

  public void sortEdgesAccordingToLabels(EdgesGroup edgesGroup, GraphColoring graphColoring)
  {
    List<Edge>[] edgesGroupedByColor = groupEdgesByColor(graphColoring, edgesGroup.getEdges());
    labelGroupedByColorEdges(edgesGroup, edgesGroupedByColor);
  }

  private List<Edge>[] groupEdgesByColor(GraphColoring graphColoring, List<Edge> edgesToSort)
  {
    List<Edge>[] colorOccurrence = (List<Edge>[]) new LinkedList<?>[graphColoring.getOriginalColorsAmount()];
    for (Edge edge : edgesToSort)
    {
      Label label = edge.getLabel();
      int color = label.getColor();
      if (colorOccurrence[color] == null)
      {
        colorOccurrence[color] = new LinkedList<>();
      }
      colorOccurrence[color].add(edge);
    }
    return colorOccurrence;
  }

  private void labelGroupedByColorEdges(EdgesGroup edgesGroup, List<Edge>[] edgesGroupedByColor)
  {
    List<Edge> sortedEdges = new ArrayList<>(edgesGroup.getEdges().size());
    int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    for (List<Edge> edges : edgesGroupedByColor)
    {
      if (CollectionUtils.isNotEmpty(edges))
      {
        colorsCounter[edges.get(0).getLabel().getColor()] = edges.size();

        List<Edge> edgesToLabel = new LinkedList<>();
        boolean[] labelsInUse = new boolean[GraphReaderImpl.MAX_NEIGHBOURS_AMOUNT];
        for (Edge edge : edges)
        {
          int edgeName = edge.getLabel().getName();
          if (edgeName == -1)
          {
            edgesToLabel.add(edge);
          }
          else
          {
            if (labelsInUse[edgeName])
            {
              edgesToLabel.add(edge);
            }
            else
            {
              labelsInUse[edgeName] = true;
            }
          }
          sortedEdges.add(edge);
        }

        Iterator<Edge> edgesToLabelIterator = edgesToLabel.iterator();
        for (int i = 0; i < labelsInUse.length; i++)
        {
          if (!labelsInUse[i] && edgesToLabelIterator.hasNext())
          {
            Edge edgeToLabel = edgesToLabelIterator.next();
            edgeToLabel.getLabel().setName(i);
            if (!edgesToLabelIterator.hasNext())
            {
              break;
            }
          }
        }

      }
    }
    EdgesRef downEdgesRef = getEdgesRef(colorsCounter);
    edgesGroup.setEdgesRef(downEdgesRef);
    edgesGroup.setEdges(sortedEdges);
  }

  public void singleFindPivotSquarePhase(PivotSquareFinderStrategy pivotSquareFinderStrategy, FactorizationStep thisPhase, FactorizationStep nextPhase, LayerLabelingData layerLabelingData)
  {
    for (Vertex x : thisPhase.getVerticesInLayer())
    {
      if (x == null || CollectionUtils.isEmpty(factorizationStepService.getAssignedVertices(thisPhase, x)))
      {
        continue;
      }
      AdjacencyVector xAdjacencyVector = new AdjacencyVector(graph.getVertices().size(), x);
      findPivotSquareForReferenceVertex(pivotSquareFinderStrategy, x, xAdjacencyVector, thisPhase, nextPhase, layerLabelingData);
    }
  }

  public void findPivotSquareForReferenceVertex(PivotSquareFinderStrategy pivotSquareFinderStrategy, Vertex x, AdjacencyVector xAdjacencyVector, FactorizationStep thisPhase, FactorizationStep nextPhase, LayerLabelingData layerLabelingData)
  {
    List<Vertex> assignedVertices = factorizationStepService.getAssignedVertices(thisPhase, x);
    Iterator<Vertex> assignedVerticesIterator = assignedVertices.iterator();
    while (assignedVerticesIterator.hasNext())
    {
      Vertex u = assignedVerticesIterator.next();
      pivotSquareFinderStrategy.findPivotSquare(u, xAdjacencyVector, thisPhase, nextPhase, layerLabelingData);
      if (nextPhase != null)
      {
        findPivotSquareForReferenceVertex(pivotSquareFinderStrategy, x, xAdjacencyVector, nextPhase, null, layerLabelingData);
      }
      assignedVerticesIterator.remove();
    }
  }
}
