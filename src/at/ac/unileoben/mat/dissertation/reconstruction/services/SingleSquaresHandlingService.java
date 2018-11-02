package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.SingleSquareList;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;

public interface SingleSquaresHandlingService
{

  SingleSquareList findSquaresForGivenEdges(Edge baseEdge, Edge otherEdge, SquareReconstructionData squareReconstructionData);

  void collectAllSingleSquares(SquareReconstructionData squareReconstructionData);
}
