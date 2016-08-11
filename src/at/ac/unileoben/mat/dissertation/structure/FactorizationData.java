package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by mwardynski on 03/04/16.
 */
public class FactorizationData
{
  private int layersAmout;
  private int factorsTotalHeight;
  private List<FactorData> factors;
  private boolean factorizationCompleted;

  public FactorizationData(int layersAmout)
  {
    this.layersAmout = layersAmout;
    factors = new LinkedList<>();
    factorizationCompleted = false;
  }

  public int getLayersAmout()
  {
    return layersAmout;
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

  public boolean isFactorizationCompleted()
  {
    return factorizationCompleted;
  }

  public void setFactorizationCompleted(boolean factorizationCompleted)
  {
    this.factorizationCompleted = factorizationCompleted;
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
