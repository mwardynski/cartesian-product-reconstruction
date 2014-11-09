package at.ac.unileoben.mat.dissertation.linearfactorization.label.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.EdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.impl.DownEdgesPivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;

import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 2/4/14
 * Time: 8:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class DownEdgesLabeler implements EdgesLabeler
{
  private Graph graph;

  public DownEdgesLabeler(Graph graph)
  {
    this.graph = graph;
  }

  @Override
  public void labelEdges(int currentLayerNo)
  {
    List<Vertex> currentLayer = graph.getLayer(currentLayerNo);
    List<Vertex> previousLayer = graph.getLayer(currentLayerNo - 1);
    List<Vertex> prePreviousLayer = graph.getLayer(currentLayerNo - 2);
    FactorizationSteps factorizationSteps = new FactorizationSteps(previousLayer, prePreviousLayer);

    assignVerticesToFactorizationSteps(currentLayer, factorizationSteps);

    PivotSquareFinderStrategy pivotSquareFinderStrategy = new DownEdgesPivotSquareFinderStrategy();
    FactorizationStep findSquareFirstPhase = factorizationSteps.getFindSquareFirstPhase();
    FactorizationStep findSquareSecondPhase = factorizationSteps.getFindSquareSecondPhase();
    LabelUtils.singleFindPivotSquarePhase(graph, pivotSquareFinderStrategy, findSquareFirstPhase, findSquareSecondPhase);
    LabelUtils.singleFindPivotSquarePhase(graph, pivotSquareFinderStrategy, findSquareSecondPhase, null);

    labelDownEdgesWithFoundPivotSquares(factorizationSteps);
  }

  private void assignVerticesToFactorizationSteps(List<Vertex> currentLayer, FactorizationSteps factorizationSteps)
  {
    for (Vertex u : currentLayer)
    {
      List<Edge> uDownEdges = u.getDownEdges().getEdges();
      if (uDownEdges.size() == 1)
      {
        setVertexAsUnitLayer(u);
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
        factorizationSteps.initialVertexInsertForDownEdges(u, v, x);
      }
    }
  }

  private void setVertexAsUnitLayer(Vertex u)
  {
    EdgesGroup downEdgesGroup = u.getDownEdges();
    List<Edge> uDownEdges = downEdgesGroup.getEdges();
    int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    Edge uv = uDownEdges.iterator().next();
    Vertex v = uv.getEndpoint();
    Edge vx = v.getDownEdges().getEdges().iterator().next();
    int vxColor = vx.getLabel().getColor();
    uv.setLabel(new Label(0, vxColor));
    colorsCounter[vxColor]++;

    u.setUnitLayer(true);
    if (!v.isUnitLayer())
    {
      graph.assignVertexToUnitLayerAndMergeColors(v, true, MergeTagEnum.LABEL_DOWN); //not invoked
    }

    int[] colorLengths = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    colorLengths[vxColor] = 1;
    EdgesRef downEdgesRef = new EdgesRef(1);
    downEdgesRef.setColorAmounts(colorLengths);
    downEdgesGroup.setEdgesRef(downEdgesRef);
  }

  private void labelDownEdgesWithFoundPivotSquares(FactorizationSteps factorizationSteps)
  {
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
        Vertex u = assignedVerticesIterator.next();
        labelDownEdgesOfGivenVertex(u, vAdjacencyVector);
        assignedVerticesIterator.remove();
      }
    }
  }

  private void labelDownEdgesOfGivenVertex(Vertex u, AdjacencyVector vAdjacencyVector)
  {
    int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
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

    EdgesRef downEdgesRef = LabelUtils.getEdgesRef(colorsCounter);
    u.getDownEdges().setEdgesRef(downEdgesRef);
    List<Edge> sortedEdges = LabelUtils.sortEdgesAccordingToLabels(u.getDownEdges().getEdges(), graph.getGraphColoring());
    u.getDownEdges().setEdges(sortedEdges);
  }
}
