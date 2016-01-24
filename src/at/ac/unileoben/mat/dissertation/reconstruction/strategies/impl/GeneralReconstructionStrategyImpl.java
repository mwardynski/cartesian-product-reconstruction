package at.ac.unileoben.mat.dissertation.reconstruction.strategies.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.reconstruction.strategies.GeneralReconstructionStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 24.01.16
 * Time: 16:50
 * To change this template use File | Settings | File Templates.
 */
@Component
public class GeneralReconstructionStrategyImpl implements GeneralReconstructionStrategy
{

  @Autowired
  Graph graph;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  EdgeService edgeService;

  @Autowired
  LinearFactorization linearFactorization;

  @Override
  public Graph reconstruct(List<Vertex> vertices, Vertex u, Vertex v, Graph localGraph)
  {
    List<Vertex> removedVertexNeighbors = new LinkedList<>();

    removedVertexNeighbors.add(u);
    removedVertexNeighbors.add(v);
    findRemovedVertexAllCrossNeighbors(u, v, removedVertexNeighbors);
    findRemovedVertexAllUpNeighbors(u, v, removedVertexNeighbors, localGraph);

    return reconstructAndFactorizeProduct(vertices, removedVertexNeighbors, localGraph);
  }

  private Graph reconstructAndFactorizeProduct(List<Vertex> vertices, List<Vertex> removedVertexNeighbors, Graph localGraph)
  {
    Graph reconstructedAndFactorizedGraph = null;
    if (CollectionUtils.isNotEmpty(removedVertexNeighbors))
    {
      List<Vertex> verticesToFactorize = graphHelper.copySubgraph(vertices, Optional.empty());
      List<Vertex> removedVertexNeighborsToFactorize = new LinkedList<>();
      for (Vertex removedVertexNeighbor : removedVertexNeighbors)
      {
        Vertex removedVertexNeighborToFactorize = verticesToFactorize.get(localGraph.getReverseReindexArray()[removedVertexNeighbor.getVertexNo()]);
        removedVertexNeighborsToFactorize.add(removedVertexNeighborToFactorize);
      }
      graphHelper.addVertex(verticesToFactorize, removedVertexNeighborsToFactorize);
      Graph factorizedGraph = linearFactorization.factorize(verticesToFactorize, null);
      if (factorizedGraph.getGraphColoring().getActualColors().size() != 1)
      {
        reconstructedAndFactorizedGraph = factorizedGraph;
      }
    }
    return reconstructedAndFactorizedGraph;
  }

  private void findRemovedVertexAllCrossNeighbors(Vertex u, Vertex v, List<Vertex> removedVertexNeighbors)
  {
    List<Vertex> removedVertexCrossNeighbors = null;
    removedVertexCrossNeighbors = findRemovedVertexCrossNeighbors(u, v);
    removedVertexNeighbors.addAll(removedVertexCrossNeighbors);
    removedVertexCrossNeighbors = findRemovedVertexCrossNeighbors(v, u);
    removedVertexNeighbors.addAll(removedVertexCrossNeighbors);
  }

  private List<Vertex> findRemovedVertexCrossNeighbors(Vertex u, Vertex v)
  {
    List<Vertex> crossNeighbors = new LinkedList<>();
    if (u.getUpEdges() != null && CollectionUtils.isNotEmpty(u.getUpEdges().getEdges()))
    {
      for (Edge upEdge : u.getUpEdges().getEdges())
      {
        if (upEdge.getEndpoint().getDownEdges().getEdges().size() == 2)
        {
          List<Edge> yDownEdges = edgeService.getFurtherEdgesOfGivenTypeAndDifferentEndpoint(upEdge, v, EdgeType.DOWN);
          Vertex y = upEdge.getEndpoint();
          if (yDownEdges.size() == 1)
          {
            Vertex w = yDownEdges.iterator().next().getEndpoint();
            EdgesGroup vCrossEdges = v.getCrossEdges();
            if (vCrossEdges != null && CollectionUtils.isNotEmpty(vCrossEdges.getEdges()))
            {
              for (Edge vCrossEdge : vCrossEdges.getEdges())
              {
                if (vCrossEdge.getEndpoint() == w)
                {
                  crossNeighbors.add(y);
                }
              }
            }
          }
        }
      }
    }
    return crossNeighbors;
  }

  private void findRemovedVertexAllUpNeighbors(Vertex u, Vertex v, List<Vertex> removedVertexNeighbors, Graph localGraph)
  {
    IntervalData[] intervalDataArray = new IntervalData[localGraph.getVertices().size()];
    populateIntervalDataArrayForFirstLayer(u, v, intervalDataArray, localGraph);
    if (localGraph.getLayers().size() > 2)
    {
      populateIntervalDataArrayForSecondLayer(intervalDataArray, localGraph);
    }
    if (localGraph.getLayers().size() > 3)
    {
      List<Vertex> singleDownNeighborVertices = new LinkedList<>();
      List<Vertex> removedVertexUpNeighbors = findRemovedVertexUpNeighbors(intervalDataArray, singleDownNeighborVertices, localGraph);
      removedVertexNeighbors.addAll(removedVertexUpNeighbors);

      boolean[] goodCandidates = new boolean[localGraph.getVertices().size()];
      List<Vertex> remainingCandidates = new LinkedList<>();
      removedVertexUpNeighbors = findRemovedVertexNeighborsAmongGoodCandidates(singleDownNeighborVertices, intervalDataArray, remainingCandidates, goodCandidates);
      removedVertexNeighbors.addAll(removedVertexUpNeighbors);

      removedVertexUpNeighbors = findRemovedVertexNeighborsAmongRemainingCandidates(remainingCandidates, goodCandidates, intervalDataArray);
      removedVertexNeighbors.addAll(removedVertexUpNeighbors);
    }
  }

  private void populateIntervalDataArrayForFirstLayer(Vertex u, Vertex v, IntervalData[] intervalDataArray, Graph localGraph)
  {
    for (Vertex vertex : localGraph.getLayers().get(1))
    {
      intervalDataArray[vertex.getVertexNo()] = new IntervalData();
      if (vertex == u)
      {
        intervalDataArray[vertex.getVertexNo()].u = true;
      }
      else if (vertex == v)
      {
        intervalDataArray[vertex.getVertexNo()].v = true;
      }
      else
      {
        intervalDataArray[vertex.getVertexNo()].other = true;
      }
    }
  }

  private void populateIntervalDataArrayForSecondLayer(IntervalData[] intervalDataArray, Graph localGraph)
  {
    for (Vertex vertex : localGraph.getLayers().get(2))
    {
      intervalDataArray[vertex.getVertexNo()] = new IntervalData();
      populateIntervalDataForVertex(vertex, intervalDataArray);
    }
  }

  private void populateIntervalDataForVertex(Vertex vertex, IntervalData[] intervalDataArray)
  {
    for (Edge downEdge : vertex.getDownEdges().getEdges())
    {
      Vertex endpoint = downEdge.getEndpoint();
      if (intervalDataArray[endpoint.getVertexNo()].u)
      {
        intervalDataArray[vertex.getVertexNo()].u = true;
      }
      if (intervalDataArray[endpoint.getVertexNo()].v)
      {
        intervalDataArray[vertex.getVertexNo()].v = true;
      }
      if (intervalDataArray[endpoint.getVertexNo()].other)
      {
        intervalDataArray[vertex.getVertexNo()].other = true;
      }
    }
  }

  private List<Vertex> findRemovedVertexUpNeighbors(IntervalData[] intervalDataArray, List<Vertex> singleDownNeighborVertices, Graph localGraph)
  {
    List<Vertex> removedVertexUpNeighbors = new LinkedList<>();
    for (Vertex y : localGraph.getLayers().get(3))
    {
      intervalDataArray[y.getVertexNo()] = new IntervalData();
      populateIntervalDataForVertex(y, intervalDataArray);
      if (y.getDownEdges().getEdges().size() == 1)
      {
        singleDownNeighborVertices.add(y);
      }
      else
      {
        if (intervalDataArray[y.getVertexNo()].u && intervalDataArray[y.getVertexNo()].v)
        {
          removedVertexUpNeighbors.add(y);
        }
      }
    }
    return removedVertexUpNeighbors;
  }

  private List<Vertex> findRemovedVertexNeighborsAmongGoodCandidates(List<Vertex> singleDownNeighborVertices, IntervalData[] intervalDataArray, List<Vertex> remainingCandidates, boolean[] goodCandidates)
  {
    List<Vertex> removedVertexUpNeighbors = new LinkedList<>();
    for (Vertex y : singleDownNeighborVertices)
    {
      if ((intervalDataArray[y.getVertexNo()].u && !intervalDataArray[y.getVertexNo()].v) || !(intervalDataArray[y.getVertexNo()].u && intervalDataArray[y.getVertexNo()].v))
      {
        EdgesGroup yUpEdges = y.getUpEdges();
        if (yUpEdges != null && CollectionUtils.isNotEmpty(yUpEdges.getEdges()))
        {
          boolean yGoodCandidate = false;
          for (Edge upEdge : yUpEdges.getEdges())
          {
            Vertex q = upEdge.getEndpoint();
            if (intervalDataArray[q.getVertexNo()] == null)
            {
              intervalDataArray[q.getVertexNo()] = new IntervalData();
              populateIntervalDataForVertex(q, intervalDataArray);
            }
            if (intervalDataArray[q.getVertexNo()].u && intervalDataArray[q.getVertexNo()].v)
            {
              removedVertexUpNeighbors.add(y);
              yGoodCandidate = true;
              break;
            }
          }
          if (yGoodCandidate)
          {
            goodCandidates[y.getVertexNo()] = true;
          }
          else
          {
            remainingCandidates.add(y);
          }
        }
      }
    }
    return removedVertexUpNeighbors;
  }

  private List<Vertex> findRemovedVertexNeighborsAmongRemainingCandidates(List<Vertex> remainingCandidates, boolean[] goodCandidates, IntervalData[] intervalDataArray)
  {
    List<Vertex> removedVertexUpNeighbors = new LinkedList<>();
    for (Vertex y : remainingCandidates)
    {
      Vertex w = y.getDownEdges().getEdges().iterator().next().getEndpoint();
      boolean wHasGoodCandidateAmongUpNeighbors = false;
      for (Edge upEdge : w.getUpEdges().getEdges())
      {
        Vertex endpoint = upEdge.getEndpoint();
        if (goodCandidates[endpoint.getVertexNo()])
        {
          wHasGoodCandidateAmongUpNeighbors = true;
          break;
        }
      }
      if (!wHasGoodCandidateAmongUpNeighbors)
      {
        EdgesGroup yUpEdges = y.getUpEdges();
        if (yUpEdges != null && CollectionUtils.isNotEmpty(yUpEdges.getEdges()))
        {
          boolean yBadCandidate = false;
          for (Edge upEdge : yUpEdges.getEdges())
          {
            Vertex z = upEdge.getEndpoint();
            if (!intervalDataArray[z.getVertexNo()].other)
            {
              yBadCandidate = true;
              break;
            }
          }
          if (!yBadCandidate)
          {
            removedVertexUpNeighbors.add(y);
          }
        }
      }
    }
    return removedVertexUpNeighbors;
  }

  private class IntervalData
  {
    boolean u;
    boolean v;
    boolean other;
  }
}
