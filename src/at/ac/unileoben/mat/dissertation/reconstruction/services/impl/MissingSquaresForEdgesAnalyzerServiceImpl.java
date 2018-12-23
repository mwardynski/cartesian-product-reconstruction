package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges.MissingSquaresAdditionalColorsAnalylserServiceImpl;
import at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges.MissingSquaresAnalyserCommons;
import at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges.MissingSquaresGeneralAnalyserServiceImpl;
import at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges.MissingSquaresSpikeAnalyserServiceImpl;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
@Profile("missingEdges")
public class MissingSquaresForEdgesAnalyzerServiceImpl extends AbstractMissingSquareAnalyzerService
{

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  MissingSquaresAnalyserCommons missingSquaresAnalyserCommons;

  @Autowired
  MissingSquaresGeneralAnalyserServiceImpl missingSquaresGeneralAnalyserService;

  @Autowired
  MissingSquaresAdditionalColorsAnalylserServiceImpl missingSquaresAdditionalColorsAnalylserService;

  @Autowired
  MissingSquaresSpikeAnalyserServiceImpl missingSquaresSpikeAnalyserService;

  @Autowired
  PartOfCycleNoSquareAtAllMissingSquaresFindingService partOfCycleNoSquareAtAllMissingSquaresGeneralService;

  @Override

  public void analyseMissingSquares(SquareReconstructionData squareReconstructionData, SquareMatchingEdgeData[][] squareMatchingEdges)
  {
    List<MissingSquaresEntryData> missingSquaresEntries = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntries();

    List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor = new List[graph.getGraphColoring().getColorsMapping().size()];
    graph.getGraphColoring().getActualColors().stream()
            .forEach(color -> irregularMissingSquaresByColor[color] = new LinkedList<>());

    groupMissingSquareEntries(missingSquaresEntries, noSquareAtAllMissingSquares, irregularMissingSquaresByColor);
    List<Integer> afterPostponedVertexColors = missingSquaresAdditionalColorsAnalylserService.collectAfterPostponedVertexColors(squareReconstructionData);

    if (CollectionUtils.isNotEmpty(noSquareAtAllMissingSquares))
    {
      missingSquaresSpikeAnalyserService.findResultForSpeciallyColoredEdges(irregularMissingSquaresByColor, noSquareAtAllMissingSquares, squareReconstructionData);
    }
    else if (CollectionUtils.isNotEmpty(squareReconstructionData.getNoticedPostponedVertices())
            && CollectionUtils.isNotEmpty(afterPostponedVertexColors))
    {
      missingSquaresAdditionalColorsAnalylserService.findResultForColoringIncludingNewColorsAfterPostponedVertex(squareReconstructionData, irregularMissingSquaresByColor);
    }
    else
    {
      missingSquaresGeneralAnalyserService.findResultForTypicalColoring(irregularMissingSquaresByColor);
    }

    if (testCaseContext.isCorrectResult())
    {
      System.out.println("OK!");
    }
    else
    {
      System.out.println("WRONG!");
    }
  }

}
