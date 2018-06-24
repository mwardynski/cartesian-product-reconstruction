package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquareReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
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

  @Override
  public void reconstructUsingSquares()
  {
    SquareReconstructionData squareReconstructionData = new SquareReconstructionData(graph.getGraphColoring().getOriginalColorsAmount(), graph.getVertices().size());
    squareReconstructionData.getNextVertices().add(graph.getRoot());

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

    List<Vertex> newNextVertices = currentVertexEdges.stream()
            .map(e -> e.getEndpoint())
            .filter(v -> !squareReconstructionData.getIncludedVertices()[v.getVertexNo()])
            .collect(Collectors.toList());
    newNextVertices.stream().forEach(v -> squareReconstructionData.getIncludedVertices()[v.getVertexNo()] = true);
    squareReconstructionData.getNextVertices().addAll(newNextVertices);

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

        findSquareForTwoEdges(squareReconstructionData, iEdge, jEdge);
      }
    }
    currentVertexEdges.stream()
            .peek(e -> squareReconstructionData.getUsedEdges()[e.getOrigin().getVertexNo()][e.getEndpoint().getVertexNo()] = true)
            .forEach(e -> squareReconstructionData.getUsedEdges()[e.getEndpoint().getVertexNo()][e.getOrigin().getVertexNo()] = true);
    squareReconstructionData.getIncludedVertices()[squareReconstructionData.getCurrentVertex().getVertexNo()] = true;
  }

  @Override
  public boolean findSquareForTwoEdges(SquareReconstructionData squareReconstructionData, Edge iEdge, Edge jEdge)
  {
    Vertex iEdgeEndpoint = iEdge.getEndpoint();
    Vertex jEdgeEndpoint = jEdge.getEndpoint();

    Edge[] iEdgeEndpointAdjacencyVector = new Edge[graph.getVertices().size()];
    iEdgeEndpoint.getEdges().stream().forEach(e -> iEdgeEndpointAdjacencyVector[e.getEndpoint().getVertexNo()] = e);

    Edge[] jEdgeEndpointAdjacencyVector = new Edge[graph.getVertices().size()];
    jEdgeEndpoint.getEdges().stream().forEach(e -> jEdgeEndpointAdjacencyVector[e.getEndpoint().getVertexNo()] = e);

    boolean squareFound = false;
    for (int k = 0; k < iEdgeEndpointAdjacencyVector.length; k++)
    {
      if (k == squareReconstructionData.getCurrentVertex().getVertexNo())
      {
        continue;
      }
      Edge iSquareEdge = jEdgeEndpointAdjacencyVector[k];
      Edge jSquareEdge = iEdgeEndpointAdjacencyVector[k];
      if (iSquareEdge != null && jSquareEdge != null)
      {
        squareFound = true;
        colorEdge(iEdge, iSquareEdge, squareReconstructionData);
        colorEdge(jEdge, jSquareEdge, squareReconstructionData);

        storeSquareFormingEdges(iEdge, jEdge, iSquareEdge, jSquareEdge, squareReconstructionData);

        if (!squareReconstructionData.getIncludedVertices()[k])
        {
          squareReconstructionData.getNextVertices().add(iSquareEdge.getEndpoint());
          squareReconstructionData.getIncludedVertices()[k] = true;
        }
      }
    }
    if (!squareFound)
    {
      MissingSquareData missingSquare = new MissingSquareData(squareReconstructionData.getCurrentVertex(), iEdge, jEdge);
      squareReconstructionData.getMissingSquares().add(missingSquare);
    }

    return squareFound;
  }

  private void colorEdge(Edge baseEdge, Edge squareEdge, SquareReconstructionData squareReconstructionData)
  {
    if (baseEdge.getLabel() != null && squareEdge.getLabel() != null)
    {
      return;
    }
    int color;
    if (baseEdge.getLabel() != null)
    {
      color = baseEdge.getLabel().getColor();
    }
    else
    {
      color = squareReconstructionData.getColorCounter();
      squareReconstructionData.setColorCounter(color + 1);
    }

    if (baseEdge.getLabel() == null)
    {
      baseEdge.setLabel(new Label(color, -1));
    }
    if (squareEdge.getLabel() == null)
    {
      squareEdge.setLabel(new Label(color, -1));
    }

  }

  private void storeSquareFormingEdges(Edge iEdge, Edge jEdge, Edge iSquareEdge, Edge jSquareEdge, SquareReconstructionData squareReconstructionData)
  {
    if (squareReconstructionData.getSquareFormingEdges() == null)
    {
      return;
    }
    storePairOfSquareFormingEdges(iEdge, jEdge, jSquareEdge, squareReconstructionData);
    storePairOfSquareFormingEdges(iEdge.getOpposite(), jSquareEdge, jEdge, squareReconstructionData);
    storePairOfSquareFormingEdges(jEdge, iEdge, iSquareEdge, squareReconstructionData);
    storePairOfSquareFormingEdges(jEdge.getOpposite(), iSquareEdge, iEdge, squareReconstructionData);

    storePairOfSquareFormingEdges(iSquareEdge, jEdge.getOpposite(), jSquareEdge.getOpposite(), squareReconstructionData);
    storePairOfSquareFormingEdges(iSquareEdge.getOpposite(), jSquareEdge.getOpposite(), jEdge.getOpposite(), squareReconstructionData);
    storePairOfSquareFormingEdges(jSquareEdge, iEdge.getOpposite(), iSquareEdge.getOpposite(), squareReconstructionData);
    storePairOfSquareFormingEdges(jSquareEdge.getOpposite(), iSquareEdge.getOpposite(), iEdge.getOpposite(), squareReconstructionData);
  }

  private void storePairOfSquareFormingEdges(Edge referenceEdge, Edge firstParallelEdge, Edge secondParallelEdge, SquareReconstructionData squareReconstructionData)
  {
    Edge[][][] squareFormingEdges = squareReconstructionData.getSquareFormingEdges();
    Edge[] parallelEdges = squareFormingEdges[referenceEdge.getOrigin().getVertexNo()][referenceEdge.getEndpoint().getVertexNo()];

    Edge parallelEdge = parallelEdges[firstParallelEdge.getEndpoint().getVertexNo()];

    if (parallelEdge == squareReconstructionData.getMultipleSquaresWardenEdge())
    {
      return;
    }
    else
    {
      Edge edgeToAssign = secondParallelEdge;

      if (parallelEdge != secondParallelEdge)
      {
        edgeToAssign = squareReconstructionData.getMultipleSquaresWardenEdge();
      }

      parallelEdges[firstParallelEdge.getEndpoint().getVertexNo()] = edgeToAssign;
    }
  }

  private List<Edge> collectCurrentVertexEdges(SquareReconstructionData squareReconstructionData)
  {
    List<Edge> currentVertexEdges = squareReconstructionData.getCurrentVertex().getEdges();
    List<Edge> resultEdges = new ArrayList<>(currentVertexEdges.size());

    currentVertexEdges.stream().forEach(
            e ->
            {
              if (true
//                      !squareReconstructionData.getUsedEdges()[e.getOrigin().getVertexNo()][e.getEndpoint().getVertexNo()]
                      )
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
            }
    );
    return resultEdges;
  }
}
