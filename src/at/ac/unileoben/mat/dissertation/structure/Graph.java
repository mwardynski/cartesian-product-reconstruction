package at.ac.unileoben.mat.dissertation.structure;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-30
 * Time: 11:54
 * To change this template use File | Settings | File Templates.
 */
@Component
public class Graph
{
  private Vertex root;
  private List<Vertex> vertices;
  private GraphColoring graphColoring;
  private List<List<Vertex>> layers;

  private int[] reverseReindexArray;

  private AnalyzeData analyzeData;

  public Vertex getRoot()
  {
    return root;
  }

  public void setRoot(Vertex root)
  {
    this.root = root;
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

  public void setGraphColoring(GraphColoring graphColoring)
  {
    this.graphColoring = graphColoring;
  }

  public List<List<Vertex>> getLayers()
  {
    return layers;
  }

  public void setLayers(List<List<Vertex>> layers)
  {
    this.layers = layers;
  }

  public int[] getReverseReindexArray()
  {
    return reverseReindexArray;
  }

  public void setReverseReindexArray(int[] reverseReindexArray)
  {
    this.reverseReindexArray = reverseReindexArray;
  }

  public AnalyzeData getAnalyzeData()
  {
    return analyzeData;
  }

  public void setAnalyzeData(AnalyzeData analyzeData)
  {
    this.analyzeData = analyzeData;
  }
}
