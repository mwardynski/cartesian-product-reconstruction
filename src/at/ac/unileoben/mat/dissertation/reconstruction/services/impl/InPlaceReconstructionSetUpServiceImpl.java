package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.InPlaceReconstructionSetUpService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionBackupLayerService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Marcin on 16.03.2017.
 */
@Component
public class InPlaceReconstructionSetUpServiceImpl implements InPlaceReconstructionSetUpService
{
  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  ReconstructionService reconstructionService;

  @Autowired
  ReconstructionBackupLayerService reconstructionBackupLayerService;

  @Autowired
  EdgeService edgeService;

  @Autowired
  VertexService vertexService;

  @Override
  public boolean isInPlaceReconstructionToBeStarted()
  {
    return reconstructionData.getOperationOnGraph() == OperationOnGraph.PRE_IN_PLACE_RECONSTRUCTION
            && graph.getGraphColoring().getActualColors().size() == 1
            && !reconstructionData.getMissingInFirstLayerReconstructionData().isMissingInFirstLayer();
  }

  @Override
  public void setUpReconstructionInPlace()
  {
    printOutMergeStatistics(reconstructionData.getCurrentLayerNo());

    reconstructionBackupLayerService.recoverAfterCompleteMerge();
    if (isMissingVertexInFirstLayer())
    {
      reconstructionData.setLayerNoToRefactorizeFromOptional(Optional.of(reconstructionData.getCurrentLayerNo()));

      Edge exampleEdge = createExampleEdgeForRefactoring();
      addNewColorToGraphColoring(exampleEdge.getLabel().getColor());
      extendEdgesRefAndRevertLabeling();

      reconstructionService.addEdgesToReconstruction(Collections.singletonList(exampleEdge), graph.getRoot(), EdgeType.UP);
      reconstructionService.reconstructWithCollectedData();
    }
    else if (isMissingVertexInCurrentLayerToBeCreatedLater())
    {
      reconstructionData.setMissingVertexToBeCreatedLaterLayer(reconstructionData.getCurrentLayerNo());
    }
    reconstructionData.setMergeTags(new LinkedList<>());

    reconstructionData.setOperationOnGraph(OperationOnGraph.IN_PLACE_RECONSTRUCTION);
    FactorizationData factorizationData = new FactorizationData(0, null, null, null);
    factorizationData.setMaxConsistentLayerNo(reconstructionData.getCurrentLayerNo() - 1);
    factorizationData.setAfterConsistencyCheck(false);
    reconstructionData.setResultFactorization(factorizationData);
  }

  private void printOutMergeStatistics(int currentLayerNo)
  {
    int mergeTagsCount = reconstructionData.getMergeTags().size();

    List<MergeTagEnum> mergeTagEnums = Arrays.asList(
            MergeTagEnum.PREPARE,
            MergeTagEnum.LABEL_DOWN,
            MergeTagEnum.LABEL_CROSS,
            MergeTagEnum.CONSISTENCY_DOWN,
            MergeTagEnum.CONSISTENCY_CROSS,
            MergeTagEnum.CONSISTENCY_UP,
            MergeTagEnum.CONSISTENCY_UP_LABELS,
            MergeTagEnum.CONSISTENCY_UP_AMOUNT_BELOW,
            MergeTagEnum.CONSISTENCY_UP_AMOUNT_ABOVE,
            MergeTagEnum.CONSISTENCY_ADDITIONAL_VERTEX,
            MergeTagEnum.RECONSTRUCTION_REJECTED_EDGES);

    System.out.println("Starting to reconstruct in layer: " + currentLayerNo);
    mergeTagEnums.stream()
            .forEach(mergeTag -> System.out.println(mergeTag + " to ALL: " +
                    calculateQuantityOfMergeTag(mergeTag) + "/" + mergeTagsCount));
    reconstructionData.getMissingInFirstLayerReconstructionData().setMissingInFirstLayer(isMissingVertexInFirstLayer());
  }

  private boolean isMissingVertexInFirstLayer()
  {
    int consistencyUpAmountBelowTagsQuantity = calculateQuantityOfMergeTag(MergeTagEnum.CONSISTENCY_UP_AMOUNT_BELOW);
    int consistencyUpLabelTagsQuantity = calculateQuantityOfMergeTag(MergeTagEnum.CONSISTENCY_UP_LABELS);
    int labelCrossTagsQuantity = calculateQuantityOfMergeTag(MergeTagEnum.LABEL_CROSS);
    int labelDownTagsQuantity = calculateQuantityOfMergeTag(MergeTagEnum.LABEL_DOWN);
    int currentLayerNo = reconstructionData.getCurrentLayerNo();

    int mergeTagsCount = reconstructionData.getMergeTags().size();

    boolean missingVetexInFirstLayerPossible = currentLayerNo == 2
            && ((mergeTagsCount == consistencyUpAmountBelowTagsQuantity
            && !checkCurrentLayerUnitLayerVerticesValidity())
//            && currentLayerNo != graph.getLayers().size() - 1)
            || mergeTagsCount == consistencyUpLabelTagsQuantity
            || mergeTagsCount == labelCrossTagsQuantity
            || mergeTagsCount == labelDownTagsQuantity);
    reconstructionData.getMissingInFirstLayerReconstructionData().setMissingInFirstLayerPossible(missingVetexInFirstLayerPossible);
    return missingVetexInFirstLayerPossible;
  }

  @Override
  public boolean checkCurrentLayerUnitLayerVerticesValidity()
  {
    int currentLayerNo = reconstructionData.getCurrentLayerNo();
    int currentLayerRelevantUnitLayerVerticesAmount = reconstructionData.getMissingInFirstLayerReconstructionData().getCurrentLayerUnitLayerVerticesAmountBeforeAmountCheck();
    return currentLayerRelevantUnitLayerVerticesAmount <= 1 ||
            (graph.getLayers().size() - 1 > currentLayerNo
                    && graph.getLayers().get(currentLayerNo + 1).size() + 1 >= currentLayerRelevantUnitLayerVerticesAmount);
  }

  private boolean isMissingVertexInCurrentLayerToBeCreatedLater()
  {
    int consistencyUpAmountAboveTagsQuantity = calculateQuantityOfMergeTag(MergeTagEnum.CONSISTENCY_UP_AMOUNT_ABOVE);
    int mergeTagsCount = reconstructionData.getMergeTags().size();

    return mergeTagsCount == consistencyUpAmountAboveTagsQuantity;
  }

  private int calculateQuantityOfMergeTag(MergeTagEnum selectedMergeTag)
  {
    return reconstructionData.getMergeTags().stream()
            .filter(mergeTag -> mergeTag == selectedMergeTag)
            .mapToInt(mergeTag -> 1)
            .sum();
  }

  private Edge createExampleEdgeForRefactoring()
  {
    Edge exampleEdge = new Edge(new Vertex(Integer.MIN_VALUE, null), new Vertex(Integer.MIN_VALUE, null));
    int newColor = graph.getRoot().getUpEdges().getEdges().size();
    Label exampleLabel = new Label(newColor, 0);
    exampleEdge.setLabel(exampleLabel);
    return exampleEdge;
  }

  private void addNewColorToGraphColoring(int newColor)
  {
    GraphColoring graphColoring = graph.getGraphColoring();
    graphColoring.setOriginalColorsAmount(graphColoring.getOriginalColorsAmount() + 1);
    graphColoring.getActualColors().add(newColor);
    graphColoring.getColorsMapping().add(newColor);
  }

  private void extendEdgesRefAndRevertLabeling()
  {
    for (int i = 0; i <= reconstructionData.getCurrentLayerNo(); i++)
    {
      for (Vertex v : graph.getLayers().get(i))
      {
        clearEdgesLabelingForVertex(v);
        if (i < reconstructionData.getCurrentLayerNo())
        {
          extendEdgesRefForVertex(v);
        }
      }
    }
  }

  private void extendEdgesRefForVertex(Vertex v)
  {
    if (v.getBfsLayer() != 0)
    {
      extendEdgesRefByNewColor(v.getDownEdges().getEdgesRef().getColorPositions());
      if (v.getCrossEdges().getEdgesRef() != null)
      {
        extendEdgesRefByNewColor(v.getCrossEdges().getEdgesRef().getColorPositions());
      }
    }
    if (v.getBfsLayer() != reconstructionData.getCurrentLayerNo() - 1)
    {
      extendEdgesRefByNewColor(v.getUpEdges().getEdgesRef().getColorPositions());
    }
  }

  private void clearEdgesLabelingForVertex(Vertex v)
  {
    if (v.getBfsLayer() == reconstructionData.getCurrentLayerNo() - 1)
    {
      v.getUpEdges().setEdgesRef(null);
      v.getUpEdges().getEdges().stream().forEach(edgeService::clearEdgeLabeling);
    }
    if (v.getBfsLayer() == reconstructionData.getCurrentLayerNo())
    {
      v.getDownEdges().setEdgesRef(null);
      v.getDownEdges().getEdges().stream().forEach(edgeService::clearEdgeLabeling);
      v.getCrossEdges().setEdgesRef(null);
      v.getCrossEdges().getEdges().stream().forEach(edgeService::clearEdgeLabeling);
    }
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

  @Override
  public Optional<Vertex> findCorrespondingVertexToMissingVertexToBeCreatedLater(List<InconsistentEdge> uvInconsistentEdges, List<InconsistentEdge> uwInconsistentEdges)
  {
    Optional<Vertex> result = Optional.empty();

    if (reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION
            && reconstructionData.getMissingVertexToBeCreatedLaterLayer() != 0)
    {
      List<Edge> inconsistentEdges = Stream.concat(
              uvInconsistentEdges.stream().map(InconsistentEdge::getEdge),
              uwInconsistentEdges.stream().map(InconsistentEdge::getEdge)
      ).collect(Collectors.toList());

      if (countDistinctEndpoints(inconsistentEdges) == 1)
      {
        result = Optional.of(inconsistentEdges.get(0).getEndpoint());
      }
    }

    return result;
  }

  private long countDistinctEndpoints(List<Edge> inconsistentEdges)
  {
    return inconsistentEdges.stream().map(edge -> edge.getEndpoint())
            .distinct().count();
  }

  @Override
  public void reconstructMissingVertexToBeCreatedLater(Vertex correspondingVertex)
  {
    reconstructionService.removeAllReconstructionEntries();

    List<Vertex> layerToComplementVertices = graph.getLayers().get(reconstructionData.getMissingVertexToBeCreatedLaterLayer());
    Optional<Vertex> additionalVertexTwinOptional = layerToComplementVertices.stream()
            .filter(v -> correspondingVertex.getDownEdges().getEdgesRef().equals(v.getDownEdges().getEdgesRef()))
            .findAny();

    additionalVertexTwinOptional.ifPresent(
            additionalVertexTwin ->
            {
              additionalVertexTwin.getDownEdges().getEdges().stream()
                      .forEach(edge -> prepareDownEdgesOfMissingVertexToBeCreatedLaterForReconstruction(correspondingVertex, edge));

              reconstructionService.reconstructWithCollectedData();
              vertexService.assignVertexToUnitLayerAndMergeColors(additionalVertexTwin, MergeTagEnum.CONSISTENCY_ADDITIONAL_VERTEX);
              vertexService.assignVertexToUnitLayerAndMergeColors(reconstructionData.getNewVertex(), MergeTagEnum.CONSISTENCY_ADDITIONAL_VERTEX);
              reconstructionData.setMissingVertexToBeCreatedLaterLayer(0);


              graph.getLayers().get(reconstructionData.getCurrentLayerNo()).stream()
                      .forEach(v -> clearEdgesLabelingForVertex(v));
              reconstructionData.setLayerNoToRefactorizeFromOptional(Optional.of(reconstructionData.getCurrentLayerNo()));
            }

    );
  }

  private void prepareDownEdgesOfMissingVertexToBeCreatedLaterForReconstruction(Vertex correspondingVertex, Edge edge)
  {
    Edge correspondingEdge = edgeService.getEdgeByLabel(correspondingVertex, edge.getLabel(), EdgeType.DOWN);
    Label correspondingEdgeLabel = correspondingEdge.getLabel();
    Label correspondingEdgeOppositeLabel = correspondingEdge.getOpposite().getLabel();

    Edge newEdgeTemplate = new Edge(edge.getEndpoint(), edge.getOrigin());
    Label newEdgeTemplateLabel = new Label(correspondingEdgeOppositeLabel.getColor(), correspondingEdgeOppositeLabel.getName());
    newEdgeTemplate.setLabel(newEdgeTemplateLabel);

    Edge newEdgeTemplateOpposite = new Edge(edge.getOrigin(), edge.getEndpoint());
    Label newEdgeTemplateOppositeLabel = new Label(correspondingEdgeLabel.getColor(), correspondingEdgeLabel.getName());
    newEdgeTemplateOpposite.setLabel(newEdgeTemplateOppositeLabel);

    newEdgeTemplate.setOpposite(newEdgeTemplateOpposite);
    newEdgeTemplateOpposite.setOpposite(newEdgeTemplate);

    reconstructionService.addEdgesToReconstruction(Collections.singletonList(newEdgeTemplate), newEdgeTemplate.getOrigin(), EdgeType.UP);
  }

}
