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

import static java.util.stream.Collectors.toList;

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
    FactorizationData factorizationData = null;
    for (Vertex vertex : vertices)
    {
      factorizationData = findFactorsForRoot(vertices, vertex);
      if (graph.getGraphColoring().getActualColors().size() != 1 &&
              graph.getGraphColoring().getActualColors().size() == factorizationData.getFactors().size()
//              && isNonOrOnlyOneVertexToAddInNewLayer(factorizationData)
              && isCorrectAmountOfVerticesInFactors(vertices, factorizationData))
      {
        break;
      }
      graphHelper.revertGraphBfsStructure();
    }
    return factorizationData;
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

  private boolean isCorrectAmountOfVerticesInFactors(List<Vertex> vertices, FactorizationData factorizationData)
  {
    List<Integer> factorSizes = factorizationData.getFactors().stream().mapToInt(factorData ->
    {
      List<Vertex> connectedComponentVertices = graphHelper.getConnectedComponentForColor(factorData.getTopVertices().iterator().next(), vertices, factorData.getMappedColor());
      return connectedComponentVertices.size();
    }).boxed().collect(toList());
    Integer amountOfVerticesAfterMultiplication = factorSizes.stream().reduce(1, (i1, i2) -> i1 * i2);
    return amountOfVerticesAfterMultiplication == vertices.size() + 1;
  }

  private FactorizationData findFactorsForRoot(List<Vertex> vertices, Vertex root)
  {
    clearVerticesAndEdges(vertices);
    graphHelper.prepareGraphBfsStructure(vertices, root);
    graph.setOperationOnGraph(OperationOnGraph.RECONSTRUCT);
    graphFactorizationPreparer.arrangeFirstLayerEdges();

    int layersAmount = graph.getLayers().size();
    FactorizationData factorizationData = new FactorizationData(layersAmount - 1);

    collectFirstLayerFactors(vertices, root, factorizationData);
    factorizationData = findFactorsForPreparedGraph(factorizationData);
    return factorizationData;
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

  private void collectFirstLayerFactors(List<Vertex> vertices, Vertex root, FactorizationData factorizationData)
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
      reconstructionService.collectFactors(factorizationData, topUnitLayerVertices);
      factorizationData.setFactorizationCompleted(true);
    }
  }

  private FactorizationData findFactorsForPreparedGraph(FactorizationData factorizationData)
  {
    int layersAmount = graph.getLayers().size();
    for (int currentLayerNo = 2; currentLayerNo < layersAmount; currentLayerNo++)
    {
      if (graph.getOperationOnGraph() == OperationOnGraph.RECONSTRUCT && breakProcessing(currentLayerNo, factorizationData))
      {
        break;
      }
      else
      {
        graphFactorizer.factorizeSingleLayer(currentLayerNo, factorizationData);
      }
    }
    return factorizationData;
  }

  private boolean breakProcessing(int currentLayer, FactorizationData factorizationData)
  {

    return isSingleFactor() || isLastIncompleteLayer(currentLayer, factorizationData);
  }

  private boolean isSingleFactor()
  {
    return graph.getGraphColoring().getActualColors().size() == 1;
  }

  private boolean isLastIncompleteLayer(int currentLayer, FactorizationData factorizationData)
  {
    boolean lastIncompleteLayer = false;
    if (currentLayer == graph.getLayers().size() - 1)
    {
      OptionalInt maxTopVerticesSizeOptional = factorizationData.getFactors().stream().mapToInt(factorData -> factorData.getTopVertices().size()).max();
      if (maxTopVerticesSizeOptional.isPresent() && graph.getLayers().get(currentLayer).size() < maxTopVerticesSizeOptional.getAsInt())
      {
        lastIncompleteLayer = true;
      }
    }
    return lastIncompleteLayer;
  }

}
