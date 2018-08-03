package at.ac.unileoben.mat.dissertation.structure.exception;

public class CompleteMergeException extends RuntimeException
{
  private int layerNo;
  private boolean afterConsistencyCheck;

  public CompleteMergeException(int layerNo, boolean afterConsistencyCheck)
  {
    this.layerNo = layerNo;
    this.afterConsistencyCheck = afterConsistencyCheck;
  }

  public int getLayerNo()
  {
    return layerNo;
  }

  public boolean getAfterConsistencyCheck()
  {
    return afterConsistencyCheck;
  }
}
