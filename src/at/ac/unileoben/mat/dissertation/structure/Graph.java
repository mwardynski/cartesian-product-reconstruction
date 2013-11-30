package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-30
 * Time: 11:54
 * To change this template use File | Settings | File Templates.
 */
public class Graph
{
  List<Vertex> vertices;
  Edge[][] adjacencyMatrix;

  public Graph(List<Vertex> vertices)
  {
    this.vertices = vertices;
    adjacencyMatrix = createAdjacencyMatrix(vertices);
  }

  private Edge[][] createAdjacencyMatrix(List<Vertex> verices)
  {
    Edge[][] adjacencyMatrix = new Edge[verices.size()][verices.size()];

    for (Vertex v : verices)
    {
      for (Edge e : v.getEdges())
      {
        adjacencyMatrix[e.getOrigin().getVertexNo()][e.getEndpoint().getVertexNo()] = e;
      }
    }

    return adjacencyMatrix;
  }

  public List<Vertex> getVertices()
  {
    return vertices;
  }

  public Edge getEdge(int origin, int endpoint)
  {
    return adjacencyMatrix[origin][endpoint];
  }

  public Vertex getRoot()
  {
    return vertices.get(0);
  }
}
