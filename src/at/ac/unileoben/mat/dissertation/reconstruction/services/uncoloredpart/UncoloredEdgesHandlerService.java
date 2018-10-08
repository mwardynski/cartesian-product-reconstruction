package at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresUniqueEdgesData;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;

import java.util.List;

public interface UncoloredEdgesHandlerService
{
  List<MissingSquaresUniqueEdgesData> filterCorrectNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData);

  boolean areNormalEdgesOfGivenColorProperty(Edge baseEdge, Edge otherEdge, boolean havingSameColorWanted);
}
