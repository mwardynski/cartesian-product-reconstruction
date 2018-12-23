package at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresUniqueEdgesData;
import at.ac.unileoben.mat.dissertation.structure.NoSquareAtAllCycleNode;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;

import java.util.List;

public interface PartOfCycleNoSquareAtAllMissingSquaresFindingService
{
  List<MissingSquaresUniqueEdgesData> findCorrectPartOfCycleNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData);

  List<List<NoSquareAtAllCycleNode>> findCycleUsingBfs(Edge arbitraryGroupFirstEdge, List<List<Edge>> groupedNoSquareAtAllEdges, Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints, SquareReconstructionData squareReconstructionData, NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo);
}
