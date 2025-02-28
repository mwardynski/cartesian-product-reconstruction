package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresEntryData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class NoSquareAtAllMissingSquaresFindingCommons
{

  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

  public List<Edge> collectFollowingEdges(MissingSquaresEntryData missingSquaresEntryData)
  {
    List<Edge> followingEdges = new LinkedList<>();
    int[] edgesByColorCounter = new int[graph.getVertices().size()];
    for (Integer color : missingSquaresEntryData.getExistingColors())
    {
      int currentColorMapping = coloringService.getCurrentColorMapping(graph.getGraphColoring(), color);
      edgesByColorCounter[currentColorMapping] += missingSquaresEntryData.getOtherEdgesByColors()[color].size();
    }

    for (Integer color : missingSquaresEntryData.getExistingColors())
    {
      int currentColorMapping = coloringService.getCurrentColorMapping(graph.getGraphColoring(), color);
      List<Edge> possibleFollowingEdges = missingSquaresEntryData.getOtherEdgesByColors()[color];
      if (edgesByColorCounter[currentColorMapping] == 1 && possibleFollowingEdges.get(0).getEndpoint().getEdges().size() + 1 == possibleFollowingEdges.get(0).getOrigin().getEdges().size())
      {
        followingEdges.add(possibleFollowingEdges.get(0));
      }
    }
    return followingEdges;
  }
}
