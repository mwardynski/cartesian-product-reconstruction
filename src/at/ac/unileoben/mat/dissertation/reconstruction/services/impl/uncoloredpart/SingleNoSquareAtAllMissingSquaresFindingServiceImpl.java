package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.SingleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SingleNoSquareAtAllMissingSquaresFindingServiceImpl implements SingleNoSquareAtAllMissingSquaresFindingService
{

  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

  public List<MissingSquaresUniqueEdgesData> findCorrectSingleNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    List<List<MissingSquaresUniqueEdgesData>> missingSquaresByMiddleVertices = splitMissingSquaresByMiddleVertexOfSingleNoSquareAtAllMissingSquare(noSquareAtAllMissingSquares);

    List<MissingSquaresUniqueEdgesData> correctSingleNoSquareAtAllMissingSquares = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData> missingSquaresForArbitrarySingleNoSquareAtAllEdge = missingSquaresByMiddleVertices.get(0);

    for (MissingSquaresUniqueEdgesData noSquareAtAllMissingSquare : missingSquaresForArbitrarySingleNoSquareAtAllEdge)
    {
      Edge normallyColoredEdge = noSquareAtAllMissingSquare.getOtherEdge();

      Edge normallyColoredEdgeOpposite = normallyColoredEdge.getOpposite();

      List<MissingSquaresUniqueEdgesData> sameColorToNormallyColoredEdgesHavingMissingSquares = normallyColoredEdge.getEndpoint().getEdges().stream()
              .filter(edge -> edge != normallyColoredEdgeOpposite)
              .filter(edge -> coloringService.getCurrentColorMapping(graph.getGraphColoring(), edge.getLabel().getColor())
                      == coloringService.getCurrentColorMapping(graph.getGraphColoring(), normallyColoredEdge.getLabel().getColor()))
              .map(Edge::getOpposite)
              .filter(edge -> squareReconstructionData.getMissingSquaresData()
                      .getMissingSquaresEntriesByBaseEdge()[edge.getOrigin().getVertexNo()][edge.getEndpoint().getVertexNo()] != null)
              .map(edge ->
              {
                MissingSquaresEntryData missingSquaresEntry = squareReconstructionData.getMissingSquaresData()
                        .getMissingSquaresEntriesByBaseEdge()[edge.getOrigin().getVertexNo()][edge.getEndpoint().getVertexNo()];
                Edge otherEdge = missingSquaresEntry.getOtherEdgesByColors()[missingSquaresEntry.getExistingColors().get(0)].get(0);
                return new MissingSquaresUniqueEdgesData(missingSquaresEntry.getBaseEdge(), otherEdge);
              })
              .collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(sameColorToNormallyColoredEdgesHavingMissingSquares))
      {
        correctSingleNoSquareAtAllMissingSquares.add(noSquareAtAllMissingSquare);
        correctSingleNoSquareAtAllMissingSquares.addAll(sameColorToNormallyColoredEdgesHavingMissingSquares);
        break;
      }
    }
    return correctSingleNoSquareAtAllMissingSquares;

  }

  private List<List<MissingSquaresUniqueEdgesData>> splitMissingSquaresByMiddleVertexOfSingleNoSquareAtAllMissingSquare(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares)
  {
    List<List<MissingSquaresUniqueEdgesData>> missingSquaresByMiddleVertices = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData>[] missingSquaresByMiddleVertexIncluded = new List[graph.getVertices().size()];

    noSquareAtAllMissingSquares.stream()
            .filter(missingSquare -> missingSquare.getBaseEdge().getLabel().getName() == -2)
            .forEach(missingSquare ->
            {
              Edge baseEdge = missingSquare.getBaseEdge();
              List<MissingSquaresUniqueEdgesData> missingSquareByMiddleVertex = missingSquaresByMiddleVertexIncluded[baseEdge.getOrigin().getVertexNo()];
              if (missingSquareByMiddleVertex == null)
              {
                missingSquareByMiddleVertex = new LinkedList<>();
                missingSquaresByMiddleVertexIncluded[baseEdge.getOrigin().getVertexNo()] = missingSquareByMiddleVertex;
                missingSquaresByMiddleVertices.add(missingSquareByMiddleVertex);
              }

              missingSquareByMiddleVertex.add(missingSquare);
            });
    return missingSquaresByMiddleVertices;
  }
}
