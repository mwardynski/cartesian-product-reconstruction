package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.LayerLabelingData;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 1/31/14
 * Time: 4:41 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class CrossEdgesPivotSquareFinderStrategyImpl implements PivotSquareFinderStrategy
{
  @Autowired
  Graph graph;

  @Autowired
  EdgeService edgeService;

  @Autowired
  VertexService vertexService;

  @Autowired
  LabelUtils labelUtils;

  @Override
  public void findPivotSquare(Vertex u, AdjacencyVector wAdjacencyVector, FactorizationStep nextPhase, LayerLabelingData layerLabelingData)
  {
    List<Edge> uCrossEdges = u.getCrossEdges().getEdges();
    int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    for (Edge uv : uCrossEdges)
    {
      if (uv.getLabel() != null)
      {
        continue;
      }
      Label oppositeEdgeLabel = uv.getOpposite().getLabel();
      if (oppositeEdgeLabel != null)
      {
        int oppositeEdgeColor = oppositeEdgeLabel.getColor();
        edgeService.addLabel(uv, oppositeEdgeColor, colorsCounter[oppositeEdgeColor]++, null, new LabelOperationDetail.Builder(LabelOperationEnum.OPPOSITE).build());
        continue;
      }
      Vertex v = uv.getEndpoint();
      Edge uw = u.getFirstEdge();
      Edge vx = edgeService.getEdgeByLabel(v, uw.getLabel(), EdgeType.DOWN);
      Edge wx = null;
      if (vx != null)
      {
        Vertex x = vx.getEndpoint();
        wx = vertexService.getEdgeToVertex(wAdjacencyVector, x);
      }
      if (wx != null && wx.getLabel() != null)
      {
        Label wxLabel = wx.getLabel();
        int wxColor = wxLabel.getColor();
        edgeService.addLabel(uv, wxColor, colorsCounter[wxColor]++, null, new LabelOperationDetail.Builder(LabelOperationEnum.PIVOT_SQUARE_FOLLOWING).sameColorEdge(wx).pivotSquareFirstEdge(uw).pivotSquareFirstEdgeCounterpart(vx).build());
        continue;
      }
      else
      {
        Label uwLabel = uw.getLabel();
        int uwColor = uwLabel.getColor();
        edgeService.addLabel(uv, uwColor, colorsCounter[uwColor]++, null, new LabelOperationDetail.Builder(LabelOperationEnum.PIVOT_SQUARE_FOLLOWING).sameColorEdge(uw).build());
      }
    }
    EdgesRef crossEdgesRef = labelUtils.getEdgesRef(colorsCounter);
    u.getCrossEdges().setEdgesRef(crossEdgesRef);
    List<Edge> sortedEdges = labelUtils.sortEdgesAccordingToLabels(uCrossEdges, graph.getGraphColoring());
    u.getCrossEdges().setEdges(sortedEdges);
    if (u.isUnitLayer())
    {
      vertexService.assignVertexToUnitLayerAndMergeColors(u, true, MergeTagEnum.LABEL_CROSS);
    }
  }
}
