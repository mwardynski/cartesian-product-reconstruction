package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.BridgeNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresEntryData;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresUniqueEdgesData;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class BridgeNoSquareAtAllMissingSquaresFindingServiceImpl implements BridgeNoSquareAtAllMissingSquaresFindingService
{

  @Autowired
  NoSquareAtAllMissingSquaresFindingCommons noSquareAtAllMissingSquaresFindingCommons;

  @Override
  public List<MissingSquaresUniqueEdgesData> findCorrectBridgeNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    List<MissingSquaresUniqueEdgesData> correctSingleNoSquareAtAllMissingSquares = new LinkedList<>();

    Edge bridgeEdge = squareReconstructionData.getSingleBridgeEdge();
    List<Edge> bridgeEdgePossibleResultEdges = addCorrectBridgeNoSquareAtAllMissingSquaresForBridge(bridgeEdge, squareReconstructionData, correctSingleNoSquareAtAllMissingSquares);

    Edge bridgeOppositeEdge = bridgeEdge.getOpposite();
    List<Edge> bridgeOppositeEdgePossibleResultEdges = addCorrectBridgeNoSquareAtAllMissingSquaresForBridge(bridgeOppositeEdge, squareReconstructionData, correctSingleNoSquareAtAllMissingSquares);


    if (bridgeEdgePossibleResultEdges.size() > 1 || bridgeOppositeEdgePossibleResultEdges.size() > 1)
    {
      System.out.println("many solution possibilities - BRIDGE");
    }

    return correctSingleNoSquareAtAllMissingSquares;
  }

  private List<Edge> addCorrectBridgeNoSquareAtAllMissingSquaresForBridge(Edge bridgeEdge, SquareReconstructionData squareReconstructionData, List<MissingSquaresUniqueEdgesData> correctSingleNoSquareAtAllMissingSquares)
  {
    MissingSquaresEntryData bridgeEdgeMissingSquaresEntryData = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntriesByBaseEdge()
            [bridgeEdge.getOrigin().getVertexNo()][bridgeEdge.getEndpoint().getVertexNo()];
    List<Edge> bridgeEdgeFollowingEdges = noSquareAtAllMissingSquaresFindingCommons.collectFollowingEdges(bridgeEdgeMissingSquaresEntryData);
    MissingSquaresUniqueEdgesData bridgeEdgeMissingSquareEdgesPair = new MissingSquaresUniqueEdgesData(bridgeEdge, bridgeEdgeFollowingEdges.get(0));
    correctSingleNoSquareAtAllMissingSquares.add(bridgeEdgeMissingSquareEdgesPair);
    return bridgeEdgeFollowingEdges;
  }

}
