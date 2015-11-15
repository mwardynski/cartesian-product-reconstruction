package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
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
public class CrossEdgesPivotSquareFinderStrategy implements PivotSquareFinderStrategy
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
  public void findPivotSquare(Vertex u, AdjacencyVector wAdjacencyVector, FactorizationStep nextPhase)
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
        edgeService.addLabel(uv, oppositeEdgeColor, colorsCounter[oppositeEdgeColor]++);
        continue;
      }
      Vertex v = uv.getEndpoint();
      Edge uw = u.getFirstEdge();

      Label wxLabel = findLabelForParallelEdgeInSquare(v, uw, wAdjacencyVector);

      if (wxLabel != null)
      {
        int wxColor = wxLabel.getColor();
        edgeService.addLabel(uv, wxColor, colorsCounter[wxColor]++);
        continue;
      }
      else
      {
        Label uwLabel = uw.getLabel();
        int uwColor = uwLabel.getColor();
        edgeService.addLabel(uv, uwColor, colorsCounter[uwColor]++);
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

  private Label findLabelForParallelEdgeInSquare(Vertex v, Edge uw, AdjacencyVector wAdjacencyVector)
  {
    Edge vx = edgeService.getEdgeByLabel(v, uw.getLabel(), EdgeType.DOWN);

    if (vx != null)
    {
      Vertex x = vx.getEndpoint();
      Edge wx = vertexService.getEdgeToVertex(wAdjacencyVector, x);
      if (wx != null)
      {
        return wx.getLabel();
      }
    }
    return null;
  }
}
