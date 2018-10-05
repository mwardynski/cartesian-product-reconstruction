package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

public class IrregularMissingSquaresData
{
  List<MissingSquaresUniqueEdgesData> irregularColorIndependentMissingSquares;
  List<MissingSquaresUniqueEdgesData> irregularNoSquareAtAllMissingSquares;
  List<MissingSquaresUniqueEdgesData> irregularAccordingToAllColorsMissingSquares;
  List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor;
  List<Integer> includedColors;

  public IrregularMissingSquaresData(List<MissingSquaresUniqueEdgesData> irregularColorIndependentMissingSquares, List<MissingSquaresUniqueEdgesData>[] irregularMissingSquaresByColor,
                                     List<MissingSquaresUniqueEdgesData> irregularNoSquareAtAllMissingSquares, List<MissingSquaresUniqueEdgesData> irregularAccordingToAllColorsMissingSquares,
                                     List<Integer> includedColors)
  {
    this.irregularColorIndependentMissingSquares = irregularColorIndependentMissingSquares;
    this.irregularMissingSquaresByColor = irregularMissingSquaresByColor;
    this.irregularNoSquareAtAllMissingSquares = irregularNoSquareAtAllMissingSquares;
    this.irregularAccordingToAllColorsMissingSquares = irregularAccordingToAllColorsMissingSquares;
    this.includedColors = includedColors;
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

  public List<Integer> getIncludedColors()
  {
    return includedColors;
  }
}
