package at.ac.unileoben.mat.dissertation.printout.data;

import at.ac.unileoben.mat.dissertation.printout.utils.VertexColorEnum;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 18.10.15
 * Time: 14:35
 * To change this template use File | Settings | File Templates.
 */
public class VertexData
{
  long vertexNo;
  long origVertexNo;
  double posX;
  double posY;
  String color = VertexColorEnum.BLACK.toString();

  public long getVertexNo()
  {
    return vertexNo;
  }

  public void setVertexNo(long vertexNo)
  {
    this.vertexNo = vertexNo;
  }

  public long getOrigVertexNo()
  {
    return origVertexNo;
  }

  public void setOrigVertexNo(long origVertexNo)
  {
    this.origVertexNo = origVertexNo;
  }

  public double getPosX()
  {
    return posX;
  }

  public void setPosX(double posX)
  {
    this.posX = posX;
  }

  public double getPosY()
  {
    return posY;
  }

  public void setPosY(double posY)
  {
    this.posY = posY;
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
