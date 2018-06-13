package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class IntervalEdgeReconstructionData
{
  private Edge originEdge;
  private List<Edge> missingSquareEdges;
  private boolean incorrect;

  public Edge getOriginEdge()
  {
    return originEdge;
  }

  public void setOriginEdge(Edge originEdge)
  {
    this.originEdge = originEdge;
  }

  public List<Edge> getMissingSquareEdges()
  {
    return missingSquareEdges;
  }

  public void setMissingSquareEdges(List<Edge> missingSquareEdges)
  {
    this.missingSquareEdges = missingSquareEdges;
  }

  public boolean isIncorrect()
  {
    return incorrect;
  }

  public void setIncorrect(boolean incorrect)
  {
    this.incorrect = incorrect;
  }
}
