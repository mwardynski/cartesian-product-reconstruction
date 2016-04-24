package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizer;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.reconstruction.DuplicateReconstruction;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

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
  VertexService vertexService;

  @Autowired
  ColoringService coloringService;


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
              graph.getGraphColoring().getActualColors().size() == factorizationData.getFactors().size())
      {
        break;
      }
      graphHelper.revertGraphBfsStructure();
    }
    return factorizationData;
  }

  private FactorizationData findFactorsForRoot(List<Vertex> vertices, Vertex root)
  {
    clearVerticesAndEdges(vertices);
    graphHelper.prepareGraphBfsStructure(vertices, root);
    graph.setOperationOnGraph(OperationOnGraph.RECONSTRUCT);
    graphFactorizationPreparer.arrangeFirstLayerEdges();
    return findFactorsForPreparedGraph();
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

  private FactorizationData findFactorsForPreparedGraph()
  {
    FactorizationData factorizationData = new FactorizationData();
    int layersAmount = graph.getLayers().size();
    for (int currentLayerNo = 2; currentLayerNo < layersAmount; currentLayerNo++)
    {
      graphFactorizer.factorizeSingleLayer(currentLayerNo);
      if (!isLastPossibleLayerForNewFactors(currentLayerNo, layersAmount, factorizationData))
      {
        collectFactorsFromPreviousLayer(currentLayerNo - 1, factorizationData);
        if (isLastPossibleLayerForNewFactors(currentLayerNo, layersAmount, factorizationData))
        {
          collectFactorsFromCurrentLayer(currentLayerNo, factorizationData);
          break;
        }
      }
      else if (isLastPossibleLayerForNewFactors(currentLayerNo, layersAmount, factorizationData))
      {
        collectFactorsFromPreviousLayer(currentLayerNo - 1, factorizationData);
        collectFactorsFromCurrentLayer(currentLayerNo, factorizationData);
        break;
      }
    }
    return factorizationData;
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
