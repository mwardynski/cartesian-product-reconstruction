package at.ac.unileoben.mat.dissertation.structure;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 15.11.15
 * Time: 18:22
 * To change this template use File | Settings | File Templates.
 */
public enum LabelOperationEnum
{
  PREPARE("Prepare"), UNIT_LAYER_GENERAL("Unit-Layer: General"), UNIT_LAYER_FIRST("Unit-Layer: First"), UNIT_LAYER_FOLLOWING("Unit-Layer: Following"), PIVOT_SQUARE_FIRST("Pivot Square: First"), PIVOT_SQUARE_FOLLOWING("Pivot Square: Following"), OPPOSITE("Opposite");

  private String name;

  private LabelOperationEnum(String name)
  {
    this.name = name;
  }

  @Override
  public String toString()
  {
    return name;
  }
}
