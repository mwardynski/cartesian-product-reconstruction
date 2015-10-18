package at.ac.unileoben.mat.dissertation.linearfactorization.services;

import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 18.10.15
 * Time: 19:29
 * To change this template use File | Settings | File Templates.
 */
public class VertexService
{
  ColoringService coloringService = new ColoringService();

  public List<Vertex> getLayer(Graph graph, int i)
  {
    return graph.getLayers().get(i);
  }

  public List<List<Vertex>> createLayersList(List<Vertex> vertices)
  {
    int layersCount = vertices.get(vertices.size() - 1).getBfsLayer() + 1;
    List<List<Vertex>> layers = new ArrayList<List<Vertex>>(layersCount);
    for (int i = 0; i < layersCount; i++)
    {
      layers.add(new ArrayList<Vertex>());
    }
    for (Vertex v : vertices)
    {
      layers.get(v.getBfsLayer()).add(v);
    }
    return layers;
  }

  public Edge getEdgeToVertex(AdjacencyVector vector, Vertex v)
  {
    return vector.getVector()[v.getVertexNo()];
  }

  public void assignVertexToUnitLayerAndMergeColors(Graph graph, Vertex v, boolean mergeCrossEdges, MergeTagEnum mergeTag) //mergeCrossEdges always true
  {
    v.setUnitLayer(true);
    List<Edge> vDownEdges = v.getDownEdges().getEdges();
    List<Edge> edgesToRelabel = new LinkedList<Edge>(vDownEdges);
    if (mergeCrossEdges)
    {
      List<Edge> vCrossEdges = v.getCrossEdges().getEdges();
      edgesToRelabel.addAll(vCrossEdges);
    }
    boolean[] colorPresence = new boolean[graph.getGraphColoring().getOriginalColorsAmount()];
    for (Edge vw : edgesToRelabel)
    {
      Vertex w = vw.getEndpoint();
      w.setUnitLayer(true);
    }
    coloringService.mergeColorsForEdges(graph, edgesToRelabel, mergeTag);
  }

}
