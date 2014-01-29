package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-01-29
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
public class GraphFactorizer2
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
    List<Vertex> previousLayer = graph.getLayer(currentLayerNo - 1);
    List<Vertex> prePreviousLayer = graph.getLayer(currentLayerNo - 2);


    FactorizationSteps factorizationSteps = new FactorizationSteps(previousLayer, prePreviousLayer);

    for (Vertex u : currentLayer)
    {
      EdgesGroup downEdgesGroup = u.getDownEdges();
      List<Edge> uDownEdges = downEdgesGroup.getEdges();
      if (uDownEdges.size() == 1)
      {
        int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
        Edge uv = uDownEdges.iterator().next();
        Vertex v = uv.getEndpoint();
        Edge vx = v.getDownEdges().getEdges().iterator().next();
        int vxColor = vx.getLabel().getColor();
        uv.setLabel(new Label(0, vxColor));
        colorsCounter[vxColor]++;

        u.setUnitLayer(true);//FIXME UNIT
        if (!v.isUnitLayer())
        {
          graph.assignVertexToUnitLayerAndMergeColors(v, true); //not invoked
        }

        int[] colorLengths = new int[graph.getGraphColoring().getOriginalColorsAmount()];
        colorLengths[vxColor] = 1;
        EdgesRef downEdgesRef = new EdgesRef(1);
        downEdgesRef.setColorAmounts(colorLengths);
        downEdgesGroup.setEdgesRef(downEdgesRef);
      }
      else
      {
        Edge uv = uDownEdges.get(0);
        u.setFirstEdge(uv);
        Vertex v = uv.getEndpoint();
        Edge vx = v.getDownEdges().getEdges().get(0);
        u.setSecondEdge(vx);
        u.setEdgeWithColorToLabel(vx);
        Vertex x = vx.getEndpoint();
        factorizationSteps.initialVertexInsert(u, v, x);
      }
    }

    FactorizationStep findSquareFirstPhase = factorizationSteps.getFindSquareFirstPhase();
    FactorizationStep findSquareSecondPhase = factorizationSteps.getFindSquareSecondPhase();
    singleFindPivotSquarePhase(graph, findSquareFirstPhase, findSquareSecondPhase);
    singleFindPivotSquarePhase(graph, findSquareSecondPhase, null);

    FactorizationStep labelVerticesPhase = factorizationSteps.getLabelVerticesPhase();
    for (Vertex v : labelVerticesPhase.getReferenceVertices())
    {
      if (v == null)
      {
        continue;
      }
      AdjacencyVector vAdjacencyVector = new AdjacencyVector(graph.getVertices().size(), v);
      List<Vertex> assignedVertices = labelVerticesPhase.getAssignedVertices(v);
      Iterator<Vertex> assignedVerticesIterator = assignedVertices.iterator();
      while (assignedVerticesIterator.hasNext())
      {
        int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
        Vertex u = assignedVerticesIterator.next();
        Edge uv = u.getFirstEdge();
        boolean noPivotSquare = false;
        Edge edgeWithColorToLabel = u.getEdgeWithColorToLabel();
        int colorToLabel = edgeWithColorToLabel.getLabel().getColor();
        if (uv.getLabel() == null)
        {
          noPivotSquare = true;
          u.setUnitLayer(true);
          uv.setLabel(new Label(0, colorToLabel));
        }
        List<Edge> uDownEdges = u.getDownEdges().getEdges();
        for (Edge uy : uDownEdges)
        {
          if (uy.equals(uv))
          {
            colorsCounter[uy.getLabel().getColor()]++;
            continue;
          }
          if (noPivotSquare)
          {
            uy.setLabel(new Label(colorsCounter[colorToLabel], colorToLabel));
            colorsCounter[colorToLabel]++;
          }
          else
          {
            Vertex y = uy.getEndpoint();
            Edge yz = y.getEdgeByLabel(uv.getLabel(), EdgeType.DOWN);
            if (yz != null)
            {
              Vertex z = yz.getEndpoint();
              Edge vz = vAdjacencyVector.getEdgeToVertex(z);
              if (vz != null)
              {
                int vzColor = vz.getLabel().getColor();
                uy.setLabel(new Label(colorsCounter[vzColor], vzColor));
                colorsCounter[vzColor]++;
              }
            }
            if (uy.getLabel() == null)
            {
              int uvColor = uv.getLabel().getColor();
              uy.setLabel(new Label(colorsCounter[uvColor], uvColor));
              colorsCounter[uvColor]++;
            }
          }
        }

        EdgesRef downEdgesRef = getEdgesRef(colorsCounter);
        u.getDownEdges().setEdgesRef(downEdgesRef);
        List<Edge> sortedEdges = sortEdgesAccordingToLabels(u.getDownEdges().getEdges(), graph.getGraphColoring());
        u.getDownEdges().setEdges(sortedEdges);

        assignedVerticesIterator.remove();
      }
    }

  }

  private void singleFindPivotSquarePhase(Graph graph, FactorizationStep thisPhase, FactorizationStep nextPhase)
  {
    for (Vertex x : thisPhase.getReferenceVertices())
    {
      if (x == null)
      {
        continue;
      }
      AdjacencyVector xAdjacencyVector = new AdjacencyVector(graph.getVertices().size(), x);
      findPivotSquareForReferenceVertex(graph, x, xAdjacencyVector, thisPhase, nextPhase);
    }
  }

  private void findPivotSquareForReferenceVertex(Graph graph, Vertex x, AdjacencyVector xAdjacencyVector, FactorizationStep thisPhase, FactorizationStep nextPhase)
  {
    List<Vertex> assignedVertices = thisPhase.getAssignedVertices(x);
    Iterator<Vertex> assignedVerticesIterator = assignedVertices.iterator();
    while (assignedVerticesIterator.hasNext())
    {
      Vertex u = assignedVerticesIterator.next();
      findPivotSquare(u, xAdjacencyVector, nextPhase, graph);
      if (nextPhase != null)
      {
        findPivotSquareForReferenceVertex(graph, x, xAdjacencyVector, nextPhase, null);
      }
      assignedVerticesIterator.remove();
    }
  }

  private void findPivotSquare(Vertex u, AdjacencyVector xAdjacencyVector, FactorizationStep nextPhase, Graph graph)
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

  private void labelCrossEdges(Graph graph, int currentLayerNo)
  {
    List<Vertex> currentLayer = graph.getLayer(currentLayerNo);
    for (Vertex u : currentLayer)
    {
      List<Edge> uCrossEdges = u.getCrossEdges().getEdges();
      int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
      for (int i = 0; i < uCrossEdges.size(); i++)
      {
        Edge uv = uCrossEdges.get(i);
        Label oppositeEdgeLabel = uv.getOpposite().getLabel();
        if (oppositeEdgeLabel != null)
        {
          int oppositeEdgeColor = oppositeEdgeLabel.getColor();
          uv.setLabel(new Label(colorsCounter[oppositeEdgeColor], oppositeEdgeColor));
          colorsCounter[oppositeEdgeColor]++;
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
          colorsCounter[wxColor]++;
        }
        else
        {
          Edge uwp = u.getEdgeOfDifferentColor(uw.getLabel().getColor(), graph.getGraphColoring());
          if (uwp != null)
          {
            wxLabel = findLabelForParallelEdgeInSquare(v, uwp, graph);
            if (wxLabel != null)
            {
              int wxColor = wxLabel.getColor();
              uv.setLabel(new Label(colorsCounter[wxColor], wxColor));
              colorsCounter[wxColor]++;
            }
          }
          if (uv.getLabel() == null)
          {
            if (!u.isUnitLayer())
            {
              //graph.assignVertexToUnitLayerAndMergeColors(u, false);//not invoked
              u.setUnitLayer(true);
            }
            int uwColor = uw.getLabel().getColor();
            uv.setLabel(new Label(colorsCounter[uwColor], uwColor));
            colorsCounter[uwColor]++;
          }
        }
      }
      EdgesRef crossEdgesRef = getEdgesRef(colorsCounter);
      u.getCrossEdges().setEdgesRef(crossEdgesRef);
      List<Edge> sortedEdges = sortEdgesAccordingToLabels(u.getCrossEdges().getEdges(), graph.getGraphColoring());
      u.getCrossEdges().setEdges(sortedEdges);
      if (u.isUnitLayer())
      {
        graph.assignVertexToUnitLayerAndMergeColors(u, true);
      }
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
      for (int i = 0; i < uUpEdges.size(); i++)
      {
        Edge uv = uUpEdges.get(i);
        int oppositeEdgeColor = uv.getOpposite().getLabel().getColor();
        uv.setLabel(new Label(colorsCounter[oppositeEdgeColor], oppositeEdgeColor));
        colorsCounter[oppositeEdgeColor]++;
      }
      EdgesRef upEdgesRef = getEdgesRef(colorsCounter);
      u.getUpEdges().setEdgesRef(upEdgesRef);
      List<Edge> sortedEdges = sortEdgesAccordingToLabels(u.getUpEdges().getEdges(), graph.getGraphColoring());
      u.getUpEdges().setEdges(sortedEdges);
    }
  }

  private EdgesRef getEdgesRef(int[] colorsCounter)
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
    edgesRef.setColorsOrderAndAmount(colorsCounter);
    return edgesRef;
  }

  private List<Edge> sortEdgesAccordingToLabels(List<Edge> edgesToSort, GraphColoring graphColoring)
  {
    List<Edge> sortedEdges = new ArrayList<Edge>(edgesToSort.size());
    List<Edge>[] colorOccurrence = (List<Edge>[]) new LinkedList<?>[graphColoring.getOriginalColorsAmount()];
    for (Edge edge : edgesToSort)
    {
      Label label = edge.getLabel();
      int color = label.getColor();
      if (colorOccurrence[color] == null)
      {
        colorOccurrence[color] = new LinkedList<Edge>();
      }
      colorOccurrence[color].add(edge);
    }
    for (List<Edge> edges : colorOccurrence)
    {
      if (edges != null)
      {
        sortedEdges.addAll(edges);
      }
    }
    return sortedEdges;
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
      Edge uw = u.getEdgeOfDifferentColor(uvMappedColor, graph.getGraphColoring());//FIXME still sth wrong with mapped colors
      if (uw == null)
      {
        graph.assignVertexToUnitLayerAndMergeColors(u, true);//not invoked
        continue;
      }
      EnumSet<EdgeType> edgeTypes = EnumSet.of(EdgeType.DOWN, EdgeType.CROSS);
      for (EdgeType edgeType : edgeTypes)
      {
        if (!checkPivotSquares(uv, edgeType, graph).isEmpty() || !checkPivotSquares(uw, edgeType, graph).isEmpty())
        {
          graph.assignVertexToUnitLayerAndMergeColors(u, true);//FIXME merge cross edges //not invoked
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
      List<Edge> inconsistentEdges = checkPivotSquares(uv, EdgeType.UP, graph);
      if (uw != null)
      {
        List<Edge> uwInconsistentEdges = checkPivotSquares(uw, EdgeType.UP, graph);
        inconsistentEdges.addAll(uwInconsistentEdges);
      }
      if (!inconsistentEdges.isEmpty())
      {
        int originalColorsAmount = graph.getGraphColoring().getOriginalColorsAmount();
        boolean[] colorOccurs = new boolean[originalColorsAmount];
        List<Integer> colors = new LinkedList<Integer>();
        for (Edge inconsistentEdge : inconsistentEdges)
        {
          int color = inconsistentEdge.getLabel().getColor();
          colorOccurs[color] = true;
        }
        for (int i = 0; i < colorOccurs.length; i++)
        {
          if (colorOccurs[i])
          {
            colors.add(i);
          }
        }
        List<Edge> allUpEdgesOfGivenColors = u.getAllEdgesOfColors(colors, EdgeType.UP);
        for (Edge edgeOfGivenColor : allUpEdgesOfGivenColors)
        {
          Vertex endpointVertex = edgeOfGivenColor.getEndpoint();
          graph.assignVertexToUnitLayerAndMergeColors(endpointVertex, true);
        }
      }
    }
  }

  private List<Edge> checkPivotSquares(Edge uv, EdgeType edgeType, Graph graph)
  {
    List<Edge> inconsistentEdges = new LinkedList<Edge>();
    Vertex u = uv.getOrigin();
    Vertex v = uv.getEndpoint();
    List<Edge> uDifferentThanUv = u.getAllEdgesOfDifferentColor(uv.getLabel().getColor(), graph.getGraphColoring(), edgeType);
    for (Edge uz : uDifferentThanUv)
    {
      if (uz.getEndpoint().isUnitLayer())
      {
        continue;
      }
      Label uzLabel = uz.getLabel();
      Edge vzp = v.getEdgeByLabel(uzLabel, edgeType);
      if (vzp == null)
      {
        inconsistentEdges.add(uz);
        continue;
      }
      Vertex z = uz.getEndpoint();
      Vertex zp = vzp.getEndpoint();
      Edge zzp = graph.getEdgeForVertices(z, zp);
      if (zzp == null || !uv.getLabel().equals(zzp.getLabel()))
      {
        inconsistentEdges.add(uz);//not invoked
      }
    }
    return inconsistentEdges;
  }

}
