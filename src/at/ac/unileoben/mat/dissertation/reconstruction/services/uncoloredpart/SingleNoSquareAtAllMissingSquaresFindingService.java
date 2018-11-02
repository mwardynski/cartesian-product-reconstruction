package at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart;

import at.ac.unileoben.mat.dissertation.structure.MissingSquaresUniqueEdgesData;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;

import java.util.List;

public interface SingleNoSquareAtAllMissingSquaresFindingService
{
  List<MissingSquaresUniqueEdgesData> findCorrectSingleNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData);
}
