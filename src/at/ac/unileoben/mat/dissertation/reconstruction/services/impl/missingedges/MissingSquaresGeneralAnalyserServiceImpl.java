package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges;

import at.ac.unileoben.mat.dissertation.reconstruction.services.impl.MissingSquaresForEdgesAnalyzerServiceImpl;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresUniqueEdgesData;
import at.ac.unileoben.mat.dissertation.structure.TestCaseContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MissingSquaresGeneralAnalyserServiceImpl
{

  @Autowired
  Graph graph;

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  MissingSquaresAnalyserCommons missingSquaresAnalyserCommons;


  public void findResultForTypicalColoring(List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    for (Integer selectedColor : graph.getGraphColoring().getActualColors())
    {
      if (selectedColor == 0)
      {
        continue;
      }

      missingSquaresAnalyserCommons.findResultForIrregularMissingSquaresByColor(selectedColor, irregularMissingSquaresByColor);
      if (testCaseContext.isCorrectResult())
      {
        break;
      }
    }
  }
}
