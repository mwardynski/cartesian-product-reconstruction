package at.ac.unileoben.mat.dissertation.structure;

import org.springframework.stereotype.Component;

/**
 * Created by mwardynski on 31/08/16.
 */
@Component
public class ReconstructionData
{
  FactorizationData currentFactorization;
  FactorizationData resultFactorization;
  OperationOnGraph operationOnGraph;

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

  public OperationOnGraph getOperationOnGraph()
  {
    return operationOnGraph;
  }

  public void setOperationOnGraph(OperationOnGraph operationOnGraph)
  {
    this.operationOnGraph = operationOnGraph;
  }
}
