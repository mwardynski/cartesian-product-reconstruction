package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Marcin on 16.07.2017.
 */
public class EdgeLabelingGroup
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


}
