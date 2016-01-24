package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.reconstruction.ProductReconstructor;
import at.ac.unileoben.mat.dissertation.reconstruction.strategies.C8BasedReconstructionStrategy;
import at.ac.unileoben.mat.dissertation.reconstruction.strategies.GeneralReconstructionStrategy;
import at.ac.unileoben.mat.dissertation.reconstruction.strategies.K2BasedReconstructionStrategy;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 23.01.16
 * Time: 11:43
 * To change this template use File | Settings | File Templates.
 */
@Component
public class ProductReconstructorImpl implements ProductReconstructor
{

  @Autowired
  Graph graph;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  C8BasedReconstructionStrategy c8BasedReconstructionStrategy;

  @Autowired
  K2BasedReconstructionStrategy k2BasedReconstructionStrategy;

  @Autowired
  GeneralReconstructionStrategy generalReconstructionStrategy;


  @Override
  public Graph reconstructProduct(List<Vertex> vertices)
  {
    Graph reconstructedAndFactorizedGraph = reconstructBasingOnSpecialConditions(vertices);

    if (reconstructedAndFactorizedGraph == null)
    {
      reconstructedAndFactorizedGraph = reconstructWithoutSpecialConditions(vertices);
    }
    return reconstructedAndFactorizedGraph;
  }

  private Graph reconstructBasingOnSpecialConditions(List<Vertex> vertices)
  {
    Graph reconstructedAndFactorizedGraph;
    if (graphHelper.isGraphC8(vertices))
    {
      reconstructedAndFactorizedGraph = c8BasedReconstructionStrategy.reconstruct(vertices);
    }
    else
    {
      reconstructedAndFactorizedGraph = reconstructBasingOnK2Factor(vertices);
    }
    return reconstructedAndFactorizedGraph;
  }

  private Graph reconstructBasingOnK2Factor(List<Vertex> vertices)
  {
    Graph factorizedGraph = null;
    for (Vertex v : vertices)
    {
      factorizedGraph = k2BasedReconstructionStrategy.reconstruct(vertices, v);
      if (factorizedGraph != null)
      {
        break;
      }
    }
    return factorizedGraph;
  }


  private Graph reconstructWithoutSpecialConditions(List<Vertex> vertices)
  {
    for (Vertex s : vertices)
    {
      List<Vertex> copiedVertices = graphHelper.copySubgraph(vertices, Optional.empty());
      graphHelper.prepareGraphBfsStructure(copiedVertices, copiedVertices.get(s.getVertexNo()));
      Graph localGraph = new Graph(graph);
      List<Vertex> firstLayer = localGraph.getLayers().get(1);
      for (int uIndex = 0; uIndex < firstLayer.size(); uIndex++)
      {
        for (int vIndex = uIndex + 1; vIndex < firstLayer.size(); vIndex++)
        {
          Vertex u = firstLayer.get(uIndex);
          Vertex v = firstLayer.get(vIndex);
          Graph reconstructedAndFactorizedGraph = generalReconstructionStrategy.reconstruct(vertices, u, v, localGraph);
          if (reconstructedAndFactorizedGraph != null)
          {
            return reconstructedAndFactorizedGraph;
          }
        }
      }
    }
    return null;
  }
}
