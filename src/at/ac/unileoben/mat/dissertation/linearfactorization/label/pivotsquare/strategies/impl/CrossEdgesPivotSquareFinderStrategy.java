package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 1/31/14
 * Time: 4:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class CrossEdgesPivotSquareFinderStrategy implements PivotSquareFinderStrategy
{
  @Override
  public void findPivotSquare(Vertex u, AdjacencyVector wAdjacencyVector, FactorizationStep nextPhase, Graph graph)
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
        uv.setLabel(new Label(colorsCounter[oppositeEdgeColor]++, oppositeEdgeColor));
        continue;
      }
      Vertex v = uv.getEndpoint();
      Edge uw = u.getFirstEdge();

      Label wxLabel = findLabelForParallelEdgeInSquare(v, uw, wAdjacencyVector);

      if (wxLabel != null)
      {
        int wxColor = wxLabel.getColor();
        uv.setLabel(new Label(colorsCounter[wxColor]++, wxColor));
        continue;
      }
      else
      {
        Label uwLabel = uw.getLabel();
        int uwColor = uwLabel.getColor();
        uv.setLabel(new Label(colorsCounter[uwColor]++, uwColor));
      }
    }
    EdgesRef crossEdgesRef = LabelUtils.getEdgesRef(colorsCounter);
    u.getCrossEdges().setEdgesRef(crossEdgesRef);
    List<Edge> sortedEdges = LabelUtils.sortEdgesAccordingToLabels(uCrossEdges, graph.getGraphColoring());
    u.getCrossEdges().setEdges(sortedEdges);
    if (u.isUnitLayer())
    {
      graph.assignVertexToUnitLayerAndMergeColors(u, true, MergeTagEnum.LABEL_CROSS);
    }
  }

  private Label findLabelForParallelEdgeInSquare(Vertex v, Edge uw, AdjacencyVector wAdjacencyVector)
  {
    Edge vx = v.getEdgeByLabel(uw.getLabel(), EdgeType.DOWN);

    if (vx != null)
    {
      Vertex x = vx.getEndpoint();
      Edge wx = wAdjacencyVector.getEdgeToVertex(x);
      if (wx != null)
      {
        return wx.getLabel();
      }
    }
    return null;
  }
}
