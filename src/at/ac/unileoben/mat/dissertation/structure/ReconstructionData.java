package at.ac.unileoben.mat.dissertation.structure;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by mwardynski on 31/08/16.
 */
@Component
public class ReconstructionData
{
  FactorizationData currentFactorization;
  FactorizationData resultFactorization;
  OperationOnGraph operationOnGraph;
  Vertex newVertex;
  Queue<ReconstructionEntryData> reconstructionEntries;

  public ReconstructionData()
  {
    reconstructionEntries = new LinkedList<>();
  }

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

  public Vertex getNewVertex()
  {
    return newVertex;
  }

  public void setNewVertex(Vertex newVertex)
  {
    this.newVertex = newVertex;
  }

  public Queue<ReconstructionEntryData> getReconstructionEntries()
  {
    return reconstructionEntries;
  }
}
