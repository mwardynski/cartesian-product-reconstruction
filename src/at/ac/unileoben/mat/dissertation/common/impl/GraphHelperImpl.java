package at.ac.unileoben.mat.dissertation.common.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.common.GraphReader;
import at.ac.unileoben.mat.dissertation.structure.Color;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
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
  GraphReader graphReader;

  @Override
  public List<Vertex> parseGraph(String graphFilePath)
  {
    return graphReader.readGraph(graphFilePath);
  }

  @Override
  public void addVertex(List<Vertex> allVertices, List<Vertex> neighbors)
  {
    Vertex newVertex = new Vertex(allVertices.size(), new ArrayList<Edge>(allVertices.size()));

    for (Vertex neighborVertex : neighbors)
    {
      addEdge(newVertex, neighborVertex);
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
}
