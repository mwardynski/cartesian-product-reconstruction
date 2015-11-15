package at.ac.unileoben.mat.dissertation.structure;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 15.11.15
 * Time: 18:24
 * To change this template use File | Settings | File Templates.
 */
public class LabelOperationDetail
{
  LabelOperationEnum type;
  Edge sameColorEdge;
  Edge pivotSquareFirstEdge;
  Edge pivotSquareFirstEdgeCounterpart;

  public static class Builder
  {
    LabelOperationEnum type;
    Edge sameColorEdge;
    Edge pivotSquareFirstEdge;
    Edge pivotSquareFirstEdgeCounterpart;

    public Builder(LabelOperationEnum type)
    {
      this.type = type;
    }

    public Builder sameColorEdge(Edge sameColorEdge)
    {
      this.sameColorEdge = sameColorEdge;
      return this;
    }

    public Builder pivotSquareFirstEdge(Edge pivotSquareFirstEdge)
    {
      this.pivotSquareFirstEdge = pivotSquareFirstEdge;
      return this;
    }

    public Builder pivotSquareFirstEdgeCounterpart(Edge pivotSquareFirstEdgeCounterpart)
    {
      this.pivotSquareFirstEdgeCounterpart = pivotSquareFirstEdgeCounterpart;
      return this;
    }

    public LabelOperationDetail build()
    {
      return new LabelOperationDetail(this);
    }
  }

  private LabelOperationDetail(Builder builder)
  {
    type = builder.type;
    sameColorEdge = builder.sameColorEdge;
    pivotSquareFirstEdge = builder.pivotSquareFirstEdge;
    pivotSquareFirstEdgeCounterpart = builder.pivotSquareFirstEdgeCounterpart;
  }

  public LabelOperationEnum getType()
  {
    return type;
  }

  public Edge getSameColorEdge()
  {
    return sameColorEdge;
  }

  public Edge getPivotSquareFirstEdge()
  {
    return pivotSquareFirstEdge;
  }

  public Edge getPivotSquareFirstEdgeCounterpart()
  {
    return pivotSquareFirstEdgeCounterpart;
  }
}
