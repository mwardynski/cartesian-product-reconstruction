package at.ac.unileoben.mat.dissertation.common;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-11-02
 * Time: 16:58
 * To change this template use File | Settings | File Templates.
 */
public class FactorizationCase
{
  String fileName;
  int amountOfFactors;
  Integer rootVertexNo;

  public FactorizationCase(String fileName, int amountOfFactors)
  {
    this.fileName = fileName;
    this.amountOfFactors = amountOfFactors;
  }

  public FactorizationCase(String fileName, int amountOfFactors, Integer rootVertexNo)
  {
    this(fileName, amountOfFactors);
    this.rootVertexNo = rootVertexNo;
  }

  public String getFileName()
  {
    return fileName;
  }

  public int getAmountOfFactors()
  {
    return amountOfFactors;
  }

  public Integer getRootVertexNo()
  {
    return rootVertexNo;
  }
}
