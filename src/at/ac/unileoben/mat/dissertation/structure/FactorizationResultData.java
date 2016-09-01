package at.ac.unileoben.mat.dissertation.structure;

/**
 * Created by mwardynski on 31/08/16.
 */
public class FactorizationResultData
{
  FactorizationData currentFactorization;
  FactorizationData resultFactorization;

  public FactorizationData getCurrentFactorization()
  {
    return currentFactorization;
  }

  public void setCurrentFactorization(FactorizationData currentFactorization)
  {
    this.currentFactorization = currentFactorization;
  }

  public FactorizationData getResultFactorization()
  {
    return resultFactorization;
  }

  public void setResultFactorization(FactorizationData resultFactorization)
  {
    this.resultFactorization = resultFactorization;
  }
}
