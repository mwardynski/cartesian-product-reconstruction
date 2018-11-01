package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionResultVerifier;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresUniqueEdgesData;
import at.ac.unileoben.mat.dissertation.structure.ResultMissingSquaresData;
import at.ac.unileoben.mat.dissertation.structure.TestCaseContext;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ReconstructionResultVerifierImpl implements ReconstructionResultVerifier
{

  @Autowired
  Graph graph;

  @Autowired
  TestCaseContext testCaseContext;

  public void compareFoundMissingVertexWithCorrectResult(ResultMissingSquaresData resultMissingSquaresData)
  {
    Set<Integer> expectedNeighborsVertexNumbers = testCaseContext.getRemovedVertexNeighbors();

    Set<Integer> noSquareAtAllMissingSquaresVertexNumbers = mapResultMissingSquaresToOriginVertexNumbers(resultMissingSquaresData.getResultNoSquareAtAllMissingSquares());
    List<MissingSquaresUniqueEdgesData>[] resultMissingSquaresByColor = resultMissingSquaresData.getResultMissingSquaresByColor();


    boolean correctResult = false;
    List<Integer> resultIncludedColors = resultMissingSquaresData.getResultIncludedColors();
    List<Integer> resultFittingColors = new LinkedList<>();

    if (resultMissingSquaresData.isCycleOfIrregularNoSquareAtAllMissingSquares())
    {
      Set<Integer> actualNeighborsVertexNumbers = new HashSet<>(noSquareAtAllMissingSquaresVertexNumbers);
      if (expectedNeighborsVertexNumbers.equals(actualNeighborsVertexNumbers))
      {
        correctResult = true;
      }
    }
    else
    {
      Set<Integer> baseNeighborsVertexNumbers = new HashSet<>();
      if (CollectionUtils.isNotEmpty(noSquareAtAllMissingSquaresVertexNumbers))
      {
        baseNeighborsVertexNumbers.addAll(noSquareAtAllMissingSquaresVertexNumbers);
      }

      for (Integer includedColor : resultIncludedColors)
      {
        List<MissingSquaresUniqueEdgesData> resultMissingSquares = resultMissingSquaresByColor[includedColor];
        Set<Integer> resultMissingSquaresByColorVertexNumbers = mapResultMissingSquaresToOriginVertexNumbers(resultMissingSquares);

        Set<Integer> actualNeighborsVertexNumbers = new HashSet<>(baseNeighborsVertexNumbers);
        actualNeighborsVertexNumbers.addAll(resultMissingSquaresByColorVertexNumbers);

        if (expectedNeighborsVertexNumbers.equals(actualNeighborsVertexNumbers))
        {
          correctResult = true;
          resultFittingColors.add(includedColor);
        }
      }

      if (!resultFittingColors.containsAll(resultIncludedColors))
      {
        System.out.println("WARN - Subfactors weren't completely merged");
      }
    }


    testCaseContext.setCorrectResult(correctResult);
  }

  private Set<Integer> mapResultMissingSquaresToOriginVertexNumbers(List<MissingSquaresUniqueEdgesData> missingSquares)
  {
    return missingSquares.stream()
            .map(missingSquare -> Arrays.asList(missingSquare.getBaseEdge().getEndpoint(), missingSquare.getOtherEdge().getEndpoint()))
            .flatMap(verticesPairs -> verticesPairs.stream())
            .map(v -> v.getVertexNo())
            .map(vNo -> graph.getReverseReindexArray()[vNo])
            .collect(Collectors.toSet());
  }
}
