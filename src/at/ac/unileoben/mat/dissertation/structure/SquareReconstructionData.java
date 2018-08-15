package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SquareReconstructionData
{
  Vertex currentVertex;
  Queue<Vertex> nextVertices;
  boolean[] includedVertices;
  boolean[][] usedEdges;
  List<MissingSquareData> missingSquares;
  Edge[][][] squareFormingEdges;
  Edge multipleSquaresWardenEdge;

  SquareMatchingEdgeData[][] squareMatchingEdgesByEdge;

  public SquareReconstructionData(int graphSize)
  {
    nextVertices = new LinkedList<>();
    includedVertices = new boolean[graphSize];
    usedEdges = new boolean[graphSize][graphSize];
    missingSquares = new LinkedList<>();
  }

  public Vertex getCurrentVertex()
  {
    return currentVertex;
  }

  public void setCurrentVertex(Vertex currentVertex)
  {
    this.currentVertex = currentVertex;
  }

  public Queue<Vertex> getNextVertices()
  {
    return nextVertices;
  }

  public boolean[] getIncludedVertices()
  {
    return includedVertices;
  }

  public boolean[][] getUsedEdges()
  {
    return usedEdges;
  }

  public List<MissingSquareData> getMissingSquares()
  {
    return missingSquares;
  }

  public Edge[][][] getSquareFormingEdges()
  {
    return squareFormingEdges;
  }

  public void setSquareFormingEdges(Edge[][][] squareFormingEdges)
  {
    this.squareFormingEdges = squareFormingEdges;
  }

  public Edge getMultipleSquaresWardenEdge()
  {
    return multipleSquaresWardenEdge;
  }

  public void setMultipleSquaresWardenEdge(Edge multipleSquaresWardenEdge)
  {
    this.multipleSquaresWardenEdge = multipleSquaresWardenEdge;
  }

  public SquareMatchingEdgeData[][] getSquareMatchingEdgesByEdge()
  {
    return squareMatchingEdgesByEdge;
  }

  public void setSquareMatchingEdgesByEdge(SquareMatchingEdgeData[][] squareMatchingEdgesByEdge)
  {
    this.squareMatchingEdgesByEdge = squareMatchingEdgesByEdge;
  }
}
