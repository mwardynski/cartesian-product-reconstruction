package at.ac.unileoben.mat.dissertation.linearfactorization.impl;

import at.ac.unileoben.mat.dissertation.common.ReconstructionHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionBackupLayerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

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
  ReconstructionHelper reconstructionHelper;

  @Autowired
  EdgeService edgeService;

  @Autowired
  ColoringService coloringService;

  @Autowired
  VertexService vertexService;

  @Autowired
  ReconstructionBackupLayerService reconstructionBackupLayerService;

  @Override
  public void checkConsistency(int currentLayerNo)
  {
    List<Vertex> currentLayer = vertexService.getGraphLayer(currentLayerNo);
    List<Vertex> previousLayer = vertexService.getGraphLayer(currentLayerNo - 1);
    checkPreviousLayerUpEdgesConsistency(previousLayer);
    checkCurrentLayerAllEdgesConsistency(currentLayer);
    if (reconstructionData.getOperationOnGraph() == OperationOnGraph.FACTORIZE && graph.getGraphColoring().getActualColors().size() == 1)
    {
      setUpReconstructionInPlace(currentLayerNo);
      if (!reconstructionData.isCurrentLayerToBeRefactorized())
      {
        checkConsistency(currentLayerNo);
      }
    }
    else if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION)
    {
      if (reconstructionHelper.isTopVertexMissingByReconstruction(currentLayerNo))
      {
        reconstructionHelper.prepareTopVertexReconstruction(currentLayer);
      }
      reconstructionHelper.reconstructWithCollectedData();
    }
  }

  private void setUpReconstructionInPlace(int currentLayerNo)
  {
    reconstructionBackupLayerService.recoverAfterCompleteMerge();
    int consistencyUpAmountTagsQuantity = reconstructionData.getMergeTags().stream()
            .filter(mergeTag -> mergeTag == MergeTagEnum.CONSISTENCY_UP_AMOUNT)
            .mapToInt(mergeTag -> 1)
            .sum();
    if (currentLayerNo == 2
            && currentLayerNo != graph.getLayers().size() - 1
            && reconstructionData.getMergeTags().size() == consistencyUpAmountTagsQuantity)
    {
      reconstructionData.setCurrentLayerToBeRefactorized(true);

      Edge templateEdge = new Edge(new Vertex(Integer.MIN_VALUE, null), new Vertex(Integer.MIN_VALUE, null));
      int newColor = graph.getRoot().getUpEdges().getEdges().size();
      Label templateLabel = new Label(newColor, 0);
      templateEdge.setLabel(templateLabel);
      GraphColoring graphColoring = graph.getGraphColoring();
      graphColoring.setOriginalColorsAmount(graphColoring.getOriginalColorsAmount() + 1);
      graphColoring.getActualColors().add(newColor);
      graphColoring.getColorsMapping().add(newColor);

      for (int i = 0; i <= currentLayerNo; i++)
      {
        for (Vertex v : graph.getLayers().get(i))
        {
          if (i < currentLayerNo)
          {
            if (v.getBfsLayer() != 0)
            {
              extendEdgesRefByNewColor(v.getCrossEdges().getEdgesRef().getColorPositions());
              extendEdgesRefByNewColor(v.getDownEdges().getEdgesRef().getColorPositions());
            }
            if (v.getBfsLayer() != currentLayerNo - 1)
            {
              extendEdgesRefByNewColor(v.getUpEdges().getEdgesRef().getColorPositions());
            }
          }

          if (v.getBfsLayer() == currentLayerNo - 1)
          {
            v.getUpEdges().setEdgesRef(null);
            v.getUpEdges().getEdges().stream().forEach(e -> e.setLabel(null));
          }
          if (v.getBfsLayer() == currentLayerNo)
          {
            v.getDownEdges().setEdgesRef(null);
            v.getDownEdges().getEdges().stream().forEach(e -> e.setLabel(null));
            v.getCrossEdges().setEdgesRef(null);
            v.getCrossEdges().getEdges().stream().forEach(e -> e.setLabel(null));
          }
        }
      }

      reconstructionHelper.addEdgesToReconstruction(Collections.singletonList(templateEdge), graph.getRoot(), EdgeType.UP);
      reconstructionHelper.reconstructWithCollectedData();
    }
    reconstructionData.setMergeTags(new LinkedList<>());

    reconstructionData.setOperationOnGraph(OperationOnGraph.IN_PLACE_RECONSTRUCTION);
    FactorizationData factorizationData = new FactorizationData(0, null, null, null);
    factorizationData.setMaxConsistentLayerNo(currentLayerNo - 1);
    factorizationData.setAfterConsistencyCheck(false);
    reconstructionData.setResultFactorization(factorizationData);
  }

  private void extendEdgesRefByNewColor(List<ColorGroupLocation> colorPositions)
  {
    if (colorPositions != null && colorPositions.size() < graph.getGraphColoring().getOriginalColorsAmount())
    {
      ColorGroupLocation lastColorGroupLocation = colorPositions.get(colorPositions.size() - 1);
      int newColorGroupLocationIndex = lastColorGroupLocation.getIndex() + lastColorGroupLocation.getLength();
      ColorGroupLocation newColorGroupLocation = new ColorGroupLocation(newColorGroupLocationIndex, 0);
      colorPositions.add(newColorGroupLocation);
    }
  }

  private void checkCurrentLayerAllEdgesConsistency(List<Vertex> layer)
  {
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
        vertexService.assignVertexToUnitLayerAndMergeColors(u, true, MergeTagEnum.CONSISTENCY_DOWN);//not invoked
        continue;
      }
      EnumSet<EdgeType> edgeTypes = EnumSet.of(EdgeType.DOWN, EdgeType.CROSS);
      for (EdgeType edgeType : edgeTypes)
      {
        if (!checkPivotSquares(uv, edgeType).isEmpty() || !checkPivotSquares(uw, edgeType).isEmpty())
        {
          MergeTagEnum mergeTagEnum = edgeType == EdgeType.DOWN ? MergeTagEnum.CONSISTENCY_DOWN : MergeTagEnum.CONSISTENCY_CROSS;
          vertexService.assignVertexToUnitLayerAndMergeColors(u, true, mergeTagEnum);
          break;
        }
      }
      if (reconstructionData.getOperationOnGraph() != OperationOnGraph.RECONSTRUCT
              && reconstructionData.getOperationOnGraph() != OperationOnGraph.IN_PLACE_RECONSTRUCTION
              && isUpEdgesAmountNotAppropriateBetweenLayers(u, uv, uw))
      {
        vertexService.assignVertexToUnitLayerAndMergeColors(u, true, MergeTagEnum.CONSISTENCY_UP_AMOUNT);
      }
    }
  }


  private boolean isUpEdgesAmountNotAppropriateBetweenLayers(Vertex u, Edge uv, Edge uw)
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
    return u.getUpEdges().getEdges().size() != upEdgesTotalSize;
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
      List<Edge> uvInconsistentEdges = checkPivotSquares(uv, EdgeType.UP);
      List<Edge> uwInconsistentEdges = null;
      if (uw != null)
      {
        uwInconsistentEdges = checkPivotSquares(uw, EdgeType.UP);
      }
      if (uvInconsistentEdges != null && !uvInconsistentEdges.isEmpty())
      {
        handleInconsistentUpEdges(uv, uvInconsistentEdges);
      }
      if (uwInconsistentEdges != null && !uwInconsistentEdges.isEmpty())
      {
        handleInconsistentUpEdges(uw, uwInconsistentEdges);
      }
    }
  }

  private void handleInconsistentUpEdges(Edge uv, List<Edge> inconsistentEdges)
  {
    List<Edge> edgesToRelabel = new LinkedList<Edge>(inconsistentEdges);
    edgesToRelabel.add(uv);
    List<Integer> colors = coloringService.getColorsForEdges(graph.getGraphColoring(), edgesToRelabel);
    coloringService.mergeColorsForEdges(edgesToRelabel, MergeTagEnum.CONSISTENCY_UP);

    Vertex u = uv.getOrigin();
    List<Edge> allUpEdgesOfGivenColors = edgeService.getAllEdgesOfColors(u, colors, EdgeType.UP);
    for (Edge edgeOfGivenColor : allUpEdgesOfGivenColors)
    {
      Vertex endpointVertex = edgeOfGivenColor.getEndpoint();
      vertexService.assignVertexToUnitLayerAndMergeColors(endpointVertex, true, MergeTagEnum.CONSISTENCY_UP);
    }
  }

  private List<Edge> checkPivotSquares(Edge uv, EdgeType edgeType)
  {
    List<Edge> inconsistentEdges = new LinkedList<>();
    Vertex u = uv.getOrigin();
    Vertex v = uv.getEndpoint();
    List<List<Edge>> uDifferentThanUv = edgeService.getAllEdgesOfDifferentColor(u, uv.getLabel().getColor(), graph.getGraphColoring(), edgeType);
    List<List<Edge>> vDifferentThanUv = edgeService.getAllEdgesOfDifferentColor(v, uv.getLabel().getColor(), graph.getGraphColoring(), edgeType);
    if (edgeType != EdgeType.UP
            || (edgeType == EdgeType.UP && reconstructionHelper.isCorrespondingEdgesCheckForUpEdgesReasonable()))
    {
      List<Edge> notCorrespondingEdges = getNotCorrespondingEdgesRegardingColor(uDifferentThanUv, vDifferentThanUv);
      inconsistentEdges.addAll(notCorrespondingEdges);
    }
    for (List<Edge> uzForColor : uDifferentThanUv)
    {
      for (Edge uz : uzForColor)
      {
        Label uzLabel = uz.getLabel();
        Edge vzp = edgeService.getEdgeByLabel(v, uzLabel, edgeType);
        if (vzp == null)
        {
          inconsistentEdges.add(uz);
          continue;
        }
        Vertex z = uz.getEndpoint();
        Vertex zp = vzp.getEndpoint();
        Edge zzp = edgeService.getEdgeByLabel(z, uv.getLabel(), EdgeType.DOWN);
        if (zzp == null || !zzp.getEndpoint().equals(zp))
        {
          inconsistentEdges.add(uz);
        }
      }
    }
    if (reconstructionHelper.isReconstructionSuitableByConsistencyCheck()
            && !inconsistentEdges.isEmpty())
    {
      reconstructionHelper.addEdgesToReconstruction(inconsistentEdges, uv.getOrigin(), edgeType);
      return new LinkedList<>();
    }
    else
    {
      return inconsistentEdges;
    }
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
