package at.ac.unileoben.mat.dissertation.printout.utils;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 15.11.15
 * Time: 16:59
 * To change this template use File | Settings | File Templates.
 */
public enum VertexColorEnum
{
  BLACK("black/25"), ORANGE("orange/75");

  String definition;

  VertexColorEnum(String definition)
  {
    this.definition = definition;
  }

  @Override
  public String toString()
  {
    return definition;
  }
}
