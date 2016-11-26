package at.ac.unileoben.mat.dissertation.linearfactorization.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
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
public class VertexServiceImpl implements VertexService
{
  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

  @Override
  public int getGraphSize()
  {
    return graph.getVertices().size();
  }

  @Override
  public List<Vertex> getGraphLayer(int i)
  {
    return graph.getLayers().get(i);
  }

  @Override
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

  @Override
  public Edge getEdgeToVertex(AdjacencyVector vector, Vertex v)
  {
    return vector.getVector()[v.getVertexNo()];
  }

  @Override
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
    for (Edge vw : edgesToRelabel)
    {
      Vertex w = vw.getEndpoint();
      w.setUnitLayer(true);
    }
    coloringService.mergeColorsForEdges(edgesToRelabel, mergeTag);
  }

}
