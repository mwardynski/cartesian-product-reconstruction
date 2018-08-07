package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class MergeInvocation
{
  private MergeTagEnum mergeTag;
  private List<Edge> edges;

  public MergeInvocation(MergeTagEnum mergeTag, List<Edge> edges)
  {
    this.mergeTag = mergeTag;
    this.edges = edges;
  }

  public MergeTagEnum getMergeTag()
  {
    return mergeTag;
  }

  public List<Edge> getEdges()
  {
    return edges;
  }
}
