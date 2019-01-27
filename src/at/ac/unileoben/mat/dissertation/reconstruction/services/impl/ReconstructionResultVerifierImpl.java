package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionResultVerifier;
import at.ac.unileoben.mat.dissertation.structure.*;
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

    if (resultMissingSquaresData.getMissingEdgesFormation() == MissingEdgesFormation.CYCLE)
    {
      boolean correctFactorization = checkCorrectnessUsingFactorization(noSquareAtAllMissingSquaresVertexNumbers);
      boolean correctNeighors = checkCorrectnessUsingNeighborsNumbers(noSquareAtAllMissingSquaresVertexNumbers);

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

        UniqueList actualNeighborsVertexNumbers = new UniqueList(noSquareAtAllMissingSquaresVertexNumbers);
        actualNeighborsVertexNumbers.addAll(resultMissingSquaresByColorVertexNumbers);

        boolean includedColorCorrectFactorization = checkCorrectnessUsingFactorization(actualNeighborsVertexNumbers);
        boolean includedColorCorrectNeighbors = checkCorrectnessUsingNeighborsNumbers(actualNeighborsVertexNumbers);
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

    OperationOnGraph initialOperationOnGraph = reconstructionData.getOperationOnGraph();
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
      reconstructionData.setOperationOnGraph(initialOperationOnGraph);
      graphHelper.overrideGlobalGraph(originalGraph);
    }


    return correctResult;
  }

  private boolean checkCorrectnessUsingNeighborsNumbers(UniqueList foundNeighborsVertexNumbersUniqueList)
  {
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
