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

  private AnalyzeData analyzeData;

  public Graph(List<Vertex> vertices)
  {
    this.vertices = vertices;
    graphColoring = new GraphColoring(getRoot().getEdges().size());
    layers = createLayersList();

    analyzeData = new AnalyzeData();
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

  public AnalyzeData getAnalyzeData()
  {
    return analyzeData;
  }

  public void assignVertexToUnitLayerAndMergeColors(Vertex v, boolean mergeCrossEdges, MergeTagEnum mergeTag) //mergeCrossEdges always true
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
      Vertex w = vw.getEndpoint();
      w.setUnitLayer(true);
    }
    mergeColorsForEdges(edgesToRelabel, mergeTag);
  }

  public List<Integer> getColorsForEdges(List<Edge> edges)
  {
    boolean[] colorPresence = new boolean[graphColoring.getOriginalColorsAmount()];
    for (Edge e : edges)
    {
      Label label = e.getLabel();
      if(label != null)
      {
        int currentLabelColor = graphColoring.getCurrentColorMapping(label.getColor());
        colorPresence[currentLabelColor] = true;
      }
    }
    List<Integer> colors = new ArrayList<Integer>(colorPresence.length);
    for (int i = 0; i < colorPresence.length; i++)
    {
      if (colorPresence[i])
      {
        colors.add(i);
      }
    }
    return colors;
  }

  public boolean mergeColorsForEdges(List<Edge> edges, MergeTagEnum mergeTag)
  {
    List<Integer> colorsToMerge = getColorsForEdges(edges);
    boolean colorsMerged = false;
    if(colorsToMerge.size() > 0)
    {
      colorsMerged = graphColoring.mergeColors(colorsToMerge);
    }
    if(colorsMerged)
    {
      analyzeData.addMergeOperation(graphColoring.getActualColors().size(), edges, mergeTag);
    }
    return colorsMerged;
  }
}
