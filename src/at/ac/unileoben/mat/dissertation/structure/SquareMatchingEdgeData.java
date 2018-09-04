package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;

public class SquareMatchingEdgeData
{
  List<Integer> existingColors;
  List<Edge>[] edgesByColors;
  Edge[] includedEdges;

  public SquareMatchingEdgeData(int size)
  {
    existingColors = new LinkedList<>();
    edgesByColors = new List[size];
    includedEdges = new Edge[size];
  }

  public List<Integer> getExistingColors()
  {
    return existingColors;
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
