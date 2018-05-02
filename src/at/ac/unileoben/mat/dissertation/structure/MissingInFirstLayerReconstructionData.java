package at.ac.unileoben.mat.dissertation.structure;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class MissingInFirstLayerReconstructionData
{

  Optional<Boolean> missingInFirstLayer;
  int currentLayerUnitLayerVerticesAmountBeforeAmountCheck;
  List<MergeTagEnum> amountMergeTags;

  public MissingInFirstLayerReconstructionData()
  {
    missingInFirstLayer = Optional.empty();
    amountMergeTags = new LinkedList<>();
  }

  public Optional<Boolean> getMissingInFirstLayer()
  {
    return missingInFirstLayer;
  }

  public void setMissingInFirstLayer(Optional<Boolean> missingInFirstLayer)
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
