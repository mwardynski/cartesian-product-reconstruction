package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.*;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import at.ac.unileoben.mat.dissertation.structure.exception.SquareWithoutAnyLabelsException;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

  @Autowired
  SingleSquaresHandlingService singleSquaresHandlingService;

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  UncoloredEdgesHandlerService uncoloredEdgesHandlerService;

  @Autowired
  SquareMatchingEdgesMergingService squareMatchingEdgesMergingService;

  @Override
  public void reconstructUsingSquares(SquareMatchingEdgeData[][] squareMatchingEdgesByEdge)
  {
    SquareReconstructionData squareReconstructionData = new SquareReconstructionData(graph.getVertices().size());
    squareReconstructionData.setSquareMatchingEdgesByEdge(squareMatchingEdgesByEdge);

    singleSquaresHandlingService.collectAllSingleSquares(squareReconstructionData);

    //SXxP3
    if (CollectionUtils.isEmpty(squareReconstructionData.getNextVertices()))
    {
      testCaseContext.setCorrectResult(true);
      return;
    }

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

    squareMatchingEdgesMergingService.mergeColorsBasedOnSquareMatching(squareReconstructionData);
    missingSquaresCleanerService.cleanNotValidMissingSquares(squareReconstructionData);
    missingSquaresAnalyzerService.analyseMissingSquares(squareReconstructionData, squareMatchingEdgesByEdge);
  }

  private void addCurrentVertexToPostponedVertices(SquareReconstructionData squareReconstructionData)
  {
    Vertex currentVertex = squareReconstructionData.getCurrentVertex();
    if (!squareReconstructionData.getIncludedPostponedVertices()[currentVertex.getVertexNo()])
    {
      squareReconstructionData.getIncludedPostponedVertices()[currentVertex.getVertexNo()] = true;
      squareReconstructionData.getPostponedVertices().add(currentVertex);
    }
  }

  private void handleNextPostponedVertex(SquareReconstructionData squareReconstructionData)
  {
    if (CollectionUtils.isNotEmpty(squareReconstructionData.getPostponedVertices()))
    {
      if (CollectionUtils.isEmpty(squareReconstructionData.getNoticedPostponedVertices()))
      {
        GraphColoring graphColoringBeforePostponedVertices = new GraphColoring(graph.getGraphColoring());
        squareReconstructionData.setGraphColoringBeforePostponedVertices(graphColoringBeforePostponedVertices);
      }
      Vertex nextPostponedVertex = squareReconstructionData.getPostponedVertices().poll();
      squareReconstructionData.getNoticedPostponedVertices().add(nextPostponedVertex);
      squareReconstructionData.getNextVertices().add(nextPostponedVertex);
    }
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

        if (iEdge.getLabel() != null && jEdge.getLabel() != null
                && uncoloredEdgesHandlerService.areNormalEdgesOfGivenColorProperty(iEdge, jEdge, true))
        {
          continue;
        }

        boolean squareFound = squareFindingService.findAndProcessSquareForTwoEdges(squareReconstructionData, iEdge, jEdge);


        if (!squareFound && isPotentialMissingSquareWithoutDiagonal(iEdge, jEdge))
        {
          MissingSquaresUniqueEdgesData missingSquare = new MissingSquaresUniqueEdgesData(iEdge, jEdge);
          missingSquares.add(missingSquare);
        }
      }
    }
    squareReconstructionData.getCurrentVertexNeighborsToQueue().stream()
            .forEach(v -> squareHandlingStrategy.queueSquareSideVertexToNextVertices(v, squareReconstructionData));

    List<Edge> edgesWithoutSquare = currentVertexEdges.stream()
            .filter(e -> e.getLabel() == null)
            .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(edgesWithoutSquare))
    {
      squareHandlingStrategy.colorEdgesWithoutSquare(edgesWithoutSquare);
    }

    missingSquares.stream()
            .filter(missingSquare -> !(uncoloredEdgesHandlerService.areNormalEdgesOfGivenColorProperty(missingSquare.getBaseEdge(), missingSquare.getOtherEdge(), true)))
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

  private boolean isPotentialMissingSquareWithoutDiagonal(Edge baseEdge, Edge otherEdge)
  {
    Edge[][] adjacencyMatrix = graph.getAdjacencyMatrix();

    Edge diagonal = adjacencyMatrix[baseEdge.getEndpoint().getVertexNo()][otherEdge.getEndpoint().getVertexNo()];

    return diagonal == null;
  }
}
