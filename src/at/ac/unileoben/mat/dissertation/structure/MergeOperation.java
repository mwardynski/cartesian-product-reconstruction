package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class MergeOperation
{
  List<Edge> edges;
  MergeTagEnum mergeTag;

  public MergeOperation(List<Edge> edges, MergeTagEnum mergeTag)
  {
    this.edges = edges;
    this.mergeTag = mergeTag;
  }

  public List<Edge> getEdges()
  {
    return edges;
  }

  public MergeTagEnum getMergeTag()
  {
    return mergeTag;
  }
}
