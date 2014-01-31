package at.ac.unileoben.mat.dissertation.linearfactorization.pivotsquare.strategies.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.LinkedList;
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
    List<Edge> notLabeledEdges = new LinkedList<Edge>();
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
        uv.setLabel(new Label(-1, oppositeEdgeColor));
        continue;
      }
      Vertex v = uv.getEndpoint();
      Edge uw = u.getFirstEdge();

      Label wxLabel = findLabelForParallelEdgeInSquare(v, uw, wAdjacencyVector);

      if (wxLabel != null)
      {
        int wxColor = wxLabel.getColor();
        uv.setLabel(new Label(-1, wxColor));
        continue;
      }
      else
      {
        notLabeledEdges.add(uv);
      }
    }
    if (notLabeledEdges.isEmpty())
    {
      return;
    }
    else
    {
      Edge uw = u.getEdgeWithColorToLabel();
      if (nextPhase != null)
      {
        Edge uwp = u.getEdgeOfDifferentColor(uw.getLabel().getColor(), graph.getGraphColoring());
        if (uwp != null)
        {
          Vertex wp = uwp.getEndpoint();
          u.setFirstEdge(uwp);
          nextPhase.addVertex(wp, u);
          return;
        }
      }
      for (Edge uv : notLabeledEdges)
      {
        if (!u.isUnitLayer())
        {
          u.setUnitLayer(true);
        }
        int uwColor = uw.getLabel().getColor();
        uv.setLabel(new Label(-1, uwColor));
      }
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
