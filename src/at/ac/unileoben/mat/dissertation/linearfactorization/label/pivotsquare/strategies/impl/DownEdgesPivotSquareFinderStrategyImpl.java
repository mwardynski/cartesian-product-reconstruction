package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.FactorizationStepService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 1/31/14
 * Time: 4:35 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class DownEdgesPivotSquareFinderStrategyImpl implements PivotSquareFinderStrategy
{
  @Autowired
  Graph graph;

  @Autowired
  EdgeService edgeService;

  @Autowired
  ColoringService coloringService;

  @Autowired
  FactorizationStepService factorizationStepService;

  @Autowired
  VertexService vertexService;

  @Override
  public void findPivotSquare(Vertex u, AdjacencyVector xAdjacencyVector, FactorizationStep nextPhase)
  {
    Edge uv = u.getFirstEdge();
    Edge vx = u.getSecondEdge();
    int vxColor = vx.getLabel().getColor();
    int vxMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), vxColor);
    List<Edge> uDownEdges = u.getDownEdges().getEdges();
    for (int j = 1; j < uDownEdges.size(); j++)
    {
      Edge uw = uDownEdges.get(j);
      Vertex w = uw.getEndpoint();
      Edge xw = vertexService.getEdgeToVertex(xAdjacencyVector, w);
      if (xw != null)
      {
        Edge wx = xw.getOpposite();
        int wxColor = wx.getLabel().getColor();
        int wxMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), wxColor);
        if (vxMappedColor != wxMappedColor)
        {
          edgeService.addLabel(uv, wxColor, 0, wx, new LabelOperationDetail.Builder(LabelOperationEnum.PIVOT_SQUARE_FIRST).sameColorEdge(wx).pivotSquareFirstEdge(uw).pivotSquareFirstEdgeCounterpart(vx).build());
          break;
        }
      }
    }
    if (uv.getLabel() == null)
    {
      Vertex v = uv.getEndpoint();
      Edge vxp = edgeService.getEdgeOfDifferentColor(v, vxMappedColor, graph.getGraphColoring());
      if (vxp != null)
      {
        Vertex xp = vxp.getEndpoint();
        u.setSecondEdge(vxp);
        if (nextPhase != null)
        {
          factorizationStepService.addVertex(nextPhase, xp, u);
        }
      }
    }
  }
}
