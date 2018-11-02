package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data;

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

}
