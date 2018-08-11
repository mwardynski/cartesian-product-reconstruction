package at.ac.unileoben.mat.dissertation.printout.data;

public class RgbColor
{
  private int red;
  private int green;
  private int blue;

  public RgbColor(int red, int green, int blue)
  {
    this.red = red;
    this.green = green;
    this.blue = blue;
  }

  public int getRed()
  {
    return red;
  }

  public int getGreen()
  {
    return green;
  }

  public int getBlue()
  {
    return blue;
  }

  @Override
  public String toString()
  {
    return String.format("{%d,%d,%d}", red, green, blue);
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    RgbColor rgbColor = (RgbColor) o;

    if (getRed() != rgbColor.getRed())
    {
      return false;
    }
    if (getGreen() != rgbColor.getGreen())
    {
      return false;
    }
    return getBlue() == rgbColor.getBlue();
  }

  @Override
  public int hashCode()
  {
    int result = getRed();
    result = 31 * result + getGreen();
    result = 31 * result + getBlue();
    return result;
  }
}
