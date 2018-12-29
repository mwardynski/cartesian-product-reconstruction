package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges;

import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MissingSquaresCycleLengthEightAnalyserServiceImpl
{
  @Autowired
  Graph graph;

  public List<List<Edge>> findMissingEdgesComparingCycles(List<List<NoSquareAtAllCycleNode>> inputCycles, SquareReconstructionData squareReconstructionData)
  {
    List<List<NoSquareAtAllCycleNode>> cycles = selectCyclesOfLengthEight(inputCycles);
    if (CollectionUtils.isEmpty(cycles))
    {
      return Collections.emptyList();
    }

    List<Vertex> resultVertices = new LinkedList<>();
    boolean[][] verticesIncludedInCycles = new boolean[cycles.size()][graph.getVertices().size()];
    collectIncludedVertexIndicesPerCycle(cycles, verticesIncludedInCycles);

    List<Vertex>[][] cycleDiffVertices = new List[cycles.size()][cycles.size()];
    collectCycleDifferencesWithStartAndEndVertices(cycles, verticesIncludedInCycles, cycleDiffVertices);

    List<Integer> startVertexNumbers = new LinkedList<>();

    for (int i = 0; i < cycles.size() - 1 && CollectionUtils.isEmpty(resultVertices); i++)
    {
      for (int j = i + 1; j < cycles.size() && CollectionUtils.isEmpty(resultVertices); j++)
      {
        List<Vertex> diffVertices = cycleDiffVertices[i][j];
        List<Vertex> corrDiffVertices = cycleDiffVertices[j][i];

        if (diffVertices.size() - 2 == 1)
        {
          collectResultVerticesBasedOnSimpleSquare(resultVertices, startVertexNumbers, diffVertices, corrDiffVertices);
        }
        else if (diffVertices.size() - 2 == 2)
        {
          collectResultVerticesBasedOnCubeWithoutSingleEdge(resultVertices, startVertexNumbers, diffVertices);
        }
        else if (diffVertices.size() - 2 == 3)
        {
          collectResultVerticesBasedOnMiddleSquare(squareReconstructionData, resultVertices, startVertexNumbers, diffVertices, corrDiffVertices);
        }
      }
    }
    List<List<Vertex>> verticesToConnect = new LinkedList<>();
    collectVerticesToConnect(verticesToConnect, cycles, resultVertices, startVertexNumbers);

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

  private List<List<NoSquareAtAllCycleNode>> selectCyclesOfLengthEight(List<List<NoSquareAtAllCycleNode>> inputCycles)
  {
    return inputCycles.stream()
            .filter(cycle -> cycle.size() == 8)
            .collect(Collectors.toList());
  }

  private void collectCycleDifferencesWithStartAndEndVertices(List<List<NoSquareAtAllCycleNode>> cycles, boolean[][] verticesIncludedInCycles, List<Vertex>[][] cycleDiffVertices)
  {
    for (int i = 0; i < cycles.size(); i++)
    {
      for (int j = 0; j < cycles.size(); j++)
      {
        cycleDiffVertices[i][j] = new ArrayList<>();
        if (j == i)
        {
          continue;
        }

        boolean lastCycleVertexAdded = false;
        for (int k = 0; k < cycles.get(i).size(); k++)
        {
          Vertex cycleVertex = cycles.get(i).get(k).getVertex();
          if (!verticesIncludedInCycles[j][cycleVertex.getVertexNo()])
          {
            lastCycleVertexAdded = true;
            if (CollectionUtils.isEmpty(cycleDiffVertices[i][j]))
            {
              Vertex prevNoSquareAtAllCycleVertex = cycles.get(i).get(k - 1).getVertex();
              cycleDiffVertices[i][j].add(prevNoSquareAtAllCycleVertex);
            }
            cycleDiffVertices[i][j].add(cycleVertex);
          }
          else if (lastCycleVertexAdded)
          {
            lastCycleVertexAdded = false;
            cycleDiffVertices[i][j].add(cycleVertex);
          }
        }
      }
    }
  }

  private void collectIncludedVertexIndicesPerCycle(List<List<NoSquareAtAllCycleNode>> cycles, boolean[][] verticesIncludedInCycles)
  {
    IntStream.range(0, cycles.size())
            .forEach(cycleIndex -> cycles.get(cycleIndex).stream()
                    .map(noSquareAtAllCycleNode -> noSquareAtAllCycleNode.getVertex().getVertexNo())
                    .forEach(vertexNumber -> verticesIncludedInCycles[cycleIndex][vertexNumber] = true)
            );
  }

  private void collectResultVerticesBasedOnSimpleSquare(List<Vertex> resultVertices, List<Integer> startVertexIndices, List<Vertex> diffVertices, List<Vertex> corrDiffVertices)
  {
    Vertex diffVertex = diffVertices.get(1);
    if (diffVertex.getEdges().size() == 2)
    {
      resultVertices.add(diffVertex);
      startVertexIndices.add(diffVertices.get(0).getVertexNo());
    }

    Vertex corrDiffVertex = corrDiffVertices.get(1);
    if (corrDiffVertex.getEdges().size() == 2 && (CollectionUtils.isEmpty(resultVertices)
            || CollectionUtils.isNotEmpty(resultVertices) && resultVertices.get(0) != corrDiffVertex))
    {
      resultVertices.add(corrDiffVertex);
      startVertexIndices.add(corrDiffVertices.get(0).getVertexNo());
    }
  }

  private void collectResultVerticesBasedOnCubeWithoutSingleEdge(List<Vertex> resultVertices, List<Integer> startVertexIndices, List<Vertex> diffVertices)
  {
    Vertex firstDiffVertex = diffVertices.get(0);
    Vertex lastDiffVertex = diffVertices.get(3);

    resultVertices.add(firstDiffVertex);
    startVertexIndices.add(lastDiffVertex.getVertexNo());

    resultVertices.add(lastDiffVertex);
    startVertexIndices.add(firstDiffVertex.getVertexNo());
  }

  private void collectResultVerticesBasedOnMiddleSquare(SquareReconstructionData squareReconstructionData, List<Vertex> resultVertices, List<Integer> startVertexIndices, List<Vertex> diffVertices, List<Vertex> corrDiffVertices)
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
      startVertexIndices.add(diffVertices.get(0).getVertexNo());
    }
  }

  private void collectVerticesToConnect(List<List<Vertex>> verticesToConnect, List<List<NoSquareAtAllCycleNode>> cycles, List<Vertex> resultVertices, List<Integer> startVertexNumbers)
  {
    boolean[] startVertexIndicesIncluded = new boolean[graph.getVertices().size()];
    startVertexNumbers.forEach(startVertexIndex -> startVertexIndicesIncluded[startVertexIndex] = true);
    for (int startVertexIndex = 0; startVertexIndex < startVertexNumbers.size(); startVertexIndex++)
    {
      Vertex resultVertex = resultVertices.get(startVertexIndex);

      List<Vertex> verticesToConnectPerVertex = new LinkedList<>();
      boolean[] verticesToConnectPerVertexIncluded = new boolean[graph.getVertices().size()];

      for (List<NoSquareAtAllCycleNode> cycle : cycles)
      {
        for (int i = 0; i < cycle.size(); i++)
        {
          if (cycle.get(i).getVertex().getVertexNo() == startVertexNumbers.get(startVertexIndex))
          {
            for (int j = i % 2; j < cycle.size(); j += 2)
            {
              Vertex vertexToConnect = cycle.get(j).getVertex();
              if (!verticesToConnectPerVertexIncluded[vertexToConnect.getVertexNo()]
                      && graph.getAdjacencyMatrix()[vertexToConnect.getVertexNo()][resultVertex.getVertexNo()] == null)
              {
                verticesToConnectPerVertex.add(vertexToConnect);
                verticesToConnectPerVertexIncluded[vertexToConnect.getVertexNo()] = true;
              }
            }
            break;
          }
        }
      }
      verticesToConnect.add(verticesToConnectPerVertex);
    }
  }
}
