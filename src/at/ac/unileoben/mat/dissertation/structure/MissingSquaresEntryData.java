package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;

public class MissingSquaresEntryData
{
  Edge baseEdge;
  List<Integer> existingColors;
  List<Edge>[] otherEdgesByColors;
  Edge[] includedOtherEdges;

  public MissingSquaresEntryData(Edge baseEdge, int size)
  {
    this.baseEdge = baseEdge;
    this.existingColors = new LinkedList<>();
    this.otherEdgesByColors = new List[size];
    this.includedOtherEdges = new Edge[size];
  }

  public Edge getBaseEdge()
  {
    return baseEdge;
  }

  public List<Integer> getExistingColors()
  {
    return existingColors;
  }

  public List<Edge>[] getOtherEdgesByColors()
  {
    return otherEdgesByColors;
  }

  public Edge[] getIncludedOtherEdges()
  {
    return includedOtherEdges;
  }
}
