package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;

public class NoSquareAtAllCycleNode
{
  private Vertex vertex;
  private int distance;
  private List<NoSquareAtAllCycleNode> previousVerticesNodes;

  public NoSquareAtAllCycleNode(Vertex vertex, int distance)
  {
    this.vertex = vertex;
    this.distance = distance;
    this.previousVerticesNodes = new LinkedList<>();
  }

  public Vertex getVertex()
  {
    return vertex;
  }

  public int getDistance()
  {
    return distance;
  }

  public List<NoSquareAtAllCycleNode> getPreviousVerticesNodes()
  {
    return previousVerticesNodes;
  }
}
