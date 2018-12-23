package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MissingSquaresSpikeAnalyserServiceImpl
{

  @Autowired
  Graph graph;

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  MissingSquaresAnalyserCommons missingSquaresAnalyserCommons;

  @Autowired
  PartOfCycleNoSquareAtAllMissingSquaresFindingService partOfCycleNoSquareAtAllMissingSquaresGeneralService;

  public List<Edge> findNoSquareAtAllEdgesWithDegreeOneAtEndpoint(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares)
  {
    List<Edge> noSquareAtAllEdgesWithDegreeOneAtEndpoint = noSquareAtAllMissingSquares.stream()
            .map(missingSquare -> missingSquare.getBaseEdge())
            .filter(edge -> edge.getEndpoint().getEdges().size() == 1)
            .collect(Collectors.toList());
    return noSquareAtAllEdgesWithDegreeOneAtEndpoint;
  }

  public Vertex handleSpikesSpecialCases(List<Edge> noSquareAtAllEdgesWithDegreeOneAtEndpoint)
  {
    Vertex vertexToRemoveForResult = null;
    for (Edge spikeBeginning : noSquareAtAllEdgesWithDegreeOneAtEndpoint)
    {
      Edge currentEdge = spikeBeginning;
      int spikeLength = 1;

      while (currentEdge != null)
      {
        List<Edge> followingEdges = currentEdge.getOrigin().getEdges();
        Edge currentEdgeForComparision = currentEdge;
        List<Edge> potentialNextEdges = followingEdges.stream()
                .filter(edge -> edge != currentEdgeForComparision)
                .filter(edge -> edge.getLabel().getColor() == 0)
                .collect(Collectors.toList());

        if (followingEdges.size() == potentialNextEdges.size() + 1 && potentialNextEdges.size() > 1 && spikeLength == 1)
        {
          currentEdge = null;
          spikeLength = -1;
        }
        else if (followingEdges.size() == 2 && potentialNextEdges.size() == 1)
        {
          currentEdge = potentialNextEdges.get(0).getOpposite();
          spikeLength++;
        }
        else
        {
          currentEdge = null;
        }
      }

      if (spikeLength == -1 || spikeLength > 3)
      {
        vertexToRemoveForResult = spikeBeginning.getEndpoint();
      }
      else if (spikeLength == 3)
      {
        if (noSquareAtAllEdgesWithDegreeOneAtEndpoint.size() == 1)
        {
          vertexToRemoveForResult = spikeBeginning.getEndpoint();
        }
        else if (noSquareAtAllEdgesWithDegreeOneAtEndpoint.size() == 2)
        {
          vertexToRemoveForResult = spikeBeginning != noSquareAtAllEdgesWithDegreeOneAtEndpoint.get(0)
                  ? noSquareAtAllEdgesWithDegreeOneAtEndpoint.get(0).getEndpoint()
                  : noSquareAtAllEdgesWithDegreeOneAtEndpoint.get(1).getEndpoint();
        }
        else
        {
          System.out.println("INFO - spike of length==3 and more than one other spikes");
        }
      }
    }
    return vertexToRemoveForResult;
  }

  public void findResultForSpeciallyColoredEdges(List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor, List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    List<Edge> noSquareAtAllEdgesWithDegreeOneAtEndpoint = findNoSquareAtAllEdgesWithDegreeOneAtEndpoint(noSquareAtAllMissingSquares);

    Vertex vertexToRemoveForResult = handleSpikesSpecialCases(noSquareAtAllEdgesWithDegreeOneAtEndpoint);
    missingSquaresAnalyserCommons.checkSelectedVertexCorrectness(vertexToRemoveForResult);
    if (testCaseContext.isCorrectResult())
    {
      return;
    }

    List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquaresWithNormallyColoredEdge = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquaresWithoutNormallyColoredEdge = new LinkedList<>();

    for (MissingSquaresUniqueEdgesData noSquareAtAllMissingSquare : noSquareAtAllMissingSquares)
    {
      if (noSquareAtAllMissingSquare.getOtherEdge().getLabel().getColor() != 0)
      {
        noSquareAtAllMissingSquaresWithNormallyColoredEdge.add(noSquareAtAllMissingSquare);
      }
      else
      {
        noSquareAtAllMissingSquaresWithoutNormallyColoredEdge.add(noSquareAtAllMissingSquare);
      }
    }

    for (Integer selectedColor : graph.getGraphColoring().getActualColors())
    {
      if (selectedColor == 0)
      {
        continue;
      }

      List<MissingEdgeData> missingEdges = new LinkedList<>();
      List<MissingSquaresUniqueEdgesData> missingSquareEdges = new LinkedList<>();
      MissingEdgeData[][] collectedMissingEdgesArray = new MissingEdgeData[graph.getVertices().size()][graph.getVertices().size()];

      Edge missingEdgesWarden = new Edge(null, null);

      List<MissingSquaresUniqueEdgesData> missingSquaresToProcess = new LinkedList<>(irregularMissingSquaresByColor[selectedColor]);
      missingSquaresToProcess.addAll(noSquareAtAllMissingSquaresWithNormallyColoredEdge);
      Edge[][] missingSquarePairsForSelectedColor = missingSquaresAnalyserCommons.findMissingSquarePairsForSelectedColor(missingSquaresToProcess, missingEdgesWarden);
      missingSquaresAnalyserCommons.collectMissingEdgesForSelectedColor(missingSquaresToProcess, missingEdges, missingSquareEdges, missingSquarePairsForSelectedColor, collectedMissingEdgesArray, missingEdgesWarden);

      boolean[] missingSquareEdgesIncludedEndpoints = new boolean[graph.getVertices().size()];
      List<Vertex> missingSquareEdgesEndpoints = new LinkedList<>();

      for (MissingSquaresUniqueEdgesData missingSquareEdge : missingSquareEdges)
      {
        if (missingSquareEdge.getBaseEdge().getLabel().getColor() != 0 && missingSquareEdge.getOtherEdge().getLabel().getColor() != 0)
        {
          if (!missingSquareEdgesIncludedEndpoints[missingSquareEdge.getBaseEdge().getEndpoint().getVertexNo()])
          {
            missingSquareEdgesIncludedEndpoints[missingSquareEdge.getBaseEdge().getEndpoint().getVertexNo()] = true;
            missingSquareEdgesEndpoints.add(missingSquareEdge.getBaseEdge().getEndpoint());
          }

          if (!missingSquareEdgesIncludedEndpoints[missingSquareEdge.getOtherEdge().getEndpoint().getVertexNo()])
          {
            missingSquareEdgesIncludedEndpoints[missingSquareEdge.getOtherEdge().getEndpoint().getVertexNo()] = true;
            missingSquareEdgesEndpoints.add(missingSquareEdge.getOtherEdge().getEndpoint());

          }
        }
      }


      int[] potentialEdgesNumberToReconstructPerVertex = new int[graph.getVertices().size()];
      boolean[][] potenrialEdgesToReconstructPerVertex = new boolean[graph.getVertices().size()][graph.getVertices().size()];
      List<Edge>[] potentialEdgesToReconstructSure = new List[graph.getVertices().size()];
      List<Edge>[] potentialEdgesToReconstructMaybe = new List[graph.getVertices().size()];
      boolean[] distanceFiveToAnotherPotentialVertex = new boolean[graph.getVertices().size()];


      missingEdges.stream()
              .filter(missingEdgeData -> !potenrialEdgesToReconstructPerVertex[missingEdgeData.getEdge().getOrigin().getVertexNo()][missingEdgeData.getEdge().getEndpoint().getVertexNo()])
              .forEach(missingEdgeData -> storePotentialEdgeToReconstruct(potentialEdgesNumberToReconstructPerVertex, potenrialEdgesToReconstructPerVertex, potentialEdgesToReconstructSure, missingEdgeData));

      for (MissingSquaresUniqueEdgesData noSquareAtAllMissingSquareWithoutNormallyColoredEdge : noSquareAtAllMissingSquaresWithoutNormallyColoredEdge)
      {
        Edge baseEdge = noSquareAtAllMissingSquareWithoutNormallyColoredEdge.getBaseEdge();
        Edge otherEdge = noSquareAtAllMissingSquareWithoutNormallyColoredEdge.getOtherEdge();
        for (Edge followingEdge : baseEdge.getEndpoint().getEdges())
        {
          if (followingEdge == baseEdge.getOpposite())
          {
            continue;
          }

          Edge missingEdge = new Edge(otherEdge.getEndpoint(), followingEdge.getEndpoint());
          MissingEdgeData missingEdgeData = new MissingEdgeData(missingEdge);
          storePotentialEdgeToReconstruct(potentialEdgesNumberToReconstructPerVertex, potenrialEdgesToReconstructPerVertex, potentialEdgesToReconstructMaybe, missingEdgeData);
        }
        int followingEdgesNumber = baseEdge.getEndpoint().getEdges().size() - 1;
        potentialEdgesNumberToReconstructPerVertex[otherEdge.getEndpoint().getVertexNo()] -= Math.max(0, followingEdgesNumber - 1);
      }

      if (CollectionUtils.isNotEmpty(noSquareAtAllEdgesWithDegreeOneAtEndpoint))
      {

        Edge arbitrarySingleEdgeWithSpecialColor = noSquareAtAllEdgesWithDegreeOneAtEndpoint.get(0);
        int[] distanceVectorFromArbitrarySingleEdgeWithSpecialColor = graphHelper.calculateDistanceVector(arbitrarySingleEdgeWithSpecialColor.getEndpoint());
        Vertex arbitrarySingleEdgeEndpointWithSpecialColor = arbitrarySingleEdgeWithSpecialColor.getEndpoint();


        List<Integer> properDistanceFromSpikeVertexNumbers = findVertexNumbersOfProperDistanceFromSpike(potentialEdgesNumberToReconstructPerVertex, distanceVectorFromArbitrarySingleEdgeWithSpecialColor);
        List<Integer> additionalPotentialEndpointNumbersToConnect = findVertexNumbersOfDistanceFiveFromSelectedSpike(distanceVectorFromArbitrarySingleEdgeWithSpecialColor, properDistanceFromSpikeVertexNumbers);

        includeVerticesOfDistanceFiveIntoPotentialEdgesToReconstruct(potentialEdgesNumberToReconstructPerVertex, potenrialEdgesToReconstructPerVertex, potentialEdgesToReconstructSure, distanceFiveToAnotherPotentialVertex, arbitrarySingleEdgeEndpointWithSpecialColor, additionalPotentialEndpointNumbersToConnect);

        List<Vertex> potentialVerticesToRemoveForResult = filterPotentialCorrectVertices(potentialEdgesNumberToReconstructPerVertex, arbitrarySingleEdgeEndpointWithSpecialColor, properDistanceFromSpikeVertexNumbers);

        potentialVerticesToRemoveForResult = filterOutMissingSquareEdgesVertices(missingSquareEdgesIncludedEndpoints, potentialVerticesToRemoveForResult);
        potentialVerticesToRemoveForResult = favorizeVerticesWithAllPotentialMissingEdgesSure(potentialEdgesNumberToReconstructPerVertex, potentialEdgesToReconstructSure, potentialVerticesToRemoveForResult);

        if (potentialVerticesToRemoveForResult.size() > 1)
        {
          vertexToRemoveForResult = selectFirstSpikeOutOfPotentialCorrectVertices(vertexToRemoveForResult, potentialVerticesToRemoveForResult);
        }
        else
        {
          vertexToRemoveForResult = potentialVerticesToRemoveForResult.get(0);
        }

        if (vertexToRemoveForResult == null)
        {
          continue;
        }

        List<Edge> baseResultEdges = collectResultFromSureEdges(potentialEdgesToReconstructSure[vertexToRemoveForResult.getVertexNo()]);

        boolean[] potentialResultIncludedEndpoints = new boolean[graph.getVertices().size()];
        baseResultEdges.forEach(edge -> potentialResultIncludedEndpoints[edge.getEndpoint().getVertexNo()] = true);

        addSpikesToResult(noSquareAtAllEdgesWithDegreeOneAtEndpoint, vertexToRemoveForResult, baseResultEdges, potentialResultIncludedEndpoints);
        addMissingSquareEdgesEndpointsToResult(vertexToRemoveForResult, missingSquareEdgesEndpoints, baseResultEdges, potentialResultIncludedEndpoints);

        boolean anyPotentialMaybyEdgeContainedInTheBaseResult =
                containsAnyEndpoints(baseResultEdges, potentialEdgesToReconstructMaybe[vertexToRemoveForResult.getVertexNo()]);
        if (CollectionUtils.isEmpty(potentialEdgesToReconstructMaybe[vertexToRemoveForResult.getVertexNo()])
                || anyPotentialMaybyEdgeContainedInTheBaseResult)
        {
          missingSquaresAnalyserCommons.checkSelectedEdgesCorrectness(baseResultEdges);
        }
        else
        {
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
      }
      else
      {
        Edge arbitraryNoSquareAtAllEdge = noSquareAtAllMissingSquares.get(0).getBaseEdge();

        NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo = new NoSquareAtAllCycleNode[graph.getVertices().size()];
        List<List<Edge>> groupedNoSquareAtAllEdges = new LinkedList<>();
        Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints = new Integer[graph.getVertices().size()];

        List<List<NoSquareAtAllCycleNode>> cycles = partOfCycleNoSquareAtAllMissingSquaresGeneralService.findCycleUsingBfs(
                arbitraryNoSquareAtAllEdge, groupedNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints,
                squareReconstructionData, noSquareAtAllCycleNodesByVertexNo);

        if (CollectionUtils.isNotEmpty(cycles))
        {
          List<NoSquareAtAllCycleNode> arbitraryCycle = cycles.get(0);

          List<Integer> vertexNumbersInCycle = arbitraryCycle.stream()
                  .map(node -> node.getVertex().getVertexNo())
                  .collect(Collectors.toList());

          List<Vertex> potentialCorrectVertices = filterPotentialCorrectVertices(potentialEdgesNumberToReconstructPerVertex, arbitraryNoSquareAtAllEdge.getEndpoint(), vertexNumbersInCycle);
          potentialCorrectVertices = filterOutMissingSquareEdgesVertices(missingSquareEdgesIncludedEndpoints, potentialCorrectVertices);
          potentialCorrectVertices = favorizeVerticesWithAllPotentialMissingEdgesSure(potentialEdgesNumberToReconstructPerVertex, potentialEdgesToReconstructSure, potentialCorrectVertices);

          if (potentialCorrectVertices.size() > 1)
          {
            if (testCaseContext.getVerticesToRemoveForResult().size() == potentialCorrectVertices.size())
            {
              vertexToRemoveForResult = potentialCorrectVertices.get(0);
            }
            else
            {
              //FIXME multiple potential correct vertices - use some cycle features
            }
          }
          else
          {
            vertexToRemoveForResult = potentialCorrectVertices.get(0);
          }

          if (vertexToRemoveForResult == null)
          {
            continue;
          }

          List<Edge> baseResultEdges = collectResultFromSureEdges(potentialEdgesToReconstructSure[vertexToRemoveForResult.getVertexNo()]);

          boolean[] potentialResultIncludedEndpoints = new boolean[graph.getVertices().size()];
          baseResultEdges.forEach(edge -> potentialResultIncludedEndpoints[edge.getEndpoint().getVertexNo()] = true);

          addMissingSquareEdgesEndpointsToResult(vertexToRemoveForResult, missingSquareEdgesEndpoints, baseResultEdges, potentialResultIncludedEndpoints);

          boolean anyPotentialMaybyEdgeContainedInTheBaseResult =
                  containsAnyEndpoints(baseResultEdges, potentialEdgesToReconstructMaybe[vertexToRemoveForResult.getVertexNo()]);
          if (CollectionUtils.isEmpty(potentialEdgesToReconstructMaybe[vertexToRemoveForResult.getVertexNo()])
                  || anyPotentialMaybyEdgeContainedInTheBaseResult)
          {
            missingSquaresAnalyserCommons.checkSelectedEdgesCorrectness(baseResultEdges);
          }
          else
          {
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

          this.getClass();

        }
        else
        {
          System.out.println("no spike or cycle");
          testCaseContext.isCorrectResult();
        }
      }

      if (testCaseContext.isCorrectResult())
      {
        break;
      }
    }

  }

  private void storePotentialEdgeToReconstruct(int[] potentialEdgesNumberToReconstructPerVertex, boolean[][] potenrialEdgesToReconstructPerVertex, List<Edge>[] potentialEdgesToReconstruct, MissingEdgeData missingEdgeData)
  {
    Edge missingEdge = missingEdgeData.getEdge();
    if (potenrialEdgesToReconstructPerVertex[missingEdge.getOrigin().getVertexNo()][missingEdge.getEndpoint().getVertexNo()])
    {
      return;
    }

    potentialEdgesNumberToReconstructPerVertex[missingEdge.getOrigin().getVertexNo()] += missingEdgeData.getNumberOfDistinctMissingSquareTriples();
    potentialEdgesNumberToReconstructPerVertex[missingEdge.getEndpoint().getVertexNo()] += missingEdgeData.getNumberOfDistinctMissingSquareTriples();
    potenrialEdgesToReconstructPerVertex[missingEdge.getOrigin().getVertexNo()][missingEdge.getEndpoint().getVertexNo()] = true;
    potenrialEdgesToReconstructPerVertex[missingEdge.getEndpoint().getVertexNo()][missingEdge.getOrigin().getVertexNo()] = true;

    Edge oppositeMissingEdge = new Edge(missingEdge.getEndpoint(), missingEdge.getOrigin());
    if (potentialEdgesToReconstruct[missingEdge.getOrigin().getVertexNo()] == null)
    {
      potentialEdgesToReconstruct[missingEdge.getOrigin().getVertexNo()] = new LinkedList<>();
    }
    potentialEdgesToReconstruct[missingEdge.getOrigin().getVertexNo()].add(missingEdge);

    if (potentialEdgesToReconstruct[missingEdge.getEndpoint().getVertexNo()] == null)
    {
      potentialEdgesToReconstruct[missingEdge.getEndpoint().getVertexNo()] = new LinkedList<>();
    }
    potentialEdgesToReconstruct[missingEdge.getEndpoint().getVertexNo()].add(oppositeMissingEdge);
  }

  private boolean containsAnyEndpoints(List<Edge> enclosingCollectionEdges, List<Edge> elementCollectionEdges)
  {
    if (CollectionUtils.isEmpty(elementCollectionEdges))
    {
      return true;
    }

    boolean[] includedEnclosingEdges = new boolean[graph.getVertices().size()];
    enclosingCollectionEdges.stream()
            .map(edge -> edge.getEndpoint().getVertexNo())
            .forEach(vertexNumber -> includedEnclosingEdges[vertexNumber] = true);

    boolean anyElementPresent = elementCollectionEdges.stream()
            .map(edge -> edge.getEndpoint().getVertexNo())
            .filter(vertexNumber -> includedEnclosingEdges[vertexNumber])
            .findAny().isPresent();

    return anyElementPresent;
  }

  private List<Integer> findVertexNumbersOfDistanceFiveFromSelectedSpike(int[] distanceVectorFromArbitrarySingleEdgeWithSpecialColor, List<Integer> properDistanceFromSpikeVertexNumbers)
  {
    return properDistanceFromSpikeVertexNumbers.stream()
            .filter(vertexNumber -> distanceVectorFromArbitrarySingleEdgeWithSpecialColor[vertexNumber] == 5)
            .collect(Collectors.toList());
  }

  private void includeVerticesOfDistanceFiveIntoPotentialEdgesToReconstruct(int[] potentialEdgesNumberToReconstructPerVertex, boolean[][] potenrialEdgesToReconstructPerVertex, List<Edge>[] potentialEdgesToReconstructSure, boolean[] distanceFiveToAnotherPotentialVertex, Vertex arbitrarySingleEdgeEndpointWithSpecialColor, List<Integer> additionalPotentialEndpointNumbersToConnect)
  {
    if (CollectionUtils.isNotEmpty(additionalPotentialEndpointNumbersToConnect))
    {
      distanceFiveToAnotherPotentialVertex[arbitrarySingleEdgeEndpointWithSpecialColor.getVertexNo()] = true;
      additionalPotentialEndpointNumbersToConnect.stream()
              .peek(additionalPotentialEndpointNumberToConnect -> distanceFiveToAnotherPotentialVertex[additionalPotentialEndpointNumberToConnect] = true)
              .map(additionalPotentialEndpointNumberToConnect -> new Edge(arbitrarySingleEdgeEndpointWithSpecialColor, graph.getVertices().get(additionalPotentialEndpointNumberToConnect)))
              .filter(potentialAdditionalEdge -> !potenrialEdgesToReconstructPerVertex[potentialAdditionalEdge.getOrigin().getVertexNo()][potentialAdditionalEdge.getEndpoint().getVertexNo()])
              .map(potentialAdditionalEdge -> new MissingEdgeData(potentialAdditionalEdge))
              .forEach(potentialAdditionalEdge -> storePotentialEdgeToReconstruct(potentialEdgesNumberToReconstructPerVertex, potenrialEdgesToReconstructPerVertex, potentialEdgesToReconstructSure, potentialAdditionalEdge));
    }
  }

  private List<Integer> findVertexNumbersOfProperDistanceFromSpike(int[] potentialEdgesNumberToReconstructPerVertex, int[] distanceVectorFromArbitrarySingleEdgeWithSpecialColor)
  {
    return IntStream.range(0, graph.getVertices().size())
            .filter(vertexNumber -> potentialEdgesNumberToReconstructPerVertex[vertexNumber] != 0)
            .filter(vertexNumber -> distanceVectorFromArbitrarySingleEdgeWithSpecialColor[vertexNumber] == 3
                    || distanceVectorFromArbitrarySingleEdgeWithSpecialColor[vertexNumber] == 5)
            .mapToObj(Integer::valueOf)
            .collect(Collectors.toList());
  }

  private List<Vertex> filterPotentialCorrectVertices(int[] potentialEdgesNumberToReconstructPerVertex, Vertex arbitraryVertex, List<Integer> preselectedVertexNumbers)
  {
    int maxPotentialEdgesNumberToReconstruct = potentialEdgesNumberToReconstructPerVertex[arbitraryVertex.getVertexNo()];
    boolean[] potentialVerticesIncludedToRemoveForResult = new boolean[graph.getVertices().size()];
    List<Vertex> potentialVerticesToRemoveForResult = new LinkedList<>(Arrays.asList(arbitraryVertex));
    potentialVerticesIncludedToRemoveForResult[arbitraryVertex.getVertexNo()] = true;

    for (Integer properDistanceFromSpikeVertexNumber : preselectedVertexNumbers)
    {
      Vertex vertexOfProperDistanceFromSpike = graph.getVertices().get(properDistanceFromSpikeVertexNumber);
      if (potentialVerticesIncludedToRemoveForResult[vertexOfProperDistanceFromSpike.getVertexNo()])
      {
        continue;
      }

      int potentialEdgesNumberToReconstruct = potentialEdgesNumberToReconstructPerVertex[properDistanceFromSpikeVertexNumber];
      if (maxPotentialEdgesNumberToReconstruct < potentialEdgesNumberToReconstruct)
      {
        maxPotentialEdgesNumberToReconstruct = potentialEdgesNumberToReconstruct;
        potentialVerticesToRemoveForResult = new LinkedList<>(Arrays.asList(vertexOfProperDistanceFromSpike));
      }
      else if (maxPotentialEdgesNumberToReconstruct == potentialEdgesNumberToReconstruct)
      {
        potentialVerticesToRemoveForResult.add(vertexOfProperDistanceFromSpike);
      }
    }
    return potentialVerticesToRemoveForResult;
  }

  private List<Vertex> filterOutMissingSquareEdgesVertices(boolean[] missingSquareEdgesIncludedEndpoints, List<Vertex> potentialVerticesToRemoveForResult)
  {
    if (potentialVerticesToRemoveForResult.size() > 1)
    {
      potentialVerticesToRemoveForResult = potentialVerticesToRemoveForResult.stream()
              .filter(vertex -> !missingSquareEdgesIncludedEndpoints[vertex.getVertexNo()])
              .collect(Collectors.toList());
    }
    return potentialVerticesToRemoveForResult;
  }

  private List<Vertex> favorizeVerticesWithAllPotentialMissingEdgesSure(int[] potentialEdgesNumberToReconstructPerVertex, List<Edge>[] potentialEdgesToReconstructSure, List<Vertex> potentialVerticesToRemoveForResult)
  {
    if (potentialVerticesToRemoveForResult.size() > 1)
    {
      List<Vertex> potentialNoMaybeEndpoints = potentialVerticesToRemoveForResult.stream()
              .filter(vertex -> CollectionUtils.isNotEmpty(potentialEdgesToReconstructSure[vertex.getVertexNo()]))
              .filter(vertex -> potentialEdgesNumberToReconstructPerVertex[vertex.getVertexNo()] == potentialEdgesToReconstructSure[vertex.getVertexNo()].size())
              .collect(Collectors.toList());
      if (CollectionUtils.isNotEmpty(potentialNoMaybeEndpoints))
      {
        potentialVerticesToRemoveForResult = potentialNoMaybeEndpoints;
      }
    }
    return potentialVerticesToRemoveForResult;
  }

  private Vertex selectFirstSpikeOutOfPotentialCorrectVertices(Vertex vertexToRemoveForResult, List<Vertex> potentialVerticesToRemoveForResult)
  {
    List<Vertex> potentialSpikeEndpoints = potentialVerticesToRemoveForResult.stream()
            .filter(vertex -> vertex.getEdges().size() == 1)
            .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(potentialSpikeEndpoints))
    {
      vertexToRemoveForResult = potentialSpikeEndpoints.get(0);
    }
    return vertexToRemoveForResult;
  }

  private List<Edge> collectResultFromSureEdges(List<Edge> sureEdges)
  {
    List<Edge> baseResultEdges = new LinkedList<>();
    if (CollectionUtils.isNotEmpty(sureEdges))
    {
      baseResultEdges.addAll(sureEdges);
    }
    return baseResultEdges;
  }

  private void addSpikesToResult(List<Edge> noSquareAtAllEdgesWithDegreeOneAtEndpoint, Vertex vertexToRemoveForResult, List<Edge> baseResultEdges, boolean[] potentialResultIncludedEndpoints)
  {
    for (Edge spike : noSquareAtAllEdgesWithDegreeOneAtEndpoint)
    {
      Vertex spikeEndpoint = spike.getEndpoint();
      if (vertexToRemoveForResult != spikeEndpoint
              && !potentialResultIncludedEndpoints[spikeEndpoint.getVertexNo()])
      {
        Edge missingSquarePotentialResultEdge = new Edge(vertexToRemoveForResult, spikeEndpoint);
        baseResultEdges.add(missingSquarePotentialResultEdge);
        potentialResultIncludedEndpoints[spikeEndpoint.getVertexNo()] = true;
      }
    }
  }

  private void addMissingSquareEdgesEndpointsToResult(Vertex vertexToRemoveForResult, List<Vertex> missingSquareEdgesEndpoints, List<Edge> baseResultEdges, boolean[] potentialResultIncludedEndpoints)
  {
    for (Vertex missingSquareEdgesEndpoint : missingSquareEdgesEndpoints)
    {
      if (!potentialResultIncludedEndpoints[missingSquareEdgesEndpoint.getVertexNo()])
      {
        Edge missingSquarePotentialResultEdge = new Edge(vertexToRemoveForResult, missingSquareEdgesEndpoint);
        baseResultEdges.add(missingSquarePotentialResultEdge);
        potentialResultIncludedEndpoints[missingSquareEdgesEndpoint.getVertexNo()] = true;
      }
    }
  }
}
