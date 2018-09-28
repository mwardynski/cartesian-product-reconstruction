package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.*;
import at.ac.unileoben.mat.dissertation.structure.*;
import at.ac.unileoben.mat.dissertation.structure.exception.SquareWithoutAnyLabelsException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SingleSquareReconstructionServiceImpl implements SingleSquareReconstructionService
{

  @Autowired
  Graph graph;

  @Autowired
  SquareFindingService squareFindingService;

  @Autowired
  SquareHandlingStrategy squareHandlingStrategy;

  @Autowired
  MissingSquaresCleanerService missingSquaresCleanerService;

  @Autowired
  MissingSquaresAnalyzerService missingSquaresAnalyzerService;

  @Override
  public void reconstructUsingSquares(SquareMatchingEdgeData[][] squareMatchingEdgesByEdge)
  {
    SquareReconstructionData squareReconstructionData = new SquareReconstructionData(graph.getVertices().size());
    squareReconstructionData.getNextVertices().add(graph.getRoot());
    squareReconstructionData.getIncludedVertices()[graph.getRoot().getVertexNo()] = true;
    squareReconstructionData.setSquareMatchingEdgesByEdge(squareMatchingEdgesByEdge);

    while (CollectionUtils.isNotEmpty(squareReconstructionData.getNextVertices()))
    {
      try
      {
        reconstructForCurrentVertex(squareReconstructionData);
      }
      catch (SquareWithoutAnyLabelsException e)
      {
        addCurrentVertexToPostponedVertices(squareReconstructionData);
      }

      if (CollectionUtils.isEmpty(squareReconstructionData.getNextVertices()))
      {
        handleNextPostponedVertex(squareReconstructionData);
      }
    }
//    printOutMissingSquares(squareReconstructionData);
    missingSquaresCleanerService.cleanNotValidMissingSquares(squareReconstructionData);
    missingSquaresAnalyzerService.analyseMissingSquares(squareReconstructionData, squareMatchingEdgesByEdge);
  }

  private void addCurrentVertexToPostponedVertices(SquareReconstructionData squareReconstructionData)
  {
    Vertex currentVertex = squareReconstructionData.getCurrentVertex();
    squareReconstructionData.getIncludedVertices()[currentVertex.getVertexNo()] = false;
    squareReconstructionData.getIncludedPostponedVertices()[currentVertex.getVertexNo()] = true;
    squareReconstructionData.getPostponedVertices().add(currentVertex);
  }

  private void handleNextPostponedVertex(SquareReconstructionData squareReconstructionData)
  {
    while (CollectionUtils.isNotEmpty(squareReconstructionData.getPostponedVertices()))
    {
      Vertex nextPostponedVertex = squareReconstructionData.getPostponedVertices().poll();
      if (!squareReconstructionData.getIncludedVertices()[nextPostponedVertex.getVertexNo()])
      {
        squareReconstructionData.getNextVertices().add(nextPostponedVertex);
        squareReconstructionData.getIncludedVertices()[nextPostponedVertex.getVertexNo()] = true;
        break;
      }
    }
  }

  private void printOutMissingSquares(SquareReconstructionData squareReconstructionData)
  {
    squareReconstructionData.getMissingSquaresData().getMissingSquaresEntries().stream()
            .forEach(missingSquaresEntry ->
            {
              Edge baseEdge = missingSquaresEntry.getBaseEdge();
              Arrays.stream(missingSquaresEntry.getOtherEdgesByColors())
                      .filter(edgesByColor -> CollectionUtils.isNotEmpty(edgesByColor))
                      .flatMap(edgesByColor -> edgesByColor.stream())
                      .forEach(otherEdge ->
                              System.out.println(String.format("%d-%d(%d), %d-%d(%d)",
                                      baseEdge.getOrigin().getVertexNo(), baseEdge.getEndpoint().getVertexNo(), baseEdge.getLabel().getColor(),
                                      otherEdge.getOrigin().getVertexNo(), otherEdge.getEndpoint().getVertexNo(), otherEdge.getLabel().getColor()))
                      );
            });
  }

  private void reconstructForCurrentVertex(SquareReconstructionData squareReconstructionData)
  {
    squareReconstructionData.setCurrentVertex(squareReconstructionData.getNextVertices().poll());
    List<Edge> currentVertexEdges = collectCurrentVertexEdges(squareReconstructionData);

    if (currentVertexEdges.size() < 2)
    {
      return;
    }

    List<MissingSquaresUniqueEdgesData> missingSquares = new LinkedList<>();
    squareReconstructionData.setCurrentVertexNeighborsToQueue(new LinkedList<>());

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

        boolean squareFound = squareFindingService.findAndProcessSquareForTwoEdges(squareReconstructionData, iEdge, jEdge);


        if (!squareFound)
        {
          MissingSquaresUniqueEdgesData missingSquare = new MissingSquaresUniqueEdgesData(iEdge, jEdge);
          missingSquares.add(missingSquare);
        }
      }
    }
    squareReconstructionData.getCurrentVertexNeighborsToQueue().stream()
            .forEach(v -> squareHandlingStrategy.queueSquareSideVertexToNextVertices(v, squareReconstructionData));
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

    missingSquares.stream()
            .filter(missingSquare -> missingSquare.getBaseEdge().getLabel().getColor() != missingSquare.getOtherEdge().getLabel().getColor())
            .forEach(missingSquare ->
            {
              Edge baseEdge = missingSquare.getBaseEdge();
              Edge otherEdge = missingSquare.getOtherEdge();
              squareHandlingStrategy.storeMissingSquareEntry(baseEdge, otherEdge, squareReconstructionData.getMissingSquaresData());
              squareHandlingStrategy.storeMissingSquareEntry(otherEdge, baseEdge, squareReconstructionData.getMissingSquaresData());
            });
  }

  private List<Edge> collectCurrentVertexEdges(SquareReconstructionData squareReconstructionData)
  {
    List<Edge> currentVertexEdges = squareReconstructionData.getCurrentVertex().getEdges();
    List<Edge> resultEdges = new ArrayList<>(currentVertexEdges.size());

    currentVertexEdges.stream().forEach(
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
