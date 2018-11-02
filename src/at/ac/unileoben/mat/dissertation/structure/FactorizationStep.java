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
  private Edge[] firstLayerPerVertexEdges;

  public FactorizationStep(int firstVertexInLayerIndex, int layerSize, int graphSize)
  {
    this.firstVertexInLayerIndex = firstVertexInLayerIndex;
    verticesInLayer = new Vertex[layerSize];
    layer = new ArrayList<List<Vertex>>(layerSize);
    for (int i = 0; i < layerSize; i++)
    {
      layer.add(new LinkedList<Vertex>());
    }
    firstLayerPerVertexEdges = new Edge[graphSize];
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

  public Edge[] getFirstLayerPerVertexEdges()
  {
    return firstLayerPerVertexEdges;
  }
}
