package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.ReconstructionAfterFindingAllFactors;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;

/**
 * Created by mwardynski on 26/11/16.
 */
public abstract class AbstractReconstructionAfterFindingAllFactors implements ReconstructionAfterFindingAllFactors
{

  @Autowired
  private Graph graph;

  @Autowired
  private GraphHelper graphHelper;

  @Autowired
  private GraphFactorizationPreparer graphFactorizationPreparer;

  @Autowired
  private GraphFactorizer graphFactorizer;

  @Autowired
  private ReconstructionService reconstructionService;

  @Autowired
  private ColoringService coloringService;

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

  private void findFactorsForRoot(List<Vertex> vertices, Vertex root, FactorizationResultData factorizationResultData)
  {
    clearVerticesAndEdges(vertices);
    graphHelper.prepareGraphBfsStructure(vertices, root);
    graph.setOperationOnGraph(OperationOnGraph.RECONSTRUCT);
    graphFactorizationPreparer.arrangeFirstLayerEdges();

    int layersAmount = graph.getLayers().size();
    FactorizationUnitLayerSpecData[] unitLayerSpecs = new FactorizationUnitLayerSpecData[vertices.size()];
    List<FactorizationData.FactorData> newFactors = new LinkedList<>();
    FactorizationData factorizationData = new FactorizationData(layersAmount - 1, root, newFactors, unitLayerSpecs);
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
    for (Edge e : root.getUpEdges().getEdges())
    {
      int arbitraryEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), e.getLabel().getColor());
      Vertex v = e.getEndpoint();
      topUnitLayerVertices.get(arbitraryEdgeColor).add(v);
    }
    int graphSizeWithFoundFactors = 1;

    FactorizationUnitLayerSpecData[] unitLayerSpecs = factorizationResultData.getCurrentFactorization().getUnitLayerSpecs();
    unitLayerSpecs[0] = new FactorizationUnitLayerSpecData(0, 0);
    for (int currentColor = 0; currentColor < topUnitLayerVertices.size(); currentColor++)
    {
      int edgesAmount = topUnitLayerVertices.get(currentColor).size();
      if (edgesAmount != 0)
      {
        graphSizeWithFoundFactors *= (++edgesAmount);
      }

      FactorizationUnitLayerSpecData factorizationUnitLayerSpecData = new FactorizationUnitLayerSpecData(currentColor, edgesAmount);
      topUnitLayerVertices.get(currentColor).forEach(v -> unitLayerSpecs[v.getVertexNo()] = factorizationUnitLayerSpecData);
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

    boolean breakProcessing = isSingleFactor() || isLastIncompleteLayer(currentLayer, factorizationResultData);
    if (breakProcessing)
    {
      reconstructionService.updateFactorizationResult(factorizationResultData);
    }
    return breakProcessing;
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
