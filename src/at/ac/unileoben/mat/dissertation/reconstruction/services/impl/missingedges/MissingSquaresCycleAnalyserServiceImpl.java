package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MissingSquaresCycleAnalyserServiceImpl
{
  @Autowired
  Graph graph;

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  ColoringService coloringService;

  @Autowired
  MissingSquaresAnalyserCommons missingSquaresAnalyserCommons;

  @Autowired
  MissingSquaresSpikeCycleCommons missingSquaresSpikeCycleCommons;

  @Autowired
  PartOfCycleNoSquareAtAllMissingSquaresFindingService partOfCycleNoSquareAtAllMissingSquaresGeneralService;

  public boolean findCycleAndMissingEdgesBasedOnIt(Edge arbitraryNoSquareAtAllEdge,
                                                   List<MissingEdgeData> missingEdges,
                                                   List<MissingSquaresUniqueEdgesData> missingSquareEdges,
                                                   int[] potentialEdgesNumberToReconstructPerVertex,
                                                   List<Edge>[] potentialEdgesToReconstructSure,
                                                   List<Edge>[] potentialEdgesToReconstructMaybe,
                                                   Vertex vertexToRemoveForResult,
                                                   boolean[] missingSquareEdgesIncludedEndpoints,
                                                   List<Vertex> missingSquareEdgesEndpoints,
                                                   SquareReconstructionData squareReconstructionData,
                                                   Edge singleSpike)
  {
    NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo = new NoSquareAtAllCycleNode[graph.getVertices().size()];
    List<List<Edge>> groupedNoSquareAtAllEdges = new LinkedList<>();
    Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints = new Integer[graph.getVertices().size()];

    List<List<NoSquareAtAllCycleNode>> cycles = partOfCycleNoSquareAtAllMissingSquaresGeneralService.findCycleUsingBfs(
            arbitraryNoSquareAtAllEdge, groupedNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints,
            squareReconstructionData, noSquareAtAllCycleNodesByVertexNo);

    boolean foundCycles = CollectionUtils.isNotEmpty(cycles);
    if (foundCycles)
    {
      List<List<NoSquareAtAllCycleNode>> cyclesOfLengthSix = new LinkedList<>();
      List<List<NoSquareAtAllCycleNode>> cyclesOfLengthEight = new LinkedList<>();

      for (List<NoSquareAtAllCycleNode> cycle : cycles)
      {
        if (cycle.size() == 6)
        {
          cyclesOfLengthSix.add(cycle);
        }
        else if (cycle.size() == 8)
        {
          cyclesOfLengthEight.add(cycle);
        }
      }

      List<Vertex> resultVertices = new LinkedList<>();
      List<Integer> startVertexIndices = new LinkedList<>();

      if (singleSpike != null && CollectionUtils.isNotEmpty(cyclesOfLengthEight))
      {
        resultVertices.add(singleSpike.getEndpoint());
        int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(singleSpike.getOrigin(), cyclesOfLengthEight.get(0));
        startVertexIndices.add(startVertexIndex);
      }
      else if (CollectionUtils.isNotEmpty(cyclesOfLengthEight) && CollectionUtils.isNotEmpty(cyclesOfLengthSix))
      {
        List<Vertex>[][][] cycleDiffVertices = collectCycleDifferencesWithStartAndEndVertices(cyclesOfLengthEight, cyclesOfLengthSix);


        for (int cycleLengthSixIndex = 0; cycleLengthSixIndex < cycleDiffVertices[1].length; cycleLengthSixIndex++)
        {
          int[] cycleCrossingNumberPerVertex = new int[graph.getVertices().size()];
          for (List<Vertex> selectedCycleDiffVertices : cycleDiffVertices[1][cycleLengthSixIndex])
          {
            cycleCrossingNumberPerVertex[selectedCycleDiffVertices.get(0).getVertexNo()]++;
            cycleCrossingNumberPerVertex[selectedCycleDiffVertices.get(selectedCycleDiffVertices.size() - 1).getVertexNo()]++;
          }

          for (int cycleLengthEightIndex = 0; cycleLengthEightIndex < cycleDiffVertices[1][cycleLengthSixIndex].length; cycleLengthEightIndex++)
          {
            if (cycleDiffVertices[1][cycleLengthSixIndex][cycleLengthEightIndex].size() == 3
                    && cycleDiffVertices[0][cycleLengthEightIndex][cycleLengthSixIndex].size() == 5)
            {
              Vertex frontVertex = cycleDiffVertices[0][cycleLengthEightIndex][cycleLengthSixIndex].get(1);
              Vertex middleVertex = cycleDiffVertices[0][cycleLengthEightIndex][cycleLengthSixIndex].get(2);
              Vertex backVertex = cycleDiffVertices[0][cycleLengthEightIndex][cycleLengthSixIndex].get(3);

              SingleSquareList squareList = squareReconstructionData.getSquares()[middleVertex.getVertexNo()][frontVertex.getVertexNo()][backVertex.getVertexNo()];
              if (CollectionUtils.isEmpty(squareList))
              {
                resultVertices.add(cycleDiffVertices[1][cycleLengthSixIndex][cycleLengthEightIndex].get(1));
                int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(cycleDiffVertices[1][cycleLengthSixIndex][cycleLengthEightIndex].get(0), cyclesOfLengthSix.get(cycleLengthSixIndex));
                startVertexIndices.add(startVertexIndex);
              }
              else
              {
                List<NoSquareAtAllCycleNode> cycle = cyclesOfLengthSix.get(cycleLengthSixIndex);
                int firstCrossingIndex = findVertexIndexInCycle(cycleDiffVertices[1][cycleLengthSixIndex][cycleLengthEightIndex].get(0), cycle);
                int firstPotentialResultVertexIndex = (firstCrossingIndex - 1 + 6) % 6;
                int secondCrossingIndex = firstCrossingIndex + 2;
                int secondPotentialResultVertexIndex = (secondCrossingIndex + 1) % 6;

                resultVertices.add(cycle.get(firstPotentialResultVertexIndex).getVertex());
                int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(cycle.get(firstCrossingIndex).getVertex(), cycle);
                startVertexIndices.add(startVertexIndex);

                resultVertices.add(cycle.get(secondPotentialResultVertexIndex).getVertex());
                startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(cycle.get(secondCrossingIndex).getVertex(), cycle);
                startVertexIndices.add(startVertexIndex);
              }
            }
            else if (cycleDiffVertices[1][cycleLengthSixIndex][cycleLengthEightIndex].size() == 2
                    && cycleDiffVertices[0][cycleLengthEightIndex][cycleLengthSixIndex].size() == 4)
            {
              Vertex frontVertex = cycleDiffVertices[1][cycleLengthSixIndex][cycleLengthEightIndex].get(0);
              Vertex backVertex = cycleDiffVertices[1][cycleLengthSixIndex][cycleLengthEightIndex].get(1);
              if (cycleCrossingNumberPerVertex[frontVertex.getVertexNo()] == 1)
              {
                resultVertices.add(frontVertex);
                int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(backVertex, cyclesOfLengthSix.get(cycleLengthSixIndex));
                startVertexIndices.add(startVertexIndex);
              }
              if (cycleCrossingNumberPerVertex[backVertex.getVertexNo()] == 1)
              {
                resultVertices.add(backVertex);
                int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(frontVertex, cyclesOfLengthSix.get(cycleLengthSixIndex));
                startVertexIndices.add(startVertexIndex);
              }
            }
            else if (cycleDiffVertices[1][cycleLengthSixIndex][cycleLengthEightIndex].size() == 4
                    && cycleDiffVertices[0][cycleLengthEightIndex][cycleLengthSixIndex].size() == 6)
            {
              List<Vertex> selectedDiffVertices = cycleDiffVertices[1][cycleLengthSixIndex][cycleLengthEightIndex];
              Vertex firstArbitraryVertex = selectedDiffVertices.get(0);
              Vertex secondArbitraryVertex = selectedDiffVertices.get(selectedDiffVertices.size() - 1);

              resultVertices.add(firstArbitraryVertex);
              int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(secondArbitraryVertex, cyclesOfLengthSix.get(cycleLengthSixIndex));
              startVertexIndices.add(startVertexIndex);

              resultVertices.add(secondArbitraryVertex);
              startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(firstArbitraryVertex, cyclesOfLengthSix.get(cycleLengthSixIndex));
              startVertexIndices.add(startVertexIndex);
            }
          }
        }
      }
      else if (CollectionUtils.isNotEmpty(cyclesOfLengthEight))
      {
        if (cyclesOfLengthEight.size() == 1)
        {
          System.out.println("SINGLE CYCLE L=8!");
          return foundCycles;
        }
        findMissingEdgesComparingCycles(cycles, resultVertices, startVertexIndices, squareReconstructionData);
      }
      else if (CollectionUtils.isNotEmpty(cyclesOfLengthSix) && cyclesOfLengthSix.size() >= 2)
      {
        List<Vertex>[][][] cycleDiffVertices = collectCycleDifferencesWithStartAndEndVertices(cyclesOfLengthSix, cyclesOfLengthSix);

        List<Vertex> twoArbitraryCyclesDiffVertices = cycleDiffVertices[0][0][1];
        if (twoArbitraryCyclesDiffVertices.size() == 5)
        {
          int firstCrossingIndex = findVertexIndexInCycle(twoArbitraryCyclesDiffVertices.get(0), cyclesOfLengthSix.get(0));
          int afterCrossingVertexIndex = firstCrossingIndex + 1;

          int firstCrossingVertexNumber = cyclesOfLengthSix.get(0).get(firstCrossingIndex).getVertex().getVertexNo();
          int afterCrossingVertexNumber = cyclesOfLengthSix.get(0).get(afterCrossingVertexIndex).getVertex().getVertexNo();

          Edge edgeAfterCrossing = graph.getAdjacencyMatrix()[firstCrossingVertexNumber][afterCrossingVertexNumber];
          findCycleAndMissingEdgesBasedOnIt(edgeAfterCrossing, missingEdges, missingSquareEdges, potentialEdgesNumberToReconstructPerVertex,
                  potentialEdgesToReconstructSure, potentialEdgesToReconstructMaybe, vertexToRemoveForResult, missingSquareEdgesIncludedEndpoints,
                  missingSquareEdgesEndpoints, squareReconstructionData, singleSpike);

        }
        else if (twoArbitraryCyclesDiffVertices.size() == 4)
        {
          resultVertices.add(twoArbitraryCyclesDiffVertices.get(0));
          int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(twoArbitraryCyclesDiffVertices.get(3), cyclesOfLengthSix.get(0));
          startVertexIndices.add(startVertexIndex);

          resultVertices.add(twoArbitraryCyclesDiffVertices.get(3));
          startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(twoArbitraryCyclesDiffVertices.get(0), cyclesOfLengthSix.get(0));
          startVertexIndices.add(startVertexIndex);
        }
      }

      if (CollectionUtils.isNotEmpty(resultVertices))
      {
        List<List<Vertex>> verticesToConnect = collectVerticesToConnect(cycles, resultVertices, startVertexIndices);
        List<List<Edge>> cycleOfLengthEightMissingEdgesPerCycle = collectMissingEdges(resultVertices, verticesToConnect);

        boolean correctResultFound = checkMissingEdgesCorrectness(cycleOfLengthEightMissingEdgesPerCycle, missingEdges, missingSquareEdges, cycles);
        if (correctResultFound)
        {
          return foundCycles;
        }
      }
      else if (singleSpike == null)
      {
        findMissingEdgesUsingCyclesAndMissingSquareTriples(arbitraryNoSquareAtAllEdge, potentialEdgesNumberToReconstructPerVertex, potentialEdgesToReconstructSure, potentialEdgesToReconstructMaybe, vertexToRemoveForResult, missingSquareEdgesIncludedEndpoints, missingSquareEdgesEndpoints, squareReconstructionData, cycles);
      }
    }
    return foundCycles;
  }

  public void findMissingEdgesUsingCyclesAndMissingSquareTriples(Edge arbitraryNoSquareAtAllEdge,
                                                                 int[] potentialEdgesNumberToReconstructPerVertex,
                                                                 List<Edge>[] potentialEdgesToReconstructSure,
                                                                 List<Edge>[] potentialEdgesToReconstructMaybe,
                                                                 Vertex vertexToRemoveForResult,
                                                                 boolean[] missingSquareEdgesIncludedEndpoints,
                                                                 List<Vertex> missingSquareEdgesEndpoints,
                                                                 SquareReconstructionData squareReconstructionData,
                                                                 List<List<NoSquareAtAllCycleNode>> cycles)
  {
    if (CollectionUtils.isEmpty(cycles))
    {
      NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo = new NoSquareAtAllCycleNode[graph.getVertices().size()];
      List<List<Edge>> groupedNoSquareAtAllEdges = new LinkedList<>();
      Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints = new Integer[graph.getVertices().size()];

      cycles = partOfCycleNoSquareAtAllMissingSquaresGeneralService.findCycleUsingBfs(
              arbitraryNoSquareAtAllEdge, groupedNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints,
              squareReconstructionData, noSquareAtAllCycleNodesByVertexNo);
    }

    List<Integer> vertexNumbersInCycles = collectVerticesInCycles(cycles);

    //FIXME mayby only for cycles of the length = 8
    modifyPotentialEdgeNumberBasedOnCycle(cycles, vertexNumbersInCycles, potentialEdgesNumberToReconstructPerVertex, squareReconstructionData);
    List<Vertex> potentialCorrectVertices = missingSquaresSpikeCycleCommons.filterPotentialCorrectVertices(potentialEdgesNumberToReconstructPerVertex, arbitraryNoSquareAtAllEdge.getEndpoint(), vertexNumbersInCycles);
    potentialCorrectVertices = missingSquaresSpikeCycleCommons.filterOutMissingSquareEdgesVertices(missingSquareEdgesIncludedEndpoints, potentialCorrectVertices);
    potentialCorrectVertices = filterVerticesBetweenEdgesOfSameColorInCycle(cycles, potentialCorrectVertices);
//    potentialCorrectVertices = filterVerticesInCycleOfLengthEightWithSquare(cycles.get(0), potentialCorrectVertices, squareReconstructionData);
//    potentialCorrectVertices = favorizeVerticesWithManyDistinctMissingSquareTriples(missingEdges, potentialCorrectVertices);
    potentialCorrectVertices = missingSquaresSpikeCycleCommons.favorizeVerticesWithAllPotentialMissingEdgesSure(potentialEdgesNumberToReconstructPerVertex, potentialEdgesToReconstructSure, potentialCorrectVertices);

    if (potentialCorrectVertices.size() > 1)
    {
      if (testCaseContext.getVerticesToRemoveForResult().size() == potentialCorrectVertices.size())
      {
        vertexToRemoveForResult = potentialCorrectVertices.get(0);
      }
    }
    else if (potentialCorrectVertices.size() == 1)
    {
      vertexToRemoveForResult = potentialCorrectVertices.get(0);
    }

    if (vertexToRemoveForResult == null)
    {
      return;
    }

    List<Edge> baseResultEdges = missingSquaresSpikeCycleCommons.collectResultFromSureEdges(potentialEdgesToReconstructSure[vertexToRemoveForResult.getVertexNo()]);

    boolean[] potentialResultIncludedEndpoints = new boolean[graph.getVertices().size()];
    baseResultEdges.forEach(edge -> potentialResultIncludedEndpoints[edge.getEndpoint().getVertexNo()] = true);

    missingSquaresSpikeCycleCommons.addMissingSquareEdgesEndpointsToResult(vertexToRemoveForResult, missingSquareEdgesEndpoints, baseResultEdges, potentialResultIncludedEndpoints);
//          addVerticesToConnectFromCycle(vertexToRemoveForResult, verticesToConnectFromCycle, baseResultEdges, potentialResultIncludedEndpoints);

    boolean anyPotentialMaybyEdgeContainedInTheBaseResult =
            missingSquaresSpikeCycleCommons.containsAnyEndpoints(baseResultEdges, potentialEdgesToReconstructMaybe[vertexToRemoveForResult.getVertexNo()]);
    if (CollectionUtils.isEmpty(potentialEdgesToReconstructMaybe[vertexToRemoveForResult.getVertexNo()])
            || anyPotentialMaybyEdgeContainedInTheBaseResult)
    {
      missingSquaresAnalyserCommons.checkSelectedEdgesCorrectness(baseResultEdges);
    }
    else
    {
      missingSquaresAnalyserCommons.checkSelectedEdgesCorrectness(baseResultEdges);
      //FIXME can't loop careless like that
      for (Edge maybeResultEdge : potentialEdgesToReconstructMaybe[vertexToRemoveForResult.getVertexNo()])
      {
        List<Edge> potentialResultEdges = new LinkedList<>(baseResultEdges);
        potentialResultEdges.add(maybeResultEdge);

        missingSquaresAnalyserCommons.checkSelectedEdgesCorrectness(potentialResultEdges);
        if (testCaseContext.isCorrectResult())
        {
          break;
        }
      }
    }
    return;
  }

  private List<Integer> collectVerticesInCycles(List<List<NoSquareAtAllCycleNode>> cycles)
  {
    boolean[] verticesIncludedInCycle = new boolean[graph.getVertices().size()];
    return cycles.stream()
            .flatMap(cycle -> cycle.stream())
            .map(node -> node.getVertex().getVertexNo())
            .filter(vertexNumber ->
            {
              if (!verticesIncludedInCycle[vertexNumber])
              {
                verticesIncludedInCycle[vertexNumber] = true;
                return true;
              }
              return false;
            })
            .collect(Collectors.toList());
  }

  public void findMissingEdgesComparingCycles(List<List<NoSquareAtAllCycleNode>> cycles, List<Vertex> resultVertices, List<Integer> startVertexIndices, SquareReconstructionData squareReconstructionData)
  {
    List<Vertex>[][][] cycleDiffVertices = collectCycleDifferencesWithStartAndEndVertices(cycles, cycles);

    for (int i = 0; i < cycles.size() - 1 && CollectionUtils.isEmpty(resultVertices); i++)
    {
      for (int j = i + 1; j < cycles.size() && CollectionUtils.isEmpty(resultVertices); j++)
      {
        List<Vertex> diffVertices = cycleDiffVertices[0][i][j];
        List<Vertex> corrDiffVertices = cycleDiffVertices[1][j][i];

        int resultVerticesSizeBefore = resultVertices.size();

        if (diffVertices.size() - 2 == 1)
        {
          collectResultVerticesBasedOnSimpleSquare(resultVertices, startVertexIndices, diffVertices, cycles.get(i), corrDiffVertices, cycles.get(j));
        }
        else if (diffVertices.size() - 2 == 2)
        {
          collectResultVerticesBasedOnCubeWithoutSingleEdge(resultVertices, startVertexIndices, diffVertices, cycles.get(i));
        }
        else if (diffVertices.size() - 2 == 3)
        {
          collectResultVerticesBasedOnMiddleSquare(squareReconstructionData, resultVertices, startVertexIndices, diffVertices, cycles.get(i), corrDiffVertices);
          if (resultVerticesSizeBefore == resultVertices.size())
          {
            List<NoSquareAtAllCycleNode> firstCycle = cycles.get(i);
            List<NoSquareAtAllCycleNode> secondCycle = cycles.get(j);

            List<List<NoSquareAtAllCycleNode>> cyclesToCheck = Arrays.asList(firstCycle, secondCycle);

            for (List<NoSquareAtAllCycleNode> cycleToCheck : cyclesToCheck)
            {
              for (int k = 1; k < cycleToCheck.size() - 1; k++)
              {
                int frontVertexNumber = cycleToCheck.get(k - 1).getVertex().getVertexNo();
                Vertex middleVertex = cycleToCheck.get(k).getVertex();
                int middleVertexNumber = middleVertex.getVertexNo();
                int backVertexNumber = cycleToCheck.get(k + 1).getVertex().getVertexNo();

                SingleSquareList squareList = squareReconstructionData.getSquares()[middleVertexNumber][frontVertexNumber][backVertexNumber];
                if (CollectionUtils.isNotEmpty(squareList) && squareList.size() == 1)
                {

                  int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(diffVertices.get(0), cycles.get(i));
                  Vertex oppositeMiddleVertex = squareList.getFirst().getSquareOtherEdge().getEndpoint();

                  if (middleVertex.getEdges().size() == 2)
                  {
                    resultVertices.add(middleVertex);
                    startVertexIndices.add(startVertexIndex);
                  }
                  if (oppositeMiddleVertex.getEdges().size() == 2)
                  {
                    resultVertices.add(oppositeMiddleVertex);
                    startVertexIndices.add(startVertexIndex);
                  }
                }

              }
            }
          }
        }
      }
    }
  }

  private List<List<NoSquareAtAllCycleNode>> selectCyclesOfLengthEight(List<List<NoSquareAtAllCycleNode>> inputCycles)
  {
    return inputCycles.stream()
            .filter(cycle -> cycle.size() == 8)
            .collect(Collectors.toList());
  }

  private int mapCycleVertexToMainCycleEdgeEndpointIndex(Vertex cycleVertex, List<NoSquareAtAllCycleNode> cycle)
  {
    int vertexIndexInCycle = findVertexIndexInCycle(cycleVertex, cycle);
    if (vertexIndexInCycle % 2 == 0)
    {
      return 0;
    }
    else
    {
      return cycle.size() - 1;
    }
  }

  public List<Vertex>[][][] collectCycleDifferencesWithStartAndEndVertices(List<List<NoSquareAtAllCycleNode>> firstCyclesGroup,
                                                                           List<List<NoSquareAtAllCycleNode>> secondCyclesGroup)
  {
    List<Vertex>[][][] cycleDiffVertices = new List[2][][];
    cycleDiffVertices[0] = new List[firstCyclesGroup.size()][secondCyclesGroup.size()];
    cycleDiffVertices[1] = new List[secondCyclesGroup.size()][firstCyclesGroup.size()];

    for (int i = 0; i < firstCyclesGroup.size(); i++)
    {
      for (int j = 0; j < secondCyclesGroup.size(); j++)
      {
        if (firstCyclesGroup == secondCyclesGroup
                && j == i)
        {
          continue;
        }


        List<NoSquareAtAllCycleNode> firstCycle = firstCyclesGroup.get(i);
        List<NoSquareAtAllCycleNode> secondCycle = secondCyclesGroup.get(j);

        int firstCommonVertexIndex = 0;
        for (int k = 1; k < firstCyclesGroup.get(i).size(); k++)
        {
          if (firstCycle.get(k) == secondCycle.get(k))
          {
            firstCommonVertexIndex = k;
          }
          else
          {
            break;
          }
        }

        int lastCommonVertexIndexFirstCycle = firstCycle.size() - 1;
        int lastCommonVertexIndexSecondCycle = secondCycle.size() - 1;
        for (int k = 2; k < firstCyclesGroup.get(i).size(); k++)
        {
          if (firstCycle.get(firstCycle.size() - k) == secondCycle.get(secondCycle.size() - k))
          {
            lastCommonVertexIndexFirstCycle = firstCycle.size() - k;
            lastCommonVertexIndexSecondCycle = secondCycle.size() - k;
          }
          else
          {
            break;
          }
        }

        List<Vertex> firstCycleDiffVertices = new ArrayList<>();
        for (int k = firstCommonVertexIndex; k <= lastCommonVertexIndexFirstCycle; k++)
        {
          firstCycleDiffVertices.add(firstCycle.get(k).getVertex());
        }

        List<Vertex> secondCycleDiffVertices = new ArrayList<>();
        for (int k = firstCommonVertexIndex; k <= lastCommonVertexIndexSecondCycle; k++)
        {
          secondCycleDiffVertices.add(secondCycle.get(k).getVertex());
        }

        cycleDiffVertices[0][i][j] = firstCycleDiffVertices;
        cycleDiffVertices[1][j][i] = secondCycleDiffVertices;
      }
    }

    return cycleDiffVertices;
  }

  private void collectResultVerticesBasedOnSimpleSquare(List<Vertex> resultVertices, List<Integer> startVertexIndices, List<Vertex> diffVertices, List<NoSquareAtAllCycleNode> cycle, List<Vertex> corrDiffVertices, List<NoSquareAtAllCycleNode> corrCycle)
  {
    Vertex diffVertex = diffVertices.get(1);
    if (diffVertex.getEdges().size() == 2)
    {
      resultVertices.add(diffVertex);
      int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(diffVertex, cycle);
      startVertexIndices.add(startVertexIndex);
    }

    Vertex corrDiffVertex = corrDiffVertices.get(1);
    if (corrDiffVertex.getEdges().size() == 2 && (CollectionUtils.isEmpty(resultVertices)
            || CollectionUtils.isNotEmpty(resultVertices) && resultVertices.get(0) != corrDiffVertex))
    {
      resultVertices.add(corrDiffVertex);
      int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(corrDiffVertices.get(0), corrCycle);
      startVertexIndices.add(startVertexIndex);
    }
  }

  private void collectResultVerticesBasedOnCubeWithoutSingleEdge(List<Vertex> resultVertices, List<Integer> startVertexIndices, List<Vertex> diffVertices, List<NoSquareAtAllCycleNode> cycle)
  {
    Vertex firstDiffVertex = diffVertices.get(0);
    Vertex lastDiffVertex = diffVertices.get(3);

    resultVertices.add(firstDiffVertex);
    int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(lastDiffVertex, cycle);
    startVertexIndices.add(startVertexIndex);

    resultVertices.add(lastDiffVertex);
    startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(firstDiffVertex, cycle);
    startVertexIndices.add(startVertexIndex);
  }

  private void collectResultVerticesBasedOnMiddleSquare(SquareReconstructionData squareReconstructionData, List<Vertex> resultVertices, List<Integer> startVertexIndices, List<Vertex> diffVertices, List<NoSquareAtAllCycleNode> cycle, List<Vertex> corrDiffVertices)
  {
    Vertex firstSquareVertexA = diffVertices.get(0);
    Vertex firstSquareVertexB = diffVertices.get(1);
    Vertex firstSquareVertexC = corrDiffVertices.get(1);

    SingleSquareList firstSingleSquareList = squareReconstructionData.getSquares()[firstSquareVertexA.getVertexNo()][firstSquareVertexB.getVertexNo()][firstSquareVertexC.getVertexNo()];
    if (CollectionUtils.isEmpty(firstSingleSquareList) || firstSingleSquareList.size() > 1)
    {
      return;
    }

    SingleSquareData firstSquare = firstSingleSquareList.getFirst();

    Edge firstSquareEdgeBD = firstSquare.getSquareOtherEdge();
    Edge firstSquareEdgeCD = firstSquare.getSquareBaseEdge();

    Edge secondSquareEdgeBD = squareReconstructionData.getSquareMatchingEdgesByEdge()
            [firstSquareEdgeBD.getOrigin().getVertexNo()][firstSquareEdgeBD.getEndpoint().getVertexNo()]
            .getIncludedEdges()[diffVertices.get(2).getVertexNo()];

    Edge secondSquareEdgeCD = squareReconstructionData.getSquareMatchingEdgesByEdge()
            [firstSquareEdgeCD.getOrigin().getVertexNo()][firstSquareEdgeCD.getEndpoint().getVertexNo()]
            .getIncludedEdges()[corrDiffVertices.get(2).getVertexNo()];

    SingleSquareList secondSingleSquareList = squareReconstructionData.getSquares()[secondSquareEdgeBD.getEndpoint().getVertexNo()]
            [secondSquareEdgeBD.getOrigin().getVertexNo()][secondSquareEdgeCD.getOrigin().getVertexNo()];

    if (CollectionUtils.isNotEmpty(secondSingleSquareList) && secondSingleSquareList.size() == 1)
    {
      SingleSquareData secondSquare = secondSingleSquareList.getFirst();

      Vertex secondSquareVertexA = secondSquare.getSquareBaseEdge().getEndpoint();
      resultVertices.add(secondSquareVertexA);
      int startVertexIndex = mapCycleVertexToMainCycleEdgeEndpointIndex(diffVertices.get(0), cycle);
      startVertexIndices.add(startVertexIndex);
    }
  }

  public List<List<Vertex>> collectVerticesToConnect(List<List<NoSquareAtAllCycleNode>> cycles, List<Vertex> resultVertices, List<Integer> startVertexIndices)
  {
    List<List<Vertex>> verticesToConnect = new LinkedList<>();

    for (int i = 0; i < resultVertices.size(); i++)
    {
      Vertex resultVertex = resultVertices.get(i);
      int startVertexIndex = startVertexIndices.get(i);

      List<Vertex> verticesToConnectPerVertex = new LinkedList<>();
      boolean[] verticesToConnectPerVertexIncluded = new boolean[graph.getVertices().size()];

      for (List<NoSquareAtAllCycleNode> cycle : cycles)
      {
        for (int j = startVertexIndex % 2; j < cycle.size(); j += 2)
        {
          Vertex vertexToConnect = cycle.get(j).getVertex();
          if (!verticesToConnectPerVertexIncluded[vertexToConnect.getVertexNo()]
                  && graph.getAdjacencyMatrix()[vertexToConnect.getVertexNo()][resultVertex.getVertexNo()] == null)
          {
            verticesToConnectPerVertex.add(vertexToConnect);
            verticesToConnectPerVertexIncluded[vertexToConnect.getVertexNo()] = true;
          }
        }
      }
      verticesToConnect.add(verticesToConnectPerVertex);
    }

    return verticesToConnect;
  }

  public List<List<Edge>> collectMissingEdges
          (List<Vertex> resultVertices, List<List<Vertex>> verticesToConnect)
  {
    List<List<Edge>> resultMissingEdges = new LinkedList<>();
    for (int resultVertexIndex = 0; resultVertexIndex < resultVertices.size(); resultVertexIndex++)
    {
      Vertex resultVertex = resultVertices.get(resultVertexIndex);
      List<Edge> missingEdgesPerCycle = verticesToConnect.get(resultVertexIndex).stream()
              .map(vertexToConnect -> new Edge(resultVertex, vertexToConnect))
              .collect(Collectors.toList());
      resultMissingEdges.add(missingEdgesPerCycle);
    }
    return resultMissingEdges;
  }

  public int findVertexIndexInCycle(Vertex cycleVertex, List<NoSquareAtAllCycleNode> cycle)
  {
    int firstCrossingIndex = -1;
    for (int i = 0; i < cycle.size(); i++)
    {
      if (cycle.get(i).getVertex() == cycleVertex)
      {
        firstCrossingIndex = i;
        break;
      }
    }
    return firstCrossingIndex;
  }

  private boolean checkMissingEdgesCorrectness(List<List<Edge>> missingEdges, List<MissingEdgeData> missingEdgesBasedOnMissingSquareEdgesTripple, List<MissingSquaresUniqueEdgesData> missingSquareEdges, List<List<NoSquareAtAllCycleNode>> cycles)
  {
    if (CollectionUtils.isNotEmpty(missingEdges))
    {
      for (List<Edge> resultEdgesPerCycle : missingEdges)
      {
        missingSquaresAnalyserCommons.checkSelectedEdgesCorrectness(resultEdgesPerCycle);
        if (testCaseContext.isCorrectResult())
        {
          return true;
        }
      }
    }
    return false;
  }

  private void modifyPotentialEdgeNumberBasedOnCycle(List<List<NoSquareAtAllCycleNode>> cycles, List<Integer> vertexNumbersInCycles, int[] potentialEdgesNumberToReconstructPerVertex, SquareReconstructionData squareReconstructionData)
  {
    boolean[] verticesInCycleOfLengthEightWithSquare = new boolean[graph.getVertices().size()];
    for (List<NoSquareAtAllCycleNode> cycle : cycles)
    {
      List<CycleEdgePair> cycleEdgePairs = collectCycleEdgePairs(cycle);

      for (CycleEdgePair cycleEdgePair : cycleEdgePairs)
      {
        Edge firstEdge = cycleEdgePair.getFirstEdge();
        Edge secondEdge = cycleEdgePair.getSecondEdge();

        SingleSquareList singleSquareDataList = squareReconstructionData.getSquares()[firstEdge.getOrigin().getVertexNo()][firstEdge.getEndpoint().getVertexNo()][secondEdge.getEndpoint().getVertexNo()];
        if (CollectionUtils.isNotEmpty(singleSquareDataList))
        {
          verticesInCycleOfLengthEightWithSquare[firstEdge.getOrigin().getVertexNo()] = true;
        }
      }
    }

    vertexNumbersInCycles.stream()
            .filter(vertexNumber -> verticesInCycleOfLengthEightWithSquare[vertexNumber])
            .forEach(vertexNumber -> potentialEdgesNumberToReconstructPerVertex[vertexNumber]++);
  }

  private List<CycleEdgePair> collectCycleEdgePairs(List<NoSquareAtAllCycleNode> cycle)
  {
    int cycleLength = cycle.size();
    List<CycleEdgePair> cycleEdgePairs = new LinkedList<>();

    for (int i = 1; i <= cycleLength; i++)
    {
      Vertex frontVertex = cycle.get((i - 1) % cycleLength).getVertex();
      Vertex middleVertex = cycle.get(i % cycleLength).getVertex();
      Vertex backVertex = cycle.get((i + 1) % cycleLength).getVertex();

      Edge firstEdge = graph.getAdjacencyMatrix()[middleVertex.getVertexNo()][frontVertex.getVertexNo()];
      Edge secondEdge = graph.getAdjacencyMatrix()[middleVertex.getVertexNo()][backVertex.getVertexNo()];

      cycleEdgePairs.add(new CycleEdgePair(firstEdge, secondEdge));
    }
    return cycleEdgePairs;
  }

  private List<Vertex> filterVerticesBetweenEdgesOfSameColorInCycle(List<List<NoSquareAtAllCycleNode>> cycles, List<Vertex> potentialCorrectVertices)
  {
    if (potentialCorrectVertices.size() > 1)
    {
      boolean[] verticesBetweenEdgesOfSameColorInCycle = new boolean[graph.getVertices().size()];

      for (List<NoSquareAtAllCycleNode> cycle : cycles)
      {
        List<CycleEdgePair> cycleEdgePairs = collectCycleEdgePairs(cycle);
        for (CycleEdgePair cycleEdgePair : cycleEdgePairs)
        {
          int firstEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), cycleEdgePair.getFirstEdge().getLabel().getColor());
          int secondEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), cycleEdgePair.getSecondEdge().getLabel().getColor());

          if (firstEdgeColor == secondEdgeColor && firstEdgeColor != 0)
          {
            verticesBetweenEdgesOfSameColorInCycle[cycleEdgePair.getFirstEdge().getOrigin().getVertexNo()] = true;
          }
        }
      }

      List<Vertex> potentialCorrectVerticesBetweenEdgesOfSameColorInCycle = potentialCorrectVertices.stream()
              .filter(vertex -> verticesBetweenEdgesOfSameColorInCycle[vertex.getVertexNo()])
              .collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(potentialCorrectVerticesBetweenEdgesOfSameColorInCycle))
      {
        potentialCorrectVertices = potentialCorrectVerticesBetweenEdgesOfSameColorInCycle;
      }
    }
    return potentialCorrectVertices;
  }

  private List<Vertex> filterVerticesInCycleOfLengthEightWithSquare(List<NoSquareAtAllCycleNode> cycle, List<Vertex> potentialCorrectVertices, SquareReconstructionData squareReconstructionData)
  {
    boolean[] verticesInCycleOfLengthEightWithSquare = new boolean[graph.getVertices().size()];

    List<CycleEdgePair> cycleEdgePairs = collectCycleEdgePairs(cycle);

    for (CycleEdgePair cycleEdgePair : cycleEdgePairs)
    {
      Edge firstEdge = cycleEdgePair.getFirstEdge();
      Edge secondEdge = cycleEdgePair.getSecondEdge();

      SingleSquareList singleSquareDataList = squareReconstructionData.getSquares()[firstEdge.getOrigin().getVertexNo()][firstEdge.getEndpoint().getVertexNo()][secondEdge.getEndpoint().getVertexNo()];
      if (CollectionUtils.isNotEmpty(singleSquareDataList))
      {
        verticesInCycleOfLengthEightWithSquare[firstEdge.getOrigin().getVertexNo()] = true;
      }
    }

    List<Vertex> potentialCorrectVerticesInCycleOfLengthEightWithSquare = potentialCorrectVertices.stream()
            .filter(vertex -> verticesInCycleOfLengthEightWithSquare[vertex.getVertexNo()])
            .collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(potentialCorrectVerticesInCycleOfLengthEightWithSquare))
    {
      potentialCorrectVertices = potentialCorrectVerticesInCycleOfLengthEightWithSquare;
    }
    return potentialCorrectVertices;
  }

  private List<Vertex> favorizeVerticesWithManyDistinctMissingSquareTriples(List<MissingEdgeData> missingEdges, List<Vertex> potentialCorrectVertices)
  {
    if (potentialCorrectVertices.size() > 1)
    {
      boolean[] endpointsWithManyDistinctMissingSquareTriples = new boolean[graph.getVertices().size()];

      missingEdges.stream()
              .filter(missingEdgeData -> missingEdgeData.getNumberOfDistinctMissingSquareTriples() > 1)
              .map(MissingEdgeData::getEdge)
              .flatMap(missingEdge -> Arrays.asList(missingEdge.getOrigin(), missingEdge.getEndpoint()).stream())
              .forEach(vertex -> endpointsWithManyDistinctMissingSquareTriples[vertex.getVertexNo()] = true);


      List<Vertex> verticesWithManyDistinctMissingSquareTriples = potentialCorrectVertices.stream()
              .filter(vertex -> endpointsWithManyDistinctMissingSquareTriples[vertex.getVertexNo()])
              .collect(Collectors.toList());

      if (CollectionUtils.isNotEmpty(verticesWithManyDistinctMissingSquareTriples))
      {
        potentialCorrectVertices = verticesWithManyDistinctMissingSquareTriples;
      }
    }
    return potentialCorrectVertices;
  }
}
