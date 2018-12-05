package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class SquareReconstructionData
{
  Vertex currentVertex;
  Queue<Vertex> nextVertices;
  boolean[] includedVertices;
  Queue<Vertex> postponedVertices;
  boolean[] includedPostponedVertices;
  List<Vertex> noticedPostponedVertices;
  MissingSquaresData missingSquaresData;
  SingleSquareList[][][] squares;
  List<Vertex> currentVertexNeighborsToQueue;

  SquareMatchingEdgeData[][] squareMatchingEdgesByEdge;

  public SquareReconstructionData(int graphSize)
  {
    nextVertices = new LinkedList<>();
    includedVertices = new boolean[graphSize];
    postponedVertices = new LinkedList<>();
    includedPostponedVertices = new boolean[graphSize];
    noticedPostponedVertices = new LinkedList<>();
    missingSquaresData = new MissingSquaresData(graphSize);
    squares = new SingleSquareList[graphSize][graphSize][graphSize];
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

  public Queue<Vertex> getPostponedVertices()
  {
    return postponedVertices;
  }

  public boolean[] getIncludedPostponedVertices()
  {
    return includedPostponedVertices;
  }

  public List<Vertex> getNoticedPostponedVertices()
  {
    return noticedPostponedVertices;
  }

  public MissingSquaresData getMissingSquaresData()
  {
    return missingSquaresData;
  }

  public SingleSquareList[][][] getSquares()
  {
    return squares;
  }

  public SquareMatchingEdgeData[][] getSquareMatchingEdgesByEdge()
  {
    return squareMatchingEdgesByEdge;
  }

  public void setSquareMatchingEdgesByEdge(SquareMatchingEdgeData[][] squareMatchingEdgesByEdge)
  {
    this.squareMatchingEdgesByEdge = squareMatchingEdgesByEdge;
  }

  public List<Vertex> getCurrentVertexNeighborsToQueue()
  {
    return currentVertexNeighborsToQueue;
  }

  public void setCurrentVertexNeighborsToQueue(List<Vertex> currentVertexNeighborsToQueue)
  {
    this.currentVertexNeighborsToQueue = currentVertexNeighborsToQueue;
  }
}
