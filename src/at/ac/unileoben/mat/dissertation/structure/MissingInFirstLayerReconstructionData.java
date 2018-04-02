package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;

public class MissingInFirstLayerReconstructionData
{

  boolean missingInFirstLayerPossible;
  boolean missingInFirstLayer;
  int currentLayerUnitLayerVerticesAmountBeforeAmountCheck;
  List<MergeTagEnum> amountMergeTags;

  public MissingInFirstLayerReconstructionData()
  {
    amountMergeTags = new LinkedList<>();
  }

  public boolean isMissingInFirstLayerPossible()
  {
    return missingInFirstLayerPossible;
  }

  public void setMissingInFirstLayerPossible(boolean missingInFirstLayerPossible)
  {
    this.missingInFirstLayerPossible = missingInFirstLayerPossible;
  }

  public boolean isMissingInFirstLayer()
  {
    return missingInFirstLayer;
  }

  public void setMissingInFirstLayer(boolean missingInFirstLayer)
  {
    this.missingInFirstLayer = missingInFirstLayer;
  }

  public int getCurrentLayerUnitLayerVerticesAmountBeforeAmountCheck()
  {
    return currentLayerUnitLayerVerticesAmountBeforeAmountCheck;
  }

  public void setCurrentLayerUnitLayerVerticesAmountBeforeAmountCheck(int currentLayerUnitLayerVerticesAmountBeforeAmountCheck)
  {
    this.currentLayerUnitLayerVerticesAmountBeforeAmountCheck = currentLayerUnitLayerVerticesAmountBeforeAmountCheck;
  }

  public List<MergeTagEnum> getAmountMergeTags()
  {
    return amountMergeTags;
  }
}
