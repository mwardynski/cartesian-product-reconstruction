package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ReconstructionSingleEdgeResultVerifierImpl extends AbstractReconstructionResultVerifier
{

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  ColoringService coloringService;

  @Override
  public void compareFoundMissingVertexWithCorrectResult(ResultMissingSquaresData resultMissingSquaresData)
  {
    if (resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.CYCLE
            || resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.SPIKE
            || resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.BRIDGE)
    {
      boolean correctResult = checkCorrectnessUsingFactorization(resultMissingSquaresData.getResultNoSquareAtAllMissingSquares());
      testCaseContext.setCorrectResult(correctResult);
    }
    else
    {
//      reconstructForIsolatedColor(resultMissingSquaresData);
      reconstructWithColorMergingForIncorrectMissingSquareEdges(resultMissingSquaresData);
    }
  }

  private void reconstructForIsolatedColor(ResultMissingSquaresData resultMissingSquaresData)
  {
    for (Integer selectedColor : resultMissingSquaresData.getResultIncludedColors())
    {
      List<MissingSquaresUniqueEdgesData> resultMissingSquares = resultMissingSquaresData.getResultMissingSquaresByColor()[selectedColor];

      boolean correctResult = checkCorrectnessUsingFactorization(resultMissingSquares);
      testCaseContext.setCorrectResult(correctResult);

      if (testCaseContext.isCorrectResult())
      {
        break;
      }
    }
  }

  private void reconstructWithColorMergingForIncorrectMissingSquareEdges(ResultMissingSquaresData resultMissingSquaresData)
  {
    Collections.reverse(resultMissingSquaresData.getResultIncludedColors());
    for (Integer color : resultMissingSquaresData.getResultIncludedColors())
    {
      List<List<MissingSquaresUniqueEdgesData>> resultMissingSquaresByOtherEdgeColor = groupResultMissingSquaresByOtherEdgeColor(resultMissingSquaresData, color);

      for (List<MissingSquaresUniqueEdgesData> resultMissingSquares : resultMissingSquaresByOtherEdgeColor)
      {
        boolean correctResult = checkCorrectnessUsingFactorization(resultMissingSquares);
        testCaseContext.setCorrectResult(correctResult);

        if (testCaseContext.isCorrectResult())
        {
          break;
        }
        else
        {
          Edge baseEdge = resultMissingSquares.iterator().next().getBaseEdge();
          Edge otherEdge = resultMissingSquares.iterator().next().getOtherEdge();

          coloringService.mergeColorsForEdges(Arrays.asList(baseEdge, otherEdge), MergeTagEnum.WRONG_EDGE_RECONSTRUCTED);
        }
      }
      if (testCaseContext.isCorrectResult())
      {
        break;
      }
    }
  }

  private List<List<MissingSquaresUniqueEdgesData>> groupResultMissingSquaresByOtherEdgeColor(ResultMissingSquaresData resultMissingSquaresData, Integer color)
  {
    int maxColor = graph.getGraphColoring().getActualColors().stream().mapToInt(v -> v).max().getAsInt();
    List<List<MissingSquaresUniqueEdgesData>> resultMissingSquaresByOtherEdgeColor =
            IntStream.range(0, maxColor + 1).mapToObj(v -> new LinkedList<MissingSquaresUniqueEdgesData>()).collect(Collectors.toList());

    for (MissingSquaresUniqueEdgesData missingSquaresUniqueEdgesData : resultMissingSquaresData.getResultMissingSquaresByColor()[color])
    {
      int baseEdgeCurrentColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), missingSquaresUniqueEdgesData.getBaseEdge().getLabel().getColor());
      int otherEdgeCurrentColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), missingSquaresUniqueEdgesData.getOtherEdge().getLabel().getColor());
      if (baseEdgeCurrentColor != otherEdgeCurrentColor)
      {
        resultMissingSquaresByOtherEdgeColor.get(otherEdgeCurrentColor).add(missingSquaresUniqueEdgesData);
      }
    }

    Iterator<List<MissingSquaresUniqueEdgesData>> resultMissingSquaresByOtherEdgeColorIterator = resultMissingSquaresByOtherEdgeColor.iterator();
    while (resultMissingSquaresByOtherEdgeColorIterator.hasNext())
    {
      List<MissingSquaresUniqueEdgesData> nextResultMissingSquaresByOtherEdgeColor = resultMissingSquaresByOtherEdgeColorIterator.next();
      if (CollectionUtils.isEmpty(nextResultMissingSquaresByOtherEdgeColor))
      {
        resultMissingSquaresByOtherEdgeColorIterator.remove();
      }
    }
    return resultMissingSquaresByOtherEdgeColor;
  }
}
