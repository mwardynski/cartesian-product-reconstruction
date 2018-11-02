package at.ac.unileoben.mat.dissertation.reconstruction.services;

import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.List;

public interface SquareHandlingStrategy
{
  void colorEdge(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData);

  void colorEdgesWithoutSquare(List<Edge> edgesWithoutSquare);

  void colorEdgeWithNewColor(Edge edge, boolean edgeWithSquare);

  void queueSquareSideVertexToNextVertices(Vertex squareSideVertex, SquareReconstructionData squareReconstructionData);

  void queueSquareTopVertexToNextVertices(Vertex squareTopVertex, SquareReconstructionData squareReconstructionData);

  void storeSingleSquareMatchingEdge(Edge baseEdge, Edge squareEdge, int otherColor, int graphSize, SquareMatchingEdgeData[][] squareMatchingEdgesByEdge);

  void storeMissingSquareEntry(Edge baseEdge, Edge otherEdge, MissingSquaresData missingSquaresData);
}
