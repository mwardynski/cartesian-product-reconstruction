package at.ac.unileoben.mat.dissertation.reconstruction.services.uncoloredpart;

import at.ac.unileoben.mat.dissertation.structure.MissingSquaresUniqueEdgesData;
import at.ac.unileoben.mat.dissertation.structure.NoSquareAtAllGroupsData;

import java.util.List;

public interface PartOfCycleNoSquareAtAllMissingSquaresGroupingService
{
  NoSquareAtAllGroupsData splitPartOfCycleNoSquareAtAllMissingSquaresIntoGroups(List<MissingSquaresUniqueEdgesData> noSquareAtAllMissingSquares);
}
