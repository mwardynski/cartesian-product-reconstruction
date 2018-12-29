package at.ac.unileoben.mat.dissertation.structure;

public class CycleEdgePair
{
  Edge firstEdge;
  Edge secondEdge;

  public CycleEdgePair(Edge firstEdge, Edge secondEdge)
  {
    this.firstEdge = firstEdge;
    this.secondEdge = secondEdge;
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
