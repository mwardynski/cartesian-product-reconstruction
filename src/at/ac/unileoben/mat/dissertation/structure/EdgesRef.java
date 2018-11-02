package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-30
 * Time: 11:38
 * To change this template use File | Settings | File Templates.
 */
public class EdgesRef
{
  List<ColorGroupLocation> colorPositions;

  public List<ColorGroupLocation> getColorPositions()
  {
    return colorPositions;
  }

  public void setColorPositions(List<ColorGroupLocation> colorPositions)
  {
    this.colorPositions = colorPositions;
  }

  @Override
  public String toString()
  {
    return colorPositions.toString();
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    EdgesRef edgesRef = (EdgesRef) o;

    return colorPositions.toString().equals(edgesRef.colorPositions.toString());
  }
}
