package at.ac.unileoben.mat.dissertation.structure;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
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
  int currentLayerNo;
  int currentBackupLayerNo;
  LayerBackupReconstructionData prevLayerBackup;
  LayerBackupReconstructionData currentLayerBackup;
  List<MergeTagEnum> mergeTags;
  boolean currentLayerToBeRefactorized;

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

  public void setReconstructionEntries(Queue<ReconstructionEntryData> reconstructionEntries)
  {
    this.reconstructionEntries = reconstructionEntries;
  }

  public int getCurrentLayerNo()
  {
    return currentLayerNo;
  }

  public void setCurrentLayerNo(int currentLayerNo)
  {
    this.currentLayerNo = currentLayerNo;
  }

  public int getCurrentBackupLayerNo()
  {
    return currentBackupLayerNo;
  }

  public void setCurrentBackupLayerNo(int currentBackupLayerNo)
  {
    this.currentBackupLayerNo = currentBackupLayerNo;
  }

  public LayerBackupReconstructionData getPrevLayerBackup()
  {
    return prevLayerBackup;
  }

  public void setPrevLayerBackup(LayerBackupReconstructionData prevLayerBackup)
  {
    this.prevLayerBackup = prevLayerBackup;
  }

  public LayerBackupReconstructionData getCurrentLayerBackup()
  {
    return currentLayerBackup;
  }

  public void setCurrentLayerBackup(LayerBackupReconstructionData currentLayerBackup)
  {
    this.currentLayerBackup = currentLayerBackup;
  }

  public List<MergeTagEnum> getMergeTags()
  {
    return mergeTags;
  }

  public void setMergeTags(List<MergeTagEnum> mergeTags)
  {
    this.mergeTags = mergeTags;
  }

  public boolean isCurrentLayerToBeRefactorized()
  {
    return currentLayerToBeRefactorized;
  }

  public void setCurrentLayerToBeRefactorized(boolean currentLayerToBeRefactorized)
  {
    this.currentLayerToBeRefactorized = currentLayerToBeRefactorized;
  }
}
