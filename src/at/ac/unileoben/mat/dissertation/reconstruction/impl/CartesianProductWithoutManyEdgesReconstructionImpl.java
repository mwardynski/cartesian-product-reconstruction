package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CartesianProductWithoutManyEdgesReconstructionImpl extends AbstractReconstruction implements Reconstruction
{

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  @Qualifier("intervalReconstructionImpl")
  Reconstruction intervalReconstruction;


  @Override
  public Graph reconstruct(List<Vertex> vertices, Vertex root)
  {
    reconstructionData.setOperationOnGraph(OperationOnGraph.MANY_EDGES_RECONSTRUCTION);

    List<Vertex> removedVerticesWithCorrectResult = new LinkedList<>();

    for (Vertex vertexToRemove : vertices)
    {
      collectAndStoreRemovedVertexNeighbors(vertexToRemove);

      testCaseContext.setCorrectResult(false);
      List<Vertex> verticesSubset = graphHelper.copySubgraph(vertices, Optional.of(vertexToRemove));

      try
      {
        intervalReconstruction.reconstruct(verticesSubset);
      }
      catch (Exception e)
      {

      } finally
      {
        if (testCaseContext.isCorrectResult())
        {
          removedVerticesWithCorrectResult.add(vertexToRemove);
        }
      }
    }

    testCaseContext.setRemovedVerticesWithCorrectResult(removedVerticesWithCorrectResult);
    return null;
  }

  private void collectAndStoreRemovedVertexNeighbors(Vertex vertexToRemove)
  {
    Set<Integer> removedVertexNeighbors = vertexToRemove.getEdges().stream()
            .map(edge -> edge.getEndpoint().getVertexNo())
            .collect(Collectors.toSet());
    testCaseContext.setRemovedVertexNeighbors(removedVertexNeighbors);
  }


}
