package at.ac.unileoben.mat.dissertation.structure;

/**
 * Created by mwardynski on 19/11/16.
 */
public class FactorizationUnitLayerSpecData
{
  private int mappedColor;
  private int unitLayerSize;

  public FactorizationUnitLayerSpecData(int mappedColor, int unitLayerSize)
  {
    this.mappedColor = mappedColor;
    this.unitLayerSize = unitLayerSize;
  }

  public int getMappedColor()
  {
    return mappedColor;
  }

  public int getUnitLayerSize()
  {
    return unitLayerSize;
  }

  public void setUnitLayerSize(int unitLayerSize)
  {
    this.unitLayerSize = unitLayerSize;
  }
}
