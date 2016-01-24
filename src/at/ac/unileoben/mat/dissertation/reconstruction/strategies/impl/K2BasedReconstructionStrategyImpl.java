package at.ac.unileoben.mat.dissertation.reconstruction.strategies.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.reconstruction.strategies.K2BasedReconstructionStrategy;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 24.01.16
 * Time: 11:34
 * To change this template use File | Settings | File Templates.
 */
@Component
public class K2BasedReconstructionStrategyImpl implements K2BasedReconstructionStrategy
{

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  ColoringService coloringService;

  @Autowired
  EdgeService edgeService;

  @Autowired
  LinearFactorization linearFactorization;


  @Override
  public Graph reconstruct(List<Vertex> vertices, Vertex currentlyRemovedVertex)
  {
    List<Vertex> subgraph = graphHelper.copySubgraph(vertices, Optional.of(currentlyRemovedVertex));
    List<List<Vertex>> subgraphConnectedComponents = graphHelper.getGraphConnectedComponents(subgraph);
    List<List<Vertex>> factorizedComponents = new LinkedList<>();
    List<Integer> factorizedComponentsK2Colors = new LinkedList<>();

    factorizeConnectedComponents(subgraphConnectedComponents, factorizedComponents, factorizedComponentsK2Colors);

    if (subgraphConnectedComponents.size() == factorizedComponents.size())
    {
      List<Vertex> factorizedRemovedVertexNeighbors = findFactorizedRemovedVertexNeighbors(currentlyRemovedVertex, factorizedComponents, factorizedComponentsK2Colors, vertices);

      List<Vertex> verticesCopy = reconstructGraphCopy(vertices, currentlyRemovedVertex, factorizedRemovedVertexNeighbors);
      Graph factorizedGraph = linearFactorization.factorize(verticesCopy, null);
      if (factorizedGraph.getGraphColoring().getActualColors().size() != 1)
      {
        return factorizedGraph;
      }
    }
    return null;
  }

  private void factorizeConnectedComponents(List<List<Vertex>> subgraphConnectedComponents, List<List<Vertex>> factorizedComponents, List<Integer> factorizedComponentsK2Colors)
  {
    for (List<Vertex> connectedComponentVertices : subgraphConnectedComponents)
    {
      if (graphHelper.isGraphK1(connectedComponentVertices))
      {
        break;
      }
      else if (graphHelper.isGraphK2(connectedComponentVertices))
      {
        factorizedComponents.add(connectedComponentVertices);
        factorizedComponentsK2Colors.add(null);
      }
      else
      {
        Graph factorizedGraph = linearFactorization.factorize(connectedComponentVertices, null);
        if (factorizedGraph.getGraphColoring().getActualColors().size() > 1)
        {
          Optional<Integer> factorizedGraphK2FactorColorOptional = getFactorizedGraphK2FactorColor(factorizedGraph);
          if (factorizedGraphK2FactorColorOptional.isPresent())
          {
            factorizedComponents.add(factorizedGraph.getVertices());
            factorizedComponentsK2Colors.add(factorizedGraphK2FactorColorOptional.get());
          }
        }
        else
        {
          break;
        }
      }
    }
  }

  private Optional<Integer> getFactorizedGraphK2FactorColor(Graph factorizedGraph)
  {
    Optional<Integer> k2FactorColorOptional = Optional.<Integer>empty();
    Vertex root = factorizedGraph.getRoot();
    for (Edge upEdge : root.getUpEdges().getEdges())
    {
      boolean factorizationContainsK2Factor = true;

      int edgeColor = coloringService.getCurrentColorMapping(factorizedGraph.getGraphColoring(), upEdge.getLabel().getColor());
      Vertex upNeighborVertex = upEdge.getEndpoint();
      List<Edge> relevantK2CheckEdges = new LinkedList<>();
      relevantK2CheckEdges.addAll(upNeighborVertex.getUpEdges().getEdges());
      relevantK2CheckEdges.addAll(upNeighborVertex.getCrossEdges().getEdges());
      for (Edge nextEdge : relevantK2CheckEdges)
      {
        int nextEdgeColor = coloringService.getCurrentColorMapping(factorizedGraph.getGraphColoring(), nextEdge.getLabel().getColor());
        if (nextEdgeColor == edgeColor)
        {
          factorizationContainsK2Factor = false;
        }
      }
      if (factorizationContainsK2Factor)
      {
        k2FactorColorOptional = Optional.of(upEdge.getLabel().getColor());
        break;
      }
    }
    return k2FactorColorOptional;
  }

  private List<Vertex> findFactorizedRemovedVertexNeighbors(Vertex currentlyRemovedVertex, List<List<Vertex>> factorizedComponents, List<Integer> factorizedComponentsK2Colors, List<Vertex> vertices)
  {
    List<Vertex> factorizedRemovedVertexNeighbors = new LinkedList<>();
    for (int i = 0; i < factorizedComponents.size(); i++)
    {
      List<Vertex> factorizedComponentVertices = factorizedComponents.get(i);
      Integer componentK2Color = factorizedComponentsK2Colors.get(i);
      List<Vertex> removedVertexCurrentComponentNeighbors;
      if (componentK2Color == null)
      {
        removedVertexCurrentComponentNeighbors = findInK2ComponentRemovedVertexNeighbors(currentlyRemovedVertex, factorizedComponentVertices);
      }
      else
      {
        removedVertexCurrentComponentNeighbors = findInFactorizedComponentRemovedVertexNeighbors(currentlyRemovedVertex, factorizedComponentVertices, componentK2Color, vertices.size());
      }
      factorizedRemovedVertexNeighbors.addAll(removedVertexCurrentComponentNeighbors);
    }
    return factorizedRemovedVertexNeighbors;
  }

  private List<Vertex> findInK2ComponentRemovedVertexNeighbors(Vertex currentlyRemovedVertex, List<Vertex> factorizedComponentVertices)
  {
    List<Vertex> removedVertexCurrentComponentNeighbors = new LinkedList<>();
    for (Edge e : currentlyRemovedVertex.getEdges())
    {
      if (e.getEndpoint().getVertexNo() == factorizedComponentVertices.get(0).getVertexNo())
      {
        removedVertexCurrentComponentNeighbors.add(factorizedComponentVertices.get(1));
        break;
      }
      else if (e.getEndpoint().getVertexNo() == factorizedComponentVertices.get(1).getVertexNo())
      {
        removedVertexCurrentComponentNeighbors.add(factorizedComponentVertices.get(0));
        break;
      }
    }
    return removedVertexCurrentComponentNeighbors;
  }

  private List<Vertex> findInFactorizedComponentRemovedVertexNeighbors(Vertex currentlyRemovedVertex, List<Vertex> factorizedComponentVertices, Integer componentK2Color, Integer verticesSize)
  {
    List<Vertex> removedVertexCurrentComponentNeighbors = new LinkedList<>();
    boolean[] additionallyRemovedVertexNeighborFlags = new boolean[verticesSize];
    List<Vertex> additionallyRemovedVertexNeighbors = new LinkedList<>();
    for (Edge e : currentlyRemovedVertex.getEdges())
    {
      additionallyRemovedVertexNeighborFlags[e.getEndpoint().getVertexNo()] = true;
    }
    for (Vertex potentialNeighborVertex : factorizedComponentVertices)
    {
      if (additionallyRemovedVertexNeighborFlags[potentialNeighborVertex.getVertexNo()])
      {
        additionallyRemovedVertexNeighbors.add(potentialNeighborVertex);
      }
    }
    for (Vertex additionallyRemovedVertexNeighbor : additionallyRemovedVertexNeighbors)
    {
      List<Edge> edgesCorrespondingToK2Component = edgeService.getAllEdgesOfColor(additionallyRemovedVertexNeighbor, componentK2Color);
      assert edgesCorrespondingToK2Component.size() == 1;
      Vertex removedVertexNeighbor = edgesCorrespondingToK2Component.iterator().next().getEndpoint();
      removedVertexCurrentComponentNeighbors.add(removedVertexNeighbor);
    }
    return removedVertexCurrentComponentNeighbors;
  }

  private List<Vertex> reconstructGraphCopy(List<Vertex> vertices, Vertex currentlyRemovedVertex, List<Vertex> factorizedRemovedVertexNeighbors)
  {
    List<Vertex> removedVertexNeighbors = new LinkedList<>();
    List<Vertex> verticesCopy = graphHelper.copySubgraph(vertices, Optional.empty());
    for (Vertex v : factorizedRemovedVertexNeighbors)
    {
      removedVertexNeighbors.add(verticesCopy.get(v.getVertexNo()));
    }
    removedVertexNeighbors.add(verticesCopy.get(currentlyRemovedVertex.getVertexNo()));
    graphHelper.addVertex(verticesCopy, removedVertexNeighbors);
    return verticesCopy;
  }

}
