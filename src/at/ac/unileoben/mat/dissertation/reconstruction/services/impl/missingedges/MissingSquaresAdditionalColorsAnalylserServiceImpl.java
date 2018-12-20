package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges;

import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MissingSquaresAdditionalColorsAnalylserServiceImpl
{
  @Autowired
  Graph graph;

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  MissingSquaresAnalyserCommons missingSquaresAnalyserCommons;

  public void findResultForColoringIncludingNewColorsAfterPostponedVertex(SquareReconstructionData squareReconstructionData, List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    int afterPostponedVertexColorsLowestIndex = squareReconstructionData.getGraphColoringBeforePostponedVertices().getActualColors().size();

    List<Integer> afterPostponedVertexColors = collectAfterPostponedVertexColors(squareReconstructionData);


    selectedColorLoop:
    for (Integer selectedColor : graph.getGraphColoring().getActualColors())
    {
      if (selectedColor == 0)
      {
        continue;
      }
      if (selectedColor >= afterPostponedVertexColorsLowestIndex)
      {
        break;
      }

      for (Integer selectedColorCorrespondingColor : afterPostponedVertexColors)
      {
        List<MissingSquaresUniqueEdgesData>[] currentIrregularMissingSquaresByColor =
                composeCurrentIrregularMissingSquaresByColor(selectedColor, selectedColorCorrespondingColor, afterPostponedVertexColorsLowestIndex, irregularMissingSquaresByColor);
        missingSquaresAnalyserCommons.findResultForIrregularMissingSquaresByColor(selectedColor, currentIrregularMissingSquaresByColor);
        if (testCaseContext.isCorrectResult())
        {
          break selectedColorLoop;
        }
      }
    }
  }

  public List<Integer> collectAfterPostponedVertexColors(SquareReconstructionData squareReconstructionData)
  {
    GraphColoring graphColoringBeforePostponedVertices = squareReconstructionData.getGraphColoringBeforePostponedVertices();
    if (graphColoringBeforePostponedVertices == null)
    {
      return Collections.emptyList();
    }

    int afterPostponedVertexColorsLowestIndex = graphColoringBeforePostponedVertices.getActualColors().size();

    return graph.getGraphColoring().getActualColors().stream()
            .filter(color -> color >= afterPostponedVertexColorsLowestIndex)
            .collect(Collectors.toList());
  }

  private List<MissingSquaresUniqueEdgesData>[] composeCurrentIrregularMissingSquaresByColor(int selectedColor, int selectedColorCorrespondingColor, int afterPostponedVertexColorsLowestIndex, List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    int otherColor = graph.getGraphColoring().getActualColors().get(1) != selectedColor
            ? graph.getGraphColoring().getActualColors().get(1)
            : graph.getGraphColoring().getActualColors().get(2);

    List<MissingSquaresUniqueEdgesData>[] currentIrregularMissingSquaresByColor = new List[afterPostponedVertexColorsLowestIndex];
    for (Integer color : graph.getGraphColoring().getActualColors())
    {
      if (color == 0)
      {
        continue;
      }
      if (color < afterPostponedVertexColorsLowestIndex)
      {
        currentIrregularMissingSquaresByColor[color] = new LinkedList(irregularMissingSquaresByColor[color]);
      }
      else if (color == selectedColorCorrespondingColor)
      {
        currentIrregularMissingSquaresByColor[selectedColor].addAll(irregularMissingSquaresByColor[color]);
      }
      else
      {
        currentIrregularMissingSquaresByColor[otherColor].addAll(irregularMissingSquaresByColor[color]);
      }
    }
    return currentIrregularMissingSquaresByColor;
  }
}
