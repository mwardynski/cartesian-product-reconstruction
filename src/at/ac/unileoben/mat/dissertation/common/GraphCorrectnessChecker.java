package at.ac.unileoben.mat.dissertation.common;

import at.ac.unileoben.mat.dissertation.structure.BipartiteColor;
import at.ac.unileoben.mat.dissertation.structure.Color;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Component
public class GraphCorrectnessChecker
{

  public final static String NOT_HIGH_ENOUGH = "The input graph has not at least 3 layers";
  public final static String NOT_SIMPLE = "The input graph is not a simple graph";
  public final static String NOT_CONNECTED = "The input graph is not a connected graph";
  public final static String NOT_THIN = "The input graph is not a thin graph";
  public final static String BIPARTITE = "The input graph is a bipartite graph";

  public boolean isSimple(List<Vertex> graph)
  {
    int[] counter = new int[graph.size()];
    for (Vertex v : graph)
    {
      for (Edge e : v.getEdges())
      {
        counter[e.getEndpoint().getVertexNo()]++;
      }
      if (counter[v.getVertexNo()] != 0)
      {
        return false;
      }
      for (int i = 0; i < counter.length; i++)
      {
        if (counter[i] > 1)
        {
          return false;
        }
        else
        {
          counter[i] = 0;
        }
      }
    }
    return true;
  }

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
