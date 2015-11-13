package at.ac.unileoben.mat.dissertation.linearfactorization.services;

import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
public class VertexService
{
  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

  public List<Vertex> getGraphLayer(int i)
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

  public void assignVertexToUnitLayerAndMergeColors(Vertex v, boolean mergeCrossEdges, MergeTagEnum mergeTag) //mergeCrossEdges always true
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
    coloringService.mergeColorsForEdges(edgesToRelabel, mergeTag);
  }

}
