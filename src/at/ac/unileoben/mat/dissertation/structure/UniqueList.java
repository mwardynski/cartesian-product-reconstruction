package at.ac.unileoben.mat.dissertation.structure;

import java.util.ArrayList;
import java.util.List;

public class UniqueList
{

  private boolean[] included;
  private List<Integer> entries;

  public UniqueList(int size)
  {
    included = new boolean[size];
    entries = new ArrayList<>(size);
  }

  public UniqueList(UniqueList origin)
  {
    this.included = origin.included;
    this.entries = origin.entries;
  }

  public void add(Integer number)
  {
    if (number != null && !included[number])
    {
      entries.add(number);
      included[number] = true;
    }
  }

  public List<Integer> getEntries()
  {
    return entries;
  }

  public int size()
  {
    return entries.size();
  }
}
