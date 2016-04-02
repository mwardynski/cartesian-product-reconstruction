package at.ac.unileoben.mat.dissertation.common.impl;

import at.ac.unileoben.mat.dissertation.common.GraphCorrectnessChecker;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.structure.BipartiteColor;
import at.ac.unileoben.mat.dissertation.structure.Color;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 23.01.16
 * Time: 13:13
 * To change this template use File | Settings | File Templates.
 */
@Component
public class GraphCorrectnessCheckerImpl implements GraphCorrectnessChecker
{
  @Autowired
  GraphHelper graphHelper;

  @Override
  public boolean isSimple(List<Vertex> vertices)
  {
    Color[] graphColoringArray = graphHelper.createGraphColoringArray(vertices, Color.WHITE);
    for (Vertex v : vertices)
    {
      for (Edge e : v.getEdges())
      {
        if (graphColoringArray[e.getEndpoint().getVertexNo()] == Color.WHITE)
        {
          graphColoringArray[e.getEndpoint().getVertexNo()] = Color.BLACK;
        }
        else
        {
          return false;
        }
      }
      if (graphColoringArray[v.getVertexNo()] != Color.WHITE)
      {
        return false;
      }
      for (Edge e : v.getEdges())
      {
        graphColoringArray[e.getEndpoint().getVertexNo()] = Color.WHITE;
      }
    }
    return true;
  }

  @Override
  public boolean isConnected(List<Vertex> vertices)
  {
    boolean result = true;
    Color[] graphColoringArray = graphHelper.createGraphColoringArray(vertices, Color.WHITE);
    BipartiteColor[] graphBipartiteColoringArray = graphHelper.createGraphColoringArray(vertices, BipartiteColor.NONE);
    bfs(vertices.get(0), graphColoringArray, graphBipartiteColoringArray);
    for (Vertex v : vertices)
    {
      if (graphColoringArray[v.getVertexNo()] != Color.BLACK)
      {
        result = false;
      }
    }
    return result;
  }

  @Override
  public boolean isNotBipartite(List<Vertex> vertices)
  {
    Color[] graphColoringArray = graphHelper.createGraphColoringArray(vertices, Color.WHITE);
    BipartiteColor[] graphBipartiteColoringArray = graphHelper.createGraphColoringArray(vertices, BipartiteColor.NONE);
    boolean result = bfs(vertices.get(0), graphColoringArray, graphBipartiteColoringArray);
    return result;
  }

  /*
   public boolean isThin(List<Vertex> structure) {
       RelationFinder relationFinder = new RelationFinder();
       List<List<Vertex>> vertexClasses = relationFinder.findClasses(structure, RelationType.TypeR);
       if (vertexClasses.size() == structure.size()) {
           return true;
       }
       return false;
   }
    */
  private boolean bfs(Vertex root, Color[] graphColoringArray, BipartiteColor[] graphBipartiteColoringArray)
  {
    boolean result = false;
    graphColoringArray[root.getVertexNo()] = Color.GRAY;
    graphBipartiteColoringArray[root.getVertexNo()] = BipartiteColor.RED;
    Queue<Vertex> queue = new LinkedList<>();
    queue.add(root);
    while (!queue.isEmpty())
    {
      Vertex u = queue.poll();
      for (Edge e : u.getEdges())
      {
        Vertex v = e.getEndpoint();
        if (graphColoringArray[v.getVertexNo()] == Color.WHITE)
        {
          graphColoringArray[v.getVertexNo()] = Color.GRAY;
          if (graphBipartiteColoringArray[u.getVertexNo()] == BipartiteColor.RED)
          {
            graphBipartiteColoringArray[v.getVertexNo()] = BipartiteColor.BLUE;
          }
          else
          {
            graphBipartiteColoringArray[v.getVertexNo()] = BipartiteColor.RED;
          }
          queue.add(v);
        }
        else
        {
          if (graphBipartiteColoringArray[u.getVertexNo()] == graphBipartiteColoringArray[v.getVertexNo()])
          {
            result = true;
          }
        }
      }
      graphColoringArray[u.getVertexNo()] = Color.BLACK;
    }
    return result;
  }
}
