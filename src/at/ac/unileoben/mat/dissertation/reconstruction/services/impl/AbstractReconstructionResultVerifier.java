package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionResultVerifier;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class AbstractReconstructionResultVerifier implements ReconstructionResultVerifier
{

  @Autowired
  Graph graph;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  LinearFactorization linearFactorization;

  protected UniqueList mapResultMissingSquaresToVertexNumbers(List<MissingSquaresUniqueEdgesData> missingSquares)
  {

    UniqueList vertexNumbers = new UniqueList(graph.getVertices().size());
    missingSquares.stream()
            .map(missingSquare -> Arrays.asList(missingSquare.getBaseEdge().getEndpoint(), missingSquare.getOtherEdge().getEndpoint()))
            .flatMap(verticesPairs -> verticesPairs.stream())
            .map(v -> v.getVertexNo())
            .forEach(vertexNumber -> vertexNumbers.add(vertexNumber));
    return vertexNumbers;
  }

  protected boolean checkCorrectnessUsingFactorization(List<MissingSquaresUniqueEdgesData> missingSquares)
  {
    boolean correctResult = false;

    List<Vertex> copiedVertices = graphHelper.copySubgraph(graph.getVertices(), Optional.empty());

    if (reconstructionData.getOperationOnGraph() == OperationOnGraph.FINDING_SQUARES)
    {
      UniqueList actualNeighborsVertexNumbers = mapResultMissingSquaresToVertexNumbers(missingSquares);
      List<Vertex> newVertexNeighborsAmongCopiedVertices = copiedVertices.stream()
              .filter(copiedVertex -> actualNeighborsVertexNumbers.contains(copiedVertex.getVertexNo()))
              .collect(Collectors.toList());
      graphHelper.addVertex(copiedVertices, newVertexNeighborsAmongCopiedVertices);
    }
    else
    {
      Edge missingEdge = extractMissingEdge(missingSquares);
      if (missingEdge == null)
      {
        return correctResult;
      }
      graphHelper.createEdgeBetweenVertices(copiedVertices.get(missingEdge.getOrigin().getVertexNo()), copiedVertices.get(missingEdge.getEndpoint().getVertexNo()));
    }

    Graph originalGraph = new Graph(graph);

    OperationOnGraph initialOperationOnGraph = reconstructionData.getOperationOnGraph();
    try
    {
      reconstructionData.setOperationOnGraph(OperationOnGraph.FACTORIZE);
      linearFactorization.factorize(copiedVertices, copiedVertices.get(0));

      if (graph.getGraphColoring().getActualColors().size() != 1)
      {
        correctResult = true;
      }
    }
    catch (Exception e)
    {
    } finally
    {
      reconstructionData.setOperationOnGraph(initialOperationOnGraph);
      graphHelper.overrideGlobalGraph(originalGraph);
    }


    return correctResult;
  }

  private Edge extractMissingEdge(List<MissingSquaresUniqueEdgesData> resultMissingSquares)
  {
    Edge missingEdge = null;
    Edge[][] resultMissingSquarePairs = findMissingSquarePairsForSelectedColor(resultMissingSquares);
    if (resultMissingSquarePairs != null)
    {
      missingEdge = findMissingEdge(resultMissingSquares, resultMissingSquarePairs);
    }
    return missingEdge;
  }

  public Edge[][] findMissingSquarePairsForSelectedColor(List<MissingSquaresUniqueEdgesData> selectedIrregularMissingSquaresByColor)
  {
    Edge[][] oneEdgeByOtherEdge = new Edge[graph.getVertices().size()][graph.getVertices().size()];

    for (MissingSquaresUniqueEdgesData missingSquare : selectedIrregularMissingSquaresByColor)
    {
      Edge baseEdge = missingSquare.getBaseEdge();
      Edge otherEdge = missingSquare.getOtherEdge();

      boolean storedForBaseEdge = storeOneEdgeByOtherEdge(oneEdgeByOtherEdge, baseEdge, otherEdge);
      boolean storedForOtherEdge = storeOneEdgeByOtherEdge(oneEdgeByOtherEdge, otherEdge, baseEdge);

      if (!storedForBaseEdge || !storedForOtherEdge)
      {
        oneEdgeByOtherEdge = null;
        break;
      }
    }
    return oneEdgeByOtherEdge;
  }

  private boolean storeOneEdgeByOtherEdge(Edge[][] oneEdgeByOtherEdge, Edge baseEdge, Edge otherEdge)
  {
    boolean edgesStored = false;

    int edgeOriginNo = baseEdge.getOrigin().getVertexNo();
    int edgeEndpointNo = baseEdge.getEndpoint().getVertexNo();
    if (oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] == null)
    {
      oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] = otherEdge;
      edgesStored = true;
    }
    return edgesStored;
  }

  public Edge findMissingEdge(List<MissingSquaresUniqueEdgesData> selectedIrregularMissingSquares,
                              Edge[][] missingSquarePairs)
  {
    Edge missingEdge = null;

    for (MissingSquaresUniqueEdgesData missingSquare : selectedIrregularMissingSquares)
    {
      Edge baseEdge = missingSquare.getBaseEdge();
      Edge otherEdge = missingSquare.getOtherEdge();

      Edge matchingEdge = missingSquarePairs[baseEdge.getEndpoint().getVertexNo()][baseEdge.getOrigin().getVertexNo()];
      Vertex edgeEndpoint = otherEdge.getEndpoint();

      Edge otherMatchingEdge = missingSquarePairs[otherEdge.getEndpoint().getVertexNo()][otherEdge.getOrigin().getVertexNo()];
      Vertex otherEdgeEndpoint = baseEdge.getEndpoint();

      if (matchingEdge == null)
      {
        matchingEdge = otherMatchingEdge;
        edgeEndpoint = otherEdgeEndpoint;
      }

      if (matchingEdge != null)
      {
        missingEdge = new Edge(edgeEndpoint, matchingEdge.getEndpoint());
        break;
      }
    }
    return missingEdge;
  }

}
