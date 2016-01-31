package at.ac.unileoben.mat.dissertation.linearfactorization.services;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.LayerLabelingData;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 31.01.16
 * Time: 19:43
 * To change this template use File | Settings | File Templates.
 */
public interface EdgeLabelingService
{
  void addVertexWithoutPivotSquare(Vertex v, LayerLabelingData layerLabelingData);

  void addEdgeLabelingGroup(List<Edge> edges, LayerLabelingData layerLabelingData);

  void addEdgeLabelingSubgroup(LayerLabelingData.EdgeLabelingGroup.EdgeLabelingSubgroup edgeLabelingSubgroup, LayerLabelingData layerLabelingData);
}
