package at.ac.unileoben.mat.dissertation.structure;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-29
 * Time: 15:26
 * To change this template use File | Settings | File Templates.
 */
public class Edge
{
  Vertex origin;
  Vertex endpoint;
  Edge opposite;
  EdgeType edgeType;
  Label label;
  Edge squareMatchingEdge;

  public Edge(Vertex origin, Vertex endpoint)
  {
    this.origin = origin;
    this.endpoint = endpoint;
  }

  public Vertex getOrigin()
  {
    return origin;
  }

  public Vertex getEndpoint()
  {
    return endpoint;
  }

  public Edge getOpposite()
  {
    return opposite;
  }

  public void setOpposite(Edge opposite)
  {
    this.opposite = opposite;
  }

  public EdgeType getEdgeType()
  {
    return edgeType;
  }

  public void setEdgeType(EdgeType edgeType)
  {
    this.edgeType = edgeType;
  }

  public Label getLabel()
  {
    return label;
  }

  public void setLabel(Label label)
  {
    this.label = label;
  }

  public Edge getSquareMatchingEdge()
  {
    return squareMatchingEdge;
  }

  public void setSquareMatchingEdge(Edge squareMatchingEdge)
  {
    this.squareMatchingEdge = squareMatchingEdge;
  }

  @Override
  public String toString()
  {
    return String.format("%d-%d (%s)",
            origin != null ? origin.getVertexNo() : -1,
            endpoint != null ? endpoint.getVertexNo() : -1,
            label);
  }
}
