package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquaresHandlingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareHandlingStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SingleSquaresHandlingServiceImpl implements SingleSquaresHandlingService
{

  @Autowired
  Graph graph;

  @Autowired
  SquareHandlingStrategy squareHandlingStrategy;


  @Override
  public SingleSquareList findSquaresForGivenEdges(Edge baseEdge, Edge otherEdge, SquareReconstructionData squareReconstructionData)
  {
    return squareReconstructionData.getSquares()
            [baseEdge.getOrigin().getVertexNo()]
            [baseEdge.getEndpoint().getVertexNo()]
            [otherEdge.getEndpoint().getVertexNo()];
  }

  @Override
  public void collectAllSingleSquares(SquareReconstructionData squareReconstructionData)
  {
    for (Vertex vertex : graph.getVertices())
    {
      for (Edge edge : vertex.getEdges())
      {
        Edge baseEdge = edge.getOpposite();
        for (Edge otherEdge : edge.getEndpoint().getEdges())
        {
          if (otherEdge == baseEdge)
          {
            continue;
          }

          SingleSquareList[][][] allSquares = squareReconstructionData.getSquares();

          if (allSquares[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()][otherEdge.getEndpoint().getVertexNo()] == null)
          {
            List<List<Edge>> edgesCompletingSquares = findEdgesCompletingSquares(baseEdge, otherEdge);

            SingleSquareList singleSquaresForGivenEdges = new SingleSquareList();
            for (List<Edge> edgesCompletingSquare : edgesCompletingSquares)
            {
              Edge squareBaseEdge = edgesCompletingSquare.get(0);
              Edge squareOtherEdge = edgesCompletingSquare.get(1);

              SingleSquareData singleSquare = createSingleSquare(baseEdge, otherEdge, squareBaseEdge, squareOtherEdge, squareReconstructionData);
              singleSquaresForGivenEdges.add(singleSquare);
            }

            squareReconstructionData.getSquares()
                    [baseEdge.getOrigin().getVertexNo()]
                    [baseEdge.getEndpoint().getVertexNo()]
                    [otherEdge.getEndpoint().getVertexNo()] = singleSquaresForGivenEdges;

            storeFirstVertexAndColorFirstSquare(singleSquaresForGivenEdges, squareReconstructionData);
          }
        }

      }
    }
  }

  private List<List<Edge>> findEdgesCompletingSquares(Edge baseEdge, Edge otherEdge)
  {
    List<List<Edge>> squareEdgesForGivenTwoEdges = otherEdge.getEndpoint().getEdges().stream()
            .filter(edge -> edge != otherEdge.getOpposite())
            .filter(edge -> graph.getAdjacencyMatrix()[baseEdge.getEndpoint().getVertexNo()][edge.getEndpoint().getVertexNo()] != null)
            .map(edge -> Arrays.asList(edge, graph.getAdjacencyMatrix()[baseEdge.getEndpoint().getVertexNo()][edge.getEndpoint().getVertexNo()]))
            .collect(Collectors.toList());

    return squareEdgesForGivenTwoEdges;
  }

  private SingleSquareData createSingleSquare(Edge baseEdge, Edge otherEdge, Edge squareBaseEdge, Edge squareOtherEdge, SquareReconstructionData squareReconstructionData)
  {
    SingleSquareData singleSquareData = new SingleSquareData(baseEdge, otherEdge, squareBaseEdge, squareOtherEdge);

    includeDiagonalForVertices(baseEdge.getEndpoint(), otherEdge.getEndpoint(), singleSquareData);
    includeDiagonalForVertices(baseEdge.getOrigin(), squareBaseEdge.getEndpoint(), singleSquareData);

    return singleSquareData;
  }

  private void includeDiagonalForVertices(Vertex firstVertex, Vertex secondVertex, SingleSquareData singleSquareData)
  {
    Edge diagonal = graph.getAdjacencyMatrix()[firstVertex.getVertexNo()][secondVertex.getVertexNo()];

    if (diagonal != null)
    {
      singleSquareData.getDiagonals().add(diagonal);
    }
  }

  private void storeFirstVertexAndColorFirstSquare(SingleSquareList singleSquareList, SquareReconstructionData squareReconstructionData)
  {
    if (CollectionUtils.isNotEmpty(squareReconstructionData.getNextVertices()) || singleSquareList.size() != 1
            || CollectionUtils.isNotEmpty(singleSquareList.get(0).getDiagonals()))
    {
      return;
    }


    SingleSquareData firstSingleSquare = singleSquareList.get(0);
    Edge baseEdge = firstSingleSquare.getBaseEdge();
    Edge otherEdge = firstSingleSquare.getOtherEdge();


    squareHandlingStrategy.colorEdgeWithNewColor(baseEdge, true);
    squareHandlingStrategy.colorEdgeWithNewColor(otherEdge, true);
    squareHandlingStrategy.colorEdge(baseEdge, firstSingleSquare.getSquareBaseEdge(), otherEdge, squareReconstructionData);
    squareHandlingStrategy.colorEdge(otherEdge, firstSingleSquare.getSquareOtherEdge(), baseEdge, squareReconstructionData);

    Vertex firstVertex = firstSingleSquare.getBaseEdge().getOrigin();
    squareReconstructionData.getNextVertices().add(firstVertex);
    squareReconstructionData.getIncludedVertices()[firstVertex.getVertexNo()] = true;
  }
}
