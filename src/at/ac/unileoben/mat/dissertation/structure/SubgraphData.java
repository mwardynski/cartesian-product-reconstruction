package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class SubgraphData
{
  List<Vertex> vertices;
  Integer[] reverseReindexArray;

  public List<Vertex> getVertices()
  {
    return vertices;
  }

  public void setVertices(List<Vertex> vertices)
  {
    this.vertices = vertices;
  }

  public Integer[] getReverseReindexArray()
  {
    return reverseReindexArray;
  }

  public void setReverseReindexArray(Integer[] reverseReindexArray)
  {
    this.reverseReindexArray = reverseReindexArray;
  }
}
