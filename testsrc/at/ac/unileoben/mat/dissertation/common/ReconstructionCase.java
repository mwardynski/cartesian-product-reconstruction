package at.ac.unileoben.mat.dissertation.common;

public class ReconstructionCase extends FactorizationCase
{
  private Integer vertexNoToRemove;

  public ReconstructionCase(String fileName, int amountOfFactors)
  {
    super(fileName, amountOfFactors);
  }

  public ReconstructionCase(String fileName, int amountOfFactors, Integer vertexNoToRemove, Integer rootVertexNo)
  {
    super(fileName, amountOfFactors, rootVertexNo);
    this.vertexNoToRemove = vertexNoToRemove;
  }

  public Integer getVertexNoToRemove()
  {
    return vertexNoToRemove;
  }
}
