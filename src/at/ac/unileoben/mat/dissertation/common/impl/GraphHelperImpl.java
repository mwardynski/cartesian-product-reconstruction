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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

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
        createEdgeBetweenVertices(newVertex, neighborVertex);
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
            createEdgeBetweenVertices(newVertex, newVerticesArray[edgeEndpointVertexNo]);
          }
        }
      }
    }
    return newVertices;
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
        orderBFS(Arrays.asList(v), vertices, Optional.empty(), Collections.emptySet(),
                (currentVertex, previousVertex) -> connectedComponentVertices.add(currentVertex));
        connectedComponents.add(connectedComponentVertices);
      }
      graphColoringArray[v.getVertexNo()] = Color.WHITE;
    }

    return connectedComponents;
  }

  @Override
  public int getConnectedComponentSizeForColor(Vertex root, List<Vertex> vertices, FactorizationUnitLayerSpecData[] unitLayerSpecs, int color)
  {
    if (unitLayerSpecs[root.getVertexNo()] != null)
    {
      return unitLayerSpecs[root.getVertexNo()].getUnitLayerSize();
    }
    boolean[] includedColors = new boolean[graph.getGraphColoring().getOriginalColorsAmount()];
    FactorizationUnitLayerSpecData newFactorizationUnitLayerSpecData = new FactorizationUnitLayerSpecData(color, 1);
    orderBFS(Arrays.asList(root), vertices, Optional.of(color), Collections.emptySet(),
            (currentVertex, previousVertex) ->
            {
              int vertexNo = currentVertex.getVertexNo();
              boolean proceedWithCurrentVertex = false;
              if (unitLayerSpecs[vertexNo] == null)
              {
                newFactorizationUnitLayerSpecData.setUnitLayerSize(newFactorizationUnitLayerSpecData.getUnitLayerSize() + 1);
                proceedWithCurrentVertex = true;
              }
              else if (includedColors[unitLayerSpecs[vertexNo].getMappedColor()] == false)
              {
                int newUnitLayerSize = calculateNewUnitLayerSize(unitLayerSpecs, newFactorizationUnitLayerSpecData, color, vertexNo);
                newFactorizationUnitLayerSpecData.setUnitLayerSize(newUnitLayerSize);
                includedColors[unitLayerSpecs[vertexNo].getMappedColor()] = true;
              }
              unitLayerSpecs[vertexNo] = newFactorizationUnitLayerSpecData;
              return proceedWithCurrentVertex;
            });
    unitLayerSpecs[root.getVertexNo()] = newFactorizationUnitLayerSpecData;
    return newFactorizationUnitLayerSpecData.getUnitLayerSize();
  }

  private int calculateNewUnitLayerSize(FactorizationUnitLayerSpecData[] unitLayerSpecs, FactorizationUnitLayerSpecData newFactorizationUnitLayerSpecData, int color, int vertexNo)
  {
    int newUnitLayerSize = newFactorizationUnitLayerSpecData.getUnitLayerSize() + unitLayerSpecs[vertexNo].getUnitLayerSize();
    if (unitLayerSpecs[vertexNo].getMappedColor() != color)
    {
      newUnitLayerSize--;
    }
    return newUnitLayerSize;
  }

  @Override
  public List<Vertex> getFactorForTopVertices(List<Vertex> topVertices, List<Vertex> vertices)
  {
    List<Vertex> factorVertices = new ArrayList<>(vertices.size());
    Vertex[] reindexArray = new Vertex[vertices.size()];
    orderBFS(topVertices, vertices, Optional.empty(), EnumSet.of(EdgeType.UP),
            (currentVertex, previousVertex) ->
            {
              Vertex factorCurrentVertex = getFactorVertex(currentVertex, factorVertices, reindexArray, vertices.size());
              Vertex factorPreviousVertex = getFactorVertex(previousVertex, factorVertices, reindexArray, vertices.size());
              createEdgeBetweenVertices(factorCurrentVertex, factorPreviousVertex);
              return true;
            });
    return factorVertices;
  }

  private Vertex getFactorVertex(Vertex vertex, List<Vertex> factorVertices, Vertex[] reindexArray, int verticesSize)
  {
    Vertex factorCurrentVertex;
    if (reindexArray[vertex.getVertexNo()] == null)
    {
      Vertex newVertex = new Vertex(factorVertices.size(), new ArrayList<>(verticesSize));
      factorVertices.add(newVertex);
      reindexArray[vertex.getVertexNo()] = newVertex;
      factorCurrentVertex = newVertex;
    }
    else
    {
      factorCurrentVertex = reindexArray[vertex.getVertexNo()];
    }
    return factorCurrentVertex;
  }

  @Override
  public void createEdgeBetweenVertices(Vertex factorCurrentVertex, Vertex factorPreviousVertex)
  {
    Edge e1 = new Edge(factorCurrentVertex, factorPreviousVertex);
    Edge e2 = new Edge(factorPreviousVertex, factorCurrentVertex);
    e1.setOpposite(e2);
    e2.setOpposite(e1);
    factorCurrentVertex.getEdges().add(e1);
    factorPreviousVertex.getEdges().add(e2);
  }

  private void orderBFS(List<Vertex> roots, List<Vertex> vertices, Optional<Integer> currentColorOptional, Set<EdgeType> excludedEdgeTypes, BiFunction<Vertex, Vertex, Boolean> biFunction)
  {
    Color[] graphColoringArray = createGraphColoringArray(vertices, Color.WHITE);
    roots.stream().forEach(root -> graphColoringArray[root.getVertexNo()] = Color.GRAY);
    Queue<Vertex> queue = new LinkedList<>();
    queue.addAll(roots);
    while (!queue.isEmpty())
    {
      Vertex u = queue.poll();
      for (Edge e : u.getEdges())
      {
        if (isEdgeColorToByExcluded(currentColorOptional, e) || isEdgeTypeToBeExcluded(excludedEdgeTypes, e))
        {
          continue;
        }
        Vertex v = e.getEndpoint();
        if (graphColoringArray[v.getVertexNo()] == Color.WHITE)
        {
          Boolean proceedWithVertex = biFunction.apply(v, u);
          if (proceedWithVertex)
          {
            graphColoringArray[v.getVertexNo()] = Color.GRAY;
            queue.add(v);
          }
          else
          {
            graphColoringArray[v.getVertexNo()] = Color.BLACK;
          }
        }
      }
      graphColoringArray[u.getVertexNo()] = Color.BLACK;
    }
  }

  private boolean isEdgeColorToByExcluded(Optional<Integer> currentColorOptional, Edge e)
  {
    boolean exculdeEge = false;
    if (currentColorOptional.isPresent())
    {
      Integer currentColor = currentColorOptional.get();
      if (e.getLabel() == null
              || coloringService.getCurrentColorMapping(graph.getGraphColoring(), e.getLabel().getColor()) != currentColor)
      {
        exculdeEge = true;
      }
    }
    return exculdeEge;
  }

  private boolean isEdgeTypeToBeExcluded(Set<EdgeType> excludedEdgeTypes, Edge e)
  {
    boolean exculdeEge = false;
    if (excludedEdgeTypes.contains(e.getEdgeType()))
    {
      exculdeEge = true;
    }
    return exculdeEge;
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
    root.setBfsLayer(0);
    orderBFS(Arrays.asList(root), vertices, Optional.empty(), Collections.emptySet(),
            (currentVertex, previousVertex) ->
            {
              currentVertex.setBfsLayer(previousVertex.getBfsLayer() + 1);
              reindexArray[currentVertex.getVertexNo()] = counter.getAndIncrement();
              return true;
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
