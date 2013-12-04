package at.ac.unileoben.mat.dissertation.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-12-04
 * Time: 20:50
 * To change this template use File | Settings | File Templates.
 */
public class ConsistencyCheckResult
{
  List<Vertex> inconsistentVertices;
  boolean[] inconsistentVertexIndexes;

  public ConsistencyCheckResult(int size)
  {
    inconsistentVertices = new ArrayList<Vertex>(size);
    inconsistentVertexIndexes = new boolean[size];
  }

  public boolean isNotEmpty()
  {
    return inconsistentVertices.size() != 0;
  }

  public List<Vertex> getInconsistentVertices()
  {
    return inconsistentVertices;
  }

  public void addVertex(Vertex v)
  {
    if (!inconsistentVertexIndexes[v.getVertexNo()])
    {
      inconsistentVertexIndexes[v.getVertexNo()] = true;
      inconsistentVertices.add(v);
    }
  }
}
