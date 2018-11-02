package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;

public interface MissingSquaresCleanerService
{
  void cleanNotValidMissingSquares(SquareReconstructionData squareReconstructionData);
}
