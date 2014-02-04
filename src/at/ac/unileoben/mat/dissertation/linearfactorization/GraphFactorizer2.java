package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.linearfactorization.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.pivotsquare.strategies.impl.CrossEdgesPivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.pivotsquare.strategies.impl.DownEdgesPivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-01-29
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
public class GraphFactorizer2
{

  public void factorize(Graph graph)
  {
    int layersAmount = graph.getLayersAmount();
    for (int currentLayerNo = 2; currentLayerNo < layersAmount; currentLayerNo++)
    {
      labelDownEdges(graph, currentLayerNo);
      labelCrossEdges(graph, currentLayerNo);
      labelUpEdges(graph, currentLayerNo);
      consistencyCheck(graph, currentLayerNo);
    }
  }

  private void labelDownEdges(Graph graph, int currentLayerNo)
  {
    List<Vertex> currentLayer = graph.getLayer(currentLayerNo);
    List<Vertex> previousLayer = graph.getLayer(currentLayerNo - 1);
    List<Vertex> prePreviousLayer = graph.getLayer(currentLayerNo - 2);


    FactorizationSteps factorizationSteps = new FactorizationSteps(previousLayer, prePreviousLayer);

    for (Vertex u : currentLayer)
    {
      EdgesGroup downEdgesGroup = u.getDownEdges();
      List<Edge> uDownEdges = downEdgesGroup.getEdges();
      if (uDownEdges.size() == 1)
      {
        int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
        Edge uv = uDownEdges.iterator().next();
        Vertex v = uv.getEndpoint();
        Edge vx = v.getDownEdges().getEdges().iterator().next();
        int vxColor = vx.getLabel().getColor();
        uv.setLabel(new Label(0, vxColor));
        colorsCounter[vxColor]++;

        u.setUnitLayer(true);
        if (!v.isUnitLayer())
        {
          graph.assignVertexToUnitLayerAndMergeColors(v, true); //not invoked
        }

        int[] colorLengths = new int[graph.getGraphColoring().getOriginalColorsAmount()];
        colorLengths[vxColor] = 1;
        EdgesRef downEdgesRef = new EdgesRef(1);
        downEdgesRef.setColorAmounts(colorLengths);
        downEdgesGroup.setEdgesRef(downEdgesRef);
      }
      else
      {
        Edge uv = uDownEdges.get(0);
        u.setFirstEdge(uv);
        Vertex v = uv.getEndpoint();
        Edge vx = v.getDownEdges().getEdges().get(0);
        u.setSecondEdge(vx);
        u.setEdgeWithColorToLabel(vx);
        Vertex x = vx.getEndpoint();
        factorizationSteps.initialVertexInsertForDownEdges(u, v, x);
      }
    }

    PivotSquareFinderStrategy pivotSquareFinderStrategy = new DownEdgesPivotSquareFinderStrategy();
    FactorizationStep findSquareFirstPhase = factorizationSteps.getFindSquareFirstPhase();
    FactorizationStep findSquareSecondPhase = factorizationSteps.getFindSquareSecondPhase();
    singleFindPivotSquarePhase(graph, pivotSquareFinderStrategy, findSquareFirstPhase, findSquareSecondPhase);
    singleFindPivotSquarePhase(graph, pivotSquareFinderStrategy, findSquareSecondPhase, null);

    FactorizationStep labelVerticesPhase = factorizationSteps.getLabelVerticesPhase();
    for (Vertex v : labelVerticesPhase.getReferenceVertices())
    {
      if (v == null)
      {
        continue;
      }
      AdjacencyVector vAdjacencyVector = new AdjacencyVector(graph.getVertices().size(), v);
      List<Vertex> assignedVertices = labelVerticesPhase.getAssignedVertices(v);
      Iterator<Vertex> assignedVerticesIterator = assignedVertices.iterator();
      while (assignedVerticesIterator.hasNext())
      {
        int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
        Vertex u = assignedVerticesIterator.next();
        Edge uv = u.getFirstEdge();
        boolean noPivotSquare = false;
        Edge edgeWithColorToLabel = u.getEdgeWithColorToLabel();
        int colorToLabel = edgeWithColorToLabel.getLabel().getColor();
        if (uv.getLabel() == null)
        {
          noPivotSquare = true;
          u.setUnitLayer(true);
          uv.setLabel(new Label(0, colorToLabel));
        }
        List<Edge> uDownEdges = u.getDownEdges().getEdges();
        for (Edge uy : uDownEdges)
        {
          if (uy.equals(uv))
          {
            colorsCounter[uy.getLabel().getColor()]++;
            continue;
          }
          if (noPivotSquare)
          {
            uy.setLabel(new Label(colorsCounter[colorToLabel], colorToLabel));
            colorsCounter[colorToLabel]++;
          }
          else
          {
            Vertex y = uy.getEndpoint();
            Edge yz = y.getEdgeByLabel(uv.getLabel(), EdgeType.DOWN);
            if (yz != null)
            {
              Vertex z = yz.getEndpoint();
              Edge vz = vAdjacencyVector.getEdgeToVertex(z);
              if (vz != null)
              {
                int vzColor = vz.getLabel().getColor();
                uy.setLabel(new Label(colorsCounter[vzColor], vzColor));
                colorsCounter[vzColor]++;
              }
            }
            if (uy.getLabel() == null)
            {
              int uvColor = uv.getLabel().getColor();
              uy.setLabel(new Label(colorsCounter[uvColor], uvColor));
              colorsCounter[uvColor]++;
            }
          }
        }

        EdgesRef downEdgesRef = FactorizationUtils.getEdgesRef(colorsCounter);
        u.getDownEdges().setEdgesRef(downEdgesRef);
        List<Edge> sortedEdges = FactorizationUtils.sortEdgesAccordingToLabels(u.getDownEdges().getEdges(), graph.getGraphColoring());
        u.getDownEdges().setEdges(sortedEdges);

        assignedVerticesIterator.remove();
      }
    }

  }

  private void singleFindPivotSquarePhase(Graph graph, PivotSquareFinderStrategy pivotSquareFinderStrategy, FactorizationStep thisPhase, FactorizationStep nextPhase)
  {
    for (Vertex x : thisPhase.getReferenceVertices())
    {
      if (x == null)
      {
        continue;
      }
      AdjacencyVector xAdjacencyVector = new AdjacencyVector(graph.getVertices().size(), x);
      findPivotSquareForReferenceVertex(graph, pivotSquareFinderStrategy, x, xAdjacencyVector, thisPhase, nextPhase);
    }
  }

  private void findPivotSquareForReferenceVertex(Graph graph, PivotSquareFinderStrategy pivotSquareFinderStrategy, Vertex x, AdjacencyVector xAdjacencyVector, FactorizationStep thisPhase, FactorizationStep nextPhase)
  {
    List<Vertex> assignedVertices = thisPhase.getAssignedVertices(x);
    Iterator<Vertex> assignedVerticesIterator = assignedVertices.iterator();
    while (assignedVerticesIterator.hasNext())
    {
      Vertex u = assignedVerticesIterator.next();
      pivotSquareFinderStrategy.findPivotSquare(u, xAdjacencyVector, nextPhase, graph);
      if (nextPhase != null)
      {
        findPivotSquareForReferenceVertex(graph, pivotSquareFinderStrategy, x, xAdjacencyVector, nextPhase, null);
      }
      assignedVerticesIterator.remove();
    }
  }

  private void labelCrossEdges(Graph graph, int currentLayerNo)
  {
    List<Vertex> currentLayer = graph.getLayer(currentLayerNo);
    List<Vertex> previousLayer = graph.getLayer(currentLayerNo - 1);


    FactorizationSteps factorizationSteps = new FactorizationSteps(previousLayer);

    for (Vertex u : currentLayer)
    {
      List<Edge> uDownEdges = u.getDownEdges().getEdges();
      Edge uw = uDownEdges.get(0);
      u.setFirstEdge(uw);
      u.setEdgeWithColorToLabel(uw);
      Vertex w = uw.getEndpoint();
      factorizationSteps.initialVertexInsertForCrossEdges(u, w);
    }

    PivotSquareFinderStrategy pivotSquareFinderStrategy = new CrossEdgesPivotSquareFinderStrategy();
    FactorizationStep findSquareFirstPhase = factorizationSteps.getFindSquareFirstPhase();
    singleFindPivotSquarePhase(graph, pivotSquareFinderStrategy, findSquareFirstPhase, null);
  }

  private void labelUpEdges(Graph graph, int currentLayerNo)
  {
    List<Vertex> currentLayer = graph.getLayer(currentLayerNo - 1);
    for (Vertex u : currentLayer)
    {
      List<Edge> uUpEdges = u.getUpEdges().getEdges();
      int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
      for (int i = 0; i < uUpEdges.size(); i++)
      {
        Edge uv = uUpEdges.get(i);
        int oppositeEdgeColor = uv.getOpposite().getLabel().getColor();
        uv.setLabel(new Label(colorsCounter[oppositeEdgeColor], oppositeEdgeColor));
        colorsCounter[oppositeEdgeColor]++;
      }
      EdgesRef upEdgesRef = FactorizationUtils.getEdgesRef(colorsCounter);
      u.getUpEdges().setEdgesRef(upEdgesRef);
      List<Edge> sortedEdges = FactorizationUtils.sortEdgesAccordingToLabels(u.getUpEdges().getEdges(), graph.getGraphColoring());
      u.getUpEdges().setEdges(sortedEdges);
    }
  }


  private void consistencyCheck(Graph graph, int currentLayerNo)
  {
    List<Vertex> currentLayer = graph.getLayer(currentLayerNo);
    List<Vertex> previousLayer = graph.getLayer(currentLayerNo - 1);
    downAndCrossEdgesConsistencyCheck(graph, currentLayer);
    upEdgesConsistencyCheck(graph, previousLayer);
  }

  private void downAndCrossEdgesConsistencyCheck(Graph graph, List<Vertex> layer)
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
        if (!checkPivotSquares(uv, edgeType, graph).isEmpty() || !checkPivotSquares(uw, edgeType, graph).isEmpty())
        {
          graph.assignVertexToUnitLayerAndMergeColors(u, true);
          break;
        }
      }
    }
  }

  private void upEdgesConsistencyCheck(Graph graph, List<Vertex> layer)
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
      List<Edge> inconsistentEdges = checkPivotSquares(uv, EdgeType.UP, graph);
      if (uw != null)
      {
        List<Edge> uwInconsistentEdges = checkPivotSquares(uw, EdgeType.UP, graph);
        inconsistentEdges.addAll(uwInconsistentEdges);
      }
      if (!inconsistentEdges.isEmpty())
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
    }
  }

  private List<Edge> checkPivotSquares(Edge uv, EdgeType edgeType, Graph graph)
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
