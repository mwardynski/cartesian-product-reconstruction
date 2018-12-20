package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges;

import at.ac.unileoben.mat.dissertation.common.GraphHelper;
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

  public void findResultForSpeciallyColoredEdges(List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor, List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares)
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

      List<Edge> missingEdges = new LinkedList<>();
      List<MissingSquaresUniqueEdgesData> missingSquareEdges = new LinkedList<>();
      boolean[][] collectedMissingEdgesArray = new boolean[graph.getVertices().size()][graph.getVertices().size()];

      Edge missingEdgesWarden = new Edge(null, null);

      List<MissingSquaresUniqueEdgesData> missingSquaresToProcess = new LinkedList<>(irregularMissingSquaresByColor[selectedColor]);
      missingSquaresToProcess.addAll(noSquareAtAllMissingSquaresWithNormallyColoredEdge);
      Edge[][] missingSquarePairsForSelectedColor = missingSquaresAnalyserCommons.findMissingSquarePairsForSelectedColor(missingSquaresToProcess, missingEdgesWarden);
      missingSquaresAnalyserCommons.collectMissingEdgesForSelectedColor(missingSquaresToProcess, missingEdges, missingSquareEdges, missingSquarePairsForSelectedColor, collectedMissingEdgesArray, missingEdgesWarden);


      int[] potentialEdgesNumberToReconstructPerVertex = new int[graph.getVertices().size()];
      boolean[][] potenrialEdgesToReconstructPerVertex = new boolean[graph.getVertices().size()][graph.getVertices().size()];
      List<Edge>[] potentialEdgesToReconstructSure = new List[graph.getVertices().size()];
      List<Edge>[] potentialEdgesToReconstructMaybe = new List[graph.getVertices().size()];
      boolean[] distanceFiveToAnotherPotentialVertex = new boolean[graph.getVertices().size()];


      missingEdges.stream()
              .filter(missingEdge -> !potenrialEdgesToReconstructPerVertex[missingEdge.getOrigin().getVertexNo()][missingEdge.getEndpoint().getVertexNo()])
              .forEach(missingEdge -> storePotentialEdgeToReconstruct(potentialEdgesNumberToReconstructPerVertex, potenrialEdgesToReconstructPerVertex, potentialEdgesToReconstructSure, missingEdge));

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
          storePotentialEdgeToReconstruct(potentialEdgesNumberToReconstructPerVertex, potenrialEdgesToReconstructPerVertex, potentialEdgesToReconstructMaybe, missingEdge);
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

        List<Integer> additionalPotentialEndpointNumbersToConnect = properDistanceFromSpikeVertexNumbers.stream()
                .filter(vertexNumber -> distanceVectorFromArbitrarySingleEdgeWithSpecialColor[vertexNumber] == 5)
                .collect(Collectors.toList());

        if (CollectionUtils.isNotEmpty(additionalPotentialEndpointNumbersToConnect))
        {
          distanceFiveToAnotherPotentialVertex[arbitrarySingleEdgeEndpointWithSpecialColor.getVertexNo()] = true;
          additionalPotentialEndpointNumbersToConnect.stream()
                  .peek(additionalPotentialEndpointNumberToConnect -> distanceFiveToAnotherPotentialVertex[additionalPotentialEndpointNumberToConnect] = true)
                  .map(additionalPotentialEndpointNumberToConnect -> new Edge(arbitrarySingleEdgeEndpointWithSpecialColor, graph.getVertices().get(additionalPotentialEndpointNumberToConnect)))
                  .filter(potentialAdditionalEdge -> !potenrialEdgesToReconstructPerVertex[potentialAdditionalEdge.getOrigin().getVertexNo()][potentialAdditionalEdge.getEndpoint().getVertexNo()])
                  .forEach(potentialAdditionalEdge -> storePotentialEdgeToReconstruct(potentialEdgesNumberToReconstructPerVertex, potenrialEdgesToReconstructPerVertex, potentialEdgesToReconstructSure, potentialAdditionalEdge));

          if (CollectionUtils.isNotEmpty(potentialEdgesToReconstructMaybe[arbitrarySingleEdgeEndpointWithSpecialColor.getVertexNo()]))
          {
            potentialEdgesToReconstructMaybe[arbitrarySingleEdgeEndpointWithSpecialColor.getVertexNo()] = null;
            potentialEdgesNumberToReconstructPerVertex[arbitrarySingleEdgeEndpointWithSpecialColor.getVertexNo()]--;
          }
        }

        int maxPotentialEdgesNumberToReconstruct = potentialEdgesNumberToReconstructPerVertex[arbitrarySingleEdgeEndpointWithSpecialColor.getVertexNo()];
        List<Vertex> potentialVerticesToRemoveForResult = new LinkedList<>(Arrays.asList(arbitrarySingleEdgeEndpointWithSpecialColor));

        for (Integer properDistanceFromSpikeVertexNumber : properDistanceFromSpikeVertexNumbers)
        {
          Vertex vertexOfProperDistanceFromSpike = graph.getVertices().get(properDistanceFromSpikeVertexNumber);

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
        missingSquaresAnalyserCommons.checkSelectedVertexCorrectness(vertexToRemoveForResult);

        if (testCaseContext.isCorrectResult())
        {
          this.getClass();
        }
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

  private void storePotentialEdgeToReconstruct(int[] potentialEdgesNumberToReconstructPerVertex, boolean[][] potenrialEdgesToReconstructPerVertex, List<Edge>[] potentialEdgesToReconstruct, Edge missingEdge)
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
      potentialEdgesToReconstruct[missingEdge.getOrigin().getVertexNo()] = new LinkedList<>();
    }
    potentialEdgesToReconstruct[missingEdge.getOrigin().getVertexNo()].add(missingEdge);

    if (potentialEdgesToReconstruct[missingEdge.getEndpoint().getVertexNo()] == null)
    {
      potentialEdgesToReconstruct[missingEdge.getEndpoint().getVertexNo()] = new LinkedList<>();
    }
    potentialEdgesToReconstruct[missingEdge.getEndpoint().getVertexNo()].add(oppositeMissingEdge);
  }
}
