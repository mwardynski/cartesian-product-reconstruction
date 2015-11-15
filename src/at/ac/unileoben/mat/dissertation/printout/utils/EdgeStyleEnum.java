package at.ac.unileoben.mat.dissertation.printout.utils;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 15.11.15
 * Time: 15:15
 * To change this template use File | Settings | File Templates.
 */
public enum EdgeStyleEnum
{
  SOLID("solid"), DASHED("dashed"), DOTTED("dotted"), DASHDOTTED("dashdotted"), DENSELY_DOTTED("densely dotted"), LOOSELY_DOTTED("loosely dotted"), DOUBLE("double");

  String name;

  EdgeStyleEnum(String name)
  {
    this.name = name;
  }

  @Override
  public String toString()
  {
    return name;
  }
}
