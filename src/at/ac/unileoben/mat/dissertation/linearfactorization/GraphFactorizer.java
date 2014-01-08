package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-30
 * Time: 19:21
 * To change this template use File | Settings | File Templates.
 */
public class GraphFactorizer
{
  public void factorize(Graph graph)
  {
    int layersAmount = graph.getLayersAmount();
    for (int currentLayerNo = 2; currentLayerNo < layersAmount; currentLayerNo++)
    {
      labelDownEdges(graph, currentLayerNo);
      labelCrossEdges(graph, currentLayerNo);
      labelUpEdges(graph, currentLayerNo);
      consistencyCheck(graph, currentLayerNo);
    }
  }

  private void labelDownEdges(Graph graph, int currentLayerNo)
  {
    List<Vertex> currentLayer = graph.getLayer(currentLayerNo);
    for (Vertex u : currentLayer)
    {
      //FIXME wrap this two in an object
      int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
      List<Integer> colorsOrder = new ArrayList<Integer>(graph.getGraphColoring().getOriginalColorsAmount());
      EdgesGroup downEdgesGroup = u.getDownEdges();
      List<Edge> uDownEdges = downEdgesGroup.getEdges();
      if (uDownEdges.size() == 1)
      {
        Edge uv = uDownEdges.iterator().next();
        Vertex v = uv.getEndpoint();
        Edge vx = v.getDownEdges().getEdges().iterator().next();
        int vxColor = vx.getLabel().getColor();
        uv.setLabel(new Label(0, vxColor));
        incrementColorCounter(vxColor, colorsCounter, colorsOrder);

        u.setUnitLayer(true);
        if (!v.isUnitLayer())
        {
          graph.assignVertexToUnitLayerAndMergeColors(v);
        }

        int[] colorLengths = new int[graph.getGraphColoring().getOriginalColorsAmount()];
        colorLengths[vxColor] = 1;
        EdgesRef downEdgesRef = new EdgesRef(1);
        downEdgesRef.setColorAmounts(colorLengths);
        downEdgesGroup.setEdgesRef(downEdgesRef);
      }
      else
      {
        Edge uv = uDownEdges.iterator().next();
        if (uv.getLabel() != null)
        {
          break;
        }
        Edge uw = uDownEdges.get(1);
        Vertex w = uw.getEndpoint();
        Vertex v = uv.getEndpoint();
        Edge vx = v.getDownEdges().getEdges().iterator().next();
        Vertex x = vx.getEndpoint();
        boolean uUnitLayer = true;
        Edge wx = graph.getEdgeForVertices(w, x);
        if (wx != null)
        {
          uUnitLayer = false;
          int vxColor = vx.getLabel().getColor();
          int vxMappedColor = graph.getGraphColoring().getCurrentColorMapping(vxColor);
          int wxColor = wx.getLabel().getColor();
          int wxMappedColor = graph.getGraphColoring().getCurrentColorMapping(wxColor);
          if (vxMappedColor != wxMappedColor)
          {
            uv.setLabel(new Label(0, wxColor));
            incrementColorCounter(wxColor, colorsCounter, colorsOrder);
          }
          else
          {
            uUnitLayer = true;
            Edge vxp = v.getEdgeOfDifferentColor(vxMappedColor, graph.getGraphColoring());
            if (vxp != null)
            {
              uUnitLayer = false;
              Vertex xp = vxp.getEndpoint();
              for (int j = 0; j < uDownEdges.size(); j++)
              {
                Edge uwp = uDownEdges.get(j);
                if (uv.equals(uwp))
                {
                  continue;
                }
                Vertex wp = uwp.getEndpoint();
                Edge wpxp = graph.getEdgeForVertices(wp, xp);
                if (wpxp != null)
                {
                  //FIXME maybe more appropriate l(uv) = l(wpxp), l(uwp) = l(vxp)
                  int wpxpColor = wpxp.getLabel().getColor();
                  uv.setLabel(new Label(0, wpxpColor));
                  incrementColorCounter(wpxpColor, colorsCounter, colorsOrder);
                }
              }
              if (uv.getLabel() == null)
              {
                graph.assignVertexToUnitLayerAndMergeColors(u);
              }
            }
          }
        }
        if (uUnitLayer)
        {
          u.setUnitLayer(true);
        }
        for (int i = 0; i < uDownEdges.size(); i++)
        {
          Edge uy = uDownEdges.get(i);
          if (uUnitLayer)
          {
            int vxColor = vx.getLabel().getColor();
            uy.setLabel(new Label(colorsCounter[vxColor], vxColor));
            incrementColorCounter(vxColor, colorsCounter, colorsOrder);
          }
          else
          {
            if (uy.equals(uv))
            {
              continue;
            }
            Vertex y = uy.getEndpoint();
            Edge yz = y.getEdgeByLabel(uv.getLabel(), EdgeType.DOWN);
            Vertex z = yz.getEndpoint();
            Edge vz = graph.getEdgeForVertices(v, z);
            if (vz != null)
            {
              int vzColor = vz.getLabel().getColor();
              if (vz.getLabel().getName() != colorsCounter[i])
              {
                System.out.println("ALLEEERRRRTTTTT");
              }
              uy.setLabel(new Label(colorsCounter[vzColor], vzColor));
              incrementColorCounter(vzColor, colorsCounter, colorsOrder);
            }
            else
            {
              int uvColor = uv.getLabel().getColor();
              uy.setLabel(new Label(colorsCounter[uvColor], uvColor));
              incrementColorCounter(uvColor, colorsCounter, colorsOrder);
            }
          }
        }
      }
      EdgesRef downEdgesRef = getEdgesRef(colorsCounter, colorsOrder);
      u.getDownEdges().setEdgesRef(downEdgesRef);
    }
  }

  private void labelCrossEdges(Graph graph, int currentLayerNo)
  {
    List<Vertex> currentLayer = graph.getLayer(currentLayerNo);
    for (Vertex u : currentLayer)
    {
      List<Edge> uCrossEdges = u.getCrossEdges().getEdges();
      int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
      List<Integer> colorsOrder = new ArrayList<Integer>(graph.getGraphColoring().getOriginalColorsAmount());
      for (int i = 0; i < uCrossEdges.size(); i++)
      {
        Edge uv = uCrossEdges.get(i);
        Label oppositeEdgeLabel = uv.getOpposite().getLabel();
        if (oppositeEdgeLabel != null)
        {
          int oppositeEdgeColor = oppositeEdgeLabel.getColor();
          uv.setLabel(new Label(colorsCounter[oppositeEdgeColor], oppositeEdgeColor));
          incrementColorCounter(oppositeEdgeColor, colorsCounter, colorsOrder);
          continue;
        }
        Vertex v = uv.getEndpoint();
        List<Edge> uDownEdges = u.getDownEdges().getEdges();
        Edge uw = uDownEdges.get(0);
        Label wxLabel = findLabelForParallelEdgeInSquare(v, uw, graph);
        if (wxLabel != null)
        {
          int wxColor = wxLabel.getColor();
          uv.setLabel(new Label(colorsCounter[wxColor], wxColor));
          incrementColorCounter(wxColor, colorsCounter, colorsOrder);
        }
        else
        {
          uw = u.getEdgeOfDifferentColor(uw.getLabel().getColor(), graph.getGraphColoring());
          wxLabel = findLabelForParallelEdgeInSquare(v, uw, graph);
          if (wxLabel != null)
          {
            int wxColor = wxLabel.getColor();
            uv.setLabel(new Label(colorsCounter[wxColor], wxColor));
            incrementColorCounter(wxColor, colorsCounter, colorsOrder);
          }
          else
          {
            graph.assignVertexToUnitLayerAndMergeColors(u);
          }
        }
      }
      EdgesRef crossEdgesRef = getEdgesRef(colorsCounter, colorsOrder);
      u.getCrossEdges().setEdgesRef(crossEdgesRef);
    }
  }

  private Label findLabelForParallelEdgeInSquare(Vertex v, Edge uw, Graph graph)
  {
    Edge vx = v.getEdgeByLabel(uw.getLabel(), EdgeType.DOWN);

    if (vx != null)
    {
      Vertex w = uw.getEndpoint();
      Vertex x = vx.getEndpoint();
      Edge wx = graph.getEdgeForVertices(w, x);
      if (wx != null)
      {
        return wx.getLabel();
      }
    }
    return null;
  }

  private void labelUpEdges(Graph graph, int currentLayerNo)
  {
    List<Vertex> currentLayer = graph.getLayer(currentLayerNo - 1);
    for (Vertex u : currentLayer)
    {
      List<Edge> uUpEdges = u.getUpEdges().getEdges();
      int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
      List<Integer> colorsOrder = new ArrayList<Integer>(graph.getGraphColoring().getOriginalColorsAmount());
      for (int i = 0; i < uUpEdges.size(); i++)
      {
        Edge uv = uUpEdges.get(i);
        int oppositeEdgeColor = uv.getOpposite().getLabel().getColor();
        uv.setLabel(new Label(colorsCounter[oppositeEdgeColor], oppositeEdgeColor));
        incrementColorCounter(oppositeEdgeColor, colorsCounter, colorsOrder);
      }
      EdgesRef upEdgesRef = getEdgesRef(colorsCounter, colorsOrder);
      u.getUpEdges().setEdgesRef(upEdgesRef);
    }
  }

  private EdgesRef getEdgesRef(int[] colorsCounter, List<Integer> colorsOrder)
  {
    int colorsAmount = 0;
    for (int singleColorCounter : colorsCounter)
    {
      if (singleColorCounter != 0)
      {
        colorsAmount++;
      }
    }
    EdgesRef edgesRef = new EdgesRef(colorsAmount);
    edgesRef.setColorsOrderAndAmount(colorsOrder, colorsCounter);
    return edgesRef;
  }

  private void incrementColorCounter(int color, int[] colorsCounter, List<Integer> colorsOrder)
  {
    if (colorsCounter[color] == 0)
    {
      colorsOrder.add(color);
    }
    colorsCounter[color]++;
  }


  private void consistencyCheck(Graph graph, int currentLayerNo)
  {
    List<Vertex> currentLayer = graph.getLayer(currentLayerNo);
    List<Vertex> previousLayer = graph.getLayer(currentLayerNo - 1);
    downAndCrossEdgesConsistencyCheck(graph, currentLayer);
    upEdgesConsistencyCheck(graph, previousLayer);
  }

  private void downAndCrossEdgesConsistencyCheck(Graph graph, List<Vertex> currentLayer)
  {
    for (Vertex u : currentLayer)
    {
      if (u.isUnitLayer())
      {
        continue;
      }
      Edge uv = u.getDownEdges().getEdges().get(0);
      int uvMappedColor = graph.getGraphColoring().getCurrentColorMapping(uv.getLabel().getColor());
      Edge uw = u.getEdgeOfDifferentColor(uvMappedColor, graph.getGraphColoring());
      EnumSet<EdgeType> edgeTypes = EnumSet.of(EdgeType.DOWN, EdgeType.CROSS);
      for (EdgeType edgeType : edgeTypes)
      {
        if (!checkPivotSquares(uv, edgeType, graph) || !checkPivotSquares(uw, edgeType, graph))
        {
          graph.assignVertexToUnitLayerAndMergeColors(u);
          break;
        }
      }
    }
  }

  private void upEdgesConsistencyCheck(Graph graph, List<Vertex> currentLayer)
  {
    for (Vertex u : currentLayer)
    {
      Edge uv = u.getDownEdges().getEdges().get(0);
      Edge uw = null;
      if (!u.isUnitLayer())
      {
        int uvMappedColor = graph.getGraphColoring().getCurrentColorMapping(uv.getLabel().getColor());
        uw = u.getEdgeOfDifferentColor(uvMappedColor, graph.getGraphColoring());
      }
      if (!checkPivotSquares(uv, EdgeType.UP, graph) || (uw != null && !checkPivotSquares(uw, EdgeType.UP, graph)))
      {
        graph.assignVertexToUnitLayerAndMergeColors(u);
      }
    }
  }

  private boolean checkPivotSquares(Edge uv, EdgeType edgeType, Graph graph)
  {
    Vertex u = uv.getOrigin();
    Vertex v = uv.getEndpoint();
    List<Edge> uDifferentThanUv = u.getAllEdgesOfDifferentColor(uv.getLabel().getColor(), graph.getGraphColoring(), edgeType);
    List<Edge> vDifferentThanUv = v.getAllEdgesOfDifferentColor(uv.getLabel().getColor(), graph.getGraphColoring(), edgeType);
    if (uDifferentThanUv.size() != vDifferentThanUv.size())
    {
      return false;
    }
    for (Edge uz : uDifferentThanUv)
    {
      Label uzLabel = uz.getLabel();
      Edge vzp = v.getEdgeByLabel(uzLabel, edgeType);
      if (vzp == null)
      {
        return false;
      }
      Vertex z = uz.getEndpoint();
      Vertex zp = vzp.getEndpoint();
      Edge zzp = graph.getEdgeForVertices(z, zp);
      if (zzp == null || !uv.getLabel().equals(zzp.getLabel()))
      {
        return false;
      }
    }
    return true;
  }
}
