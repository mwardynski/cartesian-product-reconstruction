package at.ac.unileoben.mat.dissertation.structure;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;

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
  ColoringService coloringService = new ColoringService();

  private Vertex root;
  private List<Vertex> vertices;
  private GraphColoring graphColoring;
  private List<List<Vertex>> layers;

  private int[] reindexArray;

  private AnalyzeData analyzeData;

  public Graph(List<Vertex> vertices, Vertex root, List<List<Vertex>> layers)
  {
    this.vertices = vertices;
    this.root = root;
    graphColoring = new GraphColoring(root.getEdges().size());
    this.layers = layers;

    analyzeData = new AnalyzeData();
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

  public Vertex getRoot()
  {
    return root;
  }

  public List<List<Vertex>> getLayers()
  {
    return layers;
  }

  public int[] getReindexArray()
  {
    return reindexArray;
  }

  public void setReindexArray(int[] reindexArray)
  {
    this.reindexArray = reindexArray;
  }

  public int getLayersAmount()
  {
    return layers.size();
  }

  public AnalyzeData getAnalyzeData()
  {
    return analyzeData;
  }
}
