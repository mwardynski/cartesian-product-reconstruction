package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.ArrayList;
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
      List<Vertex> currentLayer = graph.getLayer(currentLayerNo);
      for (Vertex u : currentLayer)
      {
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
          uv.getOpposite().setLabel(new Label(0, vxColor));
          incrementColorCounter(vxColor, colorsCounter, colorsOrder);

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
//          Edge uwPlus = null;
          Vertex v = uv.getEndpoint();
          Edge vx = v.getDownEdges().getEdges().iterator().next();
          Vertex x = vx.getEndpoint();
          boolean uUnitLayer = true;
          for (int i = 0; i < uDownEdges.size(); i++)
          {
            Edge uw = uDownEdges.get(i);
            if (uv.equals(uw))
            {
              continue;
            }
            Vertex w = uw.getEndpoint();
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
//                uw.setLabel(new Label(i, vxColor));
//                uwPlus = uw;
                break;
              }
              else
              {
                uUnitLayer = true;
                List<Edge> vDownEdges = v.getDownEdges().getEdges();
                for (Edge vxp : vDownEdges)
                {
                  int vxpColor = vxp.getLabel().getColor();
                  int vxpMappedColor = graph.getGraphColoring().getCurrentColorMapping(vxpColor);
                  if (vxMappedColor != vxpMappedColor)
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
//                        uwp.setLabel(new Label(j, vxpColor));
//                        uwPlus = uwp;
                      }
                    }
                    if (uv.getLabel() == null)
                    {
                      //TODO consistencyTest
                    }
                  }
                }
                break;
              }
            }
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
              if (uy.equals(uv) /*|| uy.equals(uwPlus)*/)
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
        u.getDownEdges().setEdgesRef(edgesRef);
      }
    }
  }

  private void incrementColorCounter(int color, int[] colorsCounter, List<Integer> colorsOrder)
  {
    if (colorsCounter[color] == 0)
    {
      colorsOrder.add(color);
    }
    colorsCounter[color]++;
  }
}
