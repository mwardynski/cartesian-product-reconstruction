package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class ResultMissingSquaresData
{
  List<MissingSquaresUniqueEdgesData> resultNoSquareAtAllMissingSquares;
  List<MissingSquaresUniqueEdgesData>[] resultMissingSquaresByColor;
  List<Integer> resultIncludedColors;
  MissingEdgesFormation missingEdgesFormation;

  public ResultMissingSquaresData(List<MissingSquaresUniqueEdgesData> resultNoSquareAtAllMissingSquares,
                                  List<MissingSquaresUniqueEdgesData>[] resultMissingSquaresByColor,
                                  List<Integer> resultIncludedColors,
                                  MissingEdgesFormation missingEdgesFormation)
  {
    this.resultNoSquareAtAllMissingSquares = resultNoSquareAtAllMissingSquares;
    this.resultMissingSquaresByColor = resultMissingSquaresByColor;
    this.resultIncludedColors = resultIncludedColors;
    this.missingEdgesFormation = missingEdgesFormation;
  }

  public List<MissingSquaresUniqueEdgesData> getResultNoSquareAtAllMissingSquares()
  {
    return resultNoSquareAtAllMissingSquares;
  }

  public List<MissingSquaresUniqueEdgesData>[] getResultMissingSquaresByColor()
  {
    return resultMissingSquaresByColor;
  }

  public List<Integer> getResultIncludedColors()
  {
    return resultIncludedColors;
  }

  public MissingEdgesFormation getMissingEdgesFormation()
  {
    return missingEdgesFormation;
  }
}
