package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 2/4/14
 * Time: 8:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConsistencyChecker
{
  private Graph graph;

  public ConsistencyChecker(Graph graph)
  {
    this.graph = graph;
  }

  public void checkConsistency(int currentLayerNo)
  {
    List<Vertex> currentLayer = graph.getLayer(currentLayerNo);
    List<Vertex> previousLayer = graph.getLayer(currentLayerNo - 1);
    downAndCrossEdgesConsistencyCheck(currentLayer);
    upEdgesConsistencyCheck(previousLayer);
  }

  private void downAndCrossEdgesConsistencyCheck(List<Vertex> layer)
  {
    for (Vertex u : layer)
    {
      if (u.isUnitLayer())
      {
        continue;
      }
      Edge uv = u.getDownEdges().getEdges().get(0);
      int uvMappedColor = graph.getGraphColoring().getCurrentColorMapping(uv.getLabel().getColor());
      Edge uw = u.getEdgeOfDifferentColor(uvMappedColor, graph.getGraphColoring());
      if (uw == null)
      {
        graph.assignVertexToUnitLayerAndMergeColors(u, true);//not invoked
        continue;
      }
      EnumSet<EdgeType> edgeTypes = EnumSet.of(EdgeType.DOWN, EdgeType.CROSS);
      for (EdgeType edgeType : edgeTypes)
      {
        if (!checkPivotSquares(uv, edgeType).isEmpty() || !checkPivotSquares(uw, edgeType).isEmpty())
        {
          graph.assignVertexToUnitLayerAndMergeColors(u, true);
          break;
        }
      }
    }
  }

  private void upEdgesConsistencyCheck(List<Vertex> layer)
  {
    for (Vertex u : layer)
    {
      Edge uv = u.getDownEdges().getEdges().get(0);
      Edge uw = null;
      if (!u.isUnitLayer())
      {
        int uvMappedColor = graph.getGraphColoring().getCurrentColorMapping(uv.getLabel().getColor());
        uw = u.getEdgeOfDifferentColor(uvMappedColor, graph.getGraphColoring());
      }
      List<Edge> inconsistentEdges = checkPivotSquares(uv, EdgeType.UP);
      if (uw != null)
      {
        List<Edge> uwInconsistentEdges = checkPivotSquares(uw, EdgeType.UP);
        inconsistentEdges.addAll(uwInconsistentEdges);
      }
      if (!inconsistentEdges.isEmpty())
      {
        handleInconsistentEdges(u, inconsistentEdges);
      }
    }
  }

  private void handleInconsistentEdges(Vertex u, List<Edge> inconsistentEdges)
  {
    int originalColorsAmount = graph.getGraphColoring().getOriginalColorsAmount();
    boolean[] colorOccurs = new boolean[originalColorsAmount];
    List<Integer> colors = new LinkedList<Integer>();
    for (Edge inconsistentEdge : inconsistentEdges)
    {
      int color = inconsistentEdge.getLabel().getColor();
      colorOccurs[color] = true;
    }
    for (int i = 0; i < colorOccurs.length; i++)
    {
      if (colorOccurs[i])
      {
        colors.add(i);
      }
    }
    List<Edge> allUpEdgesOfGivenColors = u.getAllEdgesOfColors(colors, EdgeType.UP);
    for (Edge edgeOfGivenColor : allUpEdgesOfGivenColors)
    {
      Vertex endpointVertex = edgeOfGivenColor.getEndpoint();
      graph.assignVertexToUnitLayerAndMergeColors(endpointVertex, true);
    }
  }

  private List<Edge> checkPivotSquares(Edge uv, EdgeType edgeType)
  {
    List<Edge> inconsistentEdges = new LinkedList<Edge>();
    Vertex u = uv.getOrigin();
    Vertex v = uv.getEndpoint();
    List<Edge> uDifferentThanUv = u.getAllEdgesOfDifferentColor(uv.getLabel().getColor(), graph.getGraphColoring(), edgeType);
    for (Edge uz : uDifferentThanUv)
    {
      if (uz.getEndpoint().isUnitLayer())
      {
        continue;
      }
      Label uzLabel = uz.getLabel();
      Edge vzp = v.getEdgeByLabel(uzLabel, edgeType);
      if (vzp == null)
      {
        inconsistentEdges.add(uz);
        continue;
      }
      Vertex z = uz.getEndpoint();
      Vertex zp = vzp.getEndpoint();
      EdgeType uvEdgeType = edgeType == EdgeType.CROSS ? EdgeType.CROSS : EdgeType.DOWN;
      Edge zzp = z.getEdgeByLabel(uv.getLabel(), uvEdgeType);
      if (zzp == null || !zzp.getEndpoint().equals(zp))
      {
        inconsistentEdges.add(uz);//not invoked
      }
    }
    return inconsistentEdges;
  }

}
