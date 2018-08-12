package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class MergeOperation
{
  List<Edge> edges;
  MergeTagEnum mergeTag;
  List<Integer> mergedColors;
  GraphColoring mergeGraphColoring;


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

  public List<Integer> getMergedColors()
  {
    return mergedColors;
  }

  public void setMergedColors(List<Integer> mergedColors)
  {
    this.mergedColors = mergedColors;
  }

  public GraphColoring getMergeGraphColoring()
  {
    return mergeGraphColoring;
  }

  public void setMergeGraphColoring(GraphColoring mergeGraphColoring)
  {
    this.mergeGraphColoring = mergeGraphColoring;
  }
}
