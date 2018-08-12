package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;

import java.util.List;

public interface SquareColoringService
{
  void colorEdge(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData);

  void colorEdgesWithoutSquare(List<Edge> edgesWithoutSquare);
}
