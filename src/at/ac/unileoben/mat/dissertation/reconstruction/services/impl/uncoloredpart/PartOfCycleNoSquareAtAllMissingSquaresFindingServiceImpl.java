package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Component
public class PartOfCycleNoSquareAtAllMissingSquaresFindingServiceImpl implements PartOfCycleNoSquareAtAllMissingSquaresFindingService
{
  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

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

    while (CollectionUtils.isNotEmpty(nextVertices))
    {
      Vertex currentVertex = nextVertices.poll();

      NoSquareAtAllCycleNode currentVertexNode = noSquareAtAllCycleNodesByVertexNo[currentVertex.getVertexNo()];
      if (currentVertexNode.getDistance() >= 7)
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
      for (int i = 1; i < 7; i++)
      {
        Edge firstEdge = graph.getAdjacencyMatrix()[correctCycle.get(i - 1).getVertex().getVertexNo()][correctCycle.get(i).getVertex().getVertexNo()];
        Edge secondEdge = graph.getAdjacencyMatrix()[correctCycle.get(i).getVertex().getVertexNo()][correctCycle.get(i + 1).getVertex().getVertexNo()];

        int vertexNeighborhoodSizeInCycles = noSquareAtAllCycleNodesByVertexNo[correctCycle.get(i).getVertex().getVertexNo()].getPreviousVerticesNodes().size();

        if (vertexNeighborhoodSizeInCycles > 1 || uncoloredEdgesHandlerService.areNormalEdgesOfGivenColorProperty(firstEdge, secondEdge, true))
        {
          this.getClass();
          for (int j = i - 1; j < i + 8; j += 2)
          {
            int missingSquareEdgesStartIndex = (j + 8 - 1) % 8;
            int missingSquareEdgesMiddleIndex = j % 8;
            int missingSquareEdgesEndIndex = (j + 1) % 8;

            int missingSquareEdgesStartVertexNo = correctCycle.get(missingSquareEdgesStartIndex).getVertex().getVertexNo();
            int missingSquareEdgesMiddleVertexNo = correctCycle.get(missingSquareEdgesMiddleIndex).getVertex().getVertexNo();
            int missingSquareEdgesEndVertexNo = correctCycle.get(missingSquareEdgesEndIndex).getVertex().getVertexNo();

            Edge firstMissingSquareEdge = graph.getAdjacencyMatrix()[missingSquareEdgesMiddleVertexNo][missingSquareEdgesStartVertexNo];
            Edge secondMissingSquareEdge = graph.getAdjacencyMatrix()[missingSquareEdgesMiddleVertexNo][missingSquareEdgesEndVertexNo];

            MissingSquaresUniqueEdgesData correctNoSquareAtAllMissingSquare = new MissingSquaresUniqueEdgesData(firstMissingSquareEdge, secondMissingSquareEdge);
            correctNoSquareAtAllMissingSquares.add(correctNoSquareAtAllMissingSquare);
          }
          break;
        }
      }
    }
    return correctNoSquareAtAllMissingSquares;
  }
}
