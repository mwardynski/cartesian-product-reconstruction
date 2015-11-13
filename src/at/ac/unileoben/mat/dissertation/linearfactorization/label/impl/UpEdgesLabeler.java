package at.ac.unileoben.mat.dissertation.linearfactorization.label.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.EdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
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
  VertexService vertexService = new VertexService();

  @Autowired
  LabelUtils labelUtils;

  @Override
  public void labelEdges(int currentLayerNo)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo - 1);
    for (Vertex u : currentLayer)
    {
      List<Edge> uUpEdges = u.getUpEdges().getEdges();
      int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
      for (int i = 0; i < uUpEdges.size(); i++)
      {
        Edge uv = uUpEdges.get(i);
        int oppositeEdgeColor = uv.getOpposite().getLabel().getColor();
        uv.setLabel(new Label(colorsCounter[oppositeEdgeColor], oppositeEdgeColor));
        colorsCounter[oppositeEdgeColor]++;
      }
      EdgesRef upEdgesRef = labelUtils.getEdgesRef(colorsCounter);
      u.getUpEdges().setEdgesRef(upEdgesRef);
      List<Edge> sortedEdges = labelUtils.sortEdgesAccordingToLabels(u.getUpEdges().getEdges(), graph.getGraphColoring());
      u.getUpEdges().setEdges(sortedEdges);
    }
  }

}
