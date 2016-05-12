package at.ac.unileoben.mat.dissertation.common.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.GraphReader;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

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

  @Autowired
  ColoringService coloringService;

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

  @Override
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
    Color[] graphColoringArray = createGraphColoringArray(vertices, Color.WHITE);
    List<List<Vertex>> connectedComponents = new LinkedList<>();

    for (Vertex v : vertices)
    {
      if (graphColoringArray[v.getVertexNo()] == Color.WHITE)
      {
        List<Vertex> connectedComponentVertices = new ArrayList<>(vertices.size());
        orderBFS(v, vertices, Optional.empty(),
                (currentVertex, previousVertex) -> connectedComponentVertices.add(currentVertex));
        connectedComponents.add(connectedComponentVertices);
      }
      graphColoringArray[v.getVertexNo()] = Color.WHITE;
    }

    return connectedComponents;
  }

  @Override
  public List<Vertex> getConnectedComponentForColor(Vertex root, List<Vertex> vertices, int color)
  {
    List<Vertex> connectedComponentVertices = new ArrayList<>(vertices.size());
    AtomicBoolean isFirstVertexMissing = new AtomicBoolean(true);
    orderBFS(root, vertices, Optional.of(color),
            (currentVertex, previousVertex) -> {
              if (isFirstVertexMissing.getAndSet(false))
              {
                connectedComponentVertices.add(previousVertex);
              }
              connectedComponentVertices.add(currentVertex);
            });
    return connectedComponentVertices;
  }

  private void orderBFS(Vertex root, List<Vertex> vertices, Optional<Integer> currentColorOptional, BiConsumer<Vertex, Vertex> biConsumer)
  {
    Color[] graphColoringArray = createGraphColoringArray(vertices, Color.WHITE);
    graphColoringArray[root.getVertexNo()] = Color.GRAY;
    root.setBfsLayer(0);
    Queue<Vertex> queue = new LinkedList<>();
    queue.add(root);
    while (!queue.isEmpty())
    {
      Vertex u = queue.poll();
      for (Edge e : u.getEdges())
      {
        if (currentColorOptional.isPresent())
        {
          Integer currentColor = currentColorOptional.get();
          if (e.getLabel() == null
                  || coloringService.getCurrentColorMapping(graph.getGraphColoring(), e.getLabel().getColor()) != currentColor)
          {
            continue;
          }
        }
        Vertex v = e.getEndpoint();
        if (graphColoringArray[v.getVertexNo()] == Color.WHITE)
        {
          graphColoringArray[v.getVertexNo()] = Color.GRAY;
          biConsumer.accept(v, u);
          queue.add(v);
        }
      }
      graphColoringArray[u.getVertexNo()] = Color.BLACK;
    }
  }


  @Override
  public <T> T[] createGraphColoringArray(List<Vertex> vertices, T defaultColor)
  {
    int vertexMaxNo = vertices.stream().mapToInt(Vertex::getVertexNo).max().getAsInt();
    T[] graphColoring = (T[]) Array.newInstance(defaultColor.getClass(), vertexMaxNo + 1);
    for (Vertex v : vertices)
    {
      graphColoring[v.getVertexNo()] = defaultColor;
    }
    return graphColoring;
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
    AtomicInteger counter = new AtomicInteger(0);
    reindexArray[root.getVertexNo()] = counter.getAndIncrement();
    orderBFS(root, vertices, Optional.empty(),
            (currentVertex, previousVertex) -> {
              currentVertex.setBfsLayer(previousVertex.getBfsLayer() + 1);
              reindexArray[currentVertex.getVertexNo()] = counter.getAndIncrement();
            });

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
      tmpEdges.add(new ArrayList<Edge>(GraphReaderImpl.MAX_NEIGHBOURS_AMOUNT));
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
