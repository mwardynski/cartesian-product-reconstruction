package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data;

import at.ac.unileoben.mat.dissertation.structure.Edge;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Marcin on 16.07.2017.
 */
public class EdgeLabelingSubgroup
{
  public EdgeLabelingSubgroup(Edge edge1, Edge edge2, List<Edge> otherEdges)
  {
    if (edge2 != null && edge2.getEndpoint().getVertexNo() < edge1.getEndpoint().getVertexNo())
    {
      this.firstLabelingBaseEdge = edge2;
      this.secondLabelingBaseEdge = edge1;
    }
    else
    {
      this.firstLabelingBaseEdge = edge1;
      this.secondLabelingBaseEdge = edge2;
    }
    this.otherEdges = otherEdges;
  }

  private Edge firstLabelingBaseEdge;
  private Edge secondLabelingBaseEdge;
  private List<Edge> otherEdges;

  public Edge getFirstLabelingBaseEdge()
  {
    return firstLabelingBaseEdge;
  }

  public Edge getSecondLabelingBaseEdge()
  {
    return secondLabelingBaseEdge;
  }

  public List<Edge> getOtherEdges()
  {
    return otherEdges;
  }
}