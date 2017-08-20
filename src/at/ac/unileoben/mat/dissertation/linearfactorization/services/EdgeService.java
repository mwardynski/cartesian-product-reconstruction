package at.ac.unileoben.mat.dissertation.linearfactorization.services;

import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 20.12.15
 * Time: 19:37
 * To change this template use File | Settings | File Templates.
 */
public interface EdgeService
{
  void addLabel(Edge edge, int color, int name, Edge squareMatchingEdge, LabelOperationDetail labelOperationDetail);

  Edge getFirstEdge(Vertex v, EdgeType edgeType);

  Edge getEdgeByLabel(Vertex v, Label label, EdgeType edgeType);

  Edge getEdgeOfDifferentColor(Vertex v, int color, GraphColoring graphColoring);

  List<List<Edge>> getAllEdgesOfDifferentColor(Vertex v, int color, GraphColoring graphColoring, EdgeType edgeType);

  List<Edge> getAllEdgesOfColors(Vertex v, List<Integer> colors, EdgeType edgeType);

  EdgesGroup getEdgeGroupForEdgeType(Vertex v, EdgeType edgeType);

  List<Edge> getAllEdgesOfColor(Vertex v, int color);

  List<Edge> getFurtherEdgesOfGivenTypeAndDifferentEndpoint(Edge e, Vertex endPoint, EdgeType edgeType);

  void clearEdgeLabeling(Edge edge);
}
