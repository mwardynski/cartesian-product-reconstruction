package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.InPlaceReconstructionSetUpService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionBackupLayerService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

  @Override
  public boolean isInPlaceReconstructionToBeStarted()
  {
    return reconstructionData.getOperationOnGraph() == OperationOnGraph.PRE_IN_PLACE_RECONSTRUCTION
            && graph.getGraphColoring().getActualColors().size() == 1;
  }

  @Override
  public void setUpReconstructionInPlace()
  {
    reconstructionBackupLayerService.recoverAfterCompleteMerge();
    if (isMissingVertexInFirstLayer())
    {
      reconstructionData.setCurrentLayerToBeRefactorized(true);

      Edge exampleEdge = createExampleEdgeForRefactoring();
      addNewColotToGraphColoring(exampleEdge.getLabel().getColor());
      extendEdgesRefAndRevertLabeling();

      reconstructionService.addEdgesToReconstruction(Collections.singletonList(exampleEdge), graph.getRoot(), EdgeType.UP);
      reconstructionService.reconstructWithCollectedData();
    }
    reconstructionData.setMergeTags(new LinkedList<>());

    reconstructionData.setOperationOnGraph(OperationOnGraph.IN_PLACE_RECONSTRUCTION);
    FactorizationData factorizationData = new FactorizationData(0, null, null, null);
    factorizationData.setMaxConsistentLayerNo(reconstructionData.getCurrentLayerNo() - 1);
    factorizationData.setAfterConsistencyCheck(false);
    reconstructionData.setResultFactorization(factorizationData);
  }

  private boolean isMissingVertexInFirstLayer()
  {
    int consistencyUpAmountTagsQuantity = calculateQuantityOfMergeTag(MergeTagEnum.CONSISTENCY_UP_AMOUNT);
    int labelCrossTagsQuantity = calculateQuantityOfMergeTag(MergeTagEnum.LABEL_CROSS);
    int labelDownTagsQuantity = calculateQuantityOfMergeTag(MergeTagEnum.LABEL_DOWN);
    int currentLayerNo = reconstructionData.getCurrentLayerNo();

    return currentLayerNo == 2
            && ((reconstructionData.getMergeTags().size() == consistencyUpAmountTagsQuantity
            && currentLayerNo != graph.getLayers().size() - 1)
            || reconstructionData.getMergeTags().size() == labelCrossTagsQuantity
            || reconstructionData.getMergeTags().size() == labelDownTagsQuantity);
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

  private void addNewColotToGraphColoring(int newColor)
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
      v.getUpEdges().getEdges().stream().forEach(e -> e.setLabel(null));
    }
    if (v.getBfsLayer() == reconstructionData.getCurrentLayerNo())
    {
      v.getDownEdges().setEdgesRef(null);
      v.getDownEdges().getEdges().stream().forEach(e -> e.setLabel(null));
      v.getCrossEdges().setEdgesRef(null);
      v.getCrossEdges().getEdges().stream().forEach(e -> e.setLabel(null));
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

}
