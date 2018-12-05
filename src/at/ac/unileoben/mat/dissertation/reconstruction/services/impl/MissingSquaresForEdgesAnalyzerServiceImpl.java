package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.SingleSquaresHandlingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.SquareFindingService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Component
@Profile("missingEdges")
public class MissingSquaresForEdgesAnalyzerServiceImpl extends AbstractMissingSquareAnalyzerService
{

  @Autowired
  TestCaseContext testCaseContext;

  @Autowired
  SquareFindingService squareFindingService;

  @Autowired
  SingleSquaresHandlingService singleSquaresHandlingService;

  @Override
  public void analyseMissingSquares(SquareReconstructionData squareReconstructionData, SquareMatchingEdgeData[][] squareMatchingEdges)
  {
    List<MissingSquaresEntryData> missingSquaresEntries = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntries();

    List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor = new List[graph.getVertices().size()];

    groupMissingSquareEntries(missingSquaresEntries, noSquareAtAllMissingSquares, irregularMissingSquaresByColor);

    if (CollectionUtils.isNotEmpty(noSquareAtAllMissingSquares))
    {
      System.out.println("special coloring");
      testCaseContext.setCorrectResult(true);
    }
    else if (CollectionUtils.isNotEmpty(squareReconstructionData.getNoticedPostponedVertices()))
    {
      System.out.println("new coloring after postponed vertex");
      testCaseContext.setCorrectResult(true);
    }
    else
    {
      for (int selectedColor = 1; selectedColor < irregularMissingSquaresByColor.length; selectedColor++)
      {

        if (CollectionUtils.isEmpty(irregularMissingSquaresByColor[selectedColor]))
        {
          continue;
        }

        List<Edge> missingEdges = new LinkedList<>();
        List<Edge> firstPhaseMissingEdges = new LinkedList<>();
        boolean[][] collectedMissingEdgesArray = new boolean[graph.getVertices().size()][graph.getVertices().size()];
        List<MissingSquaresUniqueEdgesData>[] reconstructionMissingSquaresByColor = new List[graph.getVertices().size()];

        collectMissingEdgesForSelectedColor(irregularMissingSquaresByColor, selectedColor, missingEdges, collectedMissingEdgesArray);
        if (CollectionUtils.isNotEmpty(missingEdges))
        {
          firstPhaseMissingEdges.addAll(missingEdges);
          reconstructEdges(missingEdges, reconstructionMissingSquaresByColor, irregularMissingSquaresByColor, squareReconstructionData);
          collectMissingEdgesForSelectedColor(irregularMissingSquaresByColor, selectedColor, missingEdges, collectedMissingEdgesArray);
        }

        boolean[][] reconstructedEdgesArray = new boolean[graph.getVertices().size()][graph.getVertices().size()];
        for (Edge missingEdge : missingEdges)
        {
          Integer originVertexNo = graph.getReverseReindexArray()[missingEdge.getOrigin().getVertexNo()];
          Integer endpointVertexNo = graph.getReverseReindexArray()[missingEdge.getEndpoint().getVertexNo()];

          reconstructedEdgesArray[originVertexNo][endpointVertexNo] = true;
          reconstructedEdgesArray[endpointVertexNo][originVertexNo] = true;
        }


        boolean correctResult = testCaseContext.getRemovedEdges().size() == missingEdges.size();
        for (Edge removedEdge : testCaseContext.getRemovedEdges())
        {
          if (!reconstructedEdgesArray[removedEdge.getOrigin().getVertexNo()][removedEdge.getEndpoint().getVertexNo()])
          {
            correctResult = false;
          }
        }
        if (correctResult)
        {
          testCaseContext.setCorrectResult(true);
          break;
        }
        else
        {
          revertReconstructedEdges(firstPhaseMissingEdges, reconstructionMissingSquaresByColor, irregularMissingSquaresByColor, squareReconstructionData);
        }
      }

      if (testCaseContext.isCorrectResult())
      {
        System.out.println("OK!");
      }
      else
      {
        System.out.println("WRONG!");
      }
    }
  }

  private List<Edge> collectMissingEdgesForSelectedColor(List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor, int selectedColor,
                                                         List<Edge> missingEdges, boolean[][] collectedMissingEdgesArray)
  {
    for (int currentColor = 1; currentColor < irregularMissingSquaresByColor.length; currentColor++)
    {
      if (CollectionUtils.isEmpty(irregularMissingSquaresByColor[currentColor]))
      {
        continue;
      }

      Edge[][] currentColorOneEdgeByOtherEdge = new Edge[graph.getVertices().size()][graph.getVertices().size()];

      for (MissingSquaresUniqueEdgesData missingSquare : irregularMissingSquaresByColor[currentColor])
      {
        Edge baseEdge = missingSquare.getBaseEdge();
        Edge otherEdge = missingSquare.getOtherEdge();

        int otherEdgeColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), otherEdge.getLabel().getColor());

        if ((currentColor == selectedColor && otherEdgeColor != selectedColor) ||
                (currentColor != selectedColor && otherEdgeColor == selectedColor))
        {
          currentColorOneEdgeByOtherEdge[baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()] = otherEdge;
          currentColorOneEdgeByOtherEdge[otherEdge.getOrigin().getVertexNo()][otherEdge.getEndpoint().getVertexNo()] = baseEdge;
        }
      }

      for (MissingSquaresUniqueEdgesData missingSquare : irregularMissingSquaresByColor[selectedColor])
      {
        Edge baseEdge = missingSquare.getBaseEdge();
        Edge otherEdge = missingSquare.getOtherEdge();

        Edge matchingBaseEdge = currentColorOneEdgeByOtherEdge[baseEdge.getEndpoint().getVertexNo()][baseEdge.getOrigin().getVertexNo()];
        Vertex edgeEndpoint = otherEdge.getEndpoint();
        if (matchingBaseEdge == null)
        {
          matchingBaseEdge = currentColorOneEdgeByOtherEdge[otherEdge.getEndpoint().getVertexNo()][otherEdge.getOrigin().getVertexNo()];
          edgeEndpoint = baseEdge.getEndpoint();
        }
        if (matchingBaseEdge != null
                && !collectedMissingEdgesArray[edgeEndpoint.getVertexNo()][matchingBaseEdge.getEndpoint().getVertexNo()])
        {
          Edge missingEdge = new Edge(edgeEndpoint, matchingBaseEdge.getEndpoint());
          missingEdges.add(missingEdge);

          collectedMissingEdgesArray[edgeEndpoint.getVertexNo()][matchingBaseEdge.getEndpoint().getVertexNo()] = true;
          collectedMissingEdgesArray[matchingBaseEdge.getEndpoint().getVertexNo()][edgeEndpoint.getVertexNo()] = true;
        }
      }
    }
    return missingEdges;
  }


  //FIXME not new entries in the list of squares
  private void reconstructEdges(List<Edge> missingEdges, List<MissingSquaresUniqueEdgesData>[] reconstructionMissingSquaresByColor,
                                List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor,
                                SquareReconstructionData squareReconstructionData)
  {
    for (Edge missingEdge : missingEdges)
    {
      Edge oppositeMissingEdge = new Edge(missingEdge.getEndpoint(), missingEdge.getOrigin());
      missingEdge.setOpposite(oppositeMissingEdge);
      oppositeMissingEdge.setOpposite(missingEdge);

      missingEdge.getOrigin().getEdges().add(missingEdge);
      missingEdge.getEndpoint().getEdges().add(oppositeMissingEdge);

      graph.getAdjacencyMatrix()[missingEdge.getOrigin().getVertexNo()][missingEdge.getEndpoint().getVertexNo()] = missingEdge;
      graph.getAdjacencyMatrix()[oppositeMissingEdge.getOrigin().getVertexNo()][oppositeMissingEdge.getEndpoint().getVertexNo()] = oppositeMissingEdge;

      singleSquaresHandlingService.findSquaresForSingleEdge(missingEdge, squareReconstructionData, false);
      singleSquaresHandlingService.findSquaresForSingleEdge(oppositeMissingEdge, squareReconstructionData, false);
    }

    for (Edge missingEdge : missingEdges)
    {
      colorSingleMissingEdge(missingEdge, reconstructionMissingSquaresByColor,
              irregularMissingSquaresByColor, squareReconstructionData);
      colorSingleMissingEdge(missingEdge.getOpposite(), reconstructionMissingSquaresByColor,
              irregularMissingSquaresByColor, squareReconstructionData);

    }
  }

  private void colorSingleMissingEdge(Edge missingEdge, List<MissingSquaresUniqueEdgesData>[] reconstructionMissingSquaresByColor,
                                      List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor,
                                      SquareReconstructionData squareReconstructionData)
  {
    List<MissingSquaresUniqueEdgesData> missingSquares = new LinkedList<>();
    for (Edge edge : missingEdge.getOrigin().getEdges())
    {
      if (edge == missingEdge)
      {
        continue;
      }
      boolean squareFound = squareFindingService.findAndProcessSquareForTwoEdges(squareReconstructionData, edge, missingEdge);

      if (!squareFound)
      {
        MissingSquaresUniqueEdgesData missingSquare = new MissingSquaresUniqueEdgesData(edge, missingEdge);
        missingSquares.add(missingSquare);
      }
    }

    missingSquares.stream()
            .forEach(missingSquare ->
            {
              Edge baseEdge = missingSquare.getBaseEdge();
              Edge otherEdge = missingSquare.getOtherEdge();

              addSingleReconstructionMissingSquare(baseEdge, otherEdge, reconstructionMissingSquaresByColor);
              addSingleReconstructionMissingSquare(otherEdge, baseEdge, reconstructionMissingSquaresByColor);
            });


    for (int color = 1; color < irregularMissingSquaresByColor.length; color++)
    {
      if (CollectionUtils.isNotEmpty(reconstructionMissingSquaresByColor[color]))
      {
        irregularMissingSquaresByColor[color].addAll(reconstructionMissingSquaresByColor[color]);
      }
    }
  }

  private void addSingleReconstructionMissingSquare(Edge baseEdge, Edge otherEdge, List<MissingSquaresUniqueEdgesData>[] reconstructionMissingSquaresByColor)
  {
    int baseEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), baseEdge.getLabel().getColor());
    int otherEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), otherEdge.getLabel().getColor());

    if (baseEdgeMappedColor != otherEdgeMappedColor)
    {
      List<MissingSquaresUniqueEdgesData> reconstructionMissingSquares = reconstructionMissingSquaresByColor[baseEdgeMappedColor];
      if (reconstructionMissingSquares == null)
      {
        reconstructionMissingSquares = new LinkedList<>();
        reconstructionMissingSquaresByColor[baseEdgeMappedColor] = reconstructionMissingSquares;
      }

      MissingSquaresUniqueEdgesData reconstructionMissingSquare = new MissingSquaresUniqueEdgesData(baseEdge, otherEdge);
      reconstructionMissingSquares.add(reconstructionMissingSquare);
    }
  }

  private void revertReconstructedEdges(List<Edge> missingEdges, List<MissingSquaresUniqueEdgesData>[] reconstructionMissingSquaresByColor,
                                        List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor, SquareReconstructionData squareReconstructionData)
  {
    Collections.reverse(missingEdges);
    for (Edge missingEdge : missingEdges)
    {
      //FIXME replace remove() by removing from the end of the list
      missingEdge.getOrigin().getEdges().remove(missingEdge);
      missingEdge.getEndpoint().getEdges().remove(missingEdge.getOpposite());

      graph.getAdjacencyMatrix()[missingEdge.getOrigin().getVertexNo()][missingEdge.getEndpoint().getVertexNo()] = null;
      graph.getAdjacencyMatrix()[missingEdge.getEndpoint().getVertexNo()][missingEdge.getOrigin().getVertexNo()] = null;

      revertFoundSquaresForReconstructedEdge(missingEdge, squareReconstructionData);
      revertFoundSquaresForReconstructedEdge(missingEdge.getOpposite(), squareReconstructionData);
    }

    for (int color = 1; color < reconstructionMissingSquaresByColor.length; color++)
    {
      if (CollectionUtils.isNotEmpty(reconstructionMissingSquaresByColor[color]))
      {
        for (MissingSquaresUniqueEdgesData missingSquare : reconstructionMissingSquaresByColor[color])
        {
          //FIXME replace remove() by removing from the end of the list
          irregularMissingSquaresByColor[color].remove(missingSquare);
        }
      }
    }
  }

  private void revertFoundSquaresForReconstructedEdge(Edge reconstructedEdge, SquareReconstructionData squareReconstructionData)
  {
    SingleSquareList[][][] squares = squareReconstructionData.getSquares();

    SingleSquareList[] reconstructedEdgeSquares = squares[reconstructedEdge.getOrigin().getVertexNo()][reconstructedEdge.getEndpoint().getVertexNo()];

    for (Edge edge : reconstructedEdge.getOrigin().getEdges())
    {
      reconstructedEdgeSquares[edge.getEndpoint().getVertexNo()] = null;
    }
    squares[reconstructedEdge.getOrigin().getVertexNo()][reconstructedEdge.getEndpoint().getVertexNo()] = new SingleSquareList[graph.getVertices().size()];
  }
}
