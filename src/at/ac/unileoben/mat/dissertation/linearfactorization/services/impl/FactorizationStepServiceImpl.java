package at.ac.unileoben.mat.dissertation.linearfactorization.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.FactorizationStepService;
import at.ac.unileoben.mat.dissertation.structure.FactorizationStep;
import at.ac.unileoben.mat.dissertation.structure.FactorizationSteps;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 18.10.15
 * Time: 18:58
 * To change this template use File | Settings | File Templates.
 */
@Component
public class FactorizationStepServiceImpl implements FactorizationStepService
{

  @Override
  public void addVertex(FactorizationStep factorizationStep, Vertex referenceVertex, Vertex vertexToAssign)
  {
    factorizationStep.getVerticesInLayer()[referenceVertex.getVertexNo() - factorizationStep.getFirstVertexInLayerIndex()] = referenceVertex;
    factorizationStep.getLayer().get(referenceVertex.getVertexNo() - factorizationStep.getFirstVertexInLayerIndex()).add(vertexToAssign);
  }

  @Override
  public List<Vertex> getAssignedVertices(FactorizationStep factorizationStep, Vertex referenceVertex)
  {
    return factorizationStep.getLayer().get(referenceVertex.getVertexNo() - factorizationStep.getFirstVertexInLayerIndex());
  }

  @Override
  public void initialVertexInsertForDownEdges(FactorizationSteps factorizationSteps, Vertex u, Vertex v, Vertex x)
  {
    addVertex(factorizationSteps.getFindSquareFirstPhase(), x, u);
    addVertex(factorizationSteps.getLabelVerticesPhase(), v, u);
  }

  @Override
  public void initialVertexInsertForCrossEdges(FactorizationSteps factorizationSteps, Vertex u, Vertex w)
  {
    addVertex(factorizationSteps.getFindSquareFirstPhase(), w, u);
  }
}
