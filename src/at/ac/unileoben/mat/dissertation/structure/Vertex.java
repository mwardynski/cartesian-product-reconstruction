package at.ac.unileoben.mat.dissertation.structure;

import java.util.ArrayList;
import java.util.LinkedList;
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
  private boolean unitLayer;
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

  public boolean isUnitLayer()
  {
    return unitLayer;
  }

  public void setUnitLayer(boolean unitLayer)
  {
    this.unitLayer = unitLayer;
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

  public Edge getEdgeByLabel(Label label, EdgeType edgeType)
  {
    EdgesGroup edgeGroup = getEdgeGroupForEdgeType(edgeType);

    List<Edge> edges = edgeGroup.getEdges();
    int positionForLabel1 = edgeGroup.getEdgesRef().getPositionForLabel(label);
    int positionForLabel = positionForLabel1;
    if (positionForLabel != -1)
    {
      return edges.get(positionForLabel);
    }
    else
    {
      return null;
    }
  }

  public Edge getEdgeOfDifferentColor(int color, GraphColoring graphColoring)
  {
    EdgesRef edgesRef = getDownEdges().getEdgesRef();
    List<Edge> edges = getDownEdges().getEdges();
    for (int i = 0; i < edgesRef.getColorsAmount(); i++)
    {
      if (graphColoring.getCurrentColorMapping(i) != graphColoring.getCurrentColorMapping(color))
      {
        int positionForLabel = edgesRef.getPositionForLabel(new Label(0, i));
        if (positionForLabel != -1)
        {
          return edges.get(positionForLabel);
        }
      }
    }
    return null;
  }

  public List<Edge> getAllEdgesOfDifferentColor(int color, GraphColoring graphColoring, EdgeType edgeType)
  {
    EdgesGroup edgeGroup = getEdgeGroupForEdgeType(edgeType);
    EdgesRef edgesRef = edgeGroup.getEdgesRef();
    List<Edge> allDownEdges = edgeGroup.getEdges();

    List<Edge> resultEdges = new LinkedList<Edge>();
    for (int i = 0; i < edgesRef.getAllColorsAmount(); i++)
    {
      if (graphColoring.getCurrentColorMapping(i) != graphColoring.getCurrentColorMapping(color))
      {
        List<Integer> positionsForColor = edgesRef.getPositionsForColor(i);
        for (int edgePosition : positionsForColor)
        {
          resultEdges.add(allDownEdges.get(edgePosition));
        }
      }
    }
    return resultEdges;
  }

  private EdgesGroup getEdgeGroupForEdgeType(EdgeType edgeType)
  {
    if (edgeType == EdgeType.DOWN)
    {
      return getDownEdges();
    }
    else if (edgeType == EdgeType.CROSS)
    {
      return getCrossEdges();
    }
    else
    {
      return getUpEdges();
    }
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
