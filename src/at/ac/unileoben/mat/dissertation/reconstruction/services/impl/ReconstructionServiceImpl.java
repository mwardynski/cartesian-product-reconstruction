package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.FactorizationData;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by mwardynski on 12/06/16.
 */
@Component
public class ReconstructionServiceImpl implements ReconstructionService
{

  @Autowired
  Graph graph;

  @Autowired
  VertexService vertexService;

  @Autowired
  ColoringService coloringService;


  public List<List<Vertex>> createTopVerticesList(int originalColorsAmount)
  {
    return IntStream.range(0, originalColorsAmount).mapToObj(i -> new LinkedList<Vertex>()).collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public void findReconstructionComponents(int currentLayerNo, FactorizationData factorizationData)
  {
    if (!isLastPossibleLayerForNewFactors(currentLayerNo, factorizationData))
    {
      collectFactorsFromPreviousLayer(currentLayerNo - 1, factorizationData);
//      collectFactorsFromCurrentLayer(currentLayerNo, factorizationData);
      if (isLastPossibleLayerForNewFactors(currentLayerNo, factorizationData))
      {
        collectFactorsFromCurrentLayer(currentLayerNo, factorizationData);
        factorizationData.setFactorizationCompleted(true);
      }
    }
    else if (isLastPossibleLayerForNewFactors(currentLayerNo, factorizationData))
    {
      collectFactorsFromPreviousLayer(currentLayerNo - 1, factorizationData);
      collectFactorsFromCurrentLayer(currentLayerNo, factorizationData);
      factorizationData.setFactorizationCompleted(true);
    }
  }

  private boolean isLastPossibleLayerForNewFactors(int currentLayerNo, FactorizationData factorizationData)
  {
    return currentLayerNo == factorizationData.getLayersAmout() - factorizationData.getFactorsTotalHeight() - 1;
  }

  private void collectFactorsFromCurrentLayer(int currentLayerNo, FactorizationData factorizationData)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo);
    List<List<Vertex>> topUnitLayerVertices = createTopVerticesList(graph.getGraphColoring().getOriginalColorsAmount());
    currentLayer.stream().filter(Vertex::isUnitLayer).forEach(v ->
    {
      Edge arbitraryEdge = v.getDownEdges().getEdges().get(0);
      int arbitraryEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), arbitraryEdge.getLabel().getColor());
      topUnitLayerVertices.get(arbitraryEdgeColor).add(v);
    });
    collectFactors(factorizationData, topUnitLayerVertices);
  }

  private void collectFactorsFromPreviousLayer(int previousLayerNo, FactorizationData factorizationData)
  {
    List<Vertex> previousLayer = vertexService.getGraphLayer(previousLayerNo);
    List<List<Vertex>> topUnitLayerVertices = createTopVerticesList(graph.getGraphColoring().getOriginalColorsAmount());
    for (Vertex v : previousLayer)
    {
      Edge arbitraryEdge = v.getDownEdges().getEdges().get(0);
      int arbitraryEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), arbitraryEdge.getLabel().getColor());
      if (v.isUnitLayer() && topUnitLayerVertices.get(arbitraryEdgeColor) != null)
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
          topUnitLayerVertices.get(arbitraryEdgeColor).add(v);
        }
        else
        {
          topUnitLayerVertices.set(arbitraryEdgeColor, null);
        }
      }
    }
    collectFactors(factorizationData, topUnitLayerVertices);
  }

  @Override
  public void collectFactors(FactorizationData factorizationData, List<List<Vertex>> topUnitLayerVerticesList)
  {
    for (Integer colorIndex : graph.getGraphColoring().getActualColors())
    {
      List<Vertex> topUnitLayerVertices = topUnitLayerVerticesList.get(colorIndex);
      if (topUnitLayerVertices != null && CollectionUtils.isNotEmpty(topUnitLayerVertices))
      {
        int mappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), topUnitLayerVertices.iterator().next().getDownEdges().getEdges().get(0).getLabel().getColor());
        int bfsLayer = topUnitLayerVertices.iterator().next().getBfsLayer();
        FactorizationData.FactorData factorData = new FactorizationData.FactorData(topUnitLayerVertices, bfsLayer, mappedColor);
        factorizationData.getFactors().add(factorData);
        factorizationData.setFactorsTotalHeight(factorizationData.getFactorsTotalHeight() + bfsLayer);
      }
    }
  }

}
