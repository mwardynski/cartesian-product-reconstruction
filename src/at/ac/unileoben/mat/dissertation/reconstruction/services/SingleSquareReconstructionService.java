package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.SquareMatchingEdgeData;

public interface SingleSquareReconstructionService
{
  void reconstructUsingSquares(SquareMatchingEdgeData[][] squareMatchingEdgesByEdgeAndColor);

}
