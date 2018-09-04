package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.SquareMatchingEdgeData;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;

public interface MissingSquaresAnalyzerService
{
  void analyseMissingSquares(SquareReconstructionData squareReconstructionData, SquareMatchingEdgeData[][] squareMatchingEdges);
}
