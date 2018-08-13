package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquareReconstructionService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareHandlingStrategy;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareFindingService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

  @Override
  public void reconstructUsingSquares(Edge[][][] squareMatchingEdgesByEdgeAndColor)
  {
    SquareReconstructionData squareReconstructionData = new SquareReconstructionData(graph.getVertices().size());
    squareReconstructionData.getNextVertices().add(graph.getRoot());
    squareReconstructionData.getIncludedVertices()[graph.getRoot().getVertexNo()] = true;
    squareReconstructionData.setSquareMatchingEdgesByEdgeAndColor(squareMatchingEdgesByEdgeAndColor);

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
              Integer vertexNo = graph.getReverseReindexArray()[missingSquare.getVertex().getVertexNo()];
              Integer edge1Endpoint = graph.getReverseReindexArray()[missingSquare.getFirstEdge().getEndpoint().getVertexNo()];
              Integer edge2Endpoint = graph.getReverseReindexArray()[missingSquare.getSecondEdge().getEndpoint().getVertexNo()];
              System.out.println(vertexNo + ": " + edge1Endpoint + "-" + edge2Endpoint);
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

    /*List<Vertex> newNextVertices = currentVertexEdges.stream()
            .map(e -> e.getEndpoint())
            .filter(v -> !squareReconstructionData.getIncludedVertices()[v.getVertexNo(\)])
            .collect(Collectors.toList());
    newNextVertices.stream().forEach(v -> squareReconstructionData.getIncludedVertices()[v.getVertexNo()] = true);
    squareReconstructionData.getNextVertices().addAll(newNextVertices);*/

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

        squareFindingService.findAndProcessSquareForTwoEdges(squareReconstructionData, iEdge, jEdge);
      }
    }
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
