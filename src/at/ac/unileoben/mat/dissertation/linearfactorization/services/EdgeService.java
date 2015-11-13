package at.ac.unileoben.mat.dissertation.linearfactorization.services;

import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 18.10.15
 * Time: 18:35
 * To change this template use File | Settings | File Templates.
 */
@Component
public class EdgeService
{
  @Autowired
  ColoringService coloringService;

  public Edge getEdgeByLabel(Vertex v, Label label, EdgeType edgeType)
  {
    EdgesGroup edgeGroup = getEdgeGroupForEdgeType(v, edgeType);

    List<Edge> edges = edgeGroup.getEdges();
    int positionForLabel = coloringService.getPositionForLabel(edgeGroup.getEdgesRef(), label);
    if (positionForLabel != -1)
    {
      return edges.get(positionForLabel);
    }
    else
    {
      return null;
    }
  }

  public Edge getEdgeOfDifferentColor(Vertex v, int color, GraphColoring graphColoring)
  {
    EdgesRef edgesRef = v.getDownEdges().getEdgesRef();
    List<Edge> edges = v.getDownEdges().getEdges();
    for (int i = 0; i < edgesRef.getColorPositions().size(); i++)
    {
      if (coloringService.getPositionsForColor(edgesRef, i).isEmpty())//FIXME optimize it!!!
      {
        continue;
      }

      if (coloringService.getCurrentColorMapping(graphColoring, i) != coloringService.getCurrentColorMapping(graphColoring, color))
      {
        int positionForLabel = coloringService.getPositionForLabel(edgesRef, new Label(0, i));
        if (positionForLabel != -1)
        {
          return edges.get(positionForLabel);
        }
      }
    }
    return null;
  }

  public List<List<Edge>> getAllEdgesOfDifferentColor(Vertex v, int color, GraphColoring graphColoring, EdgeType edgeType)
  {
    EdgesGroup edgeGroup = getEdgeGroupForEdgeType(v, edgeType);
    EdgesRef edgesRef = edgeGroup.getEdgesRef();
    List<Edge> allDownEdges = edgeGroup.getEdges();

    int originalColorsAmount = graphColoring.getOriginalColorsAmount();
    List<List<Edge>> resultEdges = new ArrayList<List<Edge>>();
    for (int i = 0; i < originalColorsAmount; i++)
    {
      resultEdges.add(new LinkedList<Edge>());
    }

    for (int i = 0; i < edgesRef.getColorPositions().size(); i++)
    {
      if (coloringService.getCurrentColorMapping(graphColoring, i) != coloringService.getCurrentColorMapping(graphColoring, color))
      {
        List<Integer> positionsForColor = coloringService.getPositionsForColor(edgesRef, i);
        for (int edgePosition : positionsForColor)
        {
          Edge edge = allDownEdges.get(edgePosition);
          resultEdges.get(edge.getLabel().getColor()).add(edge);
        }
      }
    }
    return resultEdges;
  }

  public List<Edge> getAllEdgesOfColors(Vertex v, List<Integer> colors, EdgeType edgeType)
  {
    List<Edge> edgesOfGivenColors = new LinkedList<Edge>();
    EdgesGroup edgeGroup = getEdgeGroupForEdgeType(v, edgeType);
    EdgesRef edgesRef = edgeGroup.getEdgesRef();
    List<Edge> allEdges = edgeGroup.getEdges();
    for (Integer givenColor : colors)
    {
      List<Integer> positionsForColor = coloringService.getPositionsForColor(edgesRef, givenColor);
      for (int edgePosition : positionsForColor)
      {
        edgesOfGivenColors.add(allEdges.get(edgePosition));
      }
    }
    return edgesOfGivenColors;
  }

  private EdgesGroup getEdgeGroupForEdgeType(Vertex v, EdgeType edgeType)
  {
    if (edgeType == EdgeType.DOWN)
    {
      return v.getDownEdges();
    }
    else if (edgeType == EdgeType.CROSS)
    {
      return v.getCrossEdges();
    }
    else
    {
      return v.getUpEdges();
    }
  }
}
