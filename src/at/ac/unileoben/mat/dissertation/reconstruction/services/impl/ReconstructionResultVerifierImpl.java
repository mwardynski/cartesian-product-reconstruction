package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionResultVerifier;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ReconstructionResultVerifierImpl implements ReconstructionResultVerifier
{

  @Autowired
  Graph graph;

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  LinearFactorization linearFactorization;

  public void compareFoundMissingVertexWithCorrectResult(ResultMissingSquaresData resultMissingSquaresData)
  {
    UniqueList noSquareAtAllMissingSquaresVertexNumbers = mapResultMissingSquaresToVertexNumbers(resultMissingSquaresData.getResultNoSquareAtAllMissingSquares());
    List<MissingSquaresUniqueEdgesData>[] resultMissingSquaresByColor = resultMissingSquaresData.getResultMissingSquaresByColor();

    boolean correctResult = false;
    List<Integer> resultIncludedColors = resultMissingSquaresData.getResultIncludedColors();
    List<Integer> resultFittingColors = new LinkedList<>();

    if (resultMissingSquaresData.isCycleOfIrregularNoSquareAtAllMissingSquares())
    {
      correctResult = checkCorrectnessUsingFactorization(noSquareAtAllMissingSquaresVertexNumbers);
    }
    else
    {
      for (Integer includedColor : resultIncludedColors)
      {
        List<MissingSquaresUniqueEdgesData> resultMissingSquares = resultMissingSquaresByColor[includedColor];
        UniqueList resultMissingSquaresByColorVertexNumbers = mapResultMissingSquaresToVertexNumbers(resultMissingSquares);

        UniqueList actualNeighborsVertexNumbers = new UniqueList(noSquareAtAllMissingSquaresVertexNumbers);
        actualNeighborsVertexNumbers.addAll(resultMissingSquaresByColorVertexNumbers);

        boolean includedColorCorrectResult = checkCorrectnessUsingFactorization(actualNeighborsVertexNumbers);
        if (includedColorCorrectResult)
        {
          correctResult = includedColorCorrectResult;
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

  private UniqueList mapResultMissingSquaresToVertexNumbers(List<MissingSquaresUniqueEdgesData> missingSquares)
  {

    UniqueList vertexNumbers = new UniqueList(graph.getVertices().size());
    missingSquares.stream()
            .map(missingSquare -> Arrays.asList(missingSquare.getBaseEdge().getEndpoint(), missingSquare.getOtherEdge().getEndpoint()))
            .flatMap(verticesPairs -> verticesPairs.stream())
            .map(v -> v.getVertexNo())
            .forEach(vertexNumber -> vertexNumbers.add(vertexNumber));
    return vertexNumbers;
  }

  private boolean checkCorrectnessUsingFactorization(UniqueList actualNeighborsVertexNumbers)
  {
    boolean correctResult = false;

    List<Vertex> copiedVertices = graphHelper.copySubgraph(graph.getVertices(), Optional.empty());
    List<Vertex> newVertexNeighborsAmongCopiedVertices = copiedVertices.stream()
            .filter(copiedVertex -> actualNeighborsVertexNumbers.contains(copiedVertex.getVertexNo()))
            .collect(Collectors.toList());
    graphHelper.addVertex(copiedVertices, newVertexNeighborsAmongCopiedVertices);

    Graph originalGraph = new Graph(graph);

    try
    {
      reconstructionData.setOperationOnGraph(OperationOnGraph.FACTORIZE);
      linearFactorization.factorize(copiedVertices, copiedVertices.get(0));

      if (graph.getGraphColoring().getActualColors().size() != 1)
      {
        correctResult = true;
      }
    }
    catch (Exception e)
    {
    } finally
    {
      reconstructionData.setOperationOnGraph(OperationOnGraph.FINDING_SQUARES);
      graphHelper.overrideGlobalGraph(originalGraph);
    }


    return correctResult;
  }
}
