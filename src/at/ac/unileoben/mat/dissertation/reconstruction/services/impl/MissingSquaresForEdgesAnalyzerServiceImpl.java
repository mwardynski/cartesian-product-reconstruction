package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Profile("missingEdges")
public class MissingSquaresForEdgesAnalyzerServiceImpl extends AbstractMissingSquareAnalyzerService
{

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  GraphHelper graphHelper;

  @Override
  public void analyseMissingSquares(SquareReconstructionData squareReconstructionData, SquareMatchingEdgeData[][] squareMatchingEdges)
  {
    List<MissingSquaresEntryData> missingSquaresEntries = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntries();

    List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor = new List[graph.getGraphColoring().getColorsMapping().size()];

    groupMissingSquareEntries(missingSquaresEntries, noSquareAtAllMissingSquares, irregularMissingSquaresByColor);

    if (CollectionUtils.isNotEmpty(noSquareAtAllMissingSquares))
    {
      findResultForSpeciallyColoredEdges(irregularMissingSquaresByColor, noSquareAtAllMissingSquares);


//      if (CollectionUtils.isNotEmpty(noSquareAtAllEdgesWithDegreeOneAtEndpoint))
//
//      {
//        System.out.println("special coloring");
//      }
//      testCaseContext.setCorrectResult(true);
    }
    else if (CollectionUtils.isNotEmpty(squareReconstructionData.getNoticedPostponedVertices()))
    {
      findResultForColoringIncludingNewColorsAfterPostponedVertex(squareReconstructionData, irregularMissingSquaresByColor);
    }
    else
    {
      findResultForTypicalColoring(irregularMissingSquaresByColor);
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

  private void findResultForSpeciallyColoredEdges(List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor, List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares)
  {
    List<Edge> noSquareAtAllEdgesWithDegreeOneAtEndpoint = findNoSquareAtAllEdgesWithDegreeOneAtEndpoint(noSquareAtAllMissingSquares);

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

    Vertex vertexToRemoveForResult = handleSpikesSpecialCases(noSquareAtAllEdgesWithDegreeOneAtEndpoint);
    checkSelectedVertexCorrectness(vertexToRemoveForResult);
    if (testCaseContext.isCorrectResult())
    {
      return;
    }

    for (Integer selectedColor : graph.getGraphColoring().getActualColors())
    {
      if (selectedColor == 0)
      {
        continue;
      }


//      if (CollectionUtils.isEmpty(irregularMissingSquaresByColor[selectedColor]))
//      {
//        continue;
//      }

      List<Edge> missingEdges = new LinkedList<>();
      List<MissingSquaresUniqueEdgesData> missingSquareEdges = new LinkedList<>();
      boolean[][] collectedMissingEdgesArray = new boolean[graph.getVertices().size()][graph.getVertices().size()];

      Edge missingEdgesWarden = new Edge(null, null);

      List<MissingSquaresUniqueEdgesData> missingSquaresToProcess = new LinkedList<>(irregularMissingSquaresByColor[selectedColor]);
      missingSquaresToProcess.addAll(noSquareAtAllMissingSquaresWithNormallyColoredEdge);
      Edge[][] missingSquarePairsForSelectedColor = findMissingSquarePairsForSelectedColor(missingSquaresToProcess, missingEdgesWarden);
      collectMissingEdgesForSelectedColor(missingSquaresToProcess, missingEdges, missingSquareEdges, missingSquarePairsForSelectedColor, collectedMissingEdgesArray, missingEdgesWarden);


      int[] potentialEdgesNumberToReconstructPerVertex = new int[graph.getVertices().size()];
      boolean[][] potenrialEdgesToReconstructPerVertex = new boolean[graph.getVertices().size()][graph.getVertices().size()];
      Set<Edge>[] potentialEdgesToReconstruct = new Set[graph.getVertices().size()];


      missingEdges.stream()
              .filter(missingEdge -> !potenrialEdgesToReconstructPerVertex[missingEdge.getOrigin().getVertexNo()][missingEdge.getEndpoint().getVertexNo()])
              .forEach(missingEdge -> storePotentialEdgeToReconstruct(potentialEdgesNumberToReconstructPerVertex, potenrialEdgesToReconstructPerVertex, potentialEdgesToReconstruct, missingEdge));

      for (MissingSquaresUniqueEdgesData noSquareAtAllMissingSquare : noSquareAtAllMissingSquaresWithoutNormallyColoredEdge)
      {
        Edge baseEdge = noSquareAtAllMissingSquare.getBaseEdge();
        Edge otherEdge = noSquareAtAllMissingSquare.getOtherEdge();
        for (Edge followingEdge : baseEdge.getEndpoint().getEdges())
        {
          if (followingEdge == baseEdge.getOpposite())
          {
            continue;
          }

          Edge missingEdge = new Edge(otherEdge.getEndpoint(), followingEdge.getEndpoint());
          storePotentialEdgeToReconstruct(potentialEdgesNumberToReconstructPerVertex, potenrialEdgesToReconstructPerVertex, potentialEdgesToReconstruct, missingEdge);
        }
        int followingEdgesNumber = baseEdge.getEndpoint().getEdges().size() - 1;
        potentialEdgesNumberToReconstructPerVertex[otherEdge.getEndpoint().getVertexNo()] -= Math.max(0, followingEdgesNumber - 1);
      }

      if (CollectionUtils.isNotEmpty(noSquareAtAllEdgesWithDegreeOneAtEndpoint))
      {

        Edge arbitrarySingleEdgeWithSpecialColor = noSquareAtAllEdgesWithDegreeOneAtEndpoint.get(0);
        int[] distanceVectorFromArbitrarySingleEdgeWithSpecialColor = graphHelper.calculateDistanceVector(arbitrarySingleEdgeWithSpecialColor.getEndpoint());
        Vertex arbitrarySingleEdgeEndpointWithSpecialColor = arbitrarySingleEdgeWithSpecialColor.getEndpoint();


        List<Integer> properDistanceFromSpikeVertexNumbers = IntStream.range(0, graph.getVertices().size())
                .filter(vertexNumber -> potentialEdgesNumberToReconstructPerVertex[vertexNumber] != 0)
                .filter(vertexNumber -> distanceVectorFromArbitrarySingleEdgeWithSpecialColor[vertexNumber] == 3
                        || distanceVectorFromArbitrarySingleEdgeWithSpecialColor[vertexNumber] == 5)
                .mapToObj(Integer::valueOf)
                .collect(Collectors.toList());

        int additionalPotentialEdgesNumberToReconstruct = 0;
        if (arbitrarySingleEdgeWithSpecialColor.getOrigin().getEdges().size() > 2)
        {
          additionalPotentialEdgesNumberToReconstruct = (int) properDistanceFromSpikeVertexNumbers.stream()
                  .filter(vertexNumber -> distanceVectorFromArbitrarySingleEdgeWithSpecialColor[vertexNumber] == 5)
                  .count();
        }

        int maxPotentialEdgesNumberToReconstruct = potentialEdgesNumberToReconstructPerVertex[arbitrarySingleEdgeEndpointWithSpecialColor.getVertexNo()] + additionalPotentialEdgesNumberToReconstruct;
        List<Vertex> potentialVerticesToRemoveForResult = new LinkedList<>(Arrays.asList(arbitrarySingleEdgeEndpointWithSpecialColor));

        for (Integer properDistanceFromSpikeVertexNumber : properDistanceFromSpikeVertexNumbers)
        {
          Vertex properDistanceFromSpikeVertex = graph.getVertices().get(properDistanceFromSpikeVertexNumber);

          int potentialEdgesNumberToReconstruct = potentialEdgesNumberToReconstructPerVertex[properDistanceFromSpikeVertexNumber];
          if (distanceVectorFromArbitrarySingleEdgeWithSpecialColor[properDistanceFromSpikeVertexNumber] == 5)
          {
            potentialEdgesNumberToReconstruct++;
          }

          if (maxPotentialEdgesNumberToReconstruct < potentialEdgesNumberToReconstruct)
          {
            maxPotentialEdgesNumberToReconstruct = potentialEdgesNumberToReconstruct;
            potentialVerticesToRemoveForResult = new LinkedList<>(Arrays.asList(properDistanceFromSpikeVertex));
          }
          else if (maxPotentialEdgesNumberToReconstruct == potentialEdgesNumberToReconstruct)
          {
            potentialVerticesToRemoveForResult.add(properDistanceFromSpikeVertex);
          }
        }

        if (potentialVerticesToRemoveForResult.size() > 1)
        {
          List<Vertex> potentialSpikeEndpoints = potentialVerticesToRemoveForResult.stream()
                  .filter(vertex -> vertex.getEdges().size() == 1)
                  .collect(Collectors.toList());
          if (CollectionUtils.isNotEmpty(potentialSpikeEndpoints))
          {
            vertexToRemoveForResult = potentialSpikeEndpoints.get(0);
          }
        }
        else
        {
          vertexToRemoveForResult = potentialVerticesToRemoveForResult.get(0);
        }
        checkSelectedVertexCorrectness(vertexToRemoveForResult);
      }
      else
      {
        System.out.println("no spikes");
        testCaseContext.setCorrectResult(true);
      }

      if (testCaseContext.isCorrectResult())
      {
        break;
      }
    }

  }

  private Vertex handleSpikesSpecialCases(List<Edge> noSquareAtAllEdgesWithDegreeOneAtEndpoint)
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

  private void checkSelectedVertexCorrectness(Vertex vertexToRemoveForResult)
  {
    if (vertexToRemoveForResult == null)
    {
      return;
    }
    Integer vertexNumberToRemoveForResult = graph.getReverseReindexArray()[vertexToRemoveForResult.getVertexNo()];
    boolean correctResult = testCaseContext.getVerticesToRemoveForResult().stream()
            .filter(acceptableVertex -> acceptableVertex.getVertexNo() == vertexNumberToRemoveForResult)
            .findAny().isPresent();
    testCaseContext.setCorrectResult(correctResult);
  }

  private void storePotentialEdgeToReconstruct(int[] potentialEdgesNumberToReconstructPerVertex, boolean[][] potenrialEdgesToReconstructPerVertex, Set<Edge>[] potentialEdgesToReconstruct, Edge missingEdge)
  {
    if (potenrialEdgesToReconstructPerVertex[missingEdge.getOrigin().getVertexNo()][missingEdge.getEndpoint().getVertexNo()])
    {
      return;
    }

    potentialEdgesNumberToReconstructPerVertex[missingEdge.getOrigin().getVertexNo()]++;
    potentialEdgesNumberToReconstructPerVertex[missingEdge.getEndpoint().getVertexNo()]++;
    potenrialEdgesToReconstructPerVertex[missingEdge.getOrigin().getVertexNo()][missingEdge.getEndpoint().getVertexNo()] = true;
    potenrialEdgesToReconstructPerVertex[missingEdge.getEndpoint().getVertexNo()][missingEdge.getOrigin().getVertexNo()] = true;

    Edge oppositeMissingEdge = new Edge(missingEdge.getEndpoint(), missingEdge.getOrigin());
    if (potentialEdgesToReconstruct[missingEdge.getOrigin().getVertexNo()] == null)
    {
      potentialEdgesToReconstruct[missingEdge.getOrigin().getVertexNo()] = new HashSet<>();
    }
    potentialEdgesToReconstruct[missingEdge.getOrigin().getVertexNo()].add(missingEdge);

    if (potentialEdgesToReconstruct[missingEdge.getEndpoint().getVertexNo()] == null)
    {
      potentialEdgesToReconstruct[missingEdge.getEndpoint().getVertexNo()] = new HashSet<>();
    }
    potentialEdgesToReconstruct[missingEdge.getEndpoint().getVertexNo()].add(oppositeMissingEdge);
  }


  private List<Edge> findNoSquareAtAllEdgesWithDegreeOneAtEndpoint(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares)
  {
    List<Edge> noSquareAtAllEdgesWithDegreeOneAtEndpoint = noSquareAtAllMissingSquares.stream()
            .map(missingSquare -> missingSquare.getBaseEdge())
            .filter(edge -> edge.getEndpoint().getEdges().size() == 1)
            .collect(Collectors.toList());
    return noSquareAtAllEdgesWithDegreeOneAtEndpoint;
  }

  private void findResultForColoringIncludingNewColorsAfterPostponedVertex(SquareReconstructionData squareReconstructionData, List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    int afterPostponedVertexColorsLowestIndex = squareReconstructionData.getGraphColoringBeforePostponedVertices().getActualColors().size();

    List<Integer> afterPostponedVertexColors = graph.getGraphColoring().getActualColors().stream()
            .filter(color -> color >= afterPostponedVertexColorsLowestIndex)
            .collect(Collectors.toList());


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
        findResultForIrregularMissingSquaresByColor(selectedColor, currentIrregularMissingSquaresByColor);
        if (testCaseContext.isCorrectResult())
        {
          break selectedColorLoop;
        }
      }
    }
  }

  private void findResultForTypicalColoring(List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    for (Integer selectedColor : graph.getGraphColoring().getActualColors())
    {
      if (selectedColor == 0)
      {
        continue;
      }

      findResultForIrregularMissingSquaresByColor(selectedColor, irregularMissingSquaresByColor);
      if (testCaseContext.isCorrectResult())
      {
        break;
      }
    }
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

  private void findResultForIrregularMissingSquaresByColor(int selectedColor, List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    if (CollectionUtils.isEmpty(irregularMissingSquaresByColor[selectedColor]))
    {
      return;
    }

    List<Edge> missingEdges = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData> missingSquareEdges = new LinkedList<>();
    boolean[][] collectedMissingEdgesArray = new boolean[graph.getVertices().size()][graph.getVertices().size()];

    Edge missingEdgesWarden = new Edge(null, null);
    Edge[][] missingSquarePairsForSelectedColor = findMissingSquarePairsForSelectedColor(irregularMissingSquaresByColor[selectedColor], missingEdgesWarden);
    collectMissingEdgesForSelectedColor(irregularMissingSquaresByColor[selectedColor], missingEdges, missingSquareEdges, missingSquarePairsForSelectedColor, collectedMissingEdgesArray, missingEdgesWarden);
    convertMissingSquaresToMissingEdges(missingEdges, missingSquareEdges, collectedMissingEdgesArray);

    boolean[][] reconstructedEdgesArray = new boolean[graph.getVertices().size()][graph.getVertices().size()];
    for (Edge missingEdge : missingEdges)
    {
      Integer originVertexNo = graph.getReverseReindexArray()[missingEdge.getOrigin().getVertexNo()];
      Integer endpointVertexNo = graph.getReverseReindexArray()[missingEdge.getEndpoint().getVertexNo()];

      reconstructedEdgesArray[originVertexNo][endpointVertexNo] = true;
      reconstructedEdgesArray[endpointVertexNo][originVertexNo] = true;
    }

    boolean correctResult = testCaseContext.getRemovedEdges().size() == missingEdges.size();
    for (Edge removedEdge : testCaseContext.getRemovedEdges())
    {
      if (!reconstructedEdgesArray[removedEdge.getOrigin().getVertexNo()][removedEdge.getEndpoint().getVertexNo()])
      {
        correctResult = false;
      }
    }
    if (correctResult)
    {
      testCaseContext.setCorrectResult(true);
    }
  }

  private void collectMissingEdgesForSelectedColor(List<MissingSquaresUniqueEdgesData> selectedIrregularMissingSquaresByColor,
                                                   List<Edge> missingEdges, List<MissingSquaresUniqueEdgesData> missingSquareEdges,
                                                   Edge[][] missingSquarePairsForSelectedColor,
                                                   boolean[][] collectedMissingEdgesArray, Edge missingEdgesWarden)
  {
    for (MissingSquaresUniqueEdgesData missingSquare : selectedIrregularMissingSquaresByColor)
    {
      Edge baseEdge = missingSquare.getBaseEdge();
      Edge otherEdge = missingSquare.getOtherEdge();

      Edge matchingEdge = missingSquarePairsForSelectedColor[baseEdge.getEndpoint().getVertexNo()][baseEdge.getOrigin().getVertexNo()];
      Vertex edgeEndpoint = otherEdge.getEndpoint();
      if (matchingEdge == null || matchingEdge == missingEdgesWarden)
      {
        matchingEdge = missingSquarePairsForSelectedColor[otherEdge.getEndpoint().getVertexNo()][otherEdge.getOrigin().getVertexNo()];
        edgeEndpoint = baseEdge.getEndpoint();
      }
      if (matchingEdge != null && matchingEdge != missingEdgesWarden
              && !collectedMissingEdgesArray[edgeEndpoint.getVertexNo()][matchingEdge.getEndpoint().getVertexNo()])
      {
        Edge missingEdge = new Edge(edgeEndpoint, matchingEdge.getEndpoint());
        missingEdges.add(missingEdge);

        collectedMissingEdgesArray[edgeEndpoint.getVertexNo()][matchingEdge.getEndpoint().getVertexNo()] = true;
        collectedMissingEdgesArray[matchingEdge.getEndpoint().getVertexNo()][edgeEndpoint.getVertexNo()] = true;
      }
      else if (matchingEdge == null)
      {
        missingSquareEdges.add(missingSquare);
      }
    }
  }

  private Edge[][] findMissingSquarePairsForSelectedColor(List<MissingSquaresUniqueEdgesData> selectedIrregularMissingSquaresByColor, Edge missingEdgesWarden)
  {
    Edge[][] oneEdgeByOtherEdge = new Edge[graph.getVertices().size()][graph.getVertices().size()];

    for (MissingSquaresUniqueEdgesData missingSquare : selectedIrregularMissingSquaresByColor)
    {
      Edge baseEdge = missingSquare.getBaseEdge();
      Edge otherEdge = missingSquare.getOtherEdge();

      storeOneEdgeByOtherEdge(oneEdgeByOtherEdge, baseEdge, otherEdge, missingEdgesWarden);
      storeOneEdgeByOtherEdge(oneEdgeByOtherEdge, otherEdge, baseEdge, missingEdgesWarden);
    }
    return oneEdgeByOtherEdge;
  }

  private void storeOneEdgeByOtherEdge(Edge[][] oneEdgeByOtherEdge, Edge baseEdge, Edge otherEdge, Edge missingEdgesWarden)
  {
    if (baseEdge.getLabel().getColor() == 0)
    {
      return;
    }

    int edgeOriginNo = baseEdge.getOrigin().getVertexNo();
    int edgeEndpointNo = baseEdge.getEndpoint().getVertexNo();
    if (oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] == null)
    {
      oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] = otherEdge;
    }
    else
    {
      oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] = missingEdgesWarden;
    }
  }

  private void convertMissingSquaresToMissingEdges(List<Edge> missingEdges, List<MissingSquaresUniqueEdgesData> missingSquareEdges, boolean[][] collectedMissingEdgesArray)
  {
    if (CollectionUtils.isNotEmpty(missingEdges) && CollectionUtils.isNotEmpty(missingSquareEdges))
    {
      Vertex vertexOfMissingEdges;
      Edge firstMissingEdge = missingEdges.get(0);
      if (missingEdges.size() > 1)
      {
        Edge secondMissingEdge = missingEdges.get(1);
        if (firstMissingEdge.getOrigin() == secondMissingEdge.getOrigin() || firstMissingEdge.getOrigin() == secondMissingEdge.getEndpoint())
        {
          vertexOfMissingEdges = firstMissingEdge.getOrigin();
        }
        else
        {
          vertexOfMissingEdges = firstMissingEdge.getEndpoint();
        }
      }
      else
      {
        MissingSquaresUniqueEdgesData firstDoubleMissingEdge = missingSquareEdges.get(0);
        if (firstMissingEdge.getOrigin() == firstDoubleMissingEdge.getBaseEdge().getEndpoint()
                || firstMissingEdge.getOrigin() == firstDoubleMissingEdge.getOtherEdge().getEndpoint())
        {
          vertexOfMissingEdges = firstMissingEdge.getEndpoint();
        }
        else
        {
          vertexOfMissingEdges = firstMissingEdge.getOrigin();
        }
      }

      missingSquareEdges.forEach(
              missingSquare ->
              {
                saveEdgeToMissingEdges(missingSquare.getBaseEdge(), vertexOfMissingEdges, missingEdges, collectedMissingEdgesArray);
                saveEdgeToMissingEdges(missingSquare.getOtherEdge(), vertexOfMissingEdges, missingEdges, collectedMissingEdgesArray);
              });
    }
  }

  private void saveEdgeToMissingEdges(Edge edge, Vertex vertexOfMissingEdges, List<Edge> missingEdges, boolean[][] collectedMissingEdgesArray)
  {
    if (!collectedMissingEdgesArray[vertexOfMissingEdges.getVertexNo()][edge.getEndpoint().getVertexNo()])
    {
      missingEdges.add(new Edge(vertexOfMissingEdges, edge.getEndpoint()));
      collectedMissingEdgesArray[vertexOfMissingEdges.getVertexNo()][edge.getEndpoint().getVertexNo()] = true;
    }
  }
}
