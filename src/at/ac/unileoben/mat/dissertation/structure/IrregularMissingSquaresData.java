package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class IrregularMissingSquaresData
{
  List<MissingSquaresUniqueEdgesData> irregularColorIndependentMissingSquares;
  List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor;
  List<MissingSquaresUniqueEdgesData> irregularNoSquareAtAllMissingSquares;
  List<MissingSquaresUniqueEdgesData> irregularAccordingToAllColorsMissingSquares;

  public IrregularMissingSquaresData(List<MissingSquaresUniqueEdgesData> irregularColorIndependentMissingSquares, List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor,
                                     List<MissingSquaresUniqueEdgesData> irregularNoSquareAtAllMissingSquares, List<MissingSquaresUniqueEdgesData> irregularAccordingToAllColorsMissingSquares)
  {
    this.irregularColorIndependentMissingSquares = irregularColorIndependentMissingSquares;
    this.irregularMissingSquaresByColor = irregularMissingSquaresByColor;
    this.irregularNoSquareAtAllMissingSquares = irregularNoSquareAtAllMissingSquares;
    this.irregularAccordingToAllColorsMissingSquares = irregularAccordingToAllColorsMissingSquares;
  }

  public List<MissingSquaresUniqueEdgesData> getIrregularColorIndependentMissingSquares()
  {
    return irregularColorIndependentMissingSquares;
  }

  public List<MissingSquaresUniqueEdgesData>[] getIrregularMissingSquaresByColor()
  {
    return irregularMissingSquaresByColor;
  }

  public List<MissingSquaresUniqueEdgesData> getIrregularNoSquareAtAllMissingSquares()
  {
    return irregularNoSquareAtAllMissingSquares;
  }

  public List<MissingSquaresUniqueEdgesData> getIrregularAccordingToAllColorsMissingSquares()
  {
    return irregularAccordingToAllColorsMissingSquares;
  }
}
