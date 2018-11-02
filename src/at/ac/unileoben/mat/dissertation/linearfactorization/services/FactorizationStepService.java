package at.ac.unileoben.mat.dissertation.linearfactorization.services;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.FactorizationStep;
import at.ac.unileoben.mat.dissertation.structure.FactorizationSteps;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 20.12.15
 * Time: 19:37
 * To change this template use File | Settings | File Templates.
 */
public interface FactorizationStepService
{
  void addVertex(FactorizationStep factorizationStep, Vertex referenceVertex, Vertex vertexToAssign);

  List<Vertex> getAssignedVertices(FactorizationStep factorizationStep, Vertex referenceVertex);

  void initialVertexInsertForDownEdges(FactorizationSteps factorizationSteps, Edge uv, Edge vx);

  void initialVertexInsertForCrossEdges(FactorizationSteps factorizationSteps, Edge uw);

  Edge getFirstLayerEdgeForVertexInFactorizationStep(FactorizationStep factorizationStep, Vertex u);

  void assignFirstLayerEdgeForVertexInFactorizationStep(FactorizationStep factorizationStep, Vertex u, Edge firstLayerEdge);
}
