package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.common.GraphReader;
import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-29
 * Time: 16:47
 * To change this template use File | Settings | File Templates.
 */
public class GraphPreparer
{

  public Graph prepareToLinearFactorization(List<Vertex> vertices, int[] reindexArray)
  {
    Vertex root = findVertexWithMinDegree(vertices);
    Graph graph = prepareToLinearFactorizationForGivenVertex(vertices, root, reindexArray);
    return graph;
  }

  public Graph prepareToLinearFactorizationForGivenVertex(List<Vertex> vertices, Vertex root, int[] reindexArray)
  {
    bfs(root, reindexArray);
    reindex(vertices, reindexArray);
    vertices = sortVertices(vertices);
    sortEdges(vertices);
    arrangeEdgesToThreeGroups(vertices);
    Graph graph = new Graph(vertices);
    arrangeFirstLayerEdges(graph);
    return graph;
  }

  public void finalizeFactorization(Graph graph, int[] reindexArray)
  {
    List<Vertex> vertices = graph.getVertices();
    int[] reverseReindexArray = createReverseReindexArray(reindexArray);
    reindex(vertices, reverseReindexArray);
    vertices = sortVertices(vertices);
    sortEdges(vertices);
    graph.setVertices(vertices);
  }

  public void removeVertex(List<Vertex> vertices, int vertexIndex)
  {
    Iterator<Vertex> vertexIterator = vertices.iterator();
    while (vertexIterator.hasNext())
    {
      Vertex v = vertexIterator.next();
      Iterator<Edge> edgeIterator = v.getEdges().iterator();
      while (edgeIterator.hasNext())
      {
        Edge e = edgeIterator.next();
        if (e.getOrigin().getVertexNo() == vertexIndex || e.getEndpoint().getVertexNo() == vertexIndex)
        {
          edgeIterator.remove();
        }
      }
      if (v.getVertexNo() == vertexIndex)
      {
        vertexIterator.remove();
      }
    }
    for (Vertex v : vertices)
    {
      if (v.getVertexNo() > vertexIndex)
      {
        v.setVertexNo(v.getVertexNo() - 1);
      }
    }
  }

  private int[] createReverseReindexArray(int[] reindexArray)
  {
    int[] reverseReindexArray = new int[reindexArray.length];
    for (int i = 0; i < reindexArray.length; i++)
    {
      reverseReindexArray[reindexArray[i]] = i;
    }
    return reverseReindexArray;
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

  private void bfs(Vertex root, int[] reindexArray)
  {
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
    }
  }

  private void reindex(List<Vertex> vertices, int[] reindexArray)
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
      tmpEdges.add(new ArrayList<Edge>(GraphReader.MAX_NEIGHBOURS_ACCOUNT));
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

  private void arrangeFirstLayerEdges(Graph graph)
  {
    Vertex root = graph.getRoot();
    EdgesGroup upEdgesGroup = root.getUpEdges();
    List<Edge> upEdges = upEdgesGroup.getEdges();

    EdgesRef edgesRef = new EdgesRef(upEdges.size());
    int[] colorLengths = new int[upEdges.size()];
    for (int i = 0; i < upEdges.size(); i++)
    {
      colorLengths[i] = 1;
    }
    edgesRef.setColorAmounts(colorLengths);

    upEdgesGroup.setEdgesRef(edgesRef);

    for (int i = 0; i < upEdges.size(); i++)
    {
      Label upLabel = new Label(0, i);
      Edge upEdge = upEdges.get(i);
      upEdge.setLabel(upLabel);
      upEdge.getEndpoint().setUnitLayer(true);

      addLabelAndRefToDownEdgesL1(upEdge, i, graph.getGraphColoring().getOriginalColorsAmount());
      addLabelAndRefToCrossEdgesL1(upEdge, graph);
    }

  }

  private void addLabelAndRefToDownEdgesL1(Edge upEdge, int i, int size)
  {
    int[] colorLengths;
    EdgesRef downEdgesRef = new EdgesRef(1);
    colorLengths = new int[size];
    for (int j = 0; j < size; j++)
    {
      if (j == i)
      {
        colorLengths[j] = 1;
      }
      else
      {
        colorLengths[j] = 0;
      }
    }
    downEdgesRef.setColorAmounts(colorLengths);

    Vertex endpointVertex = upEdge.getEndpoint();
    EdgesGroup downEdgesGroup = endpointVertex.getDownEdges();
    downEdgesGroup.setEdgesRef(downEdgesRef);
    upEdge.getOpposite().setLabel(new Label(0, i));
  }

  private void addLabelAndRefToCrossEdgesL1(Edge upEdge, Graph graph)
  {
    EdgesGroup crossEdgesGroup = upEdge.getEndpoint().getCrossEdges();
    List<Edge> crossEdges = crossEdgesGroup.getEdges();
    int[] crossEdgesAmounts = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    int crossEdgesColorsAmount = 0;
    for (int i = 0; i < crossEdges.size(); i++)
    {
      Edge crossEdge = crossEdges.get(i);
      int mergedColor = mergeCrossEdgesColors(crossEdge, upEdge, graph);
      crossEdge.setLabel(new Label(i, mergedColor));
      if (crossEdgesAmounts[mergedColor] == 0)
      {
        crossEdgesColorsAmount++;
      }
      crossEdgesAmounts[mergedColor]++;

    }
    EdgesRef crossEdgesRef = new EdgesRef(crossEdgesColorsAmount);
    crossEdgesRef.setColorAmounts(crossEdgesAmounts);
    crossEdgesGroup.setEdgesRef(crossEdgesRef);
  }

  private int mergeCrossEdgesColors(Edge crossEdge, Edge proposedColorEdge, Graph graph)
  {
    Edge oppositeEdge = crossEdge.getOpposite();
    graph.mergeColorsForEdges(Arrays.asList(proposedColorEdge, oppositeEdge), MergeTagEnum.CONSISTENCY_CROSS);

    int proposedColor = proposedColorEdge.getLabel().getColor();
    Label oppositeEdgeLabel = oppositeEdge.getLabel();
    if(oppositeEdgeLabel != null)
    {
      int oppositeEdgeColor = oppositeEdgeLabel.getColor();
      return proposedColor < oppositeEdgeColor ? proposedColor : oppositeEdgeColor;
    }
    else
    {
      return proposedColor;
    }
  }
}
