package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 31.01.16
 * Time: 17:57
 * To change this template use File | Settings | File Templates.
 */
public class LayerLabelingData
{
  private int indexOfFirstVertexInPreviousLayer;
  private List<Vertex> noPivotSquareVerties;
  private EdgeLabelingGroup[] edgeLabelingGroups;

  public LayerLabelingData(List<Vertex> previousLayerVertices)
  {
    indexOfFirstVertexInPreviousLayer = previousLayerVertices.iterator().next().getVertexNo();
    edgeLabelingGroups = new EdgeLabelingGroup[previousLayerVertices.size()];
    noPivotSquareVerties = new LinkedList<>();
  }

  public int getIndexOfFirstVertexInPreviousLayer()
  {
    return indexOfFirstVertexInPreviousLayer;
  }

  public List<Vertex> getNoPivotSquareVerties()
  {
    return noPivotSquareVerties;
  }

  public EdgeLabelingGroup[] getEdgeLabelingGroups()
  {
    return edgeLabelingGroups;
  }

  public static class EdgeLabelingGroup
  {
    private Vertex groupCommonVertex;
    private List<EdgeLabelingSubgroup> edgeLabelingSubgroups;

    public EdgeLabelingGroup(Vertex groupCommonVertex, EdgeLabelingSubgroup edgeLabelingSubgroup)
    {
      this.groupCommonVertex = groupCommonVertex;
      edgeLabelingSubgroups = new LinkedList<>();
      edgeLabelingSubgroups.add(edgeLabelingSubgroup);
    }

    public Vertex getGroupCommonVertex()
    {
      return groupCommonVertex;
    }

    public List<EdgeLabelingSubgroup> getEdgeLabelingSubgroups()
    {
      return edgeLabelingSubgroups;
    }

    public static class EdgeLabelingSubgroup
    {
      public EdgeLabelingSubgroup(Edge firstLabelingBaseEdge, Edge secondLabelingBaseEdge, List<Edge> otherEdges)
      {
        this.firstLabelingBaseEdge = firstLabelingBaseEdge;
        this.secondLabelingBaseEdge = secondLabelingBaseEdge;
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
  }
}
