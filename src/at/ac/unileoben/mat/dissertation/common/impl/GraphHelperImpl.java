package at.ac.unileoben.mat.dissertation.common.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.GraphReader;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 23.01.16
 * Time: 11:48
 * To change this template use File | Settings | File Templates.
 */
@Component
public class GraphHelperImpl implements GraphHelper
{

  @Autowired
  Graph graph;

  @Autowired
  GraphReader graphReader;

  @Autowired
  VertexService vertexService;

  @Override
  public List<Vertex> parseGraph(String graphFilePath)
  {
    return graphReader.readGraph(graphFilePath);
  }

  @Override
  public void addVertex(List<Vertex> allVertices, List<Vertex> neighbors)
  {
    Vertex newVertex = new Vertex(allVertices.size(), new ArrayList<Edge>(allVertices.size()));

    boolean[] addedNeighbors = new boolean[allVertices.size()];
    for (Vertex neighborVertex : neighbors)
    {
      if (!addedNeighbors[neighborVertex.getVertexNo()])
      {
        addEdge(newVertex, neighborVertex);
        addedNeighbors[neighborVertex.getVertexNo()] = true;
      }
    }
    allVertices.add(newVertex);
  }

  public List<Vertex> copySubgraph(List<Vertex> allVertices, Optional<Vertex> vertexToRemoveOptional)
  {
    Vertex[] newVerticesArray = new Vertex[allVertices.size()];
    List<Vertex> newVertices = new ArrayList<>(allVertices.size() - 1);

    for (int i = 0; i < allVertices.size(); i++)
    {
      if (!vertexToRemoveOptional.isPresent() || i != vertexToRemoveOptional.get().getVertexNo())
      {
        Vertex newVertex = new Vertex(i, new ArrayList<Edge>(allVertices.size()));
        newVerticesArray[i] = newVertex;
        newVertices.add(newVertex);

        Vertex origVertex = allVertices.get(i);
        for (Edge e : origVertex.getEdges())
        {
          int edgeEndpointVertexNo = e.getEndpoint().getVertexNo();
          if ((!vertexToRemoveOptional.isPresent() || edgeEndpointVertexNo != vertexToRemoveOptional.get().getVertexNo()) && edgeEndpointVertexNo < i)
          {
            addEdge(newVertex, newVerticesArray[edgeEndpointVertexNo]);
          }
        }
      }
    }
    return newVertices;
  }

  private void addEdge(Vertex newVertex, Vertex neighborVertex)
  {
    Edge e1 = new Edge(newVertex, neighborVertex);
    Edge e2 = new Edge(neighborVertex, newVertex);
    e1.setOpposite(e2);
    e2.setOpposite(e1);
    newVertex.getEdges().add(e1);
    neighborVertex.getEdges().add(e2);
  }

  public List<List<Vertex>> getGraphConnectedComponents(List<Vertex> vertices)
  {
    List<List<Vertex>> connectedComponents = new LinkedList<>();

    for (Vertex v : vertices)
    {
      if (v.getColor() == Color.WHITE)
      {
        List<Vertex> connectedComponentVertices = orderBFS(v, new Integer[vertices.get(vertices.size() - 1).getVertexNo() + 1]);
        connectedComponents.add(connectedComponentVertices);
      }
      v.setColor(Color.WHITE);
    }

    return connectedComponents;
  }

  @Override
  public List<Vertex> orderBFS(Vertex root, Integer[] reindexArray)
  {
    List<Vertex> visitedVertices = new ArrayList<>(reindexArray.length);
    int counter = 0;
    root.setColor(Color.GRAY);
    root.setBfsLayer(0);
    reindexArray[root.getVertexNo()] = counter++;
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
          v.setBfsLayer(u.getBfsLayer() + 1);
          reindexArray[v.getVertexNo()] = counter++;
          queue.add(v);
        }
      }
      u.setColor(Color.BLACK);
      visitedVertices.add(u);
    }
    return visitedVertices;
  }

  @Override
  public boolean isGraphK1(List<Vertex> vertices)
  {
    return vertices.size() == 1;
  }

  @Override
  public boolean isGraphK2(List<Vertex> vertices)
  {
    return vertices.size() == 2;
  }

  @Override
  public boolean isGraphC8(List<Vertex> vertices)
  {
    boolean isC8 = true;
    if (vertices.size() == 8)
    {
      for (Vertex v : vertices)
      {
        if (v.getEdges().size() != 2)
        {
          isC8 = false;
        }
      }
    }
    else
    {
      isC8 = false;
    }

    return isC8;
  }

  @Override
  public void prepareGraphBfsStructure(List<Vertex> vertices, Vertex root)
  {
    if (root == null)
    {
      root = findVertexWithMinDegree(vertices);
    }
    Integer[] reindexArray = new Integer[findMaxVertexNo(vertices) + 1];
    orderBFS(root, reindexArray);
    reindex(vertices, reindexArray);
    vertices = sortVertices(vertices);
    sortEdges(vertices);
    arrangeEdgesToThreeGroups(vertices);
    createNewGraph(vertices, root, reindexArray);
  }

  @Override
  public void revertGraphBfsStructure()
  {
    List<Vertex> vertices = graph.getVertices();
    Integer[] reverseReindexArray = graph.getReverseReindexArray();
    reindex(vertices, reverseReindexArray);
//    vertices = sortVertices(vertices);
//    sortEdges(vertices);
    graph.setVertices(vertices);
  }

  private Vertex findVertexWithMinDegree(List<Vertex> vertices)
  {
    int min = Integer.MAX_VALUE;
    Vertex result = null;
    for (Vertex v : vertices)
    {
      int size = v.getEdges().size();
      if (min > size)
      {
        min = size;
        result = v;
      }
    }
    return result;
  }

  private int findMaxVertexNo(List<Vertex> vertices)
  {
    int max = Integer.MIN_VALUE;
    for (Vertex v : vertices)
    {
      int number = v.getVertexNo();
      if (max < number)
      {
        max = number;
      }
    }
    return max;
  }

  private void reindex(List<Vertex> vertices, Integer[] reindexArray)
  {
    for (Vertex v : vertices)
    {
      v.setVertexNo(reindexArray[v.getVertexNo()]);
    }
  }

  private List<Vertex> sortVertices(List<Vertex> vertices)
  {
    int min = 0;
    int max = vertices.size() - 1;

    int[] frequencyArray = new int[max - min + 1];

    for (Vertex v : vertices)
    {
      frequencyArray[v.getVertexNo() - min]++;
    }

    for (int i = 1; i < frequencyArray.length; i++)
    {
      frequencyArray[i] += frequencyArray[i - 1];
    }

    Vertex[] result = new Vertex[vertices.size()];
    ListIterator<Vertex> lit = vertices.listIterator(vertices.size());
    while (lit.hasPrevious())
    {
      Vertex v = lit.previous();
      result[--frequencyArray[v.getVertexNo()]] = v;
    }
    return Arrays.asList(result);
  }

  private void sortEdges(List<Vertex> vertices)
  {
    List<List<Edge>> tmpEdges = new ArrayList<List<Edge>>(vertices.size());
    for (int i = 0; i < vertices.size(); i++)
    {
      tmpEdges.add(new ArrayList<Edge>(GraphReaderImpl.MAX_NEIGHBOURS_ACCOUNT));
    }
    for (Vertex v : vertices)
    {
      for (Edge e : v.getEdges())
      {
        Edge oppositeEdge = e.getOpposite();
        int endpointVertexNo = e.getEndpoint().getVertexNo();
        tmpEdges.get(endpointVertexNo).add(oppositeEdge);
      }
    }
    for (int i = 0; i < vertices.size(); i++)
    {
      vertices.get(i).setEdges(tmpEdges.get(i));
    }
  }

  private void arrangeEdgesToThreeGroups(List<Vertex> vertices)
  {
    for (Vertex v : vertices)
    {
      List<Edge> downEdges = new ArrayList<Edge>();
      List<Edge> crossEdges = new ArrayList<Edge>();
      List<Edge> upEdges = new ArrayList<Edge>();

      for (Edge e : v.getEdges())
      {
        if (v.getBfsLayer() > e.getEndpoint().getBfsLayer())
        {
          e.setEdgeType(EdgeType.DOWN);
          downEdges.add(e);
        }
        else if (v.getBfsLayer() == e.getEndpoint().getBfsLayer())
        {
          e.setEdgeType(EdgeType.CROSS);
          crossEdges.add(e);
        }
        else
        {
          e.setEdgeType(EdgeType.UP);
          upEdges.add(e);
        }
      }
      v.setDownEdges(new EdgesGroup(downEdges));
      v.setCrossEdges(new EdgesGroup(crossEdges));
      v.setUpEdges(new EdgesGroup(upEdges));
    }
  }

  private void createNewGraph(List<Vertex> vertices, Vertex root, Integer[] reindexArray)
  {
    graph.setRoot(root);
    graph.setVertices(vertices);
    graph.setLayers(vertexService.createLayersList(vertices));
    graph.setReverseReindexArray(createReverseReindexArray(reindexArray));
    graph.setGraphColoring(new GraphColoring(root.getEdges().size()));
    graph.setAnalyzeData(new AnalyzeData());
  }

  private Integer[] createReverseReindexArray(Integer[] reindexArray)
  {
    Integer[] reverseReindexArray = new Integer[reindexArray.length];
    for (int i = 0; i < reindexArray.length; i++)
    {
      Integer reverseCell = reindexArray[i];
      if (reverseCell != null)
      {
        reverseReindexArray[reverseCell] = i;
      }
    }
    return reverseReindexArray;
  }
}
