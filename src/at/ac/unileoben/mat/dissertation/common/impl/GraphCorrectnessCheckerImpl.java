package at.ac.unileoben.mat.dissertation.common.impl;

import at.ac.unileoben.mat.dissertation.common.GraphCorrectnessChecker;
import at.ac.unileoben.mat.dissertation.structure.BipartiteColor;
import at.ac.unileoben.mat.dissertation.structure.Color;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
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

  @Override
  public boolean isSimple(List<Vertex> graph)
  {
    for (Vertex v : graph)
    {
      for (Edge e : v.getEdges())
      {
        if (e.getEndpoint().getColor() == Color.WHITE)
        {
          e.getEndpoint().setColor(Color.BLACK);
        }
        else
        {
          return false;
        }
      }
      if (v.getColor() != Color.WHITE)
      {
        return false;
      }
      for (Edge e : v.getEdges())
      {
        e.getEndpoint().setColor(Color.WHITE);
      }
    }
    return true;
  }

  @Override
  public boolean isConnected(List<Vertex> graph)
  {
    boolean result = true;
    bfs(graph.get(0));
    for (Vertex v : graph)
    {
      if (v.getColor() != Color.BLACK)
      {
        result = false;
      }
    }
    removeGraphColoring(graph);
    return result;
  }

  @Override
  public boolean isNotBipartite(List<Vertex> graph)
  {
    boolean result = bfs(graph.get(0));
    removeGraphColoring(graph);
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
  private boolean bfs(Vertex root)
  {
    boolean result = false;
    root.setColor(Color.GRAY);
    root.setBipartiteColor(BipartiteColor.RED);
    Queue<Vertex> queue = new LinkedList<Vertex>();
    queue.add(root);
    while (!queue.isEmpty())
    {
      Vertex u = queue.poll();
      for (Edge e : u.getEdges())
      {
        Vertex v = e.getEndpoint();
        if (v.getColor() == Color.WHITE)
        {
          v.setColor(Color.GRAY);
          if (u.getBipartiteColor() == BipartiteColor.RED)
          {
            v.setBipartiteColor(BipartiteColor.BLUE);
          }
          else
          {
            v.setBipartiteColor(BipartiteColor.RED);
          }
          queue.add(v);
        }
        else
        {
          if (u.getBipartiteColor() == v.getBipartiteColor())
          {
            result = true;
          }
        }
      }
      u.setColor(Color.BLACK);
    }
    return result;
  }

  private void removeGraphColoring(List<Vertex> graph)
  {
    for (Vertex v : graph)
    {
      v.setColor(Color.WHITE);
      v.setBipartiteColor(null);
    }
  }
}
