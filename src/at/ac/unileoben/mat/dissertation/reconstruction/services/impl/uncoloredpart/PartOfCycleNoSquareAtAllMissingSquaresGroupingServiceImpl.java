package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresGroupingService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Component
public class PartOfCycleNoSquareAtAllMissingSquaresGroupingServiceImpl implements PartOfCycleNoSquareAtAllMissingSquaresGroupingService
{
  @Autowired
  Graph graph;

  public NoSquareAtAllGroupsData splitPartOfCycleNoSquareAtAllMissingSquaresIntoGroups(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares)
  {
    List<Edge> boarderNoSquareAtAllEdges = collectBoarderNoSquareAtAllEdges(noSquareAtAllMissingSquares);

    Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints = new Integer[graph.getVertices().size()];
    List<List<Edge>> groupedNoSquareAtAllEdges = new ArrayList<>();

    assignNoSquareAtAllEdgesIntoIncidentGroups(boarderNoSquareAtAllEdges, groupNumbersForNoSquareAtAllEdgesEndpoints, groupedNoSquareAtAllEdges);
    NoSquareAtAllGroupsData noSquareAtAllGroupsData =
            new NoSquareAtAllGroupsData(groupNumbersForNoSquareAtAllEdgesEndpoints, groupedNoSquareAtAllEdges);
    return noSquareAtAllGroupsData;
  }

  private List<Edge> collectBoarderNoSquareAtAllEdges(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares)
  {
    Vertex[] boarderEdgesEndpoints = new Vertex[graph.getVertices().size()];
    List<Edge> boarderNoSquareAtAllEdges = new LinkedList<>();

    noSquareAtAllMissingSquares.stream().forEach(
            missingSquare ->
            {
              Edge baseEdge = missingSquare.getBaseEdge();
              Edge otherEdge = missingSquare.getOtherEdge();
              Vertex middleVertex = baseEdge.getOrigin();

              if (boarderEdgesEndpoints[middleVertex.getVertexNo()] == null)
              {
                if (baseEdge.getLabel().getName() == -2 && otherEdge.getLabel().getName() != -2)
                {
                  boarderNoSquareAtAllEdges.add(baseEdge);
                  boarderEdgesEndpoints[middleVertex.getVertexNo()] = middleVertex;
                }
                else if (otherEdge.getLabel().getName() == -2 && baseEdge.getLabel().getName() != -2)
                {
                  boarderNoSquareAtAllEdges.add(otherEdge);
                  boarderEdgesEndpoints[middleVertex.getVertexNo()] = middleVertex;
                }
              }
            }
    );
    return boarderNoSquareAtAllEdges;
  }


  private void assignNoSquareAtAllEdgesIntoIncidentGroups(List<Edge> boarderNoSquareAtAllEdges, Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints, List<List<Edge>> groupedNoSquareAtAllEdges)
  {
    int groupNumber = 0;
    Edge[][] includedEdges = new Edge[graph.getVertices().size()][graph.getVertices().size()];
    for (Edge boarderNoSquareAtAllEdge : boarderNoSquareAtAllEdges)
    {
      if (groupNumbersForNoSquareAtAllEdgesEndpoints[boarderNoSquareAtAllEdge.getOrigin().getVertexNo()] != null)
      {
        continue;
      }
      else
      {
        groupedNoSquareAtAllEdges.add(new LinkedList<>());
      }

      Queue<Edge> nextEdges = new LinkedList<>();

      nextEdges.add(boarderNoSquareAtAllEdge);
      while (CollectionUtils.isNotEmpty(nextEdges))
      {

        Edge currentEdge = nextEdges.poll();

        groupNumbersForNoSquareAtAllEdgesEndpoints[currentEdge.getOrigin().getVertexNo()] = groupNumber;
        groupNumbersForNoSquareAtAllEdgesEndpoints[currentEdge.getEndpoint().getVertexNo()] = groupNumber;

        groupedNoSquareAtAllEdges.get(groupNumber).add(currentEdge);


        currentEdge.getEndpoint().getEdges().stream()
                .filter((edge -> edge.getOpposite() != currentEdge))
                .filter(edge -> edge.getLabel().getName() == -2)
                .filter(edge -> includedEdges[edge.getOrigin().getVertexNo()][edge.getEndpoint().getVertexNo()] == null)
                .forEach(edge ->
                        {
                          nextEdges.add(edge);
                          includedEdges[edge.getOrigin().getVertexNo()][edge.getEndpoint().getVertexNo()] = edge;
                          includedEdges[edge.getEndpoint().getVertexNo()][edge.getOrigin().getVertexNo()] = edge.getOpposite();
                        }
                );
      }

      groupNumber++;
    }
  }
}
