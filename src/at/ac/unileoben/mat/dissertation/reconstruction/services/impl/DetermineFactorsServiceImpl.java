package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.DetermineFactorsService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

/**
 * Created by mwardynski on 12/06/16.
 */
@Component
public class DetermineFactorsServiceImpl implements DetermineFactorsService
{

  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  VertexService vertexService;

  @Autowired
  ColoringService coloringService;

  @Autowired
  GraphHelper graphHelper;

  public List<List<Vertex>> createTopVerticesList(int originalColorsAmount)
  {
    return IntStream.range(0, originalColorsAmount).mapToObj(i -> new LinkedList<Vertex>()).collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public void findReconstructionComponents(int currentLayerNo, boolean afterConsistencyCheck)
  {
    boolean processCurrentLayer = handleCurrentFactorization(currentLayerNo, afterConsistencyCheck);
    if (processCurrentLayer)
    {
      processCurrentLayer(currentLayerNo);
    }
  }

  private boolean handleCurrentFactorization(int currentLayerNo, boolean afterConsistencyCheck)
  {
    int actualColorsAmount = graph.getGraphColoring().getActualColors().size();
    FactorizationData currentFactorizationData = reconstructionData.getCurrentFactorization();

    boolean processCurrentLayer = actualColorsAmount != 1;
    List<FactorizationData.FactorData> currentFactors = currentFactorizationData.getFactors();
    if (CollectionUtils.isNotEmpty(currentFactors) && currentFactors.size() != actualColorsAmount)
    {
      updateFactorizationResult();
      List<FactorizationData.FactorData> newFactors = new LinkedList<>(currentFactorizationData.getFactors());
      int collectedFactorsTotalHeight = currentFactorizationData.getCollectedFactorsTotalHeight();
      currentFactorizationData = new FactorizationData(currentFactorizationData.getMaxFactorsHeight(), graph.getRoot(),
              newFactors, currentFactorizationData.getUnitLayerSpecs());


      currentFactorizationData.setCollectedFactorsTotalHeight(collectedFactorsTotalHeight);

      reconstructionData.setCurrentFactorization(currentFactorizationData);
    }
    else
    {
      processCurrentLayer &= !currentFactorizationData.isFactorizationCompleted();
    }
    currentFactorizationData.setMaxConsistentLayerNo(currentLayerNo);
    currentFactorizationData.setAfterConsistencyCheck(afterConsistencyCheck);
    if (isLastLayerAndCurrentFactorizationCompleted(currentLayerNo, currentFactorizationData))
    {
      reconstructionData.setResultFactorization(currentFactorizationData);
    }
    return processCurrentLayer;
  }

  public void updateFactorizationResult()
  {
    FactorizationData currentFactorizationData = reconstructionData.getCurrentFactorization();
    if (currentFactorizationData.isFactorizationCompleted())
    {
      FactorizationData resultFactorizationData = reconstructionData.getResultFactorization();
      if (currentFactorizationData.compareTo(resultFactorizationData) > 0)
      {
        reconstructionData.setResultFactorization(currentFactorizationData);
      }
    }
  }

  private boolean isLastLayerAndCurrentFactorizationCompleted(int currentLayerNo, FactorizationData currentFactorizationData)
  {
    return currentLayerNo == graph.getLayers().size() - 1 && currentFactorizationData.isFactorizationCompleted();
  }

  private void processCurrentLayer(int currentLayerNo)
  {
    FactorizationData currentFactorizationData = reconstructionData.getCurrentFactorization();

    int prevFactorsAmount = currentFactorizationData.getFactors().size();
    collectFactorsFromPreviousLayer(currentLayerNo - 1, currentFactorizationData);
    collectFactorsFromCurrentLayer(currentLayerNo, currentFactorizationData);

    if (currentFactorizationData.getFactors().size() != prevFactorsAmount)
    {
      removeOutdatedFactors(currentFactorizationData);
    }

    if (isFactorizationPossiblyCompleted(currentLayerNo, currentFactorizationData))
    {
      currentFactorizationData.setFactorizationCompleted(isCorrectAmountOfVerticesInFactors(currentFactorizationData));
    }
  }

  private boolean isFactorizationPossiblyCompleted(int currentLayerNo, FactorizationData currentFactorizationData)
  {
    return isLastPossibleLayerForNewFactors(currentLayerNo, currentFactorizationData) || (!isLastPossibleLayerForNewFactors(currentLayerNo, currentFactorizationData) && areCollectedFactorsOfMaxTotalHeight(currentFactorizationData));
  }

  private boolean isLastPossibleLayerForNewFactors(int currentLayerNo, FactorizationData factorizationData)
  {
    return currentLayerNo == factorizationData.getMaxFactorsHeight() - factorizationData.getCollectedFactorsTotalHeight();
  }


  private boolean areCollectedFactorsOfMaxTotalHeight(FactorizationData factorizationData)
  {
    int factorsHeightDifference = factorizationData.getMaxFactorsHeight() - factorizationData.getCollectedFactorsTotalHeight();
    return factorsHeightDifference == 0 || factorsHeightDifference == -1;
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
      if (CollectionUtils.isNotEmpty(topUnitLayerVertices))
      {
        Vertex unitLayerVertex = topUnitLayerVertices.iterator().next();
        int mappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), unitLayerVertex.getDownEdges().getEdges().get(0).getLabel().getColor());
        int bfsLayer = unitLayerVertex.getBfsLayer();
        FactorizationData.FactorData factorData = new FactorizationData.FactorData(topUnitLayerVertices, bfsLayer, mappedColor);

        factorizationData.getFactors().add(0, factorData);
        factorizationData.setCollectedFactorsTotalHeight(factorizationData.getCollectedFactorsTotalHeight() + bfsLayer);
      }
    }
  }

  private void removeOutdatedFactors(FactorizationData currentFactorizationData)
  {
    FactorizationData.FactorData[] higherFactors = new FactorizationData.FactorData[graph.getGraphColoring().getOriginalColorsAmount()];
    Iterator<FactorizationData.FactorData> factorsIterator = currentFactorizationData.getFactors().iterator();
    while (factorsIterator.hasNext())
    {
      FactorizationData.FactorData factor = factorsIterator.next();
      int factorMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), factor.getMappedColor());
      if (higherFactors[factorMappedColor] != null)
      {
        FactorizationData.FactorData higherFactor = higherFactors[factorMappedColor];
        higherFactor.getTopVertices().addAll(factor.getTopVertices());
        int newCollectedFactorsTotalHeight = currentFactorizationData.getCollectedFactorsTotalHeight() - factor.getHeight();
        currentFactorizationData.setCollectedFactorsTotalHeight(newCollectedFactorsTotalHeight);
        factorsIterator.remove();
      }
      else
      {
        higherFactors[factorMappedColor] = factor;
      }
    }
  }

  private boolean isCorrectAmountOfVerticesInFactors(FactorizationData factorizationData)
  {
    List<Integer> factorSizes = factorizationData.getFactors().stream().mapToInt(factorData ->
            graphHelper.getConnectedComponentSizeForColor(factorData.getTopVertices(), graph.getVertices(), factorizationData.getUnitLayerSpecs(), factorData.getMappedColor()))
            .boxed().collect(toList());
    Integer amountOfVerticesAfterMultiplication = factorSizes.stream().reduce(1, (i1, i2) -> i1 * i2);
    return amountOfVerticesAfterMultiplication == graph.getVertices().size() + 1;
  }

}
