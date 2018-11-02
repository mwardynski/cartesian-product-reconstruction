package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;

public class MissingSquaresData
{
  List<MissingSquaresEntryData> missingSquaresEntries;
  MissingSquaresEntryData[][] missingSquaresEntriesByBaseEdge;


  public MissingSquaresData(int size)
  {
    this.missingSquaresEntriesByBaseEdge = new MissingSquaresEntryData[size][size];
    missingSquaresEntries = new LinkedList<>();
  }


  public List<MissingSquaresEntryData> getMissingSquaresEntries()
  {
    return missingSquaresEntries;
  }

  public MissingSquaresEntryData[][] getMissingSquaresEntriesByBaseEdge()
  {
    return missingSquaresEntriesByBaseEdge;
  }


}
