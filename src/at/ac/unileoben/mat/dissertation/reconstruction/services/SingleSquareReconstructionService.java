package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;

import java.util.List;

public interface SingleSquareReconstructionService
{
  void reconstructUsingSquares(Edge[][][] squareMatchingEdgesByEdgeAndColor);

}
