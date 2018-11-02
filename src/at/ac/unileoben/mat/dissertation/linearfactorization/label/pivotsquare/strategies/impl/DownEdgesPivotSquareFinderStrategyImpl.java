package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.LayerLabelingData;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.*;
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

  @Autowired
  EdgeLabelingService edgeLabelingService;

  @Override
  public void findPivotSquare(Vertex u, AdjacencyVector xAdjacencyVector, FactorizationStep thisPhase, FactorizationStep nextPhase, LayerLabelingData layerLabelingData)
  {
    Edge uv = edgeService.getFirstEdge(u, EdgeType.DOWN);
    Edge vx = factorizationStepService.getFirstLayerEdgeForVertexInFactorizationStep(thisPhase, u);
    Label vxLabel = vx.getLabel();
    int vxMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), vxLabel.getColor());
    List<Edge> uDownEdges = u.getDownEdges().getEdges();
    Edge uw = null;
    for (int j = 1; j < uDownEdges.size(); j++)
    {
      uw = uDownEdges.get(j);
      Vertex w = uw.getEndpoint();
      Edge xw = vertexService.getEdgeToVertex(xAdjacencyVector, w);
      if (xw != null)
      {
        Edge wx = xw.getOpposite();
        Label wxLabel = wx.getLabel();
        int wxMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), wxLabel.getColor());
        if (vxMappedColor != wxMappedColor)
        {
          edgeService.addLabel(uv, wxLabel.getColor(), wxLabel.getName(), wx, new LabelOperationDetail.Builder(LabelOperationEnum.PIVOT_SQUARE_FIRST).sameColorEdge(wx).pivotSquareFirstEdge(uw).pivotSquareFirstEdgeCounterpart(vx).build());
          edgeService.addLabel(uw, vxLabel.getColor(), vxLabel.getName(), vx, new LabelOperationDetail.Builder(LabelOperationEnum.PIVOT_SQUARE_FIRST).sameColorEdge(wx).pivotSquareFirstEdge(uw).pivotSquareFirstEdgeCounterpart(vx).build());
          break;
        }
      }
    }

    if (uv.getLabel() != null)
    {
      edgeLabelingService.addEdgeLabelingGroup(uDownEdges, layerLabelingData);
    }
    else
    {
      Vertex v = uv.getEndpoint();
      Edge vxp = edgeService.getEdgeOfDifferentColor(v, vxMappedColor, graph.getGraphColoring());
      if (nextPhase != null && vxp != null)
      {
        Vertex xp = vxp.getEndpoint();
        factorizationStepService.assignFirstLayerEdgeForVertexInFactorizationStep(nextPhase, u, vxp);
        factorizationStepService.addVertex(nextPhase, xp, u);
      }
      else
      {
        edgeLabelingService.addVertexWithoutPivotSquare(u, layerLabelingData);
      }
    }

  }
}
