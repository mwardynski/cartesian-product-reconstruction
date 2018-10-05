package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresGroupingService;
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
  PartOfCycleNoSquareAtAllMissingSquaresGroupingService partOfCycleNoSquareAtAllMissingSquaresGroupingService;

  public List<MissingSquaresUniqueEdgesData> findCorrectPartOfCycleNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    NoSquareAtAllGroupsData noSquareAtAllGroupsData = partOfCycleNoSquareAtAllMissingSquaresGroupingService.splitPartOfCycleNoSquareAtAllMissingSquaresIntoGroups(noSquareAtAllMissingSquares);
    return findCycleForNoSquareAtAllGroups(noSquareAtAllGroupsData, squareReconstructionData);
  }

  private List<MissingSquaresUniqueEdgesData> findCycleForNoSquareAtAllGroups(NoSquareAtAllGroupsData noSquareAtAllGroupsData, SquareReconstructionData squareReconstructionData)
  {
    List<List<Edge>> groupedNoSquareAtAllEdges = noSquareAtAllGroupsData.getGroupedNoSquareAtAllEdges();

    Edge arbitraryGroupFirstEdge = groupedNoSquareAtAllEdges.get(0).get(0);
    List<List<NoSquareAtAllCycleNode>> correctCycles = findCycleUsingBfs(arbitraryGroupFirstEdge, groupedNoSquareAtAllEdges, noSquareAtAllGroupsData.getGroupNumbersForNoSquareAtAllEdgesEndpoints(), squareReconstructionData);

    List<MissingSquaresUniqueEdgesData> correctNoSquareAtAllMissingSquares = splitCyclesIntoMissingSquares(correctCycles);
    return correctNoSquareAtAllMissingSquares;
  }

  private List<List<NoSquareAtAllCycleNode>> findCycleUsingBfs(Edge arbitraryGroupFirstEdge, List<List<Edge>> groupedNoSquareAtAllEdges, Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints, SquareReconstructionData squareReconstructionData)
  {
    NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo = new NoSquareAtAllCycleNode[graph.getVertices().size()];

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
    UniqueList collectedMappedColors = new UniqueList(graph.getVertices().size());
    UniqueList collectedGroups = new UniqueList(groupedNoSquareAtAllEdges.size());

    processCycle(noSquareAtAllCycleNodesByVertexNo[firstVertex.getVertexNo()], noSquareAtAllCycleNodesByVertexNo[endVertex.getVertexNo()],
            correctCycles, currentCycle, collectedMappedColors, collectedGroups, groupedNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints);
    return correctCycles;
  }

  private boolean includeVerticesDifferentThanEndVertex(Vertex currentVertex, Vertex nextVertex, Vertex endVertex, NoSquareAtAllCycleNode[] noSquareAtAllCycleNodesByVertexNo)
  {
    return !(nextVertex == endVertex && noSquareAtAllCycleNodesByVertexNo[currentVertex.getVertexNo()].getDistance() == 0);
  }

  private void processCycle(NoSquareAtAllCycleNode endVertexNode, NoSquareAtAllCycleNode currentVertexNode, List<List<NoSquareAtAllCycleNode>> correctCycles, List<NoSquareAtAllCycleNode> currentCycle,
                            UniqueList collectedMappedColors, UniqueList collectedGroups, List<List<Edge>> groupedNoSquareAtAllEdges, Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints)
  {
    currentCycle.add(currentVertexNode);
    collectedGroups.add(groupNumbersForNoSquareAtAllEdgesEndpoints[currentVertexNode.getVertex().getVertexNo()]);

    if (currentVertexNode == endVertexNode)
    {
      if (groupedNoSquareAtAllEdges.size() == collectedGroups.size())
      {
        if (groupedNoSquareAtAllEdges.size() > 1 ||
                (groupedNoSquareAtAllEdges.get(0).size() >= 6 ||
                        (groupedNoSquareAtAllEdges.get(0).size() < 6 & collectedMappedColors.size() > 1)))
        {
          correctCycles.add(currentCycle);
        }
      }
    }
    else
    {
      for (int i = 0; i < currentVertexNode.getPreviousVerticesNodes().size(); i++)
      {
        NoSquareAtAllCycleNode previousVertexNode = currentVertexNode.getPreviousVerticesNodes().get(i);

        List<NoSquareAtAllCycleNode> iterationCurrentCycle;
        UniqueList iterationCollectedMappedColors;
        UniqueList iterationCollectedGroups;

        if (i < currentVertexNode.getPreviousVerticesNodes().size() - 1)
        {
          iterationCurrentCycle = new LinkedList<>(currentCycle);
          iterationCollectedMappedColors = new UniqueList(collectedMappedColors);
          iterationCollectedGroups = new UniqueList(collectedGroups);
        }
        else
        {
          iterationCurrentCycle = currentCycle;
          iterationCollectedMappedColors = collectedMappedColors;
          iterationCollectedGroups = collectedGroups;
        }


        Edge edge = graph.getAdjacencyMatrix()[currentVertexNode.getVertex().getVertexNo()][previousVertexNode.getVertex().getVertexNo()];
        if (edge.getLabel().getName() != -2)
        {
          int edgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), edge.getLabel().getColor());
          iterationCollectedMappedColors.add(edgeMappedColor);
        }

        processCycle(endVertexNode, previousVertexNode, correctCycles, iterationCurrentCycle, iterationCollectedMappedColors, iterationCollectedGroups, groupedNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints);
      }
    }
  }

  private List<MissingSquaresUniqueEdgesData> splitCyclesIntoMissingSquares(List<List<NoSquareAtAllCycleNode>> correctCycles)
  {
    List<MissingSquaresUniqueEdgesData> correctNoSquareAtAllMissingSquares = new LinkedList<>();
    for (List<NoSquareAtAllCycleNode> correctCycle : correctCycles)
    {
      for (int i = 0; i < 6; i++)
      {
        Edge firstEdge = graph.getAdjacencyMatrix()[correctCycle.get(i).getVertex().getVertexNo()][correctCycle.get(i + 1).getVertex().getVertexNo()];
        Edge secondEdge = graph.getAdjacencyMatrix()[correctCycle.get(i + 1).getVertex().getVertexNo()][correctCycle.get(i + 2).getVertex().getVertexNo()];

        int firstEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), firstEdge.getLabel().getColor());
        int secondEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), secondEdge.getLabel().getColor());

        if (firstEdgeMappedColor == secondEdgeMappedColor)
        {
          for (int j = i; j < i + 8; j += 2)
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
