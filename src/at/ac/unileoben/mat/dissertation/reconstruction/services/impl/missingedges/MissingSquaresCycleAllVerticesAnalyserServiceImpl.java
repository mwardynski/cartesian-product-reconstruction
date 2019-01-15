package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges;

import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MissingSquaresCycleAllVerticesAnalyserServiceImpl
{

  @Autowired
  Graph graph;

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  MissingSquaresAnalyserCommons missingSquaresAnalyserCommons;

  @Autowired
  PartOfCycleNoSquareAtAllMissingSquaresFindingService partOfCycleNoSquareAtAllMissingSquaresGeneralService;

  public List<List<NoSquareAtAllCycleNode>> findMissingEdges(Edge arbitraryNoSquareAtAllEdge, SquareReconstructionData squareReconstructionData, int recursiveLevelsToGo)
  {
    NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo = new NoSquareAtAllCycleNode[graph.getVertices().size()];
    List<List<Edge>> groupedNoSquareAtAllEdges = new LinkedList<>();
    Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints = new Integer[graph.getVertices().size()];

    List<List<NoSquareAtAllCycleNode>> cycles = partOfCycleNoSquareAtAllMissingSquaresGeneralService.findCycleUsingBfs(
            arbitraryNoSquareAtAllEdge, groupedNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints,
            squareReconstructionData, noSquareAtAllCycleNodesByVertexNo);

    if (CollectionUtils.isEmpty(cycles))
    {
      return cycles;
    }

    List<NoSquareAtAllCycleNode> cycleVertexNodes = new ArrayList<>();
    Integer[] cycleVerticesIncluded = new Integer[graph.getVertices().size()];

    collectCycleVertices(cycles, cycleVertexNodes, cycleVerticesIncluded);

    List<Vertex> oddDistanceVertices = new LinkedList<>();
    List<Vertex> evenDistanceVertices = new LinkedList<>();
    partitionVerticesIntoOddAndEvenDistance(cycleVertexNodes, oddDistanceVertices, evenDistanceVertices);

    List<Vertex> additionalPotentialResultVerticesForOddDistance = new LinkedList<>();
    List<Vertex> additionalPotentialResultVerticesForEvenDistance = new LinkedList<>();
    collectAdditionalPotentialResultVertices(additionalPotentialResultVerticesForOddDistance,
            additionalPotentialResultVerticesForEvenDistance,
            cycleVerticesIncluded,
            noSquareAtAllCycleNodesByVertexNo,
            squareReconstructionData);

    checkPotentialReconstructions(oddDistanceVertices, evenDistanceVertices,
            additionalPotentialResultVerticesForOddDistance,
            additionalPotentialResultVerticesForEvenDistance);

    runMissingEdgesFindingAgainForDifferentCycles(cycles, squareReconstructionData, recursiveLevelsToGo);
    return cycles;
  }

  private void collectCycleVertices(List<List<NoSquareAtAllCycleNode>> cycles, List<NoSquareAtAllCycleNode> cycleVertexNodes, Integer[] cycleVerticesIncluded)
  {
    IntStream.range(0, cycles.size())
            .forEach(i -> cycles.get(i).stream()
                    .forEach(cycleNode ->
                    {
                      Vertex vertex = cycleNode.getVertex();
                      if (cycleVerticesIncluded[vertex.getVertexNo()] == null)
                      {
                        cycleVertexNodes.add(cycleNode);
                        cycleVerticesIncluded[vertex.getVertexNo()] = i;
                      }
                    })
            );
  }

  private void partitionVerticesIntoOddAndEvenDistance(List<NoSquareAtAllCycleNode> cycleVertexNodes, List<Vertex> oddDistanceVertices, List<Vertex> evenDistanceVertices)
  {
    cycleVertexNodes.forEach(cycleVertexNode ->
    {
      if (cycleVertexNode.getDistance() % 2 == 1)
      {
        oddDistanceVertices.add(cycleVertexNode.getVertex());
      }
      else
      {
        evenDistanceVertices.add(cycleVertexNode.getVertex());
      }
    });
  }

  private void collectAdditionalPotentialResultVertices(
          List<Vertex> additionalPotentialResultVerticesForOddDistance,
          List<Vertex> additionalPotentialResultVerticesForEvenDistance,
          Integer[] cycleVerticesIncluded,
          NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo,
          SquareReconstructionData squareReconstructionData)
  {
    boolean[] additionalPotentialResultVerticesForOddDistanceIncluded = new boolean[graph.getVertices().size()];
    boolean[] additionalPotentialResultVerticesForEvenDistanceIncluded = new boolean[graph.getVertices().size()];

    for (Vertex potentialAdditionalVertex : graph.getVertices())
    {
      int potentialAdditionalVertexNumber = potentialAdditionalVertex.getVertexNo();
      if (cycleVerticesIncluded[potentialAdditionalVertexNumber] != null)
      {
        continue;
      }
      for (Edge edge : potentialAdditionalVertex.getEdges())
      {
        Vertex endpointVertex = edge.getEndpoint();
        int endpointVertexNumber = endpointVertex.getVertexNo();

        MissingSquaresEntryData missingSquaresEntryData = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntriesByBaseEdge()[endpointVertexNumber][potentialAdditionalVertexNumber];
        NoSquareAtAllCycleNode neighborNoSquareAtAllCycleNode = noSquareAtAllCycleNodesByVertexNo[endpointVertexNumber];
        if (missingSquaresEntryData != null && cycleVerticesIncluded[endpointVertexNumber] != null && neighborNoSquareAtAllCycleNode != null)
        {
          if (neighborNoSquareAtAllCycleNode.getDistance() % 2 == 1
                  && !additionalPotentialResultVerticesForOddDistanceIncluded[potentialAdditionalVertexNumber])
          {
            additionalPotentialResultVerticesForEvenDistance.add(potentialAdditionalVertex);
            additionalPotentialResultVerticesForEvenDistanceIncluded[potentialAdditionalVertexNumber] = true;
          }
          else if (neighborNoSquareAtAllCycleNode.getDistance() % 2 == 0
                  && !additionalPotentialResultVerticesForEvenDistanceIncluded[potentialAdditionalVertexNumber])
          {
            additionalPotentialResultVerticesForOddDistance.add(potentialAdditionalVertex);
            additionalPotentialResultVerticesForOddDistanceIncluded[potentialAdditionalVertexNumber] = true;
          }
        }
      }
    }
  }

  private void checkPotentialReconstructions(List<Vertex> oddDistanceVertices, List<Vertex> evenDistanceVertices,
                                             List<Vertex> additionalPotentialResultVerticesForOddDistance,
                                             List<Vertex> additionalPotentialResultVerticesForEvenDistance)
  {
    List<Vertex> potentialRestultVertices = new LinkedList<>(oddDistanceVertices);
    potentialRestultVertices.addAll(additionalPotentialResultVerticesForOddDistance);
    checkPotentialResultVertices(potentialRestultVertices, evenDistanceVertices);

    if (!testCaseContext.isCorrectResult())
    {
      potentialRestultVertices = new LinkedList<>(evenDistanceVertices);
      potentialRestultVertices.addAll(additionalPotentialResultVerticesForEvenDistance);
      checkPotentialResultVertices(potentialRestultVertices, oddDistanceVertices);
    }
  }

  private void checkPotentialResultVertices(List<Vertex> potentialRestultVertices, List<Vertex> resultVertexNeighbors)
  {
    for (Vertex potentialRestultVertex : potentialRestultVertices)
    {
      List<Edge> missingEdges = resultVertexNeighbors.stream()
              .filter(vertexToConnect -> graph.getAdjacencyMatrix()[potentialRestultVertex.getVertexNo()][vertexToConnect.getVertexNo()] == null)
              .map(vertexToConnect -> new Edge(potentialRestultVertex, vertexToConnect))
              .collect(Collectors.toList());

      missingSquaresAnalyserCommons.checkSelectedEdgesCorrectness(missingEdges);
      if (testCaseContext.isCorrectResult())
      {
        break;
      }
    }
  }

  private void runMissingEdgesFindingAgainForDifferentCycles(List<List<NoSquareAtAllCycleNode>> cycles, SquareReconstructionData squareReconstructionData, int recursiveLevelsToGo)
  {
    if (recursiveLevelsToGo > 0 && !testCaseContext.isCorrectResult())
    {
      List<List<NoSquareAtAllCycleNode>> cyclesOfLengthSix = new LinkedList<>();
      List<List<NoSquareAtAllCycleNode>> cyclesOfLengthEight = new LinkedList<>();
      splitCyclesByLength(cycles, cyclesOfLengthSix, cyclesOfLengthEight);

      if (CollectionUtils.isEmpty(cyclesOfLengthEight))
      {
        List<NoSquareAtAllCycleNode> arbitraryCycle = cycles.get(0);
        Edge nextEdge = graph.getAdjacencyMatrix()[arbitraryCycle.get(0).getVertex().getVertexNo()][arbitraryCycle.get(1).getVertex().getVertexNo()];
        findMissingEdges(nextEdge, squareReconstructionData, recursiveLevelsToGo - 1);
      }
    }
  }

  private void splitCyclesByLength(List<List<NoSquareAtAllCycleNode>> cycles,
                                   List<List<NoSquareAtAllCycleNode>> cyclesOfLengthSix,
                                   List<List<NoSquareAtAllCycleNode>> cyclesOfLengthEight)
  {
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
  }
}
