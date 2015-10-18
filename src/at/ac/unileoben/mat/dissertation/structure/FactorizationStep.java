package at.ac.unileoben.mat.dissertation.structure;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-01-29
 * Time: 18:45
 * To change this template use File | Settings | File Templates.
 */
public class FactorizationStep
{
  private Vertex[] verticesInLayer;
  private List<List<Vertex>> layer;
  private int firstVertexInLayerIndex;

  public FactorizationStep(int firstVertexInLayerIndex, int layerSize)
  {
    this.firstVertexInLayerIndex = firstVertexInLayerIndex;
    verticesInLayer = new Vertex[layerSize];
    layer = new ArrayList<List<Vertex>>(layerSize);
    for (int i = 0; i < layerSize; i++)
    {
      layer.add(new LinkedList<Vertex>());
    }
  }

  public Vertex[] getVerticesInLayer()
  {
    return verticesInLayer;
  }

  public List<List<Vertex>> getLayer()
  {
    return layer;
  }

  public int getFirstVertexInLayerIndex()
  {
    return firstVertexInLayerIndex;
  }
}
