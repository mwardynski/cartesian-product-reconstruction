package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;

public interface SingleSquareReconstructionService
{
  void reconstructUsingSquares();

  boolean findSquareForTwoEdges(SquareReconstructionData squareReconstructionData, Edge iEdge, Edge jEdge);
}
