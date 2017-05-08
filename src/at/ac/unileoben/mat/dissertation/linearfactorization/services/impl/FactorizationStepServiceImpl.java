package at.ac.unileoben.mat.dissertation.linearfactorization.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.FactorizationStepService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
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
  public void initialVertexInsertForDownEdges(FactorizationSteps factorizationSteps, Edge uv, Edge vx)
  {
    Vertex u = uv.getOrigin();
    Vertex v = uv.getEndpoint();
    Vertex x = vx.getEndpoint();
    addVertex(factorizationSteps.getFindSquareFirstPhase(), x, u);
    addVertex(factorizationSteps.getLabelVerticesPhase(), v, u);
    assignFirstLayerEdgeForVertexInFactorizationStep(factorizationSteps.getFindSquareFirstPhase(), u, vx);
  }

  @Override
  public void initialVertexInsertForCrossEdges(FactorizationSteps factorizationSteps, Edge uw)
  {
    Vertex u = uw.getOrigin();
    Vertex w = uw.getEndpoint();
    addVertex(factorizationSteps.getFindSquareFirstPhase(), w, u);
    assignFirstLayerEdgeForVertexInFactorizationStep(factorizationSteps.getFindSquareFirstPhase(), u, uw);
  }

  @Override
  public Edge getFirstLayerEdgeForVertexInFactorizationStep(FactorizationStep factorizationStep, Vertex u)
  {
    return factorizationStep.getFirstLayerPerVertexEdges()[u.getVertexNo()];
  }

  @Override
  public void assignFirstLayerEdgeForVertexInFactorizationStep(FactorizationStep factorizationStep, Vertex u, Edge firstLayerEdge)
  {
    factorizationStep.getFirstLayerPerVertexEdges()[u.getVertexNo()] = firstLayerEdge;
  }
}
