package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;

public interface SquareMatchingEdgesMergingService
{
  void mergeColorsBasedOnSquareMatching(SquareReconstructionData squareReconstructionData);

  boolean isColorToBeExtended(Edge baseEdge, Edge baseEdgeExtendingEdge, Edge squareEdge, Edge squareEdgeExtendingEdge, SquareReconstructionData squareReconstructionData);
}
