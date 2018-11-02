package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class NoSquareAtAllGroupsData
{
  private Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints;
  private List<List<Edge>> groupedNoSquareAtAllEdges;

  public NoSquareAtAllGroupsData(Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints, List<List<Edge>> groupedNoSquareAtAllEdges)
  {
    this.groupNumbersForNoSquareAtAllEdgesEndpoints = groupNumbersForNoSquareAtAllEdgesEndpoints;
    this.groupedNoSquareAtAllEdges = groupedNoSquareAtAllEdges;
  }

  public Integer[] getGroupNumbersForNoSquareAtAllEdgesEndpoints()
  {
    return groupNumbersForNoSquareAtAllEdgesEndpoints;
  }

  public List<List<Edge>> getGroupedNoSquareAtAllEdges()
  {
    return groupedNoSquareAtAllEdges;
  }
}
