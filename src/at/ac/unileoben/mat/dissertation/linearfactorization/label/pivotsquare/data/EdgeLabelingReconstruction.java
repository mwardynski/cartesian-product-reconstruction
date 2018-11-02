package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

/**
 * Created by Marcin on 16.07.2017.
 */
public class EdgeLabelingReconstruction
{

  Edge edge;
  Vertex vertex;

  public EdgeLabelingReconstruction(Edge edge, Vertex vertex)
  {
    this.edge = edge;
    this.vertex = vertex;
  }

  public Edge getEdge()
  {
    return edge;
  }

  public Vertex getVertex()
  {
    return vertex;
  }
}
