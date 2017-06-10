package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.LayerLabelingData;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.FactorizationStepService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
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
  ReconstructionService reconstructionService;

  @Autowired
  EdgeService edgeService;

  @Autowired
  VertexService vertexService;

  @Autowired
  LabelUtils labelUtils;

  @Autowired
  FactorizationStepService factorizationStepService;

  @Autowired
  ColoringService coloringService;

  @Autowired
  ReconstructionData reconstructionData;

  @Override
  public void findPivotSquare(Vertex u, AdjacencyVector wAdjacencyVector, FactorizationStep thisPhase, FactorizationStep nextPhase, LayerLabelingData layerLabelingData)
  {

    boolean allEdgesFactorized = factorizeCrossEdgesOfSingleVertex(u, wAdjacencyVector, thisPhase);
    finalizeCrossEdgesFactorization(u, thisPhase, nextPhase, allEdgesFactorized);
  }

  private boolean factorizeCrossEdgesOfSingleVertex(Vertex u, AdjacencyVector wAdjacencyVector, FactorizationStep thisPhase)
  {
    EdgesGroup crossEdgesGroup = u.getCrossEdges();
    List<Edge> uCrossEdges = crossEdgesGroup.getEdges();
    boolean allEdgesFactorized = true;
    boolean someEdgesReconstructed = false;
    for (Edge uv : uCrossEdges)
    {
      if (uv.getLabel() != null)
      {
        continue;
      }
      Label oppositeEdgeLabel = uv.getOpposite().getLabel();
      if (oppositeEdgeLabel != null)
      {
        Edge oppositeSquareMatchingEdge = uv.getOpposite().getSquareMatchingEdge();
        if (oppositeSquareMatchingEdge != null)
        {
          Edge squareMatchingEdge = oppositeSquareMatchingEdge.getOpposite();
          Label squareMatchingEdgeLabel = squareMatchingEdge.getLabel();
          edgeService.addLabel(uv, squareMatchingEdgeLabel.getColor(), squareMatchingEdgeLabel.getName(), squareMatchingEdge, new LabelOperationDetail.Builder(LabelOperationEnum.OPPOSITE).build());
        }
        else
        {
          int oppositeEdgeColor = oppositeEdgeLabel.getColor();
          edgeService.addLabel(uv, oppositeEdgeColor, -1, null, new LabelOperationDetail.Builder(LabelOperationEnum.OPPOSITE).build());
        }

        continue;
      }

      Vertex v = uv.getEndpoint();
      Edge uw = factorizationStepService.getFirstLayerEdgeForVertexInFactorizationStep(thisPhase, u);
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
        edgeService.addLabel(uv, wxColor, -1, wx, new LabelOperationDetail.Builder(LabelOperationEnum.PIVOT_SQUARE_FOLLOWING).sameColorEdge(wx).pivotSquareFirstEdge(uw).pivotSquareFirstEdgeCounterpart(vx).build());
      }
      else if (vx != null && isEdgeToReconstruct(vx, u.getBfsLayer()))
      {
        Vertex w = uw.getEndpoint();
        reconstructionService.addEdgesToReconstruction(Arrays.asList(uv), w, EdgeType.CROSS);
        someEdgesReconstructed = true;
      }
      else
      {
        allEdgesFactorized = false;
      }
    }
    if (someEdgesReconstructed)
    {
      reconstructionService.reconstructWithCollectedData();
      Vertex w = factorizationStepService.getFirstLayerEdgeForVertexInFactorizationStep(thisPhase, u).getEndpoint();
      wAdjacencyVector.setVector(labelUtils.calculateAdjacencyVector(graph.getVertices().size(), w));

      Edge colorEdge = w.getDownEdges().getEdges().get(0);
      labelUnitLayerVertex(w, colorEdge);
      labelUnitLayerVertex(reconstructionData.getNewVertex(), colorEdge);

      Vertex newVertex = reconstructionData.getNewVertex();
      Edge newVertexDownEdge = newVertex.getDownEdges().getEdges().get(0);
      coloringService.mergeColorsForEdges(Arrays.asList(colorEdge, newVertexDownEdge), MergeTagEnum.LABEL_CROSS);


      factorizeCrossEdgesOfSingleVertex(u, wAdjacencyVector, thisPhase);
    }

    return allEdgesFactorized;
  }

  private void finalizeCrossEdgesFactorization(Vertex u, FactorizationStep thisPhase, FactorizationStep nextPhase, boolean allEdgesFactorized)
  {
    if (!allEdgesFactorized)
    {
      Edge uwp = findFirstLayerEdgeOfDifferentColor(u, thisPhase);
      if (nextPhase != null && uwp != null && !u.isUnitLayer())
      {
        addNextPhaseForVertex(u, nextPhase, uwp);
      }
      else
      {
        Edge uw = factorizationStepService.getFirstLayerEdgeForVertexInFactorizationStep(thisPhase, u);
        labelUnitLayerVertex(u, uw);
      }
    }

    if (allEdgesFactorized)
    {
      EdgesGroup crossEdgesGroup = u.getCrossEdges();
      labelUtils.sortEdgesAccordingToLabels(crossEdgesGroup, graph.getGraphColoring());
    }
  }

  private Edge findFirstLayerEdgeOfDifferentColor(Vertex u, FactorizationStep thisPhase)
  {
    Edge uw = factorizationStepService.getFirstLayerEdgeForVertexInFactorizationStep(thisPhase, u);
    int uwColor = uw.getLabel().getColor();
    int uwMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), uwColor);
    return edgeService.getEdgeOfDifferentColor(u, uwMappedColor, graph.getGraphColoring());
  }

  private void addNextPhaseForVertex(Vertex u, FactorizationStep nextPhase, Edge uwp)
  {
    Vertex wp = uwp.getEndpoint();
    factorizationStepService.assignFirstLayerEdgeForVertexInFactorizationStep(nextPhase, u, uwp);
    factorizationStepService.addVertex(nextPhase, wp, u);
  }

  private void labelUnitLayerVertex(Vertex u, Edge colorEdge)
  {
    int color = colorEdge.getLabel().getColor();
    labelUtils.setVertexAsUnitLayer(u, color, EdgeType.CROSS);
  }

  private boolean isEdgeToReconstruct(Edge edge, int currentBfsLayer)
  {
    return reconstructionService.isNewVertex(edge.getEndpoint())
            && reconstructionService.isReconstructionSuitableByLabeling(currentBfsLayer);
  }
}
