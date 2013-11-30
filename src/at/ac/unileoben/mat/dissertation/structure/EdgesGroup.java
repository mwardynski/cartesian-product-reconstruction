package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-30
 * Time: 11:37
 * To change this template use File | Settings | File Templates.
 */
public class EdgesGroup
{
  List<Edge> edges;
  EdgesRef edgesRef;

  public EdgesGroup(List<Edge> edges)
  {
    this.edges = edges;
  }

  public List<Edge> getEdges()
  {
    return edges;
  }

  public EdgesRef getEdgesRef()
  {
    return edgesRef;
  }

  public void setEdgesRef(EdgesRef edgesRef)
  {
    this.edgesRef = edgesRef;
  }
}
