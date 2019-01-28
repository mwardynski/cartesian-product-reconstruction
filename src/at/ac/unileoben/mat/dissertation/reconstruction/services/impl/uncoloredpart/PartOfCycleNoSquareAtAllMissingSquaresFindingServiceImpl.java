package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Component
public class PartOfCycleNoSquareAtAllMissingSquaresFindingServiceImpl implements PartOfCycleNoSquareAtAllMissingSquaresFindingService
{
  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  UncoloredEdgesHandlerService uncoloredEdgesHandlerService;

  public List<MissingSquaresUniqueEdgesData> findCorrectPartOfCycleNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    MissingSquaresUniqueEdgesData arbitraryMissingSquaresUniqueEdges = noSquareAtAllMissingSquares.get(0);
    Edge arbitraryMissingSquareEdge = arbitraryMissingSquaresUniqueEdges.getBaseEdge().getLabel().getColor() == 0 ? arbitraryMissingSquaresUniqueEdges.getBaseEdge() : arbitraryMissingSquaresUniqueEdges.getOtherEdge();
    return findCycleForNoSquareAtAllGroups(arbitraryMissingSquareEdge, squareReconstructionData);
  }

  private List<MissingSquaresUniqueEdgesData> findCycleForNoSquareAtAllGroups(Edge arbitraryMissingSquareEdge, SquareReconstructionData squareReconstructionData)
  {
    NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo = new NoSquareAtAllCycleNode[graph.getVertices().size()];
    List<List<NoSquareAtAllCycleNode>> correctCycles = findCycleUsingBfs(arbitraryMissingSquareEdge, squareReconstructionData, noSquareAtAllCycleNodesByVertexNo);

    List<MissingSquaresUniqueEdgesData> correctNoSquareAtAllMissingSquares = splitCyclesIntoMissingSquares(correctCycles, noSquareAtAllCycleNodesByVertexNo);
    return correctNoSquareAtAllMissingSquares;
  }

  private List<List<NoSquareAtAllCycleNode>> findCycleUsingBfs(Edge arbitraryGroupFirstEdge, SquareReconstructionData squareReconstructionData, NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo)
  {
    Vertex endVertex = arbitraryGroupFirstEdge.getEndpoint();
    Vertex firstVertex = arbitraryGroupFirstEdge.getOrigin();
    NoSquareAtAllCycleNode firstNoSquareAtAllCycleNode = new NoSquareAtAllCycleNode(firstVertex, 0);
    noSquareAtAllCycleNodesByVertexNo[firstVertex.getVertexNo()] = firstNoSquareAtAllCycleNode;

    Queue<Vertex> nextVertices = new LinkedList<>();
    nextVertices.add(firstVertex);

    int maxDistance = 7;
    if (reconstructionData.getOperationOnGraph() == OperationOnGraph.SINGLE_EDGE_RECONSTRUCTION)
    {
      maxDistance = 5;
    }

    while (CollectionUtils.isNotEmpty(nextVertices))
    {
      Vertex currentVertex = nextVertices.poll();

      NoSquareAtAllCycleNode currentVertexNode = noSquareAtAllCycleNodesByVertexNo[currentVertex.getVertexNo()];
      if (currentVertexNode.getDistance() >= maxDistance)
      {
        break;
      }

      MissingSquaresEntryData[][] missingSquaresByEdges = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntriesByBaseEdge();

      currentVertex.getEdges().stream()
              .map(edge -> edge.getEndpoint())
              .filter(nextVertex -> includeVerticesDifferentThanEndVertex(currentVertex, nextVertex, endVertex, noSquareAtAllCycleNodesByVertexNo))
              .forEach(nextVertex ->
              {
                if (missingSquaresByEdges[currentVertex.getVertexNo()][nextVertex.getVertexNo()] == null
                        && missingSquaresByEdges[nextVertex.getVertexNo()][currentVertex.getVertexNo()] == null)
                {
                  return;
                }

                if (noSquareAtAllCycleNodesByVertexNo[nextVertex.getVertexNo()] == null)
                {
                  noSquareAtAllCycleNodesByVertexNo[nextVertex.getVertexNo()] = new NoSquareAtAllCycleNode(nextVertex, currentVertexNode.getDistance() + 1);
                  nextVertices.add(nextVertex);
                }

                NoSquareAtAllCycleNode nextVertexNode = noSquareAtAllCycleNodesByVertexNo[nextVertex.getVertexNo()];
                if (nextVertexNode.getDistance() == currentVertexNode.getDistance() + 1)
                {
                  nextVertexNode.getPreviousVerticesNodes().add(currentVertexNode);
                }
              });
    }

    List<List<NoSquareAtAllCycleNode>> correctCycles = new LinkedList<>();
    List<NoSquareAtAllCycleNode> currentCycle = new LinkedList<>();

    processCycle(noSquareAtAllCycleNodesByVertexNo[firstVertex.getVertexNo()], noSquareAtAllCycleNodesByVertexNo[endVertex.getVertexNo()],
            correctCycles, currentCycle);
    return correctCycles;
  }

  private boolean includeVerticesDifferentThanEndVertex(Vertex currentVertex, Vertex nextVertex, Vertex endVertex, NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo)
  {
    return !(nextVertex == endVertex && noSquareAtAllCycleNodesByVertexNo[currentVertex.getVertexNo()].getDistance() == 0);
  }

  private void processCycle(NoSquareAtAllCycleNode endVertexNode, NoSquareAtAllCycleNode currentVertexNode, List<List<NoSquareAtAllCycleNode>> correctCycles, List<NoSquareAtAllCycleNode> currentCycle)
  {
    currentCycle.add(currentVertexNode);

    if (currentVertexNode == endVertexNode)
    {
      correctCycles.add(currentCycle);
    }
    else
    {
      for (int i = 0; i < currentVertexNode.getPreviousVerticesNodes().size(); i++)
      {
        NoSquareAtAllCycleNode previousVertexNode = currentVertexNode.getPreviousVerticesNodes().get(i);

        List<NoSquareAtAllCycleNode> iterationCurrentCycle;

        if (i < currentVertexNode.getPreviousVerticesNodes().size() - 1)
        {
          iterationCurrentCycle = new LinkedList<>(currentCycle);
        }
        else
        {
          iterationCurrentCycle = currentCycle;
        }

        processCycle(endVertexNode, previousVertexNode, correctCycles, iterationCurrentCycle);
      }
    }
  }

  private List<MissingSquaresUniqueEdgesData> splitCyclesIntoMissingSquares(List<List<NoSquareAtAllCycleNode>> correctCycles, NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo)
  {
    List<MissingSquaresUniqueEdgesData> correctNoSquareAtAllMissingSquares = new LinkedList<>();
    for (List<NoSquareAtAllCycleNode> correctCycle : correctCycles)
    {
      int cycleLength = correctCycle.get(0).getDistance() + 1;

      int singleEdgeReconstructionDoubleSpecialEdgesCorrectMissingSquaresIndex = -1;
      for (int i = 1; i < cycleLength - 1; i++)
      {
        Edge firstEdge = graph.getAdjacencyMatrix()[correctCycle.get(i - 1).getVertex().getVertexNo()][correctCycle.get(i).getVertex().getVertexNo()];
        Edge secondEdge = graph.getAdjacencyMatrix()[correctCycle.get(i).getVertex().getVertexNo()][correctCycle.get(i + 1).getVertex().getVertexNo()];

        int vertexNeighborhoodSizeInCycles = noSquareAtAllCycleNodesByVertexNo[correctCycle.get(i).getVertex().getVertexNo()].getPreviousVerticesNodes().size();

        if (isCyclesCrossing(vertexNeighborhoodSizeInCycles) || uncoloredEdgesHandlerService.areNormalEdgesOfGivenColorProperty(firstEdge, secondEdge, true))
        {
          if (reconstructionData.getOperationOnGraph() == OperationOnGraph.SINGLE_EDGE_RECONSTRUCTION)
          {
            MissingSquaresUniqueEdgesData firstCorrectNoSquareAtAllMissingSquare = extractCorrectNoSquareAtAllMissingSquare(i - 1, cycleLength, correctCycle);
            MissingSquaresUniqueEdgesData secondCorrectNoSquareAtAllMissingSquare = extractCorrectNoSquareAtAllMissingSquare(i, cycleLength, correctCycle);
            correctNoSquareAtAllMissingSquares.addAll(Arrays.asList(firstCorrectNoSquareAtAllMissingSquare, secondCorrectNoSquareAtAllMissingSquare));
          }
          else
          {
            for (int j = i - 1; j < i + cycleLength; j += 2)
            {
              MissingSquaresUniqueEdgesData correctNoSquareAtAllMissingSquare = extractCorrectNoSquareAtAllMissingSquare(j, cycleLength, correctCycle);
              correctNoSquareAtAllMissingSquares.add(correctNoSquareAtAllMissingSquare);
            }
          }
          break;
        }
        else if (reconstructionData.getOperationOnGraph() == OperationOnGraph.SINGLE_EDGE_RECONSTRUCTION &&
                firstEdge.getLabel().getColor() == 0 && secondEdge.getLabel().getColor() == 0)
        {
          int beforeFirstEdgeEndpointIndex = (i - 2 + cycleLength) % cycleLength;
          Edge beforeFirstEdge = graph.getAdjacencyMatrix()[correctCycle.get(beforeFirstEdgeEndpointIndex).getVertex().getVertexNo()][correctCycle.get(i - 1).getVertex().getVertexNo()];
          Edge afterSecondEdge = graph.getAdjacencyMatrix()[correctCycle.get(i + 1).getVertex().getVertexNo()][correctCycle.get((i + 2 + cycleLength) % cycleLength).getVertex().getVertexNo()];

          if (beforeFirstEdge.getLabel().getColor() != 0)
          {
            singleEdgeReconstructionDoubleSpecialEdgesCorrectMissingSquaresIndex = beforeFirstEdgeEndpointIndex;
          }
          else if (afterSecondEdge.getLabel().getColor() != 0)
          {
            singleEdgeReconstructionDoubleSpecialEdgesCorrectMissingSquaresIndex = i + 1;
          }
        }
      }
      if (reconstructionData.getOperationOnGraph() == OperationOnGraph.SINGLE_EDGE_RECONSTRUCTION)
      {
        if (CollectionUtils.isEmpty(correctNoSquareAtAllMissingSquares))
        {
          MissingSquaresUniqueEdgesData firstCorrectNoSquareAtAllMissingSquare = extractCorrectNoSquareAtAllMissingSquare(singleEdgeReconstructionDoubleSpecialEdgesCorrectMissingSquaresIndex, cycleLength, correctCycle);
          MissingSquaresUniqueEdgesData secondCorrectNoSquareAtAllMissingSquare = extractCorrectNoSquareAtAllMissingSquare(singleEdgeReconstructionDoubleSpecialEdgesCorrectMissingSquaresIndex + 1, cycleLength, correctCycle);
          correctNoSquareAtAllMissingSquares.addAll(Arrays.asList(firstCorrectNoSquareAtAllMissingSquare, secondCorrectNoSquareAtAllMissingSquare));
        }
        break;
      }
    }
    return correctNoSquareAtAllMissingSquares;
  }

  private boolean isCyclesCrossing(int vertexNeighborhoodSizeInCycles)
  {
    return vertexNeighborhoodSizeInCycles > 1;
  }

  private MissingSquaresUniqueEdgesData extractCorrectNoSquareAtAllMissingSquare(int cycleVertexIndex, int cycleLength, List<NoSquareAtAllCycleNode> correctCycle)
  {
    int missingSquareEdgesStartIndex = (cycleVertexIndex + cycleLength - 1) % cycleLength;
    int missingSquareEdgesMiddleIndex = cycleVertexIndex % cycleLength;
    int missingSquareEdgesEndIndex = (cycleVertexIndex + 1) % cycleLength;

    int missingSquareEdgesStartVertexNo = correctCycle.get(missingSquareEdgesStartIndex).getVertex().getVertexNo();
    int missingSquareEdgesMiddleVertexNo = correctCycle.get(missingSquareEdgesMiddleIndex).getVertex().getVertexNo();
    int missingSquareEdgesEndVertexNo = correctCycle.get(missingSquareEdgesEndIndex).getVertex().getVertexNo();

    Edge firstMissingSquareEdge = graph.getAdjacencyMatrix()[missingSquareEdgesMiddleVertexNo][missingSquareEdgesStartVertexNo];
    Edge secondMissingSquareEdge = graph.getAdjacencyMatrix()[missingSquareEdgesMiddleVertexNo][missingSquareEdgesEndVertexNo];

    MissingSquaresUniqueEdgesData correctNoSquareAtAllMissingSquare = new MissingSquaresUniqueEdgesData(firstMissingSquareEdge, secondMissingSquareEdge);
    return correctNoSquareAtAllMissingSquare;
  }
}
