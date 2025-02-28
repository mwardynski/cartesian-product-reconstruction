package at.ac.unileoben.mat.dissertation.reconstruction.services.impl.uncoloredpart;

import at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart.SingleNoSquareAtAllMissingSquaresFindingService;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresEntryData;
import at.ac.unileoben.mat.dissertation.structure.MissingSquaresUniqueEdgesData;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Component
public class SingleNoSquareAtAllMissingSquaresForSingleEdgeReconstructionFindingServiceImpl implements SingleNoSquareAtAllMissingSquaresFindingService
{

  @Autowired
  NoSquareAtAllMissingSquaresFindingCommons noSquareAtAllMissingSquaresFindingCommons;

  @Override
  public List<MissingSquaresUniqueEdgesData> findCorrectSingleNoSquareAtAllMissingSquares(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    MissingSquaresUniqueEdgesData missingSquareEdgesPairWithSpikeHavingPotentialFollowing = findMissingSquareEdgesPairWithSpikeHavingPotentialFollowing(noSquareAtAllMissingSquares, squareReconstructionData);
    List<MissingSquaresUniqueEdgesData> correctSingleNoSquareAtAllMissingSquares = new LinkedList<>();

    Edge otherEdge = missingSquareEdgesPairWithSpikeHavingPotentialFollowing.getOtherEdge();
    Edge otherOppositeEdge = otherEdge.getOpposite();
    MissingSquaresEntryData missingSquaresEntryData = squareReconstructionData.getMissingSquaresData()
            .getMissingSquaresEntriesByBaseEdge()[otherOppositeEdge.getOrigin().getVertexNo()][otherOppositeEdge.getEndpoint().getVertexNo()];

    if (otherEdge.getLabel().getColor() != 0)
    {
      Integer arbitraryFollowingEdgeColor = missingSquaresEntryData.getExistingColors().get(0);
      Edge arbitraryFollowingEdge = missingSquaresEntryData.getOtherEdgesByColors()[arbitraryFollowingEdgeColor].get(0);

      MissingSquaresUniqueEdgesData correspondingToSpikeMissingSquareEdgesPair = new MissingSquaresUniqueEdgesData(otherOppositeEdge, arbitraryFollowingEdge);
      correctSingleNoSquareAtAllMissingSquares.addAll(Arrays.asList(missingSquareEdgesPairWithSpikeHavingPotentialFollowing, correspondingToSpikeMissingSquareEdgesPair));
    }
    else
    {
      List<Edge> followingEdges = noSquareAtAllMissingSquaresFindingCommons.collectFollowingEdges(missingSquaresEntryData);

      MissingSquaresUniqueEdgesData correspondingToSpikeMissingSquareEdgesPair = new MissingSquaresUniqueEdgesData(otherOppositeEdge, followingEdges.get(0));
      correctSingleNoSquareAtAllMissingSquares.addAll(Arrays.asList(missingSquareEdgesPairWithSpikeHavingPotentialFollowing, correspondingToSpikeMissingSquareEdgesPair));
      if (followingEdges.size() > 1)
      {
        System.out.println("many solution possibilities - SPIKE");
      }
    }

    return correctSingleNoSquareAtAllMissingSquares;
  }

  private MissingSquaresUniqueEdgesData findMissingSquareEdgesPairWithSpikeHavingPotentialFollowing(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares, SquareReconstructionData squareReconstructionData)
  {
    return noSquareAtAllMissingSquares.stream()
            .filter(missingSquare -> missingSquare.getBaseEdge().getEndpoint().getEdges().size() == 1)
            .filter(missingSquare ->
            {
              Edge otherOppositeEdge = missingSquare.getOtherEdge().getOpposite();
              MissingSquaresEntryData missingSquaresEntryData = squareReconstructionData.getMissingSquaresData().getMissingSquaresEntriesByBaseEdge()[otherOppositeEdge.getOrigin().getVertexNo()][otherOppositeEdge.getEndpoint().getVertexNo()];
              return missingSquaresEntryData != null && CollectionUtils.isNotEmpty(missingSquaresEntryData.getExistingColors());
            })
            .findAny().get();
  }
}
