package at.ac.unileoben.mat.dissertation.linearfactorization.label.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.EdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.LabelUtils;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.*;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.*;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;


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
  ReconstructionService reconstructionService;

  @Override
  public void labelEdges(int currentLayerNo)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo);
    List<Vertex> previousLayer = vertexService.getGraphLayer(currentLayerNo - 1);
    List<Vertex> prePreviousLayer = vertexService.getGraphLayer(currentLayerNo - 2);
    labelEdgesForSelectedVertices(currentLayer, previousLayer, prePreviousLayer);

  }

  public void labelEdgesForSelectedVertices(List<Vertex> selectedVertices, List<Vertex> previousLayerVertices, List<Vertex> prePreviousLayerVertices)
  {
    int currentLayerNo = selectedVertices.get(0).getBfsLayer();
    FactorizationSteps factorizationSteps = new FactorizationSteps(previousLayerVertices, prePreviousLayerVertices, vertexService.getGraphSize());

    LayerLabelingData layerLabelingData = new LayerLabelingData(previousLayerVertices);
    assignVerticesToFactorizationSteps(selectedVertices, factorizationSteps, layerLabelingData);

    FactorizationStep findSquareFirstPhase = factorizationSteps.getFindSquareFirstPhase();
    FactorizationStep findSquareSecondPhase = factorizationSteps.getFindSquareSecondPhase();


    labelUtils.singleFindPivotSquarePhase(downEdgesPivotSquareFinderStrategyImpl, findSquareFirstPhase, findSquareSecondPhase, layerLabelingData);
    labelUtils.singleFindPivotSquarePhase(downEdgesPivotSquareFinderStrategyImpl, findSquareSecondPhase, null, layerLabelingData);

    List<Vertex> noPivotSquareVerties = layerLabelingData.getNoPivotSquareVerties();
    if (CollectionUtils.isNotEmpty(noPivotSquareVerties) && reconstructionService.isReconstructionSuitableByLabeling(currentLayerNo))
    {
      noPivotSquareVerties.forEach(v -> reconstructionService.addEdgesToReconstruction(Collections.singletonList(v.getDownEdges().getEdges().get(0)), v, EdgeType.DOWN));
      reconstructionService.reconstructWithCollectedData();
      labelEdges(currentLayerNo);
    }
    else
    {
      labelDownEdgesWithFoundPivotSquares(layerLabelingData, selectedVertices);
    }
  }


  private void assignVerticesToFactorizationSteps(List<Vertex> currentLayer, FactorizationSteps factorizationSteps, LayerLabelingData layerLabelingData)
  {
    currentLayer.forEach(u -> assignSingleVertexToFactorizationSteps(u, factorizationSteps, layerLabelingData));
  }

  private void assignSingleVertexToFactorizationSteps(Vertex u, FactorizationSteps factorizationSteps, LayerLabelingData layerLabelingData)
  {
    List<Edge> uDownEdges = u.getDownEdges().getEdges();
    if (uDownEdges.size() == 1)
    {
      if (reconstructionService.isReconstructionSuitableByLabeling(u.getBfsLayer()))
      {
        reconstructionService.addEdgesToReconstruction(new ArrayList<>(uDownEdges), u, EdgeType.DOWN);
        reconstructionService.reconstructWithCollectedData();
        assignSingleVertexToFactorizationSteps(u, factorizationSteps, layerLabelingData);
      }
      else
      {
        layerLabelingData.getNoPivotSquareVerties().add(u);
      }
    }
    else
    {
      Edge uv = edgeService.getFirstEdge(u, EdgeType.DOWN);
      Edge vx = edgeService.getFirstEdge(uv.getEndpoint(), EdgeType.DOWN);
      factorizationStepService.initialVertexInsertForDownEdges(factorizationSteps, uv, vx);
    }
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
    boolean rerunLabeling = false;
    EdgeLabelingGroup[] edgeLabelingGroups = layerLabelingData.getEdgeLabelingGroups();
    for (EdgeLabelingGroup group : edgeLabelingGroups)
    {
      if (group != null)
      {
        Vertex groupVertex = group.getGroupCommonVertex();
        AdjacencyVector adjacencyVector = new AdjacencyVector(labelUtils.calculateAdjacencyVector(graph.getVertices().size(), groupVertex));
        List<EdgeLabelingSubgroup> edgeLabelingSubgroups = group.getEdgeLabelingSubgroups();
        for (EdgeLabelingSubgroup subgroup : edgeLabelingSubgroups)
        {
          List<EdgeLabelingWrapper> notLabeledEdges = labelDownEdgesForGivenLabelingBaseEdge(subgroup.getOtherEdges(), subgroup.getFirstLabelingBaseEdge(), adjacencyVector);
          if (CollectionUtils.isNotEmpty(notLabeledEdges))
          {
            if (subgroup.getSecondLabelingBaseEdge() != null)
            {
              EdgeLabelingSubgroup edgeLabelingSubgroup = new EdgeLabelingSubgroup(subgroup.getSecondLabelingBaseEdge(), null, notLabeledEdges);
              edgeLabelingService.addEdgeLabelingSubgroup(edgeLabelingSubgroup, layerLabelingData);
            }
            else
            {
              rerunLabeling = true;
              for (EdgeLabelingWrapper notLabeledEdge : notLabeledEdges)
              {
                if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION && notLabeledEdge.getPotentialEdgeReconstruction().isPresent())
                {
                  EdgeLabelingReconstruction edgeLabelingReconstruction = notLabeledEdge.getPotentialEdgeReconstruction().get();
                  reconstructionService.addEdgesToReconstruction(Collections.singletonList(edgeLabelingReconstruction.getEdge()), edgeLabelingReconstruction.getVertex(), EdgeType.DOWN);
                }
                else
                {
                  Vertex v = subgroup.getFirstLabelingBaseEdge().getOrigin();
                  layerLabelingData.getNoPivotSquareVerties().add(v);
                }
              }
            }
          }
        }
      }
    }
    if (rerunLabeling)
    {
      if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION)
      {
        reconstructionService.reconstructWithCollectedData();
      }
      labelDownEdgesWithFoundPivotSquares(layerLabelingData, currentLayer);
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
    labelUtils.setVertexAsUnitLayer(v, edgeWithColorToLabel.getLabel().getColor(), EdgeType.DOWN);
  }

  private List<EdgeLabelingWrapper> labelDownEdgesForGivenLabelingBaseEdge(List<EdgeLabelingWrapper> wrappedEdges, Edge uv, AdjacencyVector adjacencyVector)
  {
    List<EdgeLabelingWrapper> notLabeledEdges = new LinkedList<>();

    for (EdgeLabelingWrapper wrappedEdge : wrappedEdges)
    {
      Edge uy = wrappedEdge.getEdge();
      if (uy.getLabel() != null)
      {
        continue;
      }
      boolean noAdjacencyVectorMatchForNewVertex = false;
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
        else if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION && reconstructionData.getNewVertex() == z)
        {
          noAdjacencyVectorMatchForNewVertex = true;
        }
      }
      if (uy.getLabel() == null)
      {
        if (noAdjacencyVectorMatchForNewVertex)
        {
          EdgeLabelingReconstruction edgeLabelingReconstruction = new EdgeLabelingReconstruction(uy, uv.getEndpoint());
          EdgeLabelingWrapper edgeLabelingWrapper = new EdgeLabelingWrapper(uy, Optional.of(edgeLabelingReconstruction));
          notLabeledEdges.add(edgeLabelingWrapper);
        }
        else
        {
          notLabeledEdges.add(wrappedEdge);
        }
      }
    }
    return notLabeledEdges;
  }
}
