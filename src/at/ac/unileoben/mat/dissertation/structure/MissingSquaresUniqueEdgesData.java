package at.ac.unileoben.mat.dissertation.structure;

public class MissingSquaresUniqueEdgesData
{
  Edge baseEdge;
  Edge otherEdge;

  public MissingSquaresUniqueEdgesData(Edge baseEdge, Edge otherEdge)
  {
    this.baseEdge = baseEdge;
    this.otherEdge = otherEdge;
  }

  public Edge getBaseEdge()
  {
    return baseEdge;
  }

  public Edge getOtherEdge()
  {
    return otherEdge;
  }
}
