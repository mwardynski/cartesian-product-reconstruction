package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquareReconstructionService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SingleSquareReconstructionServiceImpl implements SingleSquareReconstructionService
{

  @Autowired
  Graph graph;

  @Override
  public void reconstructUsingSquares(Edge[][][] matchingSquareEdgesByEdgeAndColor)
  {
    SquareReconstructionData squareReconstructionData = new SquareReconstructionData(graph.getGraphColoring().getOriginalColorsAmount(), graph.getVertices().size());
    squareReconstructionData.getNextVertices().add(graph.getRoot());
    squareReconstructionData.getIncludedVertices()[graph.getRoot().getVertexNo()] = true;
    squareReconstructionData.setMatchingSquareEdgesByEdgeAndColor(matchingSquareEdgesByEdgeAndColor);

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

        findAndProcessSquareForTwoEdges(squareReconstructionData, iEdge, jEdge);
      }
    }
    currentVertexEdges.stream()
            .peek(e -> squareReconstructionData.getUsedEdges()[e.getOrigin().getVertexNo()][e.getEndpoint().getVertexNo()] = true)
            .forEach(e -> squareReconstructionData.getUsedEdges()[e.getEndpoint().getVertexNo()][e.getOrigin().getVertexNo()] = true);
  }

  @Override
  public boolean findAndProcessSquareForTwoEdges(SquareReconstructionData squareReconstructionData, Edge iEdge, Edge jEdge)
  {
    List<List<Edge>> squareEdgesList = findSquaresForVertexAndEdge(iEdge.getEndpoint(), jEdge);
    boolean squareFound = CollectionUtils.isNotEmpty(squareEdgesList);

    squareEdgesList.stream()
            .forEach(edgesPair ->
            {
              Edge iSquareEdge = edgesPair.get(0);
              Edge jSquareEdge = edgesPair.get(1);

              if (jEdge.getLabel() != null)
              {
                colorEdge(iEdge, iSquareEdge, jEdge, squareReconstructionData);
                colorEdge(jEdge, jSquareEdge, iEdge, squareReconstructionData);
              }
              else
              {
                colorEdge(jEdge, jSquareEdge, iEdge, squareReconstructionData);
                colorEdge(iEdge, iSquareEdge, jEdge, squareReconstructionData);
              }

              storeSquareFormingEdges(iEdge, jEdge, iSquareEdge, jSquareEdge, squareReconstructionData);

              Vertex iSquareEdgeEndpoint = iSquareEdge.getEndpoint();
              if (!squareReconstructionData.getIncludedVertices()[iSquareEdgeEndpoint.getVertexNo()])
              {
                squareReconstructionData.getNextVertices().add(iSquareEdgeEndpoint);
                squareReconstructionData.getIncludedVertices()[iSquareEdgeEndpoint.getVertexNo()] = true;
              }
            });

    if (!squareFound)
    {
      MissingSquareData missingSquare = new MissingSquareData(squareReconstructionData.getCurrentVertex(), iEdge, jEdge);
      squareReconstructionData.getMissingSquares().add(missingSquare);
    }

    return squareFound;
  }

  private List<List<Edge>> findSquaresForVertexAndEdge(Vertex baseEdgeEndpoint, Edge otherEdge)
  {
    List<List<Edge>> squareEdgesForGivenTwoEdges = otherEdge.getEndpoint().getEdges().stream()
            .filter(edge -> edge != otherEdge.getOpposite())
            .filter(edge -> graph.getAdjacencyMatrix()[baseEdgeEndpoint.getVertexNo()][edge.getEndpoint().getVertexNo()] != null)
            .map(edge -> Arrays.asList(edge, graph.getAdjacencyMatrix()[baseEdgeEndpoint.getVertexNo()][edge.getEndpoint().getVertexNo()]))
            .collect(Collectors.toList());

    return squareEdgesForGivenTwoEdges;
  }

  private void colorEdge(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    if (baseEdge.getLabel() != null && squareEdge.getLabel() != null)
    {
      return;
    }
    int color = -1;
    if (baseEdge.getLabel() != null)
    {
      color = baseEdge.getLabel().getColor();
    }
    else if (squareEdge.getLabel() != null)
    {
      color = squareEdge.getLabel().getColor();
    }
    else
    {
      color = findExtensionColor(baseEdge, squareEdge, otherColorBaseEdge, squareReconstructionData);

      if (color == -1)
      {
        color = squareReconstructionData.getColorCounter();
        squareReconstructionData.setColorCounter(color + 1);
      }
    }


    if (baseEdge.getLabel() == null)
    {
      baseEdge.setLabel(new Label(color, -1));
      baseEdge.getOpposite().setLabel(new Label(color, -1));

      squareReconstructionData.getMatchingSquareEdgesByEdgeAndColor()[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()][otherColorBaseEdge.getLabel().getColor()] = squareEdge;
    }
    if (squareEdge.getLabel() == null)
    {
      squareEdge.setLabel(new Label(color, -1));
      squareEdge.getOpposite().setLabel(new Label(color, -1));

      squareReconstructionData.getMatchingSquareEdgesByEdgeAndColor()[squareEdge.getOrigin().getVertexNo()][squareEdge.getEndpoint().getVertexNo()][otherColorBaseEdge.getLabel().getColor()] = baseEdge;
    }

  }

  private int findExtensionColor(Edge baseEdge, Edge squareEdge, Edge otherColorBaseEdge, SquareReconstructionData squareReconstructionData)
  {
    int extensionColor = -1;

    Edge[][][] matchingSquareEdgesByEdgeAndColor = squareReconstructionData.getMatchingSquareEdgesByEdgeAndColor();
    Edge[] matchingSquareEdgesByColor = matchingSquareEdgesByEdgeAndColor[otherColorBaseEdge.getOrigin().getVertexNo()][otherColorBaseEdge.getEndpoint().getVertexNo()];

    for (int i = 0; i < matchingSquareEdgesByColor.length; i++)
    {
      Edge matchingSquareEdge = matchingSquareEdgesByColor[i];
      if (matchingSquareEdge == null)
      {
        continue;
      }

      List<List<Edge>> squares1 = findSquaresForVertexAndEdge(matchingSquareEdge.getOrigin(), baseEdge);
      List<List<Edge>> squares2 = findSquaresForVertexAndEdge(matchingSquareEdge.getEndpoint(), squareEdge);

      if (CollectionUtils.isEmpty(squares1) && CollectionUtils.isEmpty(squares2))
      {
        extensionColor = i;
        break;
      }
    }
    return extensionColor;
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
