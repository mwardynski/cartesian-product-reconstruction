package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class SquareMatchingEdgeData
{
  List<Edge>[] edgesByColors;
  Edge[] includedEdges;

  public SquareMatchingEdgeData(int size)
  {
    edgesByColors = new List[size];
    includedEdges = new Edge[size];
  }

  public List<Edge>[] getEdgesByColors()
  {
    return edgesByColors;
  }

  public Edge[] getIncludedEdges()
  {
    return includedEdges;
  }
}
