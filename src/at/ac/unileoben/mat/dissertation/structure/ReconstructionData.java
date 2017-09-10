package at.ac.unileoben.mat.dissertation.structure;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
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
  LayerBackupReconstructionData prevLayerBackup;
  LayerBackupReconstructionData currentLayerBackup;
  List<MergeTagEnum> mergeTags;
  int missingVertexToBeCreatedLaterLayer;
  Optional<Integer> layerNoToRefactorizeFromOptional = Optional.empty();
  Vertex shiftVertex;


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

  public int getMissingVertexToBeCreatedLaterLayer()
  {
    return missingVertexToBeCreatedLaterLayer;
  }

  public void setMissingVertexToBeCreatedLaterLayer(int missingVertexToBeCreatedLaterLayer)
  {
    this.missingVertexToBeCreatedLaterLayer = missingVertexToBeCreatedLaterLayer;
  }

  public Optional<Integer> getLayerNoToRefactorizeFromOptional()
  {
    return layerNoToRefactorizeFromOptional;
  }

  public void setLayerNoToRefactorizeFromOptional(Optional<Integer> layerNoToRefactorizeFromOptional)
  {
    this.layerNoToRefactorizeFromOptional = layerNoToRefactorizeFromOptional;
  }

  public Vertex getShiftVertex()
  {
    return shiftVertex;
  }

  public void setShiftVertex(Vertex shiftVertex)
  {
    this.shiftVertex = shiftVertex;
  }
}
