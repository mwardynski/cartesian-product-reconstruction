package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Profile("missingEdges")
public class MissingSquaresForEdgesAnalyzerServiceImpl extends AbstractMissingSquareAnalyzerService
{

  @Autowired
  TestCaseContext testCaseContext;

  @Override
  public void analyseMissingSquares(SquareReconstructionData squareReconstructionData, SquareMatchingEdgeData[][] squareMatchingEdges)
  {
    List<MissingSquaresEntryData> missingSquaresEntries = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntries();

    List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor = new List[graph.getGraphColoring().getColorsMapping().size()];

    groupMissingSquareEntries(missingSquaresEntries, noSquareAtAllMissingSquares, irregularMissingSquaresByColor);

    if (CollectionUtils.isNotEmpty(noSquareAtAllMissingSquares))
    {
      System.out.println("special coloring");
      testCaseContext.setCorrectResult(true);
    }
    else if (CollectionUtils.isNotEmpty(squareReconstructionData.getNoticedPostponedVertices()))
    {
      findResultForColoringIncludingNewColorsAfterPostponedVertex(squareReconstructionData, irregularMissingSquaresByColor);
    }
    else
    {
      findResultForTypicalColoring(irregularMissingSquaresByColor);
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

  private void findResultForColoringIncludingNewColorsAfterPostponedVertex(SquareReconstructionData squareReconstructionData, List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    int afterPostponedVertexColorsLowestIndex = squareReconstructionData.getGraphColoringBeforePostponedVertices().getActualColors().size();

    List<Integer> afterPostponedVertexColors = graph.getGraphColoring().getActualColors().stream()
            .filter(color -> color >= afterPostponedVertexColorsLowestIndex)
            .collect(Collectors.toList());


    selectedColorLoop:
    for (Integer selectedColor : graph.getGraphColoring().getActualColors())
    {
      if (selectedColor == 0)
      {
        continue;
      }
      if (selectedColor >= afterPostponedVertexColorsLowestIndex)
      {
        break;
      }

      for (Integer selectedColorCorrespondingColor : afterPostponedVertexColors)
      {
        List<MissingSquaresUniqueEdgesData>[] currentIrregularMissingSquaresByColor =
                composeCurrentIrregularMissingSquaresByColor(selectedColor, selectedColorCorrespondingColor, afterPostponedVertexColorsLowestIndex, irregularMissingSquaresByColor);
        findResultForIrregularMissingSquaresByColor(selectedColor, currentIrregularMissingSquaresByColor);
        if (testCaseContext.isCorrectResult())
        {
          break selectedColorLoop;
        }
      }
    }
  }

  private void findResultForTypicalColoring(List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    for (Integer selectedColor : graph.getGraphColoring().getActualColors())
    {
      if (selectedColor == 0)
      {
        continue;
      }

      findResultForIrregularMissingSquaresByColor(selectedColor, irregularMissingSquaresByColor);
      if (testCaseContext.isCorrectResult())
      {
        break;
      }
    }
  }

  private List<MissingSquaresUniqueEdgesData>[] composeCurrentIrregularMissingSquaresByColor(int selectedColor, int selectedColorCorrespondingColor, int afterPostponedVertexColorsLowestIndex, List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    int otherColor = graph.getGraphColoring().getActualColors().get(1) != selectedColor
            ? graph.getGraphColoring().getActualColors().get(1)
            : graph.getGraphColoring().getActualColors().get(2);

    List<MissingSquaresUniqueEdgesData>[] currentIrregularMissingSquaresByColor = new List[afterPostponedVertexColorsLowestIndex];
    for (Integer color : graph.getGraphColoring().getActualColors())
    {
      if (color == 0)
      {
        continue;
      }
      if (color < afterPostponedVertexColorsLowestIndex)
      {
        currentIrregularMissingSquaresByColor[color] = new LinkedList(irregularMissingSquaresByColor[color]);
      }
      else if (color == selectedColorCorrespondingColor)
      {
        currentIrregularMissingSquaresByColor[selectedColor].addAll(irregularMissingSquaresByColor[color]);
      }
      else
      {
        currentIrregularMissingSquaresByColor[otherColor].addAll(irregularMissingSquaresByColor[color]);
      }
    }
    return currentIrregularMissingSquaresByColor;
  }

  private void findResultForIrregularMissingSquaresByColor(int selectedColor, List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    if (CollectionUtils.isEmpty(irregularMissingSquaresByColor[selectedColor]))
    {
      return;
    }

    List<Edge> missingEdges = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData> missingSquareEdges = new LinkedList<>();
    boolean[][] collectedMissingEdgesArray = new boolean[graph.getVertices().size()][graph.getVertices().size()];

    collectMissingEdgesForSelectedColor(irregularMissingSquaresByColor, selectedColor, missingEdges, missingSquareEdges, collectedMissingEdgesArray);
    convertMissingSquaresToMissingEdges(missingEdges, missingSquareEdges, collectedMissingEdgesArray);

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
    }
  }

  private void collectMissingEdgesForSelectedColor(List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor, int selectedColor,
                                                   List<Edge> missingEdges, List<MissingSquaresUniqueEdgesData> missingSquareEdges,
                                                   boolean[][] collectedMissingEdgesArray)
  {
    Edge missingEdgesWarden = new Edge(null, null);
    Edge[][] oneEdgeByOtherEdge = new Edge[graph.getVertices().size()][graph.getVertices().size()];

    for (MissingSquaresUniqueEdgesData missingSquare : irregularMissingSquaresByColor[selectedColor])
    {
      Edge baseEdge = missingSquare.getBaseEdge();
      Edge otherEdge = missingSquare.getOtherEdge();

      storeOneEdgeByOtherEdge(oneEdgeByOtherEdge, baseEdge.getOrigin().getVertexNo(), baseEdge.getEndpoint().getVertexNo(), otherEdge, missingEdgesWarden);
      storeOneEdgeByOtherEdge(oneEdgeByOtherEdge, otherEdge.getOrigin().getVertexNo(), otherEdge.getEndpoint().getVertexNo(), baseEdge, missingEdgesWarden);
    }

    for (MissingSquaresUniqueEdgesData missingSquare : irregularMissingSquaresByColor[selectedColor])
    {
      Edge baseEdge = missingSquare.getBaseEdge();
      Edge otherEdge = missingSquare.getOtherEdge();

      Edge matchingEdge = oneEdgeByOtherEdge[baseEdge.getEndpoint().getVertexNo()][baseEdge.getOrigin().getVertexNo()];
      Vertex edgeEndpoint = otherEdge.getEndpoint();
      if (matchingEdge == null || matchingEdge == missingEdgesWarden)
      {
        matchingEdge = oneEdgeByOtherEdge[otherEdge.getEndpoint().getVertexNo()][otherEdge.getOrigin().getVertexNo()];
        edgeEndpoint = baseEdge.getEndpoint();
      }
      if (matchingEdge != null && matchingEdge != missingEdgesWarden
              && !collectedMissingEdgesArray[edgeEndpoint.getVertexNo()][matchingEdge.getEndpoint().getVertexNo()])
      {
        Edge missingEdge = new Edge(edgeEndpoint, matchingEdge.getEndpoint());
        missingEdges.add(missingEdge);

        collectedMissingEdgesArray[edgeEndpoint.getVertexNo()][matchingEdge.getEndpoint().getVertexNo()] = true;
        collectedMissingEdgesArray[matchingEdge.getEndpoint().getVertexNo()][edgeEndpoint.getVertexNo()] = true;
      }
      else if (matchingEdge == null)
      {
        missingSquareEdges.add(missingSquare);
      }
    }
  }

  private void storeOneEdgeByOtherEdge(Edge[][] oneEdgeByOtherEdge, int edgeOriginNo, int edgeEndpointNo, Edge otherEdge, Edge missingEdgesWarden)
  {
    if (oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] == null)
    {
      oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] = otherEdge;
    }
    else
    {
      oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] = missingEdgesWarden;
    }
  }

  private void convertMissingSquaresToMissingEdges(List<Edge> missingEdges, List<MissingSquaresUniqueEdgesData> missingSquareEdges, boolean[][] collectedMissingEdgesArray)
  {
    if (CollectionUtils.isNotEmpty(missingEdges) && CollectionUtils.isNotEmpty(missingSquareEdges))
    {
      Vertex vertexOfMissingEdges;
      Edge firstMissingEdge = missingEdges.get(0);
      if (missingEdges.size() > 1)
      {
        Edge secondMissingEdge = missingEdges.get(1);
        if (firstMissingEdge.getOrigin() == secondMissingEdge.getOrigin() || firstMissingEdge.getOrigin() == secondMissingEdge.getEndpoint())
        {
          vertexOfMissingEdges = firstMissingEdge.getOrigin();
        }
        else
        {
          vertexOfMissingEdges = firstMissingEdge.getEndpoint();
        }
      }
      else
      {
        MissingSquaresUniqueEdgesData firstDoubleMissingEdge = missingSquareEdges.get(0);
        Vertex baseEdgeEndpoint = firstDoubleMissingEdge.getBaseEdge().getEndpoint();
        if (firstMissingEdge.getOrigin() == firstDoubleMissingEdge.getBaseEdge().getEndpoint()
                || firstMissingEdge.getOrigin() == firstDoubleMissingEdge.getOtherEdge().getEndpoint())
        {
          vertexOfMissingEdges = firstMissingEdge.getEndpoint();
        }
        else
        {
          vertexOfMissingEdges = firstMissingEdge.getOrigin();
        }
      }

      missingSquareEdges.forEach(
              missingSquare ->
              {
                saveEdgeToMissingEdges(missingSquare.getBaseEdge(), vertexOfMissingEdges, missingEdges, collectedMissingEdgesArray);
                saveEdgeToMissingEdges(missingSquare.getOtherEdge(), vertexOfMissingEdges, missingEdges, collectedMissingEdgesArray);
              });
    }
  }

  private void saveEdgeToMissingEdges(Edge edge, Vertex vertexOfMissingEdges, List<Edge> missingEdges, boolean[][] collectedMissingEdgesArray)
  {
    if (!collectedMissingEdgesArray[vertexOfMissingEdges.getVertexNo()][edge.getEndpoint().getVertexNo()])
    {
      missingEdges.add(new Edge(vertexOfMissingEdges, edge.getEndpoint()));
      collectedMissingEdgesArray[vertexOfMissingEdges.getVertexNo()][edge.getEndpoint().getVertexNo()] = true;
    }
  }
}
