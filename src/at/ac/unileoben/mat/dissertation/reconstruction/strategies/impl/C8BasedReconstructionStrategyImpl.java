package at.ac.unileoben.mat.dissertation.reconstruction.strategies.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.reconstruction.strategies.C8BasedReconstructionStrategy;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 24.01.16
 * Time: 11:32
 * To change this template use File | Settings | File Templates.
 */
@Component
public class C8BasedReconstructionStrategyImpl implements C8BasedReconstructionStrategy
{
  @Autowired
  GraphHelper graphHelper;

  @Autowired
  LinearFactorization linearFactorization;

  @Override
  public Graph reconstruct(List<Vertex> vertices)
  {
    Graph reconstructedAndFactorizedGraph;
    List<Vertex> reconstructedVertexNeighbors = new LinkedList<>();
    for (int i = 0; i < vertices.size(); i += 2)
    {
      reconstructedVertexNeighbors.add(vertices.get(i));
    }
    graphHelper.addVertex(vertices, reconstructedVertexNeighbors);
    reconstructedAndFactorizedGraph = linearFactorization.factorize(vertices, null);
    return reconstructedAndFactorizedGraph;
  }
}
