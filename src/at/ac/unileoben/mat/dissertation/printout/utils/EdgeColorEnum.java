package at.ac.unileoben.mat.dissertation.printout.utils;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 15.11.15
 * Time: 15:22
 * To change this template use File | Settings | File Templates.
 */
public enum EdgeColorEnum
{
  RED("red"), GREEN("green"), BLUE("blue"), YELLOW("yellow");

  String definition;

  EdgeColorEnum(String definition)
  {
    this.definition = definition;
  }

  @Override
  public String toString()
  {
    return definition;
  }
}
