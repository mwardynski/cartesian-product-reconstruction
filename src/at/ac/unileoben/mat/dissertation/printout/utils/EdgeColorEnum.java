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
  BLACK("black"), RED("red"), GREEN("green"), BLUE("blue"), CYAN("cyan"), YELLOW("yellow");

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
