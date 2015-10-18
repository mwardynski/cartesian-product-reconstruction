package at.ac.unileoben.mat.dissertation.printout.data;

import at.ac.unileoben.mat.dissertation.structure.Vertex;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 18.10.15
 * Time: 16:04
 * To change this template use File | Settings | File Templates.
 */
public class EdgeData
{
  long originNo;
  long endpointNo;
  String color = "black";

  public long getOriginNo()
  {
    return originNo;
  }

  public void setOriginNo(long originNo)
  {
    this.originNo = originNo;
  }

  public long getEndpointNo()
  {
    return endpointNo;
  }

  public void setEndpointNo(long endpointNo)
  {
    this.endpointNo = endpointNo;
  }

  public String getColor()
  {
    return color;
  }

  public void setColor(String color)
  {
    this.color = color;
  }
}
