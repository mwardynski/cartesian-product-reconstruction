package at.ac.unileoben.mat.dissertation.linearfactorization.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.CrossEdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.DownEdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.UpEdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.FactorizationData;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-01-29
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
@Component
public class GraphFactorizerImpl implements GraphFactorizer
{
  @Autowired
  Graph graph;

  @Autowired
  DownEdgesLabeler downEdgesLabeler;

  @Autowired
  CrossEdgesLabeler crossEdgesLabeler;

  @Autowired
  UpEdgesLabeler upEdgesLabeler;

  @Autowired
  ConsistencyChecker consistencyChecker;
  @Autowired
  VertexService vertexService;
  @Autowired
  ColoringService coloringService;

  @Override
  public void factorize()
  {
    FactorizationData factorizationData = new FactorizationData();
    int layersAmount = graph.getLayers().size();
    for (int currentLayerNo = 2; currentLayerNo < layersAmount; currentLayerNo++)
    {
      downEdgesLabeler.labelEdges(currentLayerNo);
      crossEdgesLabeler.labelEdges(currentLayerNo);
      upEdgesLabeler.labelEdges(currentLayerNo);
      consistencyChecker.checkConsistency(currentLayerNo);
      if (currentLayerNo == 2 || !isLastPossibleLayerForNewFactors(currentLayerNo, layersAmount, factorizationData))
      {
        collectFactorsFromPreviousLayer(currentLayerNo - 1, factorizationData);
      }
      if (isLastPossibleLayerForNewFactors(currentLayerNo, layersAmount, factorizationData))
      {
        collectFactorsFromCurrentLayer(currentLayerNo, factorizationData);
        break;
      }
    }
    factorizationData.getFactors().stream().forEach(factor -> System.out.println(factor));
    System.out.println(graph.getGraphColoring().getActualColors().size() + ", h: " + (layersAmount - 1));
  }

  private boolean isLastPossibleLayerForNewFactors(int currentLayerNo, int layersAmount, FactorizationData factorizationData)
  {
    return currentLayerNo == layersAmount - 1 - factorizationData.getFactorsTotalHeight();
  }

  private void collectFactorsFromCurrentLayer(int currentLayerNo, FactorizationData factorizationData)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo);
    Vertex[] topUnitLayerVertices = new Vertex[graph.getGraphColoring().getOriginalColorsAmount()];
    for (Vertex v : currentLayer)
    {
      if (v.isUnitLayer())
      {
        Edge arbitraryEdge = v.getDownEdges().getEdges().get(0);
        int arbitraryEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), arbitraryEdge.getLabel().getColor());
        topUnitLayerVertices[arbitraryEdgeColor] = v;
      }
    }
    collectFactors(factorizationData, topUnitLayerVertices, null);
  }

  private void collectFactorsFromPreviousLayer(int previousLayerNo, FactorizationData factorizationData)
  {
    List<Vertex> previousLayer = vertexService.getGraphLayer(previousLayerNo);
    Vertex[] topUnitLayerVertices = new Vertex[graph.getGraphColoring().getOriginalColorsAmount()];
    Vertex exclusionVertex = new Vertex(-1, null);
    for (Vertex v : previousLayer)
    {
      Edge arbitraryEdge = v.getDownEdges().getEdges().get(0);
      int arbitraryEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), arbitraryEdge.getLabel().getColor());
      if (v.isUnitLayer() && topUnitLayerVertices[arbitraryEdgeColor] != exclusionVertex)
      {
        boolean potentialCompletedFactor = true;
        for (Edge vUpEdge : v.getUpEdges().getEdges())
        {
          Vertex endpointVertex = vUpEdge.getEndpoint();
          if (endpointVertex.isUnitLayer())
          {
            potentialCompletedFactor = false;
            break;
          }
        }
        if (potentialCompletedFactor)
        {
          topUnitLayerVertices[arbitraryEdgeColor] = v;
        }
        else
        {
          topUnitLayerVertices[arbitraryEdgeColor] = exclusionVertex;
        }
      }
    }
    collectFactors(factorizationData, topUnitLayerVertices, exclusionVertex);
  }

  private void collectFactors(FactorizationData factorizationData, Vertex[] topUnitLayerVertices, Vertex exclusionVertex)
  {
    for (Integer colorIndex : graph.getGraphColoring().getActualColors())
    {
      Vertex topUnitLayerVertex = topUnitLayerVertices[colorIndex];
      if (topUnitLayerVertex != null && topUnitLayerVertex != exclusionVertex)
      {
        int mappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), topUnitLayerVertex.getDownEdges().getEdges().get(0).getLabel().getColor());
        FactorizationData.FactorData factorData = new FactorizationData.FactorData(topUnitLayerVertex, topUnitLayerVertex.getBfsLayer(), mappedColor);
        factorizationData.getFactors().add(factorData);
        factorizationData.setFactorsTotalHeight(factorizationData.getFactorsTotalHeight() + topUnitLayerVertex.getBfsLayer());
      }
    }
  }
}
