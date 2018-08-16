package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.SquareFindingEnum;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;

public interface SquareFindingService
{
  SquareFindingEnum findAndProcessSquareForTwoEdges(SquareReconstructionData squareReconstructionData, Edge iEdge, Edge jEdge, boolean firstRunForEdgePair);
}
