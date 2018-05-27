package at.ac.unileoben.mat.dissertation.structure;

public class MissingSquareData
{
  Vertex vertex;
  Edge firstEdge;
  Edge secondEdge;

  public MissingSquareData(Vertex vertex, Edge firstEdge, Edge secondEdge)
  {
    this.vertex = vertex;
    this.firstEdge = firstEdge;
    this.secondEdge = secondEdge;
  }

  public Vertex getVertex()
  {
    return vertex;
  }

  public Edge getFirstEdge()
  {
    return firstEdge;
  }

  public Edge getSecondEdge()
  {
    return secondEdge;
  }
}
