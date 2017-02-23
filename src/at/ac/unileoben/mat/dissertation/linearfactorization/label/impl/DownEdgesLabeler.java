package at.ac.unileoben.mat.dissertation.linearfactorization.label.impl;

import at.ac.unileoben.mat.dissertation.common.ReconstructionHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.EdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.LayerLabelingData;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.*;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.LayerLabelingData.EdgeLabelingGroup;
import static at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.LayerLabelingData.EdgeLabelingGroup.EdgeLabelingSubgroup;

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
  ReconstructionData reconstructionData;

  @Autowired
  EdgeService edgeService;

  @Autowired
  FactorizationStepService factorizationStepService;

  @Autowired
  ColoringService coloringService;

  @Autowired
  VertexService vertexService;

  @Autowired
  PivotSquareFinderStrategy downEdgesPivotSquareFinderStrategyImpl;

  @Autowired
  LabelUtils labelUtils;

  @Autowired
  EdgeLabelingService edgeLabelingService;

  @Autowired
  ReconstructionHelper reconstructionHelper;

  @Override
  public void labelEdges(int currentLayerNo)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo);
    List<Vertex> previousLayer = vertexService.getGraphLayer(currentLayerNo - 1);
    List<Vertex> prePreviousLayer = vertexService.getGraphLayer(currentLayerNo - 2);
    FactorizationSteps factorizationSteps = new FactorizationSteps(previousLayer, prePreviousLayer, vertexService.getGraphSize());

    assignVerticesToFactorizationSteps(currentLayer, factorizationSteps);

    FactorizationStep findSquareFirstPhase = factorizationSteps.getFindSquareFirstPhase();
    FactorizationStep findSquareSecondPhase = factorizationSteps.getFindSquareSecondPhase();

    LayerLabelingData layerLabelingData = new LayerLabelingData(previousLayer);

    labelUtils.singleFindPivotSquarePhase(downEdgesPivotSquareFinderStrategyImpl, findSquareFirstPhase, findSquareSecondPhase, layerLabelingData);
    labelUtils.singleFindPivotSquarePhase(downEdgesPivotSquareFinderStrategyImpl, findSquareSecondPhase, null, layerLabelingData);

    List<Vertex> noPivotSquareVerties = layerLabelingData.getNoPivotSquareVerties();
    if (CollectionUtils.isNotEmpty(noPivotSquareVerties) && reconstructionHelper.isReconstructionSuitableByLabeling(currentLayerNo))
    {
      noPivotSquareVerties.forEach(v -> reconstructionHelper.addEdgesToReconstruction(Collections.singletonList(v.getDownEdges().getEdges().get(0)), v, EdgeType.DOWN));
      reconstructionHelper.reconstructWithCollectedData();
      labelEdges(currentLayerNo);

    }
    else
    {
      labelDownEdgesWithFoundPivotSquares(layerLabelingData, currentLayer);
    }

  }

  private void assignVerticesToFactorizationSteps(List<Vertex> currentLayer, FactorizationSteps factorizationSteps)
  {
    currentLayer.forEach(u -> assignSingleVertexToFactorizationSteps(u, factorizationSteps));
  }

  private void assignSingleVertexToFactorizationSteps(Vertex u, FactorizationSteps factorizationSteps)
  {
    List<Edge> uDownEdges = u.getDownEdges().getEdges();
    if (uDownEdges.size() == 1)
    {
      if (reconstructionHelper.isReconstructionSuitableByLabeling(u.getBfsLayer()))
      {
        reconstructionHelper.addEdgesToReconstruction(new ArrayList<>(uDownEdges), u, EdgeType.DOWN);
        reconstructionHelper.reconstructWithCollectedData();
        assignSingleVertexToFactorizationSteps(u, factorizationSteps);
      }
      else
      {
        int colorToLabel = u.getDownEdges().getEdges().get(0).getEndpoint().getDownEdges().getEdges().get(0).getLabel().getColor();
        setVertexAsUnitLayer(u, colorToLabel);
      }
    }
    else
    {
      Edge uv = edgeService.getFirstEdge(u, EdgeType.DOWN);
      Edge vx = edgeService.getFirstEdge(uv.getEndpoint(), EdgeType.DOWN);
      factorizationStepService.initialVertexInsertForDownEdges(factorizationSteps, uv, vx);
    }
  }

  private void setVertexAsUnitLayer(Vertex u, int colorToLabel)
  {
    EdgesGroup downEdgesGroup = u.getDownEdges();
    List<Edge> uDownEdges = downEdgesGroup.getEdges();
    int nameCounter = 0;
    for (Edge e : uDownEdges)
    {
      edgeService.addLabel(e, colorToLabel, nameCounter++, null, new LabelOperationDetail.Builder(LabelOperationEnum.UNIT_LAYER_FOLLOWING).build());
    }

    Vertex v = downEdgesGroup.getEdges().iterator().next().getEndpoint();
    u.setUnitLayer(true);
    if (!v.isUnitLayer())
    {
      vertexService.assignVertexToUnitLayerAndMergeColors(v, true, MergeTagEnum.LABEL_DOWN);
    }

    int[] colorLengths = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    colorLengths[colorToLabel] = uDownEdges.size();
    EdgesRef downEdgesRef = new EdgesRef(1);
    coloringService.setColorAmounts(downEdgesRef, colorLengths);
    downEdgesGroup.setEdgesRef(downEdgesRef);
  }

  private void labelDownEdgesWithFoundPivotSquares(LayerLabelingData layerLabelingData, List<Vertex> currentLayer)
  {
    List<Vertex> noPivotSquareVerties = layerLabelingData.getNoPivotSquareVerties();
    if (CollectionUtils.isNotEmpty(noPivotSquareVerties))
    {
      for (Vertex v : noPivotSquareVerties)
      {
        labelUnitLayerVertex(v);

      }
    }
    EdgeLabelingGroup[] edgeLabelingGroups = layerLabelingData.getEdgeLabelingGroups();
    for (EdgeLabelingGroup group : edgeLabelingGroups)
    {
      if (group != null)
      {
        Vertex groupVertex = group.getGroupCommonVertex();
        AdjacencyVector adjacencyVector = new AdjacencyVector(graph.getVertices().size(), groupVertex);
        List<EdgeLabelingSubgroup> edgeLabelingSubgroups = group.getEdgeLabelingSubgroups();
        for (EdgeLabelingSubgroup subgroup : edgeLabelingSubgroups)
        {
          List<Edge> notLabeledEdges = labelDownEdgesForGivenLabelingBaseEdge(subgroup.getOtherEdges(), subgroup.getFirstLabelingBaseEdge(), adjacencyVector);
          if (CollectionUtils.isNotEmpty(notLabeledEdges))
          {
            if (subgroup.getSecondLabelingBaseEdge() != null)
            {
              EdgeLabelingSubgroup edgeLabelingSubgroup = new EdgeLabelingSubgroup(subgroup.getSecondLabelingBaseEdge(), null, notLabeledEdges);
              edgeLabelingService.addEdgeLabelingSubgroup(edgeLabelingSubgroup, layerLabelingData);
            }
            else
            {
              Vertex v = subgroup.getFirstLabelingBaseEdge().getOrigin();
              labelUnitLayerVertex(v);
            }
          }
        }
      }
    }
    for (Vertex v : currentLayer)
    {
      labelUtils.sortEdgesAccordingToLabels(v.getDownEdges(), graph.getGraphColoring());
    }
  }

  private void labelUnitLayerVertex(Vertex v)
  {
    Edge firstEdge = edgeService.getFirstEdge(v, EdgeType.DOWN);
    Edge edgeWithColorToLabel = edgeService.getFirstEdge(firstEdge.getEndpoint(), EdgeType.DOWN);
    setVertexAsUnitLayer(v, edgeWithColorToLabel.getLabel().getColor());
  }

  private List<Edge> labelDownEdgesForGivenLabelingBaseEdge(List<Edge> edges, Edge uv, AdjacencyVector adjacencyVector)
  {
    List<Edge> notLabeledEdges = new LinkedList<>();

    for (Edge uy : edges)
    {
      Vertex y = uy.getEndpoint();
      Edge yz = edgeService.getEdgeByLabel(y, uv.getLabel(), EdgeType.DOWN);
      if (yz != null)
      {
        Vertex z = yz.getEndpoint();
        Edge vz = vertexService.getEdgeToVertex(adjacencyVector, z);
        if (vz != null)
        {
          Label vzLabel = vz.getLabel();
          int vzMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), vz.getLabel().getColor());
          int yzMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), yz.getLabel().getColor());
          if (vzMappedColor != yzMappedColor)
          {
            edgeService.addLabel(uy, vzLabel.getColor(), vzLabel.getName(), vz, new LabelOperationDetail.Builder(LabelOperationEnum.PIVOT_SQUARE_FOLLOWING).sameColorEdge(vz).pivotSquareFirstEdge(uv).pivotSquareFirstEdgeCounterpart(yz).build());
          }
        }
      }
      if (uy.getLabel() == null)
      {
        notLabeledEdges.add(uy);
      }
    }
    return notLabeledEdges;
  }
}
