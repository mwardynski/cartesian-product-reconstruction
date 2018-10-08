package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;

public class SingleSquareData
{
  Edge baseEdge;
  Edge otherEdge;
  Edge squareBaseEdge;
  Edge squareOtherEdge;

  List<Edge> diagonals;

  public SingleSquareData(Edge baseEdge, Edge otherEdge, Edge squareBaseEdge, Edge squareOtherEdge)
  {
    this.baseEdge = baseEdge;
    this.otherEdge = otherEdge;
    this.squareBaseEdge = squareBaseEdge;
    this.squareOtherEdge = squareOtherEdge;
    this.diagonals = new LinkedList<>();
  }

  public Edge getBaseEdge()
  {
    return baseEdge;
  }

  public Edge getOtherEdge()
  {
    return otherEdge;
  }

  public Edge getSquareBaseEdge()
  {
    return squareBaseEdge;
  }

  public Edge getSquareOtherEdge()
  {
    return squareOtherEdge;
  }

  public List<Edge> getDiagonals()
  {
    return diagonals;
  }
}
