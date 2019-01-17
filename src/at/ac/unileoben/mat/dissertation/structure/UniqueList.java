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
    this.included = origin.included.clone();
    this.entries = new ArrayList<>(origin.entries);
  }

  public void add(Integer number)
  {
    if (number != null && !this.contains(number))
    {
      entries.add(number);
      included[number] = true;
    }
  }

  public void addAll(UniqueList otherList)
  {
    otherList.getEntries().forEach(element -> this.add(element));
  }

  public boolean contains(Integer number)
  {
    return included[number];
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
