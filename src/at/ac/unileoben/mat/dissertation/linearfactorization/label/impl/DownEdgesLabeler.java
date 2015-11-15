package at.ac.unileoben.mat.dissertation.linearfactorization.label.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.EdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.FactorizationStepService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 2/4/14
 * Time: 8:26 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class DownEdgesLabeler implements EdgesLabeler
{
  @Autowired
  Graph graph;

  @Autowired
  EdgeService edgeService;

  @Autowired
  FactorizationStepService factorizationStepService;

  @Autowired
  ColoringService coloringService;

  @Autowired
  VertexService vertexService;

  @Autowired
  PivotSquareFinderStrategy downEdgesPivotSquareFinderStrategy;

  @Autowired
  LabelUtils labelUtils;

  @Override
  public void labelEdges(int currentLayerNo)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo);
    List<Vertex> previousLayer = vertexService.getGraphLayer(currentLayerNo - 1);
    List<Vertex> prePreviousLayer = vertexService.getGraphLayer(currentLayerNo - 2);
    FactorizationSteps factorizationSteps = new FactorizationSteps(previousLayer, prePreviousLayer);

    assignVerticesToFactorizationSteps(currentLayer, factorizationSteps);

    FactorizationStep findSquareFirstPhase = factorizationSteps.getFindSquareFirstPhase();
    FactorizationStep findSquareSecondPhase = factorizationSteps.getFindSquareSecondPhase();
    labelUtils.singleFindPivotSquarePhase(downEdgesPivotSquareFinderStrategy, findSquareFirstPhase, findSquareSecondPhase);
    labelUtils.singleFindPivotSquarePhase(downEdgesPivotSquareFinderStrategy, findSquareSecondPhase, null);

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
        factorizationStepService.initialVertexInsertForDownEdges(factorizationSteps, u, v, x);
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
    edgeService.addLabel(uv, vxColor, 0);
    colorsCounter[vxColor]++;

    u.setUnitLayer(true);
    if (!v.isUnitLayer())
    {
      vertexService.assignVertexToUnitLayerAndMergeColors(v, true, MergeTagEnum.LABEL_DOWN); //not invoked
    }

    int[] colorLengths = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    colorLengths[vxColor] = 1;
    EdgesRef downEdgesRef = new EdgesRef(1);
    coloringService.setColorAmounts(downEdgesRef, colorLengths);
    downEdgesGroup.setEdgesRef(downEdgesRef);
  }

  private void labelDownEdgesWithFoundPivotSquares(FactorizationSteps factorizationSteps)
  {
    FactorizationStep labelVerticesPhase = factorizationSteps.getLabelVerticesPhase();
    for (Vertex v : labelVerticesPhase.getVerticesInLayer())
    {
      if (v == null)
      {
        continue;
      }
      AdjacencyVector vAdjacencyVector = new AdjacencyVector(graph.getVertices().size(), v);
      List<Vertex> assignedVertices = factorizationStepService.getAssignedVertices(labelVerticesPhase, v);
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
      edgeService.addLabel(uv, colorToLabel, 0);
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
        edgeService.addLabel(uy, colorToLabel, colorsCounter[colorToLabel]++);
      }
      else
      {
        Vertex y = uy.getEndpoint();
        Edge yz = edgeService.getEdgeByLabel(y, uv.getLabel(), EdgeType.DOWN);
        if (yz != null)
        {
          Vertex z = yz.getEndpoint();
          Edge vz = vertexService.getEdgeToVertex(vAdjacencyVector, z);
          if (vz != null)
          {
            int vzColor = vz.getLabel().getColor();
            edgeService.addLabel(uy, vzColor, colorsCounter[vzColor]++);
          }
        }
        if (uy.getLabel() == null)
        {
          int uvColor = uv.getLabel().getColor();
          edgeService.addLabel(uy, uvColor, colorsCounter[uvColor]++);
        }
      }
    }

    EdgesRef downEdgesRef = labelUtils.getEdgesRef(colorsCounter);
    u.getDownEdges().setEdgesRef(downEdgesRef);
    List<Edge> sortedEdges = labelUtils.sortEdgesAccordingToLabels(u.getDownEdges().getEdges(), graph.getGraphColoring());
    u.getDownEdges().setEdges(sortedEdges);
  }
}
