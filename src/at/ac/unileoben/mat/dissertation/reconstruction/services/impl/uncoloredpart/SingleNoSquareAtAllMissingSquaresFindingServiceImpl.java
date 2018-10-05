package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.SingleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresUniqueEdgesData;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;
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
    List<MissingSquaresUniqueEdgesData> correctSingleNoSquareAtAllMissingSquares = new LinkedList<>();

    for (MissingSquaresUniqueEdgesData noSquareAtAllMissingSquare : noSquareAtAllMissingSquares)
    {
      Edge normallyColoredEdge;

      if (noSquareAtAllMissingSquare.getBaseEdge().getLabel().getName() == -2)
      {
        normallyColoredEdge = noSquareAtAllMissingSquare.getOtherEdge();
      }
      else
      {
        normallyColoredEdge = noSquareAtAllMissingSquare.getBaseEdge();
      }
      Edge otherEdgeOpposite = normallyColoredEdge.getOpposite();

      List<Edge> sameColorToNormallyColoredEdgesHavingMissingSquares = normallyColoredEdge.getEndpoint().getEdges().stream()
              .filter(edge -> edge != otherEdgeOpposite)
              .filter(edge -> coloringService.getCurrentColorMapping(graph.getGraphColoring(), edge.getLabel().getColor())
                      == coloringService.getCurrentColorMapping(graph.getGraphColoring(), normallyColoredEdge.getLabel().getColor()))
              .map(Edge::getOpposite)
              .filter(edge -> squareReconstructionData.getMissingSquaresData()
                      .getMissingSquaresEntriesByBaseEdge()[edge.getOrigin().getVertexNo()][edge.getEndpoint().getVertexNo()] != null)
              .collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(sameColorToNormallyColoredEdgesHavingMissingSquares))
      {
        correctSingleNoSquareAtAllMissingSquares.add(noSquareAtAllMissingSquare);
      }
    }
    return correctSingleNoSquareAtAllMissingSquares;

  }
}
