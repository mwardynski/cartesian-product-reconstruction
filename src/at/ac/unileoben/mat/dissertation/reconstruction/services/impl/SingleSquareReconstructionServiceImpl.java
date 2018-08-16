package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquareReconstructionService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareHandlingStrategy;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SingleSquareReconstructionServiceImpl implements SingleSquareReconstructionService
{

  @Autowired
  Graph graph;

  @Autowired
  SquareFindingService squareFindingService;

  @Autowired
  SquareHandlingStrategy squareHandlingStrategy;

  @Override
  public void reconstructUsingSquares(SquareMatchingEdgeData[][] squareMatchingEdgesByEdge)
  {
    SquareReconstructionData squareReconstructionData = new SquareReconstructionData(graph.getVertices().size());
    squareReconstructionData.getNextVertices().add(graph.getRoot());
    squareReconstructionData.getIncludedVertices()[graph.getRoot().getVertexNo()] = true;
    squareReconstructionData.setSquareMatchingEdgesByEdge(squareMatchingEdgesByEdge);

    while (squareReconstructionData.getNextVertices().size() > 0)
    {
      reconstructForCurrentVertex(squareReconstructionData);
    }
    printOutMissingSquares(squareReconstructionData);
  }

  private void printOutMissingSquares(SquareReconstructionData squareReconstructionData)
  {
    squareReconstructionData.getMissingSquares().stream()
            .forEach(missingSquare ->
            {
              Edge edge1 = missingSquare.getFirstEdge();
              Edge edge2 = missingSquare.getSecondEdge();
              System.out.println(String.format("%d-%d(%d), %d-%d(%d)",
                      edge1.getOrigin().getVertexNo(), edge1.getEndpoint().getVertexNo(), edge1.getLabel().getColor(),
                      edge2.getOrigin().getVertexNo(), edge2.getEndpoint().getVertexNo(), edge2.getLabel().getColor()));
            });
  }

  private void reconstructForCurrentVertex(SquareReconstructionData squareReconstructionData)
  {
    squareReconstructionData.setCurrentVertex(squareReconstructionData.getNextVertices().poll());
    List<Edge> currentVertexEdges = prepareEdgesForColoringUsingSquares(squareReconstructionData.getCurrentVertex().getEdges(), squareReconstructionData);

    if (currentVertexEdges.size() < 2)
    {
      return;
    }

    /*List<Vertex> newNextVertices = currentVertexEdges.stream()
            .map(e -> e.getEndpoint())
            .filter(v -> !squareReconstructionData.getIncludedVertices()[v.getVertexNo(\)])
            .collect(Collectors.toList());
    newNextVertices.stream().forEach(v -> squareReconstructionData.getIncludedVertices()[v.getVertexNo()] = true);
    squareReconstructionData.getNextVertices().addAll(newNextVertices);*/

    traverseAndColorAllEdgePairs(currentVertexEdges, squareReconstructionData, true);

    currentVertexEdges.stream()
            .peek(e -> squareReconstructionData.getUsedEdges()[e.getOrigin().getVertexNo()][e.getEndpoint().getVertexNo()] = true)
            .forEach(e -> squareReconstructionData.getUsedEdges()[e.getEndpoint().getVertexNo()][e.getOrigin().getVertexNo()] = true);

    List<Edge> edgesWithoutSquare = currentVertexEdges.stream()
            .filter(e -> e.getLabel() == null)
            .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(edgesWithoutSquare))
    {
      squareHandlingStrategy.colorEdgesWithoutSquare(edgesWithoutSquare);
    }
  }

  private void traverseAndColorAllEdgePairs(List<Edge> currentVertexEdges, SquareReconstructionData squareReconstructionData, boolean firstRun)
  {
    List<Edge> edgesToRepeatColoring = new ArrayList<>(currentVertexEdges.size());
    boolean[] includedEdgesToRepeatColoring = new boolean[graph.getVertices().size()];

    for (int i = 0; i < currentVertexEdges.size() - 1; i++)
    {
      for (int j = i + 1; j < currentVertexEdges.size(); j++)
      {
        Edge iEdge = currentVertexEdges.get(i);
        Edge jEdge = currentVertexEdges.get(j);

        if (iEdge.getLabel() != null && jEdge.getLabel() != null && iEdge.getLabel().getColor() == jEdge.getLabel().getColor())
        {
          continue;
        }

        SquareFindingEnum squareFindingResult = squareFindingService.findAndProcessSquareForTwoEdges(squareReconstructionData, iEdge, jEdge, firstRun);

        if (squareFindingResult == SquareFindingEnum.REPEAT_SQUARE)
        {
          Stream.of(iEdge, jEdge)
                  .filter(edge -> !includedEdgesToRepeatColoring[edge.getEndpoint().getVertexNo()])
                  .forEach(edge ->
                  {
                    edgesToRepeatColoring.add(edge);
                    includedEdgesToRepeatColoring[edge.getEndpoint().getVertexNo()] = true;
                  });
        }
      }
    }

    if (CollectionUtils.isNotEmpty(edgesToRepeatColoring))
    {
      List<Edge> preparedEdgesToRepeatColoring = prepareEdgesForColoringUsingSquares(edgesToRepeatColoring, squareReconstructionData);
      traverseAndColorAllEdgePairs(preparedEdgesToRepeatColoring, squareReconstructionData, false);
    }
  }

  private List<Edge> prepareEdgesForColoringUsingSquares(List<Edge> edges, SquareReconstructionData squareReconstructionData)
  {
    List<Edge> resultEdges = new ArrayList<>(edges.size());

    edges.stream().forEach(
            e ->
            {
              if (e.getLabel() != null)
              {
                resultEdges.add(0, e);
              }
              else
              {
                resultEdges.add(e);
              }
            }
    );
    return resultEdges;
  }
}
