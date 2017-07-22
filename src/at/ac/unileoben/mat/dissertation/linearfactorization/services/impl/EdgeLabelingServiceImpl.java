package at.ac.unileoben.mat.dissertation.linearfactorization.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.EdgeLabelingGroup;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.EdgeLabelingSubgroup;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.LayerLabelingData;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeLabelingService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 31.01.16
 * Time: 19:38
 * To change this template use File | Settings | File Templates.
 */
@Component
public class EdgeLabelingServiceImpl implements EdgeLabelingService
{

  @Override
  public void addVertexWithoutPivotSquare(Vertex v, LayerLabelingData layerLabelingData)
  {
    layerLabelingData.getNoPivotSquareVerties().add(v);
  }

  @Override
  public void addEdgeLabelingGroup(List<Edge> edges, LayerLabelingData layerLabelingData)
  {
    List<Edge> labeledEdges = new ArrayList<>(2);
    List<Edge> notLabeledEdges = new LinkedList<>();
    for (Edge e : edges)
    {
      if (e.getLabel() != null)
      {
        labeledEdges.add(e);
      }
      else
      {
        notLabeledEdges.add(e);
      }
    }
    EdgeLabelingSubgroup edgeLabelingSubgroup = new EdgeLabelingSubgroup(labeledEdges.get(0), labeledEdges.get(1), notLabeledEdges);
    addEdgeLabelingSubgroup(edgeLabelingSubgroup, layerLabelingData);
  }

  @Override
  public void addEdgeLabelingSubgroup(EdgeLabelingSubgroup edgeLabelingSubgroup, LayerLabelingData layerLabelingData)
  {
    int vertexNumberDifference = layerLabelingData.getIndexOfFirstVertexInPreviousLayer();
    Vertex labelingGroupVertex = edgeLabelingSubgroup.getFirstLabelingBaseEdge().getEndpoint();

    EdgeLabelingGroup[] edgeLabelingGroups = layerLabelingData.getEdgeLabelingGroups();
    EdgeLabelingGroup targetGroup = edgeLabelingGroups[labelingGroupVertex.getVertexNo() - vertexNumberDifference];
    if (targetGroup != null)
    {
      targetGroup.getEdgeLabelingSubgroups().add(edgeLabelingSubgroup);
    }
    else
    {
      targetGroup = new EdgeLabelingGroup(labelingGroupVertex, edgeLabelingSubgroup);
      edgeLabelingGroups[labelingGroupVertex.getVertexNo() - vertexNumberDifference] = targetGroup;
    }
  }
}
