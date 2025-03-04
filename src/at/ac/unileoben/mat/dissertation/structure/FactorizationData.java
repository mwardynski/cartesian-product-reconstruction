package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mwardynski on 03/04/16.
 */
public class FactorizationData implements Comparable<FactorizationData>
{
  private int maxFactorsHeight;
  private int collectedFactorsTotalHeight;
  private boolean factorizationCompleted;
  private Vertex rootVertex;
  private List<FactorData> factors;
  private int maxConsistentLayerNo;


  private boolean afterConsistencyCheck;
  private FactorizationUnitLayerSpecData[] unitLayerSpecs;


  public FactorizationData(int maxFactorsHeight, Vertex rootVertex, List<FactorData> factors, FactorizationUnitLayerSpecData[] unitLayerSpecs)
  {
    this.maxFactorsHeight = maxFactorsHeight;
    this.rootVertex = rootVertex;
    this.factors = factors;
    this.unitLayerSpecs = unitLayerSpecs;
    this.factorizationCompleted = false;
  }

  public int getMaxFactorsHeight()
  {
    return maxFactorsHeight;
  }

  public boolean isFactorizationCompleted()
  {
    return factorizationCompleted;
  }

  public List<FactorData> getFactors()
  {
    return factors;
  }

  public int getCollectedFactorsTotalHeight()
  {
    return collectedFactorsTotalHeight;
  }

  public void setCollectedFactorsTotalHeight(int collectedFactorsTotalHeight)
  {
    this.collectedFactorsTotalHeight = collectedFactorsTotalHeight;
  }

  public void setFactorizationCompleted(boolean factorizationCompleted)
  {
    this.factorizationCompleted = factorizationCompleted;
  }

  public Vertex getRootVertex()
  {
    return rootVertex;
  }

  public int getMaxConsistentLayerNo()
  {
    return maxConsistentLayerNo;
  }

  public void setMaxConsistentLayerNo(int maxConsistentLayerNo)
  {
    this.maxConsistentLayerNo = maxConsistentLayerNo;
  }

  public boolean isAfterConsistencyCheck()
  {
    return afterConsistencyCheck;
  }

  public void setAfterConsistencyCheck(boolean afterConsistencyCheck)
  {
    this.afterConsistencyCheck = afterConsistencyCheck;
  }

  public FactorizationUnitLayerSpecData[] getUnitLayerSpecs()
  {
    return unitLayerSpecs;
  }

  @Override
  public int compareTo(FactorizationData other)
  {
    if (other == null)
    {
      return 1;
    }
    else if (this.getMaxConsistentLayerNo() == other.getMaxConsistentLayerNo())
    {
      if (this.isAfterConsistencyCheck() && !other.isAfterConsistencyCheck())
      {
        return 1;
      }
      else if (!this.isAfterConsistencyCheck() && other.isAfterConsistencyCheck())
      {
        return -1;
      }
      else
      {
        return 0;
      }
    }
    else
    {
      return this.getMaxConsistentLayerNo() - other.getMaxConsistentLayerNo();
    }
  }

  public static class FactorData
  {
    private List<Vertex> topVertices;
    private int height;
    private int mappedColor;

    public FactorData(List<Vertex> topVertices, int height, int mappedColor)
    {
      this.topVertices = topVertices;
      this.height = height;
      this.mappedColor = mappedColor;
    }

    public List<Vertex> getTopVertices()
    {
      return topVertices;
    }

    public int getHeight()
    {
      return height;
    }

    public int getMappedColor()
    {
      return mappedColor;
    }

    @Override
    public String toString()
    {
      return String.format("v:%d c:%d h:%d", topVertices.stream().map(v -> v.toString()).collect(Collectors.joining(",")), mappedColor, height);
    }
  }

}
