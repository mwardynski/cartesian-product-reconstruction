package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges;

import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MissingSquaresCycleAnalyserServiceImpl
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

    List<Vertex>[][][] cycleDiffVertices = collectCycleDifferencesWithStartAndEndVertices(cycles, cycles);

    List<Vertex> resultVertices = new LinkedList<>();
    List<Integer> startVertexNumbers = new LinkedList<>();

    for (int i = 0; i < cycles.size() - 1 && CollectionUtils.isEmpty(resultVertices); i++)
    {
      for (int j = i + 1; j < cycles.size() && CollectionUtils.isEmpty(resultVertices); j++)
      {
        List<Vertex> diffVertices = cycleDiffVertices[0][i][j];
        List<Vertex> corrDiffVertices = cycleDiffVertices[1][j][i];

        int resultVerticesSizeBefore = resultVertices.size();

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
                  Vertex oppositeMiddleVertex = squareList.getFirst().getSquareOtherEdge().getEndpoint();

                  if (middleVertex.getEdges().size() == 2)
                  {
                    resultVertices.add(middleVertex);
                    startVertexNumbers.add(diffVertices.get(0).getVertexNo());
                  }
                  if (oppositeMiddleVertex.getEdges().size() == 2)
                  {
                    resultVertices.add(oppositeMiddleVertex);
                    startVertexNumbers.add(diffVertices.get(0).getVertexNo());
                  }
                }

              }
            }

          }
        }
      }
    }

    List<List<Vertex>> verticesToConnect = collectVerticesToConnect(cycles, resultVertices, startVertexNumbers);
    List<List<Edge>> resultMissingEdges = collectMissingEdges(resultVertices, verticesToConnect);


    return resultMissingEdges;
  }

  private List<List<NoSquareAtAllCycleNode>> selectCyclesOfLengthEight(List<List<NoSquareAtAllCycleNode>> inputCycles)
  {
    return inputCycles.stream()
            .filter(cycle -> cycle.size() == 8)
            .collect(Collectors.toList());
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
        if (firstCyclesGroup.get(0).size() == secondCyclesGroup.get(0).size()
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

  public List<List<Vertex>> collectVerticesToConnect(List<List<NoSquareAtAllCycleNode>> cycles, List<Vertex> resultVertices, List<Integer> startVertexNumbers)
  {
    List<List<Vertex>> verticesToConnect = new LinkedList<>();
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

    return verticesToConnect;
  }

  public List<List<Edge>> collectMissingEdges(List<Vertex> resultVertices, List<List<Vertex>> verticesToConnect)
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
}
