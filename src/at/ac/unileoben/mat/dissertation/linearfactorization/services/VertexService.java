package at.ac.unileoben.mat.dissertation.linearfactorization.services;

import at.ac.unileoben.mat.dissertation.structure.AdjacencyVector;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.MergeTagEnum;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 20.12.15
 * Time: 19:38
 * To change this template use File | Settings | File Templates.
 */
public interface VertexService
{
  List<Vertex> getGraphLayer(int i);

  List<List<Vertex>> createLayersList(List<Vertex> vertices);

  Edge getEdgeToVertex(AdjacencyVector vector, Vertex v);

  void assignVertexToUnitLayerAndMergeColors(Vertex v, boolean mergeCrossEdges, MergeTagEnum mergeTag) //mergeCrossEdges always true
  ;
}
