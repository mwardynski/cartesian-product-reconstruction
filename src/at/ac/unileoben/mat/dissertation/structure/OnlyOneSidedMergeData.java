package at.ac.unileoben.mat.dissertation.structure;

public class OnlyOneSidedMergeData
{
  Edge squareBaseEdge;
  Edge squareExtensionEdge;
  Edge otherEdge;

  public OnlyOneSidedMergeData(Edge squareBaseEdge, Edge squareExtensionEdge, Edge otherEdge)
  {
    this.squareBaseEdge = squareBaseEdge;
    this.squareExtensionEdge = squareExtensionEdge;
    this.otherEdge = otherEdge;
  }

  public Edge getSquareBaseEdge()
  {
    return squareBaseEdge;
  }

  public Edge getSquareExtensionEdge()
  {
    return squareExtensionEdge;
  }

  public Edge getOtherEdge()
  {
    return otherEdge;
  }
}
