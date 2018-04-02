package at.ac.unileoben.mat.dissertation.linearfactorization.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.InPlaceReconstructionSetUpService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionShiftLayerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 2/4/14
 * Time: 8:18 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class ConsistencyCheckerImpl implements ConsistencyChecker
{
  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  ReconstructionService reconstructionService;

  @Autowired
  EdgeService edgeService;

  @Autowired
  ColoringService coloringService;

  @Autowired
  VertexService vertexService;

  @Autowired
  InPlaceReconstructionSetUpService inPlaceReconstructionSetUpService;

  @Autowired
  ReconstructionShiftLayerService reconstructionShiftLayerService;

  @Override
  public void checkConsistency(int currentLayerNo)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo);
    List<Vertex> previousLayer = vertexService.getGraphLayer(currentLayerNo - 1);
    if (graph.getGraphColoring().getActualColors().size() != 1)
    {
      checkPreviousLayerUpEdgesConsistency(previousLayer);
      boolean amountConsistencyCheck = reconstructionData.getOperationOnGraph() != OperationOnGraph.RECONSTRUCT
              && reconstructionData.getOperationOnGraph() != OperationOnGraph.IN_PLACE_RECONSTRUCTION;

      checkCurrentLayerAllEdgesConsistency(currentLayer, amountConsistencyCheck);
    }
    if (inPlaceReconstructionSetUpService.isInPlaceReconstructionToBeStarted())
    {
      inPlaceReconstructionSetUpService.setUpReconstructionInPlace();
      if (!reconstructionData.getLayerNoToRefactorizeFromOptional().isPresent())
      {
        checkConsistency(currentLayerNo);
      }
    }
    else if (reconstructionShiftLayerService.isVertexToShiftAvailable())
    {
      reconstructionShiftLayerService.shiftVertex();
    }
    else if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION)
    {
      if (reconstructionService.isTopVertexMissingByReconstruction(currentLayerNo))
      {
        reconstructionService.prepareTopVertexReconstruction(currentLayer);
      }
      reconstructionService.reconstructWithCollectedData();
    }
  }

  @Override
  public boolean checkConsistencyDuringReconstruction(int currentLayerNo)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo);
    List<Vertex> previousLayer = vertexService.getGraphLayer(currentLayerNo - 1);
    if (graph.getGraphColoring().getActualColors().size() != 1)
    {
      checkPreviousLayerUpEdgesConsistency(previousLayer);
      boolean amountConsistencyCheck = currentLayerNo > reconstructionData.getNewVertex().getBfsLayer();
      checkCurrentLayerAllEdgesConsistency(currentLayer, amountConsistencyCheck);
    }
    return graph.getGraphColoring().getActualColors().size() != 1;
  }

  private void checkCurrentLayerAllEdgesConsistency(List<Vertex> layer, boolean amountConsistencyCheck)
  {
    if (reconstructionData.getLayerNoToRefactorizeFromOptional().isPresent())
    {
      return;
    }
    for (Vertex u : layer)
    {
      if (u.isUnitLayer())
      {
        continue;
      }
      Edge uv = u.getDownEdges().getEdges().get(0);
      int uvMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), uv.getLabel().getColor());
      Edge uw = edgeService.getEdgeOfDifferentColor(u, uvMappedColor, graph.getGraphColoring());
      if (uw == null)
      {
        vertexService.assignVertexToUnitLayerAndMergeColors(u, MergeTagEnum.CONSISTENCY_DOWN);//not invoked
        continue;
      }
      EnumSet<EdgeType> edgeTypes = EnumSet.of(EdgeType.DOWN, EdgeType.CROSS);
      for (EdgeType edgeType : edgeTypes)
      {
        if (!checkPivotSquares(uv, edgeType).isEmpty() || !checkPivotSquares(uw, edgeType).isEmpty())
        {
          MergeTagEnum mergeTagEnum = edgeType == EdgeType.DOWN ? MergeTagEnum.CONSISTENCY_DOWN : MergeTagEnum.CONSISTENCY_CROSS;
          vertexService.assignVertexToUnitLayerAndMergeColors(u, mergeTagEnum);
          break;
        }
      }
      if (amountConsistencyCheck)
      {
        amountConsistencyCheck(layer, u, uv, uw);
      }
    }
  }

  private void amountConsistencyCheck(List<Vertex> layer, Vertex u, Edge uv, Edge uw)
  {
    storeCurrentLayerUnitLayerVerticesAmountBeforeAmountCheck(layer);
    int upEdgesAmountDifference = calculateUpEdgesAmountDifference(u, uv, uw);
    if (upEdgesAmountDifference != 0)
    {
      MergeTagEnum mergeTag = upEdgesAmountDifference < 0 ? MergeTagEnum.CONSISTENCY_UP_AMOUNT_BELOW : MergeTagEnum.CONSISTENCY_UP_AMOUNT_ABOVE;
      if (inPlaceReconstructionSetUpService.checkCurrentLayerUnitLayerVerticesValidity())
      {
        IntStream.range(0, Math.abs(upEdgesAmountDifference))
                .forEach(i -> reconstructionData.getMissingInFirstLayerReconstructionData().getAmountMergeTags().add(mergeTag));

      }
      else
      {
        vertexService.assignVertexToUnitLayerAndMergeColors(u, mergeTag);
      }
    }
  }

  private void storeCurrentLayerUnitLayerVerticesAmountBeforeAmountCheck(List<Vertex> layer)
  {
    int currentLayerUnitLayerVerticesAmount = layer.stream()
            .mapToInt(v -> v.isUnitLayer() ? 1 : 0)
            .sum();
    reconstructionData.getMissingInFirstLayerReconstructionData().setCurrentLayerUnitLayerVerticesAmountBeforeAmountCheck(currentLayerUnitLayerVerticesAmount);
  }


  private int calculateUpEdgesAmountDifference(Vertex u, Edge uv, Edge uw)
  {
    Vertex v = uv.getEndpoint();
    List<List<Edge>> vDifferentThanUv = edgeService.getAllEdgesOfDifferentColor(v, uv.getLabel().getColor(), graph.getGraphColoring(), EdgeType.UP);
    Vertex w = uw.getEndpoint();
    List<List<Edge>> wDifferentThanUw = edgeService.getAllEdgesOfDifferentColor(w, uw.getLabel().getColor(), graph.getGraphColoring(), EdgeType.UP);
    int upEdgesTotalSize = 0;
    for (int i = 0; i < vDifferentThanUv.size(); i++)
    {
      int upEdgesSize = vDifferentThanUv.get(i).size();
      if (upEdgesSize == 0)
      {
        upEdgesSize = wDifferentThanUw.get(i).size();
      }
      upEdgesTotalSize += upEdgesSize;
    }
    return u.getUpEdges().getEdges().size() - upEdgesTotalSize;
  }

  private void checkPreviousLayerUpEdgesConsistency(List<Vertex> layer)
  {
    for (Vertex u : layer)
    {
      Edge uv = u.getDownEdges().getEdges().get(0);
      Edge uw = null;
      if (!u.isUnitLayer())
      {
        int uvMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), uv.getLabel().getColor());
        uw = edgeService.getEdgeOfDifferentColor(u, uvMappedColor, graph.getGraphColoring());
      }
      List<InconsistentEdge> uvInconsistentEdges = checkPivotSquares(uv, EdgeType.UP);
      List<InconsistentEdge> uwInconsistentEdges = new LinkedList<>();
      if (uw != null)
      {
        uwInconsistentEdges = checkPivotSquares(uw, EdgeType.UP);
      }

      Optional<Vertex> correspondingVertexOptional = inPlaceReconstructionSetUpService.findCorrespondingVertexToMissingVertexToBeCreatedLater(uvInconsistentEdges, uwInconsistentEdges);
      if (correspondingVertexOptional.isPresent())
      {
        inPlaceReconstructionSetUpService.reconstructMissingVertexToBeCreatedLater(correspondingVertexOptional.get());
        break;
      }
      else
      {
        if (CollectionUtils.isNotEmpty(uvInconsistentEdges))
        {
          handleInconsistentUpEdges(uv, uvInconsistentEdges);
        }
        if (CollectionUtils.isNotEmpty(uwInconsistentEdges))
        {
          handleInconsistentUpEdges(uw, uwInconsistentEdges);
        }
      }
    }
  }

  private void handleInconsistentUpEdges(Edge uv, List<InconsistentEdge> inconsistentEdges)
  {
    boolean allEdgesInconsistentBeacauseOfLabel = inconsistentEdges.stream()
            .map(inconsistentEdge -> inconsistentEdge.getInconsistentEdgeTag())
            .allMatch(inconsistentEdgeTag -> inconsistentEdgeTag == InconsistentEdgeTag.NO_EDGE_FOR_LABEL);
    List<Edge> edgesToRelabel = inconsistentEdges.stream().map(inconsistentEdge -> inconsistentEdge.getEdge()).collect(Collectors.toList());
    edgesToRelabel.add(uv);
    List<Integer> colors = coloringService.getColorsForEdges(graph.getGraphColoring(), edgesToRelabel);
    MergeTagEnum mergeTag = allEdgesInconsistentBeacauseOfLabel ? MergeTagEnum.CONSISTENCY_UP_LABELS : MergeTagEnum.CONSISTENCY_UP;
    coloringService.mergeColorsForEdges(edgesToRelabel, mergeTag);

    Vertex u = uv.getOrigin();
    List<Edge> allUpEdgesOfGivenColors = edgeService.getAllEdgesOfColors(u, colors, EdgeType.UP);
    for (Edge edgeOfGivenColor : allUpEdgesOfGivenColors)
    {
      Vertex endpointVertex = edgeOfGivenColor.getEndpoint();
      vertexService.assignVertexToUnitLayerAndMergeColors(endpointVertex, mergeTag);
    }
  }

  private List<InconsistentEdge> checkPivotSquares(Edge uv, EdgeType edgeType)
  {
    List<InconsistentEdge> inconsistentEdges = new LinkedList<>();
    Vertex u = uv.getOrigin();
    Vertex v = uv.getEndpoint();
    List<List<Edge>> uDifferentThanUv = edgeService.getAllEdgesOfDifferentColor(u, uv.getLabel().getColor(), graph.getGraphColoring(), edgeType);
    List<List<Edge>> vDifferentThanUv = edgeService.getAllEdgesOfDifferentColor(v, uv.getLabel().getColor(), graph.getGraphColoring(), edgeType);
    if (edgeType != EdgeType.UP
            || (edgeType == EdgeType.UP && reconstructionService.isCorrespondingEdgesCheckForUpEdgesReasonable()))
    {
      List<Edge> notCorrespondingEdges = getNotCorrespondingEdgesRegardingColor(uDifferentThanUv, vDifferentThanUv);
      notCorrespondingEdges.stream().forEach(edge -> inconsistentEdges.add(new InconsistentEdge(edge, InconsistentEdgeTag.NOT_CORRESPONDING_EDGE)));
    }
    for (List<Edge> uzForColor : uDifferentThanUv)
    {
      for (Edge uz : uzForColor)
      {
        Label uzLabel = uz.getLabel();
        Edge vzp = edgeService.getEdgeByLabel(v, uzLabel, edgeType);
        if (vzp == null)
        {
          inconsistentEdges.add(new InconsistentEdge(uz, InconsistentEdgeTag.NO_EDGE_FOR_LABEL));
          continue;
        }
        Vertex z = uz.getEndpoint();
        Vertex zp = vzp.getEndpoint();
        Edge zzp = edgeService.getEdgeByLabel(z, uv.getLabel(), EdgeType.DOWN);
        if (zzp == null || !zzp.getEndpoint().equals(zp))
        {
          inconsistentEdges.add(new InconsistentEdge(uz/*uv???*/, InconsistentEdgeTag.PARALLEL_EDGE_PROBLEM));
        }
      }
    }
    if (reconstructionService.isReconstructionSuitableByConsistencyCheck()
            && !inconsistentEdges.isEmpty())
    {
      List<InconsistentEdge> edgesToAssign = inconsistentEdges;
      List<InconsistentEdge> edgesToRefactor = new LinkedList<>();
      List<InconsistentEdge> edgesToReconstruct = new LinkedList<>();
      assignInconsistentEdgesToRefactorOrReconstruction(edgesToAssign, edgesToRefactor, edgesToReconstruct);

      if (edgeType == EdgeType.UP && edgesToReconstruct.size() > 1)
      {
        boolean[] uMappedColors = new boolean[graph.getGraphColoring().getOriginalColorsAmount()];
        /*NOT NEEDE BEGIN*/
        for (List<Edge> uEdges : uDifferentThanUv)
        {
          if (CollectionUtils.isNotEmpty(uEdges))
          {
            int edgeColorMapping = coloringService.getCurrentColorMapping(graph.getGraphColoring(), uEdges.get(0).getLabel().getColor());
            uMappedColors[edgeColorMapping] = true;
          }
        }
        /*NOT NEEDE END*/
        collectNotCorrespondingColors(uDifferentThanUv, uMappedColors);

        for (Iterator<InconsistentEdge> edgesToReconstructIt = edgesToReconstruct.iterator(); edgesToReconstructIt.hasNext(); )
        {
          InconsistentEdge inconsistentEdge = edgesToReconstructIt.next();
          int edgeColorMapping = coloringService.getCurrentColorMapping(graph.getGraphColoring(), inconsistentEdge.getEdge().getLabel().getColor());
          if (!uMappedColors[edgeColorMapping])
          {
            edgesToRefactor.add(inconsistentEdge);
            edgesToReconstructIt.remove();
          }
        }
      }
      if (CollectionUtils.isNotEmpty(edgesToReconstruct))
      {
        List<Edge> edgesToReconstructSimpleList = edgesToReconstruct.stream().map(InconsistentEdge::getEdge).collect(Collectors.toList());
        reconstructionService.addEdgesToReconstruction(edgesToReconstructSimpleList, uv.getOrigin(), edgeType);
      }
      return edgesToRefactor;
    }
    else
    {
      return inconsistentEdges;
    }
  }

  private void assignInconsistentEdgesToRefactorOrReconstruction(List<InconsistentEdge> edgesToAssign, List<InconsistentEdge> edgesToRefactor, List<InconsistentEdge> edgesToReconstruct)
  {
    for (Iterator<InconsistentEdge> edgesToAssignIt = edgesToAssign.iterator(); edgesToAssignIt.hasNext(); )
    {
      InconsistentEdge inconsistentEdge = edgesToAssignIt.next();
      if (inconsistentEdge.getInconsistentEdgeTag() == InconsistentEdgeTag.NO_EDGE_FOR_LABEL)
      {
        edgesToRefactor.add(inconsistentEdge);
      }
      else
      {
        edgesToReconstruct.add(inconsistentEdge);
      }
      edgesToAssignIt.remove();
    }
  }

  private boolean collectNotCorrespondingColors(List<List<Edge>> groupedByColorEdges, boolean[] existingMappedColors)
  {
    boolean existingMappedColorsNotEmpty = false;
    for (List<Edge> sameColorEdges : groupedByColorEdges)
    {
      if (CollectionUtils.isNotEmpty(sameColorEdges))
      {
        int edgeColorMapping = coloringService.getCurrentColorMapping(graph.getGraphColoring(), sameColorEdges.get(0).getLabel().getColor());
        existingMappedColors[edgeColorMapping] = true;
        existingMappedColorsNotEmpty = true;
      }
    }
    return existingMappedColorsNotEmpty;
  }

  private List<Edge> getNotCorrespondingEdgesRegardingColor(List<List<Edge>> uEdges, List<List<Edge>> vEdges)
  {
    List<Edge> notCorrespondingEdges = new LinkedList<>();
    for (int i = 0; i < uEdges.size(); i++)
    {
      List<Edge> uEdgesOfColorI = uEdges.get(i);
      List<Edge> vEdgesOfColorI = vEdges.get(i);
      if (uEdgesOfColorI.size() != vEdgesOfColorI.size())
      {
        Edge[] matchingEdges = new Edge[graph.getVertices().size()];

        for (Edge e : uEdgesOfColorI)
        {
          matchingEdges[e.getSquareMatchingEdge().getEndpoint().getVertexNo()] = e;
        }

        for (Edge e : vEdgesOfColorI)
        {
          Edge matchingEdge = matchingEdges[e.getEndpoint().getVertexNo()];
          if (matchingEdge != null)
          {
            matchingEdges[e.getEndpoint().getVertexNo()] = null;
          }
          else
          {
            notCorrespondingEdges.add(e);
          }
        }

        for (Edge e : matchingEdges)
        {
          if (e != null)
          {
            notCorrespondingEdges.add(e);
          }
        }
      }

    }
    return notCorrespondingEdges;
  }

}
