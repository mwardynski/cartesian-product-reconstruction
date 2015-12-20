package at.ac.unileoben.mat.dissertation.linearfactorization.impl;

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

  FactorizationCase(String fileName, int amountOfFactors)
  {
    this.fileName = fileName;
    this.amountOfFactors = amountOfFactors;
  }

  public String getFileName()
  {
    return fileName;
  }

  public int getAmountOfFactors()
  {
    return amountOfFactors;
  }
}
