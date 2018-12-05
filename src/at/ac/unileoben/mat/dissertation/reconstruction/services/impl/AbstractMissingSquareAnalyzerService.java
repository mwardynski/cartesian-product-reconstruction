package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.MissingSquaresAnalyzerService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresEntryData;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresUniqueEdgesData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractMissingSquareAnalyzerService implements MissingSquaresAnalyzerService
{

  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

  protected void groupMissingSquareEntries(List<MissingSquaresEntryData> missingSquaresEntries, List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor)
  {
    for (MissingSquaresEntryData missingSquaresEntry : missingSquaresEntries)
    {
      Edge baseEdge = missingSquaresEntry.getBaseEdge();
      int baseEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), baseEdge.getLabel().getColor());

      List<MissingSquaresUniqueEdgesData> collectedMissingSquares = irregularMissingSquaresByColor[baseEdgeMappedColor];
      if (collectedMissingSquares == null)
      {
        collectedMissingSquares = new LinkedList<>();
        irregularMissingSquaresByColor[baseEdgeMappedColor] = collectedMissingSquares;
      }

      for (Integer otherEdgesColor : missingSquaresEntry.getExistingColors())
      {
        List<Edge> otherEdges = missingSquaresEntry.getOtherEdgesByColors()[otherEdgesColor];

        Iterator<Edge> otherEdgesItertor = otherEdges.iterator();
        while (otherEdgesItertor.hasNext())
        {
          Edge otherEdge = otherEdgesItertor.next();

          MissingSquaresUniqueEdgesData missingSquaresUniqueEdgesData = new MissingSquaresUniqueEdgesData(baseEdge, otherEdge);
          if (baseEdgeMappedColor == 0)
          {
            noSquareAtAllMissingSquares.add(missingSquaresUniqueEdgesData);
          }

          else if (otherEdgesColor != 0)
          {
            collectedMissingSquares.add(missingSquaresUniqueEdgesData);
          }
        }
      }
    }
  }
}
