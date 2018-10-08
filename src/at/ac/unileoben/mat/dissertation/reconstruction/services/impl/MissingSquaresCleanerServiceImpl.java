package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.services.MissingSquaresCleanerService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.UncoloredEdgesHandlerService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresEntryData;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;

@Component
public class MissingSquaresCleanerServiceImpl implements MissingSquaresCleanerService
{

  @Autowired
  Graph graph;

  @Autowired
  UncoloredEdgesHandlerService uncoloredEdgesHandlerService;

  @Override
  public void cleanNotValidMissingSquares(SquareReconstructionData squareReconstructionData)
  {
    Iterator<MissingSquaresEntryData> missingSquaresEntriesIterator = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntries().iterator();

    while (missingSquaresEntriesIterator.hasNext())
    {
      MissingSquaresEntryData missingSquareEntry = missingSquaresEntriesIterator.next();

      Edge baseEdge = missingSquareEntry.getBaseEdge();

      Iterator<Integer> otherEdgesColorsIterator = missingSquareEntry.getExistingColors().iterator();
      while (otherEdgesColorsIterator.hasNext())
      {
        Integer otherEdgesColor = otherEdgesColorsIterator.next();

        List<Edge> otherEdges = missingSquareEntry.getOtherEdgesByColors()[otherEdgesColor];
        if (uncoloredEdgesHandlerService.areNormalEdgesOfGivenColorProperty(baseEdge, otherEdges.get(0), true))
        {
          otherEdgesColorsIterator.remove();


          for (Edge otherEdge : otherEdges)
          {
            missingSquareEntry.getIncludedOtherEdges()[otherEdge.getEndpoint().getVertexNo()] = null;
          }
          missingSquareEntry.getOtherEdgesByColors()[otherEdgesColor] = null;
        }
      }

      if (CollectionUtils.isEmpty(missingSquareEntry.getExistingColors()))
      {
        squareReconstructionData.getMissingSquaresData().getMissingSquaresEntriesByBaseEdge()
                [baseEdge.getOrigin().getVertexNo()][baseEdge.getEndpoint().getVertexNo()] = null;

        missingSquaresEntriesIterator.remove();
      }
    }
  }
}
