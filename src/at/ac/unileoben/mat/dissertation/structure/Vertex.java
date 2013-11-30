package at.ac.unileoben.mat.dissertation.structure;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-29
 * Time: 15:20
 * To change this template use File | Settings | File Templates.
 */
public class Vertex
{

  private int vertexNo;
  private List<Edge> edges;
  private Color color;
  private int bfsLayer;
  private EdgesGroup downEdges;
  private EdgesGroup crossEdges;
  private EdgesGroup upEdges;


  public Vertex(int vertexNo, List<Edge> edges)
  {
    this.vertexNo = vertexNo;
    this.edges = edges;
    color = Color.WHITE;
  }

  public int getVertexNo()
  {
    return vertexNo;
  }

  public void setVertexNo(int vertexNo)
  {
    this.vertexNo = vertexNo;
  }

  public List<Edge> getEdges()
  {
    return edges;
  }

  public void setEdges(List<Edge> edges)
  {
    this.edges = edges;
  }

  public Color getColor()
  {
    return color;
  }

  public void setColor(Color color)
  {
    this.color = color;
  }

  public int getBfsLayer()
  {
    return bfsLayer;
  }

  public void setBfsLayer(int bfsLayer)
  {
    this.bfsLayer = bfsLayer;
  }

  public EdgesGroup getDownEdges()
  {
    return downEdges;
  }

  public void setDownEdges(EdgesGroup downEdges)
  {
    this.downEdges = downEdges;
  }

  public EdgesGroup getCrossEdges()
  {
    return crossEdges;
  }

  public void setCrossEdges(EdgesGroup crossEdges)
  {
    this.crossEdges = crossEdges;
  }

  public EdgesGroup getUpEdges()
  {
    return upEdges;
  }

  public void setUpEdges(EdgesGroup upEdges)
  {
    this.upEdges = upEdges;
  }

  @Override
  public String toString()
  {
    List<Integer> neighbors = new ArrayList<Integer>(getEdges().size());
    for (Edge e : getEdges())
    {
      neighbors.add(e.getEndpoint().getVertexNo());
    }
    return String.format("%d(L%d): %s", getVertexNo(), getBfsLayer(), neighbors);
  }
}
