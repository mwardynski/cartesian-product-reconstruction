package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.ReconstructionAfterFindingAllFactors;
import at.ac.unileoben.mat.dissertation.reconstruction.services.DetermineFactorsService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionBackupLayerService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;
import java.util.OptionalInt;

/**
 * Created by mwardynski on 26/11/16.
 */
public abstract class AbstractReconstructionAfterFindingAllFactors extends AbstractReconstruction implements ReconstructionAfterFindingAllFactors
{

  @Autowired
  private Graph graph;

  @Autowired
  private ReconstructionData reconstructionData;

  @Autowired
  private GraphHelper graphHelper;

  @Autowired
  private GraphFactorizationPreparer graphFactorizationPreparer;

  @Autowired
  private GraphFactorizer graphFactorizer;

  @Autowired
  private DetermineFactorsService determineFactorsService;

  @Autowired
  private ColoringService coloringService;

  @Autowired
  private ReconstructionService reconstructionService;

  @Autowired
  private ReconstructionBackupLayerService reconstructionBackupLayerService;

  @Override
  public FactorizationData findFactors(List<Vertex> vertices)
  {
    reconstructionService.clearReconstructionData();
    for (Vertex vertex : vertices)
    {
      findFactorsForRoot(vertices, vertex);
      graphHelper.revertGraphBfsStructure();
    }
    return reconstructionData.getResultFactorization();
  }

  protected Graph findFactorsForRoot(List<Vertex> vertices, Vertex root)
  {
    clearVerticesAndEdges(vertices);
    graphHelper.prepareGraphBfsStructure(vertices, root);

    graphFactorizationPreparer.arrangeFirstLayerEdges();

    int layersAmount = graph.getLayers().size();
    FactorizationUnitLayerSpecData[] unitLayerSpecs = new FactorizationUnitLayerSpecData[vertices.size()];
    List<FactorizationData.FactorData> newFactors = new LinkedList<>();
    FactorizationData factorizationData = new FactorizationData(layersAmount - 1, root, newFactors, unitLayerSpecs);
    reconstructionData.setCurrentFactorization(factorizationData);

    collectFirstLayerFactors(vertices, root);
    findFactorsForPreparedGraph();
    return graph;
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

  private void collectFirstLayerFactors(List<Vertex> vertices, Vertex root)
  {
    List<List<Vertex>> topUnitLayerVertices = determineFactorsService.createTopVerticesList(graph.getGraphColoring().getOriginalColorsAmount());
    for (Edge e : root.getUpEdges().getEdges())
    {
      int arbitraryEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), e.getLabel().getColor());
      Vertex v = e.getEndpoint();
      topUnitLayerVertices.get(arbitraryEdgeColor).add(v);
    }
    int graphSizeWithFoundFactors = 1;

    FactorizationUnitLayerSpecData[] unitLayerSpecs = reconstructionData.getCurrentFactorization().getUnitLayerSpecs();
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
      FactorizationData currentFactorization = reconstructionData.getCurrentFactorization();
      determineFactorsService.collectFactors(currentFactorization, topUnitLayerVertices);
      currentFactorization.setMaxConsistentLayerNo(1);
      currentFactorization.setFactorizationCompleted(true);
    }
  }

  private void findFactorsForPreparedGraph()
  {
    int layersAmount = graph.getLayers().size();
    for (int currentLayerNo = 2; currentLayerNo < layersAmount; currentLayerNo++)
    {
      if (reconstructionData.getOperationOnGraph() == OperationOnGraph.RECONSTRUCT && breakProcessing(currentLayerNo))
      {
        break;
      }
      else
      {
        reconstructionData.setCurrentLayerNo(currentLayerNo);
        reconstructionBackupLayerService.storeCurrentLayerBackup();
        graphFactorizer.factorizeSingleLayer(currentLayerNo);
      }
    }
  }

  private boolean breakProcessing(int currentLayer)
  {
    boolean breakProcessing = isSingleFactor() || isLastIncompleteLayer(currentLayer);
    if (breakProcessing)
    {
      determineFactorsService.updateFactorizationResult();
    }
    return breakProcessing;
  }

  private boolean isSingleFactor()
  {
    return graph.getGraphColoring().getActualColors().size() == 1;
  }

  private boolean isLastIncompleteLayer(int currentLayer)
  {
    boolean lastIncompleteLayer = false;
    FactorizationData currentFactorizationData = reconstructionData.getCurrentFactorization();
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
