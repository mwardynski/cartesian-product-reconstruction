package at.ac.unileoben.mat.dissertation.structure;

/**
 * Created by Marcin on 19.08.2017.
 */
public class InconsistentEdge
{
  private Edge edge;
  private InconsistentEdgeTag inconsistentEdgeTag;

  public InconsistentEdge(Edge edge, InconsistentEdgeTag inconsistentEdgeTag)
  {
    this.edge = edge;
    this.inconsistentEdgeTag = inconsistentEdgeTag;
  }

  public Edge getEdge()
  {
    return edge;
  }

  public InconsistentEdgeTag getInconsistentEdgeTag()
  {
    return inconsistentEdgeTag;
  }
}
