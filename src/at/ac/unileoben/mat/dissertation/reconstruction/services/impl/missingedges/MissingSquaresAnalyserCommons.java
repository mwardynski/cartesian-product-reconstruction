package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.missingedges;

import at.ac.unileoben.mat.dissertation.reconstruction.services.impl.MissingSquaresForEdgesAnalyzerServiceImpl;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class MissingSquaresAnalyserCommons
{
  @Autowired
  Graph graph;

  @Autowired
  TestCaseContext testCaseContext;

  public void checkSelectedVertexCorrectness(Vertex vertexToRemoveForResult)
  {
    if (vertexToRemoveForResult == null)
    {
      return;
    }
    Integer vertexNumberToRemoveForResult = graph.getReverseReindexArray()[vertexToRemoveForResult.getVertexNo()];
    boolean correctResult = testCaseContext.getVerticesToRemoveForResult().stream()
            .filter(acceptableVertex -> acceptableVertex.getVertexNo() == vertexNumberToRemoveForResult)
            .findAny().isPresent();
    testCaseContext.setCorrectResult(correctResult);
  }

  public Edge[][] findMissingSquarePairsForSelectedColor(List<MissingSquaresUniqueEdgesData> selectedIrregularMissingSquaresByColor, Edge missingEdgesWarden)
  {
    Edge[][] oneEdgeByOtherEdge = new Edge[graph.getVertices().size()][graph.getVertices().size()];

    for (MissingSquaresUniqueEdgesData missingSquare : selectedIrregularMissingSquaresByColor)
    {
      Edge baseEdge = missingSquare.getBaseEdge();
      Edge otherEdge = missingSquare.getOtherEdge();

      storeOneEdgeByOtherEdge(oneEdgeByOtherEdge, baseEdge, otherEdge, missingEdgesWarden);
      storeOneEdgeByOtherEdge(oneEdgeByOtherEdge, otherEdge, baseEdge, missingEdgesWarden);
    }
    return oneEdgeByOtherEdge;
  }

  private void storeOneEdgeByOtherEdge(Edge[][] oneEdgeByOtherEdge, Edge baseEdge, Edge otherEdge, Edge missingEdgesWarden)
  {
    if (baseEdge.getLabel().getColor() == 0)
    {
      return;
    }

    int edgeOriginNo = baseEdge.getOrigin().getVertexNo();
    int edgeEndpointNo = baseEdge.getEndpoint().getVertexNo();
    if (oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] == null)
    {
      oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] = otherEdge;
    }
    else
    {
      oneEdgeByOtherEdge[edgeOriginNo][edgeEndpointNo] = missingEdgesWarden;
    }
  }

  public void findResultForIrregularMissingSquaresByColor(int selectedColor, List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    if (CollectionUtils.isEmpty(irregularMissingSquaresByColor[selectedColor]))
    {
      return;
    }

    List<Edge> missingEdges = new LinkedList<>();
    List<MissingSquaresUniqueEdgesData> missingSquareEdges = new LinkedList<>();
    boolean[][] collectedMissingEdgesArray = new boolean[graph.getVertices().size()][graph.getVertices().size()];

    Edge missingEdgesWarden = new Edge(null, null);
    Edge[][] missingSquarePairsForSelectedColor = findMissingSquarePairsForSelectedColor(irregularMissingSquaresByColor[selectedColor], missingEdgesWarden);
    collectMissingEdgesForSelectedColor(irregularMissingSquaresByColor[selectedColor], missingEdges, missingSquareEdges, missingSquarePairsForSelectedColor, collectedMissingEdgesArray, missingEdgesWarden);
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

  public void collectMissingEdgesForSelectedColor(List<MissingSquaresUniqueEdgesData> selectedIrregularMissingSquaresByColor,
                                                  List<Edge> missingEdges, List<MissingSquaresUniqueEdgesData> missingSquareEdges,
                                                  Edge[][] missingSquarePairsForSelectedColor,
                                                  boolean[][] collectedMissingEdgesArray, Edge missingEdgesWarden)
  {
    for (MissingSquaresUniqueEdgesData missingSquare : selectedIrregularMissingSquaresByColor)
    {
      Edge baseEdge = missingSquare.getBaseEdge();
      Edge otherEdge = missingSquare.getOtherEdge();

      Edge matchingEdge = missingSquarePairsForSelectedColor[baseEdge.getEndpoint().getVertexNo()][baseEdge.getOrigin().getVertexNo()];
      Vertex edgeEndpoint = otherEdge.getEndpoint();
      if (matchingEdge == null || matchingEdge == missingEdgesWarden)
      {
        matchingEdge = missingSquarePairsForSelectedColor[otherEdge.getEndpoint().getVertexNo()][otherEdge.getOrigin().getVertexNo()];
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
