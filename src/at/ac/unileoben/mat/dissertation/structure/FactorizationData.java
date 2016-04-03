package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mwardynski on 03/04/16.
 */
public class FactorizationData
{
  private int factorsTotalHeight;
  private List<FactorData> factors;

  public FactorizationData()
  {
    factors = new LinkedList<>();
  }

  public int getFactorsTotalHeight()
  {
    return factorsTotalHeight;
  }

  public void setFactorsTotalHeight(int factorsTotalHeight)
  {
    this.factorsTotalHeight = factorsTotalHeight;
  }

  public List<FactorData> getFactors()
  {
    return factors;
  }

  public static class FactorData
  {
    private Vertex topVertex;
    private int height;
    private int mappedColor;

    public FactorData(Vertex topVertex, int height, int mappedColor)
    {
      this.topVertex = topVertex;
      this.height = height;
      this.mappedColor = mappedColor;
    }

    @Override
    public String toString()
    {
      return String.format("v:%d c:%d h:%d", topVertex.getVertexNo(), mappedColor, height);
    }
  }
}
