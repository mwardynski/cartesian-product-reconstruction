package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 1/31/14
 * Time: 4:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class DownEdgesPivotSquareFinderStrategy implements PivotSquareFinderStrategy
{
  @Override
  public void findPivotSquare(Vertex u, AdjacencyVector xAdjacencyVector, FactorizationStep nextPhase, Graph graph)
  {
    Edge uv = u.getFirstEdge();
    Edge vx = u.getSecondEdge();
    int vxColor = vx.getLabel().getColor();
    int vxMappedColor = graph.getGraphColoring().getCurrentColorMapping(vxColor);
    List<Edge> uDownEdges = u.getDownEdges().getEdges();
    for (int j = 1; j < uDownEdges.size(); j++)
    {
      Edge uw = uDownEdges.get(j);
      Vertex w = uw.getEndpoint();
      Edge xw = xAdjacencyVector.getEdgeToVertex(w);
      if (xw != null)
      {
        Edge wx = xw.getOpposite();
        int wxColor = wx.getLabel().getColor();
        int wxMappedColor = graph.getGraphColoring().getCurrentColorMapping(wxColor);
        if (vxMappedColor != wxMappedColor)
        {
          uv.setLabel(new Label(0, wxColor));
          break;
        }
      }
    }
    if (uv.getLabel() == null)
    {
      Vertex v = uv.getEndpoint();
      Edge vxp = v.getEdgeOfDifferentColor(vxMappedColor, graph.getGraphColoring());
      if (vxp != null)
      {
        Vertex xp = vxp.getEndpoint();
        u.setSecondEdge(vxp);
        if (nextPhase != null)
        {
          nextPhase.addVertex(xp, u);
        }
      }
    }
  }
}
