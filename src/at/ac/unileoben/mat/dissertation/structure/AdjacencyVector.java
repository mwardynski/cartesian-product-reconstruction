package at.ac.unileoben.mat.dissertation.structure;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-01-29
 * Time: 20:36
 * To change this template use File | Settings | File Templates.
 */
public class AdjacencyVector
{
  Edge vector[];

  public AdjacencyVector(Edge vector[])
  {
    this.vector = vector;
  }

  public Edge[] getVector()
  {
    return vector;
  }

  public void setVector(Edge[] vector)
  {
    this.vector = vector;
  }
}
