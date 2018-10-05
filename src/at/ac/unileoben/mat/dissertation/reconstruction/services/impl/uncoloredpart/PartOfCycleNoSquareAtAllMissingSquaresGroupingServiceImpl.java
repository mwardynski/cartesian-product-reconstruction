package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.PartOfCycleNoSquareAtAllMissingSquaresGroupingService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Component
public class PartOfCycleNoSquareAtAllMissingSquaresGroupingServiceImpl implements PartOfCycleNoSquareAtAllMissingSquaresGroupingService
{
  @Autowired
  Graph graph;

  public NoSquareAtAllGroupsData splitPartOfCycleNoSquareAtAllMissingSquaresIntoGroups(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares)
  {
    List<Edge> boarderNoSquareAtAllEdges = new LinkedList<>();
    Edge[][] noSquareAtAllEdgesByEndpoints = new Edge[graph.getVertices().size()][2];

    noSquareAtAllMissingSquares.stream().forEach(
            missingSquare ->
            {
              Edge baseEdge = missingSquare.getBaseEdge();
              Edge otherEdge = missingSquare.getOtherEdge();

              if (baseEdge.getLabel().getName() == -2)
              {
                assignNoSquareAtAllEdgesToArrays(baseEdge, otherEdge, boarderNoSquareAtAllEdges, noSquareAtAllEdgesByEndpoints);
              }
              if (otherEdge.getLabel().getName() == -2)
              {
                assignNoSquareAtAllEdgesToArrays(otherEdge, baseEdge, boarderNoSquareAtAllEdges, noSquareAtAllEdgesByEndpoints);
              }
            }
    );

    Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints = new Integer[graph.getVertices().size()];
    List<List<Edge>> groupedNoSquareAtAllEdges = new ArrayList<>();

    assignNoSquareAtAllEdgesIntoIncidentGroups(boarderNoSquareAtAllEdges, noSquareAtAllEdgesByEndpoints, groupNumbersForNoSquareAtAllEdgesEndpoints, groupedNoSquareAtAllEdges);
    NoSquareAtAllGroupsData noSquareAtAllGroupsData =
            new NoSquareAtAllGroupsData(groupNumbersForNoSquareAtAllEdgesEndpoints, groupedNoSquareAtAllEdges);
    return noSquareAtAllGroupsData;
  }

  private void assignNoSquareAtAllEdgesToArrays(Edge edge1, Edge edge2, List<Edge> boarderNoSquareAtAllEdges, Edge[][] noSquareAtAllEdgesByEndpoints)
  {
    Edge[] boarderNoSquareAtAllEdgesByEndpoints = new Edge[graph.getVertices().size()];
    int originVertexNo = edge1.getOrigin().getVertexNo();
    int endpointVertexNo = edge1.getEndpoint().getVertexNo();

    boolean assignedToOriginNo = assignEdgeToArray(edge1, originVertexNo, noSquareAtAllEdgesByEndpoints);
    boolean assignedToEndpointNo = assignEdgeToArray(edge1.getOpposite(), endpointVertexNo, noSquareAtAllEdgesByEndpoints);

    if (!assignedToOriginNo || !assignedToEndpointNo)
    {
      throw new RuntimeException("!assignedToOriginNo || !assignedToEndpointNo");
    }

    if (edge2.getLabel().getName() != -2 && boarderNoSquareAtAllEdgesByEndpoints[edge1.getOrigin().getVertexNo()] == null)
    {
      boarderNoSquareAtAllEdges.add(edge1);
      boarderNoSquareAtAllEdgesByEndpoints[edge1.getOrigin().getVertexNo()] = edge1;
    }
  }

  private boolean assignEdgeToArray(Edge edge, int vertexNo, Edge[][] noSquareAtAllEdgesByEndpoint)
  {
    boolean edgeAssigned = false;
    for (int i = 0; i < 2; i++)
    {
      if (noSquareAtAllEdgesByEndpoint[vertexNo][i] == null ||
              noSquareAtAllEdgesByEndpoint[vertexNo][i] == edge)
      {
        noSquareAtAllEdgesByEndpoint[vertexNo][i] = edge;
        edgeAssigned = true;
        break;
      }
    }
    return edgeAssigned;
  }

  private void assignNoSquareAtAllEdgesIntoIncidentGroups(List<Edge> boarderNoSquareAtAllEdges, Edge[][] noSquareAtAllEdgesByEndpoints, Integer[] groupNumbersForNoSquareAtAllEdgesEndpoints, List<List<Edge>> groupedNoSquareAtAllEdges)
  {
    int groupNumber = 0;
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

      Edge currentEdge = boarderNoSquareAtAllEdge;
      while (currentEdge != null)
      {
        groupNumbersForNoSquareAtAllEdgesEndpoints[currentEdge.getOrigin().getVertexNo()] = groupNumber;
        groupNumbersForNoSquareAtAllEdgesEndpoints[currentEdge.getEndpoint().getVertexNo()] = groupNumber;

        groupedNoSquareAtAllEdges.get(groupNumber).add(currentEdge);

        Edge possibleNextEdge1 = noSquareAtAllEdgesByEndpoints[currentEdge.getEndpoint().getVertexNo()][0];
        Edge possibleNextEdge2 = noSquareAtAllEdgesByEndpoints[currentEdge.getEndpoint().getVertexNo()][1];
        if (possibleNextEdge1 != null && possibleNextEdge1 != currentEdge.getOpposite())
        {
          currentEdge = possibleNextEdge1;
        }
        else if (possibleNextEdge2 != null && possibleNextEdge2 != currentEdge.getOpposite())
        {
          currentEdge = possibleNextEdge2;
        }
        else
        {
          currentEdge = null;
        }
      }

      groupNumber++;
    }
  }
}
