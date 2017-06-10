package at.ac.unileoben.mat.dissertation.linearfactorization.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
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
public class EdgeServiceImpl implements EdgeService
{
  @Autowired
  ColoringService coloringService;

  @Override
  public void addLabel(Edge edge, int color, int name, Edge squareMatchingEdge, LabelOperationDetail labelOperationDetail)
  {
    Label label = new Label(color, name);
    edge.setLabel(label);
    edge.setSquareMatchingEdge(squareMatchingEdge);
  }

  @Override
  public Edge getFirstEdge(Vertex v, EdgeType edgeType)
  {
    EdgesGroup edgeGroup = getEdgeGroupForEdgeType(v, edgeType);
    return edgeGroup.getEdges().get(0);
  }

  @Override
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

  @Override
  public Edge getEdgeOfDifferentColor(Vertex v, int color, GraphColoring graphColoring)
  {
    EdgesRef edgesRef = v.getDownEdges().getEdgesRef();
    List<Edge> edges = v.getDownEdges().getEdges();
    for (int i = 0; i < edgesRef.getColorPositions().size(); i++)
    {
      if (coloringService.getPositionsForColor(edgesRef, i).isEmpty())
      {
        continue;
      }

      if (coloringService.getCurrentColorMapping(graphColoring, i) != coloringService.getCurrentColorMapping(graphColoring, color))
      {
        int positionForLabel = coloringService.getPositionForLabel(edgesRef, new Label(i, 0));
        if (positionForLabel != -1)
        {
          return edges.get(positionForLabel);
        }
      }
    }
    return null;
  }

  @Override
  public List<List<Edge>> getAllEdgesOfDifferentColor(Vertex v, int color, GraphColoring graphColoring, EdgeType edgeType)
  {
    EdgesGroup edgeGroup = getEdgeGroupForEdgeType(v, edgeType);
    EdgesRef edgesRef = edgeGroup.getEdgesRef();
    List<Edge> allGroupEdges = edgeGroup.getEdges();

    int originalColorsAmount = graphColoring.getOriginalColorsAmount();
    List<List<Edge>> resultEdges = new ArrayList<List<Edge>>();
    for (int i = 0; i < originalColorsAmount; i++)
    {
      resultEdges.add(new LinkedList<>());
    }

    if (CollectionUtils.isNotEmpty(allGroupEdges))
    {
      for (int i = 0; i < edgesRef.getColorPositions().size(); i++)
      {
        if (coloringService.getCurrentColorMapping(graphColoring, i) != coloringService.getCurrentColorMapping(graphColoring, color))
        {
          List<Integer> positionsForColor = coloringService.getPositionsForColor(edgesRef, i);
          for (int edgePosition : positionsForColor)
          {
            Edge edge = allGroupEdges.get(edgePosition);
            resultEdges.get(edge.getLabel().getColor()).add(edge);
          }
        }
      }
    }
    return resultEdges;
  }

  @Override
  public List<Edge> getAllEdgesOfColors(Vertex v, List<Integer> colors, EdgeType edgeType)
  {
    List<Edge> edgesOfGivenColors = new LinkedList<Edge>();
    EdgesGroup edgeGroup = getEdgeGroupForEdgeType(v, edgeType);
    EdgesRef edgesRef = edgeGroup.getEdgesRef();
    List<Edge> allEdgesOfGroup = edgeGroup.getEdges();
    if (CollectionUtils.isNotEmpty(allEdgesOfGroup))
    {
      for (Integer givenColor : colors)
      {
        List<Integer> positionsForColor = coloringService.getPositionsForColor(edgesRef, givenColor);
        for (int edgePosition : positionsForColor)
        {
          edgesOfGivenColors.add(allEdgesOfGroup.get(edgePosition));
        }
      }
    }
    return edgesOfGivenColors;
  }

  @Override
  public EdgesGroup getEdgeGroupForEdgeType(Vertex v, EdgeType edgeType)
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

  @Override
  public List<Edge> getAllEdgesOfColor(Vertex v, int color)
  {
    LinkedList<Edge> allEdges = new LinkedList<>();
    allEdges.addAll(getAllEdgesOfColors(v, Collections.singletonList(color), EdgeType.DOWN));
    allEdges.addAll(getAllEdgesOfColors(v, Collections.singletonList(color), EdgeType.CROSS));
    allEdges.addAll(getAllEdgesOfColors(v, Collections.singletonList(color), EdgeType.UP));
    return allEdges;
  }

  @Override
  public List<Edge> getFurtherEdgesOfGivenTypeAndDifferentEndpoint(Edge e, Vertex endPoint, EdgeType edgeType)
  {
    List<Edge> furtherEdges = new LinkedList<>();
    EdgesGroup edgesGroup = getEdgeGroupForEdgeType(e.getEndpoint(), edgeType);
    if (edgesGroup != null && CollectionUtils.isNotEmpty(edgesGroup.getEdges()))
    {
      for (Edge furtherEdge : edgesGroup.getEdges())
      {
        if (furtherEdge.getEndpoint() != e.getOrigin() && furtherEdge.getEndpoint() != endPoint)
        {
          furtherEdges.add(furtherEdge);
        }
      }
    }
    return furtherEdges;
  }
}
