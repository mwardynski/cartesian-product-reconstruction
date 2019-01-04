package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MissingSquaresSpikeCycleCommons
{
  @Autowired
  Graph graph;

  public List<Vertex> filterPotentialCorrectVertices(int[] potentialEdgesNumberToReconstructPerVertex, Vertex arbitraryVertex, List<Integer> preselectedVertexNumbers)
  {
    int maxPotentialEdgesNumberToReconstruct = potentialEdgesNumberToReconstructPerVertex[arbitraryVertex.getVertexNo()];
    boolean[] potentialVerticesIncludedToRemoveForResult = new boolean[graph.getVertices().size()];
    List<Vertex> potentialVerticesToRemoveForResult = new LinkedList<>(Arrays.asList(arbitraryVertex));
    potentialVerticesIncludedToRemoveForResult[arbitraryVertex.getVertexNo()] = true;

    for (Integer properDistanceFromSpikeVertexNumber : preselectedVertexNumbers)
    {
      Vertex vertexOfProperDistanceFromSpike = graph.getVertices().get(properDistanceFromSpikeVertexNumber);
      if (potentialVerticesIncludedToRemoveForResult[vertexOfProperDistanceFromSpike.getVertexNo()])
      {
        continue;
      }

      int potentialEdgesNumberToReconstruct = potentialEdgesNumberToReconstructPerVertex[properDistanceFromSpikeVertexNumber];
      if (maxPotentialEdgesNumberToReconstruct < potentialEdgesNumberToReconstruct)
      {
        maxPotentialEdgesNumberToReconstruct = potentialEdgesNumberToReconstruct;
        potentialVerticesToRemoveForResult = new LinkedList<>(Arrays.asList(vertexOfProperDistanceFromSpike));
      }
      else if (maxPotentialEdgesNumberToReconstruct == potentialEdgesNumberToReconstruct)
      {
        potentialVerticesToRemoveForResult.add(vertexOfProperDistanceFromSpike);
      }
    }
    return potentialVerticesToRemoveForResult;
  }

  public List<Vertex> filterOutMissingSquareEdgesVertices(boolean[] missingSquareEdgesIncludedEndpoints, List<Vertex> potentialVerticesToRemoveForResult)
  {
    if (potentialVerticesToRemoveForResult.size() > 1)
    {
      potentialVerticesToRemoveForResult = potentialVerticesToRemoveForResult.stream()
              .filter(vertex -> !missingSquareEdgesIncludedEndpoints[vertex.getVertexNo()])
              .collect(Collectors.toList());
    }
    return potentialVerticesToRemoveForResult;
  }

  public List<Vertex> favorizeVerticesWithAllPotentialMissingEdgesSure(int[] potentialEdgesNumberToReconstructPerVertex, List<Edge>[] potentialEdgesToReconstructSure, List<Vertex> potentialVerticesToRemoveForResult)
  {
    if (potentialVerticesToRemoveForResult.size() > 1)
    {
      List<Vertex> potentialNoMaybeEndpoints = potentialVerticesToRemoveForResult.stream()
              .filter(vertex -> CollectionUtils.isNotEmpty(potentialEdgesToReconstructSure[vertex.getVertexNo()]))
              .filter(vertex -> potentialEdgesNumberToReconstructPerVertex[vertex.getVertexNo()] == potentialEdgesToReconstructSure[vertex.getVertexNo()].size())
              .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(potentialNoMaybeEndpoints))
      {
        potentialVerticesToRemoveForResult = potentialNoMaybeEndpoints;
      }
    }
    return potentialVerticesToRemoveForResult;
  }

  public List<Edge> collectResultFromSureEdges(List<Edge> sureEdges)
  {
    List<Edge> baseResultEdges = new LinkedList<>();
    if (CollectionUtils.isNotEmpty(sureEdges))
    {
      baseResultEdges.addAll(sureEdges);
    }
    return baseResultEdges;
  }

  public void addMissingSquareEdgesEndpointsToResult(Vertex vertexToRemoveForResult, List<Vertex> missingSquareEdgesEndpoints, List<Edge> baseResultEdges, boolean[] potentialResultIncludedEndpoints)
  {
    for (Vertex missingSquareEdgesEndpoint : missingSquareEdgesEndpoints)
    {
      if (!potentialResultIncludedEndpoints[missingSquareEdgesEndpoint.getVertexNo()]
              && graph.getAdjacencyMatrix()[vertexToRemoveForResult.getVertexNo()][missingSquareEdgesEndpoint.getVertexNo()] == null)
      {
        Edge missingSquarePotentialResultEdge = new Edge(vertexToRemoveForResult, missingSquareEdgesEndpoint);
        baseResultEdges.add(missingSquarePotentialResultEdge);
        potentialResultIncludedEndpoints[missingSquareEdgesEndpoint.getVertexNo()] = true;
      }
    }
  }

  public boolean containsAnyEndpoints(List<Edge> enclosingCollectionEdges, List<Edge> elementCollectionEdges)
  {
    if (CollectionUtils.isEmpty(elementCollectionEdges))
    {
      return true;
    }

    boolean[] includedEnclosingEdges = new boolean[graph.getVertices().size()];
    enclosingCollectionEdges.stream()
            .map(edge -> edge.getEndpoint().getVertexNo())
            .forEach(vertexNumber -> includedEnclosingEdges[vertexNumber] = true);

    boolean anyElementPresent = elementCollectionEdges.stream()
            .map(edge -> edge.getEndpoint().getVertexNo())
            .filter(vertexNumber -> includedEnclosingEdges[vertexNumber])
            .findAny().isPresent();

    return anyElementPresent;
  }
}
