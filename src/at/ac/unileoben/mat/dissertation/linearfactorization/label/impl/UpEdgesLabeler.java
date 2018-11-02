package at.ac.unileoben.mat.dissertation.linearfactorization.label.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.EdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 2/4/14
 * Time: 9:04 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class UpEdgesLabeler implements EdgesLabeler
{
  @Autowired
  Graph graph;

  @Autowired
  VertexService vertexService;

  @Autowired
  EdgeService edgeService;

  @Autowired
  LabelUtils labelUtils;

  @Override
  public void labelEdges(int currentLayerNo)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo - 1);
    labelEdgesForSelectedVertices(currentLayer);
  }

  public void labelEdgesForSelectedVertices(List<Vertex> selectedVertices)
  {
    for (Vertex u : selectedVertices)
    {
      List<Edge> uUpEdges = u.getUpEdges().getEdges();
      for (int i = 0; i < uUpEdges.size(); i++)
      {
        Edge uv = uUpEdges.get(i);
        Edge vu = uv.getOpposite();
        Edge vuSquareMatchingEdge = vu.getSquareMatchingEdge();
        if (vuSquareMatchingEdge != null)
        {
          Edge uvSquareMatchingEdge = vuSquareMatchingEdge.getOpposite();
          Label uvSquareMatchingEdgeLabel = uvSquareMatchingEdge.getLabel();
          edgeService.addLabel(uv, uvSquareMatchingEdgeLabel.getColor(), uvSquareMatchingEdgeLabel.getName(), uvSquareMatchingEdge, new LabelOperationDetail.Builder(LabelOperationEnum.OPPOSITE).build());
        }
        else
        {
          int oppositeEdgeColor = vu.getLabel().getColor();
          edgeService.addLabel(uv, oppositeEdgeColor, -1, null, new LabelOperationDetail.Builder(LabelOperationEnum.OPPOSITE).build());
        }
      }
      labelUtils.sortEdgesAccordingToLabels(u.getUpEdges(), graph.getGraphColoring());
    }
  }

}
