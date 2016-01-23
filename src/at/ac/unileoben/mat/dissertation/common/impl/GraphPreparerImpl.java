package at.ac.unileoben.mat.dissertation.common.impl;

import at.ac.unileoben.mat.dissertation.common.GraphPreparer;
import at.ac.unileoben.mat.dissertation.common.GraphReader;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 23.01.16
 * Time: 11:48
 * To change this template use File | Settings | File Templates.
 */
@Component
public class GraphPreparerImpl implements GraphPreparer
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
      Edge e1 = new Edge(newVertex, neighborVertex);
      Edge e2 = new Edge(neighborVertex, newVertex);
      e1.setOpposite(e2);
      e2.setOpposite(e1);
      newVertex.getEdges().add(e1);
      neighborVertex.getEdges().add(e2);
    }

    allVertices.add(newVertex);
  }
}
