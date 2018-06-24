package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class IntervalEdgeReconstructionData
{
  private Edge intervalColorEdge;
  private List<Edge> missingSquareEdges;
  private boolean checked;

  public Edge getIntervalColorEdge()
  {
    return intervalColorEdge;
  }

  public void setIntervalColorEdge(Edge intervalColorEdge)
  {
    this.intervalColorEdge = intervalColorEdge;
  }

  public List<Edge> getMissingSquareEdges()
  {
    return missingSquareEdges;
  }

  public void setMissingSquareEdges(List<Edge> missingSquareEdges)
  {
    this.missingSquareEdges = missingSquareEdges;
  }

  public boolean isChecked()
  {
    return checked;
  }

  public void setChecked(boolean checked)
  {
    this.checked = checked;
  }
}
