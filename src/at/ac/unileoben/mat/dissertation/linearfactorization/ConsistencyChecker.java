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
        List<Integer> unitLayerColors = collectUnitLayerColors(u);
        if (unitLayerColors.size() > 1)
        {
          graph.getGraphColoring().mergeColors(unitLayerColors);
        }
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

  private List<Integer> collectUnitLayerColors(Vertex u)
  {
    boolean[] collectedColors = new boolean[graph.getGraphColoring().getOriginalColorsAmount()];
    List<Edge> uDownEdges = u.getDownEdges().getEdges();
    for (Edge uDownEdge : uDownEdges)
    {
      Vertex v = uDownEdge.getEndpoint();
      Edge firstVDownEdge = v.getDownEdges().getEdges().iterator().next();
      int unitLayerColor = graph.getGraphColoring().getCurrentColorMapping(firstVDownEdge.getLabel().getColor());
      collectedColors[unitLayerColor] = true;
    }
    List<Integer> unitLayerColors = new LinkedList<>();
    for (int collectedColorIndex = 0; collectedColorIndex < collectedColors.length; collectedColorIndex++)
    {
      if (collectedColors[collectedColorIndex])
      {
        unitLayerColors.add(collectedColorIndex);
      }
    }
    return unitLayerColors;
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
    List<List<Edge>> uDifferentThanUv = u.getAllEdgesOfDifferentColor(uv.getLabel().getColor(), graph.getGraphColoring(), edgeType);
    List<List<Edge>> vDifferentThanUv = v.getAllEdgesOfDifferentColor(uv.getLabel().getColor(), graph.getGraphColoring(), edgeType);
    List<Edge> notCorrespondingEdges = getNotCorrespondingEdgesRegardingColor(uDifferentThanUv, vDifferentThanUv);
    inconsistentEdges.addAll(notCorrespondingEdges);
    for (List<Edge> uzForColor : uDifferentThanUv)
    {
      for (Edge uz : uzForColor)
      {
        Label uzLabel = uz.getLabel();
        Edge vzp = v.getEdgeByLabel(uzLabel, edgeType);
        if (vzp == null)
        {
          inconsistentEdges.add(uz);
          continue;
        }
        Vertex z = uz.getEndpoint();
        Vertex zp = vzp.getEndpoint();
        Edge zzp = z.getEdgeByLabel(uv.getLabel(), EdgeType.DOWN);
        if (zzp == null || !zzp.getEndpoint().equals(zp))
        {
          inconsistentEdges.add(uz);//not invoked
        }
      }
    }
    return inconsistentEdges;
  }

  private List<Edge> getNotCorrespondingEdgesRegardingColor(List<List<Edge>> uEdges, List<List<Edge>> vEdges)
  {
    List<Edge> notCorrespondingEdges = new LinkedList<>();
    for (int i = 0; i < uEdges.size(); i++)
    {
      List<Edge> uEdgesOfColorI = uEdges.get(i);
      List<Edge> vEdgesOfColorI = vEdges.get(i);
      if (uEdgesOfColorI.size() != vEdgesOfColorI.size())
      {
        notCorrespondingEdges.addAll(uEdgesOfColorI);
      }
    }
    return notCorrespondingEdges;
  }

}
