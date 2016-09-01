package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.DuplicateReconstruction;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.OptionalInt;

/**
 * Created by mwardynski on 24/04/16.
 */
@Component
public class DuplicateReconstructionImpl implements DuplicateReconstruction
{

  @Autowired
  Graph graph;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  GraphFactorizationPreparer graphFactorizationPreparer;

  @Autowired
  GraphFactorizer graphFactorizer;

  @Autowired
  ColoringService coloringService;

  @Autowired
  ReconstructionService reconstructionService;


  @Override
  public Graph reconstruct(List<Vertex> vertices)
  {
    return null;
  }

  @Override
  public FactorizationData findFactors(List<Vertex> vertices)
  {
    FactorizationResultData factorizationResultData = new FactorizationResultData();
    for (Vertex vertex : vertices)
    {
      findFactorsForRoot(vertices, vertex, factorizationResultData);
      graphHelper.revertGraphBfsStructure();
    }
    return factorizationResultData.getResultFactorization();
  }


  private boolean isNonOrOnlyOneVertexToAddInNewLayer(FactorizationData factorizationData)
  {
    boolean nonOrOnlyOneVertexToAddInNewLayer = true;
    List<List<Vertex>> layers = graph.getLayers();
    int highestLayerSize;
    if (factorizationData.getCollectedFactorsTotalHeight() == layers.size())
    {
      highestLayerSize = 0;
    }
    else
    {
      List<Vertex> highestLayer = layers.get(layers.size() - 1);
      highestLayerSize = highestLayer.size();
    }
    nonOrOnlyOneVertexToAddInNewLayer = !factorizationData.getFactors()
            .stream()
            .filter(factorData -> factorData.getTopVertices().size() - highestLayerSize < 0 || factorData.getTopVertices().size() - highestLayerSize > 1)
            .findAny()
            .isPresent();

    return nonOrOnlyOneVertexToAddInNewLayer;
  }


  private void findFactorsForRoot(List<Vertex> vertices, Vertex root, FactorizationResultData factorizationResultData)
  {
    clearVerticesAndEdges(vertices);
    graphHelper.prepareGraphBfsStructure(vertices, root);
    graph.setOperationOnGraph(OperationOnGraph.RECONSTRUCT);
    graphFactorizationPreparer.arrangeFirstLayerEdges();

    int layersAmount = graph.getLayers().size();
    FactorizationData factorizationData = new FactorizationData(layersAmount - 1, root);
    factorizationResultData.setCurrentFactorization(factorizationData);

    collectFirstLayerFactors(vertices, root, factorizationResultData);
    findFactorsForPreparedGraph(factorizationResultData);
  }

  private void clearVerticesAndEdges(List<Vertex> vertices)
  {
    for (Vertex vertex : vertices)
    {
      vertex.setUnitLayer(false);
      for (Edge edge : vertex.getEdges())
      {
        edge.setLabel(null);
      }
    }
  }

  private void collectFirstLayerFactors(List<Vertex> vertices, Vertex root, FactorizationResultData factorizationResultData)
  {
    List<List<Vertex>> topUnitLayerVertices = reconstructionService.createTopVerticesList(graph.getGraphColoring().getOriginalColorsAmount());
    int[] unitLayerVerticesAmountPerColor = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    for (Edge e : root.getUpEdges().getEdges())
    {
      int arbitraryEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), e.getLabel().getColor());
      Vertex v = e.getEndpoint();
      topUnitLayerVertices.get(arbitraryEdgeColor).add(v);
      unitLayerVerticesAmountPerColor[arbitraryEdgeColor]++;
    }
    int graphSizeWithFoundFactors = 1;
    for (int edgesAmount : unitLayerVerticesAmountPerColor)
    {
      if (edgesAmount != 0)
      {
        graphSizeWithFoundFactors *= (edgesAmount + 1);
      }
    }
    if (vertices.size() + 1 == graphSizeWithFoundFactors)
    {
      FactorizationData currentFactorization = factorizationResultData.getCurrentFactorization();
      reconstructionService.collectFactors(currentFactorization, topUnitLayerVertices);
      currentFactorization.setMaxConsistentLayerNo(1);
      currentFactorization.setFactorizationCompleted(true);
    }
  }

  private void findFactorsForPreparedGraph(FactorizationResultData factorizationResultData)
  {
    int layersAmount = graph.getLayers().size();
    for (int currentLayerNo = 2; currentLayerNo < layersAmount; currentLayerNo++)
    {
      if (graph.getOperationOnGraph() == OperationOnGraph.RECONSTRUCT && breakProcessing(currentLayerNo, factorizationResultData))
      {
        break;
      }
      else
      {
        graphFactorizer.factorizeSingleLayer(currentLayerNo, factorizationResultData);
      }
    }
  }

  private boolean breakProcessing(int currentLayer, FactorizationResultData factorizationResultData)
  {

    boolean breakProcesssing = isSingleFactor() || isLastIncompleteLayer(currentLayer, factorizationResultData);
    if (breakProcesssing)
    {
      reconstructionService.updateFactorizationResult(factorizationResultData);
    }
    return breakProcesssing;
  }

  private boolean isSingleFactor()
  {
    return graph.getGraphColoring().getActualColors().size() == 1;
  }

  private boolean isLastIncompleteLayer(int currentLayer, FactorizationResultData factorizationResultData)
  {
    boolean lastIncompleteLayer = false;
    FactorizationData currentFactorizationData = factorizationResultData.getCurrentFactorization();
    if (currentLayer == graph.getLayers().size() - 1)
    {
      OptionalInt maxTopVerticesSizeOptional = currentFactorizationData.getFactors().stream().mapToInt(factorData -> factorData.getTopVertices().size()).max();
      if (maxTopVerticesSizeOptional.isPresent() && graph.getLayers().get(currentLayer).size() < maxTopVerticesSizeOptional.getAsInt())
      {
        lastIncompleteLayer = true;

      }
    }
    return lastIncompleteLayer;
  }

}
