package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class ResultMissingSquaresData
{
  List<MissingSquaresUniqueEdgesData> resultNoSquareAtAllMissingSquares;
  List<MissingSquaresUniqueEdgesData>[] resultMissingSquaresByColor;
  List<Integer> resultIncludedColors;
  boolean cycleOfIrregularNoSquareAtAllMissingSquares;

  public ResultMissingSquaresData(List<MissingSquaresUniqueEdgesData> resultNoSquareAtAllMissingSquares,
                                  List<MissingSquaresUniqueEdgesData>[] resultMissingSquaresByColor,
                                  List<Integer> resultIncludedColors,
                                  boolean cycleOfIrregularNoSquareAtAllMissingSquares)
  {
    this.resultNoSquareAtAllMissingSquares = resultNoSquareAtAllMissingSquares;
    this.resultMissingSquaresByColor = resultMissingSquaresByColor;
    this.resultIncludedColors = resultIncludedColors;
    this.cycleOfIrregularNoSquareAtAllMissingSquares = cycleOfIrregularNoSquareAtAllMissingSquares;
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

  public boolean isCycleOfIrregularNoSquareAtAllMissingSquares()
  {
    return cycleOfIrregularNoSquareAtAllMissingSquares;
  }
}
