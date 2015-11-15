package at.ac.unileoben.mat.dissertation.structure;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-11-06
 * Time: 20:07
 * To change this template use File | Settings | File Templates.
 */
public enum MergeTagEnum
{
  PREPARE("Prepare"), LABEL_DOWN("Label: Down-edges"), LABEL_CROSS("Label: Cross-edges"), CONSISTENCY_DOWN("Consistency Check: Down-edges"), CONSISTENCY_CROSS("Consistency Check: Cross-edges"), CONSISTENCY_UP("Consistency Check: Up-edges");

  private String name;

  private MergeTagEnum(String name)
  {
    this.name = name;
  }

  @Override
  public String toString()
  {
    return name;
  }
}
