package at.ac.unileoben.mat.dissertation.common.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.GraphReader;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
  ReconstructionData reconstructionData;

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

  @Override
  public List<List<Vertex>> getGraphConnectedComponents(List<Vertex> vertices)
  {
    Color[] graphColoringArray = createGraphColoringArray(vertices, Color.WHITE);
    List<List<Vertex>> connectedComponents = new LinkedList<>();

    for (Vertex v : vertices)
    {
      if (graphColoringArray[v.getVertexNo()] == Color.WHITE)
      {
        List<Vertex> connectedComponentVertices = getGraphConnectedComponentVerticesForColor(v, vertices, Optional.empty());
        connectedComponents.add(connectedComponentVertices);
      }
      graphColoringArray[v.getVertexNo()] = Color.WHITE;
    }

    return connectedComponents;
  }

  @Override
  public List<Vertex> getGraphConnectedComponentVerticesForColor(Vertex vertex, List<Vertex> vertices, Optional<Integer> colorOptional)
  {
    List<Vertex> connectedComponentVertices = new ArrayList<>(vertices.size());
    orderBFS(Arrays.asList(vertex), vertices, colorOptional, Collections.emptySet(),
            (currentVertex, previousVertex) -> connectedComponentVertices.add(currentVertex),
            (currentVertex, previousVertex) ->
            {
            });
    return connectedComponentVertices;
  }

  @Override
  public List<Edge> getGraphConnectedComponentEdgesForColor(Vertex vertex, List<Vertex> vertices, Optional<Integer> colorOptional, Edge[][] adjacencyMatrix)
  {
    List<Edge> connectedComponentEdges = new LinkedList<>();
    orderBFS(Arrays.asList(vertex), vertices, colorOptional, Collections.emptySet(),
            (currentVertex, previousVertex) -> connectedComponentEdges.add(adjacencyMatrix[previousVertex.getVertexNo()][currentVertex.getVertexNo()]),
            (currentVertex, previousVertex) ->
            {
            });
    return connectedComponentEdges;
  }


  @Override
  public int getConnectedComponentSizeForColor(List<Vertex> topVertices, List<Vertex> vertices, FactorizationUnitLayerSpecData[] unitLayerSpecs, int color)
  {
    boolean[] includedColors = new boolean[graph.getGraphColoring().getOriginalColorsAmount()];
    FactorizationUnitLayerSpecData newFactorizationUnitLayerSpecData = new FactorizationUnitLayerSpecData(color, 0);

    Iterator<Vertex> topVerticesIterator = topVertices.iterator();
    while (topVerticesIterator.hasNext())
    {
      Vertex topVertex = topVerticesIterator.next();
      if (unitLayerSpecs[topVertex.getVertexNo()] != null)
      {
        handleUnitLayerVertexWithSpec(topVertex, topVerticesIterator, newFactorizationUnitLayerSpecData, unitLayerSpecs, color, includedColors);
      }
      else
      {
        handleUnitLayerVertexWithoutSpec(topVertex, vertices, newFactorizationUnitLayerSpecData, unitLayerSpecs, color, includedColors);
      }
      unitLayerSpecs[topVertex.getVertexNo()] = newFactorizationUnitLayerSpecData;
    }
    return newFactorizationUnitLayerSpecData.getUnitLayerSize();
  }

  private void handleUnitLayerVertexWithSpec(Vertex topVertex, Iterator<Vertex> topVerticesIterator, FactorizationUnitLayerSpecData newFactorizationUnitLayerSpecData, FactorizationUnitLayerSpecData[] unitLayerSpecs, int color, boolean[] includedColors)
  {
    int topVertexNo = topVertex.getVertexNo();
    if (includedColors[unitLayerSpecs[topVertexNo].getMappedColor()] == false)
    {
      int newUnitLayerSize = calculateNewUnitLayerSize(unitLayerSpecs, newFactorizationUnitLayerSpecData, color, topVertexNo);
      newFactorizationUnitLayerSpecData.setUnitLayerSize(newUnitLayerSize);
      includedColors[unitLayerSpecs[topVertexNo].getMappedColor()] = true;
    }
    else
    {
      topVerticesIterator.remove();
    }
  }

  private void handleUnitLayerVertexWithoutSpec(Vertex topVertex, List<Vertex> vertices, FactorizationUnitLayerSpecData newFactorizationUnitLayerSpecData, FactorizationUnitLayerSpecData[] unitLayerSpecs, int color, boolean[] includedColors)
  {
    newFactorizationUnitLayerSpecData.setUnitLayerSize(newFactorizationUnitLayerSpecData.getUnitLayerSize() + 1);
    orderBFS(Arrays.asList(topVertex), vertices, Optional.of(color), Collections.emptySet(),
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
            },
            (currentVertex, previousVertex) ->
            {
            });
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
  public SubgraphData getSubgraphForTopVertices(List<Vertex> topVertices, List<Vertex> vertices, boolean includeCrossEdges)
  {
    List<Vertex> factorVertices = new ArrayList<>(vertices.size());
    Integer[] reindexArray = new Integer[vertices.size()];
    EnumSet<EdgeType> edgesTypesToExclude;
    if (includeCrossEdges)
    {
      edgesTypesToExclude = EnumSet.of(EdgeType.UP);
    }
    else
    {
      edgesTypesToExclude = EnumSet.of(EdgeType.UP, EdgeType.CROSS);
    }
    orderBFS(topVertices, vertices, Optional.empty(), edgesTypesToExclude,
            (currentVertex, previousVertex) -> true,
            (currentVertex, previousVertex) ->
            {
              Vertex factorPreviousVertex = getFactorVertex(previousVertex, factorVertices, reindexArray, vertices.size());
              Vertex factorCurrentVertex = getFactorVertex(currentVertex, factorVertices, reindexArray, vertices.size());
              createEdgeBetweenVertices(factorCurrentVertex, factorPreviousVertex);
            });
    SubgraphData subgraph = new SubgraphData();
    subgraph.setVertices(factorVertices);
    subgraph.setReverseReindexArray(createReverseReindexArray(reindexArray));
    return subgraph;
  }

  private Vertex getFactorVertex(Vertex vertex, List<Vertex> factorVertices, Integer[] reindexArray, int verticesSize)
  {
    Vertex factorCurrentVertex;
    if (reindexArray[vertex.getVertexNo()] == null)
    {
      Vertex newVertex = new Vertex(factorVertices.size(), new ArrayList<>(verticesSize));
      factorVertices.add(newVertex);
      reindexArray[vertex.getVertexNo()] = newVertex.getVertexNo();
      factorCurrentVertex = newVertex;
    }
    else
    {
      factorCurrentVertex = factorVertices.get(reindexArray[vertex.getVertexNo()]);
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

  private void orderBFS(List<Vertex> roots, List<Vertex> vertices, Optional<Integer> currentColorOptional, Set<EdgeType> excludedEdgeTypes, BiFunction<Vertex, Vertex, Boolean> whiteVertexFunction, BiConsumer<Vertex, Vertex> greyVertexConsumer)
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
        if (graphColoringArray[v.getVertexNo()] != Color.BLACK)
        {
          greyVertexConsumer.accept(v, u);
          if (graphColoringArray[v.getVertexNo()] == Color.WHITE)
          {
            Boolean proceedWithVertex = whiteVertexFunction.apply(v, u);
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
            },
            (currentVertex, previousVertex) ->
            {
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
//    graph.setVertices(vertices);
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
    return new ArrayList<>(Arrays.asList(result));
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
      List<Edge> downEdges = new LinkedList<>();
      List<Edge> crossEdges = new LinkedList<>();
      List<Edge> upEdges = new LinkedList<>();

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
      v.setDownEdges(new EdgesGroup(new ArrayList<>(downEdges)));
      v.setCrossEdges(new EdgesGroup(new ArrayList<>(crossEdges)));
      v.setUpEdges(new EdgesGroup(new ArrayList<>(upEdges)));
    }
  }

  private void createNewGraph(List<Vertex> vertices, Vertex root, Integer[] reindexArray)
  {
    graph.setRoot(root);
    graph.setVertices(vertices);
    graph.setLayers(vertexService.createLayersList(vertices));
    graph.setReverseReindexArray(createReverseReindexArray(reindexArray));
    if (reconstructionData.getOperationOnGraph() == OperationOnGraph.FINDING_SQUARES)
    {
      graph.setGraphColoring(new GraphColoring(1));
    }
    else
    {
      graph.setGraphColoring(new GraphColoring(root.getEdges().size()));
    }

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

  @Override
  public boolean isMoreThanOneColorLeft(Graph graph)
  {
    return graph.getGraphColoring().getActualColors().size() > 1;
  }

  @Override
  public void overrideGlobalGraph(Graph graph)
  {
    this.graph.setRoot(graph.getRoot());
    this.graph.setVertices(graph.getVertices());
    this.graph.setLayers(graph.getLayers());
    this.graph.setGraphColoring(graph.getGraphColoring());
    this.graph.setReverseReindexArray(graph.getReverseReindexArray());
    this.graph.setAnalyzeData(graph.getAnalyzeData());
  }

  @Override
  public Edge[][] createAdjacencyMatrix()
  {
    int graphSize = graph.getVertices().size();
    Edge[][] adjacencyMatrix = new Edge[graphSize][graphSize];

    graph.getVertices().stream().forEach(
            v -> v.getEdges().forEach(
                    e ->
                    {
                      Vertex u = e.getEndpoint();
                      adjacencyMatrix[v.getVertexNo()][u.getVertexNo()] = e;
                    }
            )
    );

    return adjacencyMatrix;
  }

  @Override
  public void traverseBfsGivenColors(Vertex root, List<Vertex> vertices, int currentColor, List<Integer> remainingColors, Consumer<Vertex> lastColorConsumer)
  {
    Color[] graphColoringArray = createGraphColoringArray(vertices, Color.WHITE);
    graphColoringArray[root.getVertexNo()] = Color.GRAY;
    Queue<Vertex> queue = new LinkedList<>();
    queue.add(root);
    while (!queue.isEmpty())
    {
      Vertex u = queue.poll();

      if (CollectionUtils.isNotEmpty(remainingColors))
      {
        int newCurrentColorIndex = remainingColors.size() - 1;
        Integer newCurrentColor = remainingColors.get(newCurrentColorIndex);
        List<Integer> newRemainingColors = remainingColors.subList(0, newCurrentColorIndex);
        traverseBfsGivenColors(u, vertices, newCurrentColor, newRemainingColors, lastColorConsumer);
      }
      else
      {
        lastColorConsumer.accept(u);
      }

      for (Edge e : u.getEdges())
      {
        if (isEdgeColorToByExcluded(Optional.of(currentColor), e))
        {
          continue;
        }
        Vertex v = e.getEndpoint();
        if (graphColoringArray[v.getVertexNo()] != Color.BLACK)
        {
          if (graphColoringArray[v.getVertexNo()] == Color.WHITE)
          {
            graphColoringArray[v.getVertexNo()] = Color.GRAY;
            queue.add(v);
          }
        }

      }
      graphColoringArray[u.getVertexNo()] = Color.BLACK;
    }
  }

  @Override
  public List<List<Edge>> findSquaresForTwoEdges(Edge baseEdge, Edge otherEdge)
  {
    List<List<Edge>> squareEdgesForGivenTwoEdges = otherEdge.getEndpoint().getEdges().stream()
            .filter(edge -> edge != otherEdge.getOpposite())
            .filter(edge -> graph.getAdjacencyMatrix()[baseEdge.getEndpoint().getVertexNo()][edge.getEndpoint().getVertexNo()] != null)
            .map(edge -> Arrays.asList(edge, graph.getAdjacencyMatrix()[baseEdge.getEndpoint().getVertexNo()][edge.getEndpoint().getVertexNo()]))
            .collect(Collectors.toList());

    return squareEdgesForGivenTwoEdges;
  }
}
