package at.ac.unileoben.mat.dissertation.common;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

public class GraphCorrectnessChecker
{

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
        } else
        {
          counter[i] = 0;
        }
      }
    }
    return true;
  }
/*
    public boolean isNotBipartite(List<Vertex> structure) {
        boolean result = bfs(structure.iterator().next());
        recoverAfterBfs();
        return result;
    }

    public boolean isConnected(List<Vertex> structure) {
        boolean result = true;
        bfs(structure.iterator().next());
        for (Vertex v : structure) {
            if (v.colour != Color.BLACK) {
                result = false;
            }
        }
        recoverAfterBfs();
        return result;
    }

    public boolean isThin(List<Vertex> structure) {
        RelationFinder relationFinder = new RelationFinder();
        List<List<Vertex>> vertexClasses = relationFinder.findClasses(structure, RelationType.TypeR);
        if (vertexClasses.size() == structure.size()) {
            return true;
        }
        return false;
    }

    private boolean bfs(Vertex root) {
        boolean result = false;
        root.colour = Color.GREY;
        root.bipartiteColor = BipartiteColor.RED;
        Queue<Vertex> queue = new LinkedList<Vertex>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Vertex u = queue.poll();
            Iterator<Neighbor> it = u.neighbours.iterator();
            while (it.hasNext()) {
                Vertex v = structure.get(it.next().neighbourVertexNo);
                if (v.colour == Color.WHITE) {
                    v.colour = Color.GREY;
                    if (u.bipartiteColor == BipartiteColor.RED) {
                        v.bipartiteColor = BipartiteColor.BLUE;
                    } else {
                        v.bipartiteColor = BipartiteColor.RED;
                    }
                    queue.add(v);
                } else {
                    if (u.bipartiteColor == v.bipartiteColor) {
                        result = true;
                    }
                }
            }
            u.colour = Color.BLACK;
        }
        return result;
    }

    private void recoverAfterBfs() {
        for (Vertex v : structure) {
            v.colour = Color.WHITE;
            v.bipartiteColor = null;
        }
    }
    */
}
