package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;

public class SpreadingIntervalVerticesCollection
{
  boolean[] verticesOccurance;
  List<Vertex> vertices;

  public SpreadingIntervalVerticesCollection(int verticesQuantity)
  {
    verticesOccurance = new boolean[verticesQuantity];
    vertices = new LinkedList<>();
  }

  public boolean[] getVerticesOccurance()
  {
    return verticesOccurance;
  }

  public List<Vertex> getVertices()
  {
    return vertices;
  }

  public void setVertices(List<Vertex> vertices)
  {
    this.vertices = vertices;
  }
}
