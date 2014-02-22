package at.ac.unileoben.mat.dissertation.structure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-30
 * Time: 11:54
 * To change this template use File | Settings | File Templates.
 */
public class Graph
{
  private List<Vertex> vertices;
  private GraphColoring graphColoring;
  private List<List<Vertex>> layers;

  public Graph(List<Vertex> vertices)
  {
    this.vertices = vertices;
    graphColoring = new GraphColoring(getRoot().getEdges().size());
    layers = createLayersList();
  }

  private List<List<Vertex>> createLayersList()
  {
    int layersCount = vertices.get(vertices.size() - 1).getBfsLayer() + 1;
    List<List<Vertex>> layers = new ArrayList<List<Vertex>>(layersCount);
    for (int i = 0; i < layersCount; i++)
    {
      layers.add(new ArrayList<Vertex>());
    }
    for (Vertex v : vertices)
    {
      layers.get(v.getBfsLayer()).add(v);
    }
    return layers;
  }

  public List<Vertex> getVertices()
  {
    return vertices;
  }

  public void setVertices(List<Vertex> vertices)
  {
    this.vertices = vertices;
  }

  public GraphColoring getGraphColoring()
  {
    return graphColoring;
  }

  public List<Vertex> getLayer(int i)
  {
    return layers.get(i);
  }

  public Vertex getRoot()
  {
    return vertices.get(0);
  }

  public int getLayersAmount()
  {
    return layers.size();
  }

  public void assignVertexToUnitLayerAndMergeColors(Vertex v, boolean mergeCrossEdges)
  {
    v.setUnitLayer(true);
    List<Edge> vDownEdges = v.getDownEdges().getEdges();
    List<Edge> edgesToRelabel = new LinkedList<Edge>(vDownEdges);
    if (mergeCrossEdges)
    {
      List<Edge> vCrossEdges = v.getCrossEdges().getEdges();
      edgesToRelabel.addAll(vCrossEdges);
    }
    boolean[] colorPresence = new boolean[graphColoring.getOriginalColorsAmount()];
    for (Edge vw : edgesToRelabel)
    {
      if (!colorPresence[vw.getLabel().getColor()])
      {
        colorPresence[vw.getLabel().getColor()] = true;
      }
      Vertex w = vw.getEndpoint();
      w.setUnitLayer(true);
    }
    List<Integer> colorsToMerge = new ArrayList<Integer>(colorPresence.length);
    for (int i = 0; i < colorPresence.length; i++)
    {
      if (colorPresence[i])
      {
        colorsToMerge.add(i);
      }
    }
    graphColoring.mergeColors(colorsToMerge);
  }
}
