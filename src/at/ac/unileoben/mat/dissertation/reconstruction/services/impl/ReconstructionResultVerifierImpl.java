package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReconstructionResultVerifierImpl extends AbstractReconstructionResultVerifier
{
  @Autowired
  TestCaseContext testCaseContext;

  public void compareFoundMissingVertexWithCorrectResult(ResultMissingSquaresData resultMissingSquaresData)
  {
    List<MissingSquaresUniqueEdgesData>[] resultMissingSquaresByColor = resultMissingSquaresData.getResultMissingSquaresByColor();

    boolean correctResult = false;
    List<Integer> resultIncludedColors = resultMissingSquaresData.getResultIncludedColors();
    List<Integer> resultFittingColors = new LinkedList<>();

    if (resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.CYCLE)
    {
      boolean correctFactorization = checkCorrectnessUsingFactorization(resultMissingSquaresData.getResultNoSquareAtAllMissingSquares());
      boolean correctNeighors = checkCorrectnessUsingNeighborsNumbers(resultMissingSquaresData.getResultNoSquareAtAllMissingSquares());

      if (correctFactorization && correctNeighors)
      {
        correctResult = true;
      }
      else if (correctFactorization != correctNeighors)
      {
        throw new IllegalStateException("correctFactorization != correctNeighbors");
      }
    }
    else
    {
      for (Integer includedColor : resultIncludedColors)
      {
        List<MissingSquaresUniqueEdgesData> resultMissingSquares = resultMissingSquaresByColor[includedColor];
        UniqueList resultMissingSquaresByColorVertexNumbers = mapResultMissingSquaresToVertexNumbers(resultMissingSquares);

        UniqueList noSquareAtAllMissingSquaresVertexNumbers = mapResultMissingSquaresToVertexNumbers(resultMissingSquaresData.getResultNoSquareAtAllMissingSquares());
        UniqueList actualNeighborsVertexNumbers = new UniqueList(noSquareAtAllMissingSquaresVertexNumbers);
        actualNeighborsVertexNumbers.addAll(resultMissingSquaresByColorVertexNumbers);

        boolean includedColorCorrectFactorization = checkCorrectnessUsingFactorization(resultMissingSquaresData.getResultNoSquareAtAllMissingSquares());
        boolean includedColorCorrectNeighbors = checkCorrectnessUsingNeighborsNumbers(resultMissingSquaresData.getResultNoSquareAtAllMissingSquares());
        if (includedColorCorrectFactorization && includedColorCorrectNeighbors)
        {
          correctResult = includedColorCorrectFactorization;
          resultFittingColors.add(includedColor);
        }
        else if (reconstructionData.getOperationOnGraph() == OperationOnGraph.FINDING_SQUARES
                && includedColorCorrectFactorization != includedColorCorrectNeighbors)
        {
          throw new IllegalStateException("correctFactorization != correctNeighors");
        }
      }

      if (reconstructionData.getOperationOnGraph() == OperationOnGraph.FINDING_SQUARES
              && !resultFittingColors.containsAll(resultIncludedColors))
      {
        System.out.println("WARN - Subfactors weren't completely merged");
      }
    }

    testCaseContext.setCorrectResult(correctResult);
  }

  private boolean checkCorrectnessUsingNeighborsNumbers(List<MissingSquaresUniqueEdgesData> missingSquares)
  {
    UniqueList foundNeighborsVertexNumbersUniqueList = mapResultMissingSquaresToVertexNumbers(missingSquares);

    Set<Integer> actualNeighborsVertexNumbers = foundNeighborsVertexNumbersUniqueList.getEntries().stream()
            .map(vNo -> graph.getReverseReindexArray()[vNo])
            .collect(Collectors.toSet());
    Set<Integer> expectedNeighborsVertexNumbers = testCaseContext.getRemovedVertexNeighbors();

    boolean correctResult = false;
    if (reconstructionData.getOperationOnGraph() == OperationOnGraph.FINDING_SQUARES)
    {
      correctResult = expectedNeighborsVertexNumbers.equals(actualNeighborsVertexNumbers);
    }
    else if (reconstructionData.getOperationOnGraph() == OperationOnGraph.MANY_EDGES_RECONSTRUCTION)
    {
      correctResult = actualNeighborsVertexNumbers.containsAll(expectedNeighborsVertexNumbers);
    }

    return correctResult;
  }
}
