package at.ac.unileoben.mat.dissertation.structure;

public class MissingEdgeData
{
  Edge edge;
  int numberOfDistinctMissingSquareTriples;
  boolean warden;

  public MissingEdgeData(Edge edge)
  {
    this.edge = edge;
    this.numberOfDistinctMissingSquareTriples = 1;
  }

  public Edge getEdge()
  {
    return edge;
  }

  public int getNumberOfDistinctMissingSquareTriples()
  {
    return numberOfDistinctMissingSquareTriples;
  }

  public void setNumberOfDistinctMissingSquareTriples(int numberOfDistinctMissingSquareTriples)
  {
    this.numberOfDistinctMissingSquareTriples = numberOfDistinctMissingSquareTriples;
  }

  public boolean isWarden()
  {
    return warden;
  }

  public void setWarden(boolean warden)
  {
    this.warden = warden;
  }
}
