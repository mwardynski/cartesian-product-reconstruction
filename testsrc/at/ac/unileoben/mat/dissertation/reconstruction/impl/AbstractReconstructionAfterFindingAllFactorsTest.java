package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.structure.ReconstructionData;
import at.ac.unileoben.mat.dissertation.structure.TestCaseContext;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by mwardynski on 26/11/16.
 */
public class AbstractReconstructionAfterFindingAllFactorsTest
{

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  GraphFactorizationPreparer graphFactorizationPreparer;

  @Autowired
  TestCaseContext testCaseContext;

  void checkExamples(Reconstruction reconstruction, List<FactorizationCase> examplesList)
  {
    for (FactorizationCase factorizationCase : examplesList)
    {
      try
      {
        List<Vertex> vertices = graphHelper.parseGraph(factorizationCase.getFileName());
        for (int vertexNumberToRemove = 0; vertexNumberToRemove < vertices.size(); vertexNumberToRemove++)
        {
          StopWatch stopWatch = new StopWatch();
          stopWatch.start();
          List<Vertex> incompleteVertices = graphHelper.parseGraph(factorizationCase.getFileName());
          graphFactorizationPreparer.removeVertex(incompleteVertices, vertexNumberToRemove);
          testCaseContext.setCorrectResult(false);

          if (factorizationCase.getRootVertexNo() == null)
          {
            reconstruction.reconstruct(incompleteVertices);
          }
          else
          {
            Vertex rootVertex = incompleteVertices.get(factorizationCase.getRootVertexNo());
            reconstruction.reconstruct(incompleteVertices, rootVertex);
          }

          if (!testCaseContext.isCorrectResult())
          {
            throw new IllegalStateException("no result!!");
          }

          System.out.print("OK - file: " + factorizationCase.getFileName() + ", removed vertex no: " + vertexNumberToRemove);
          stopWatch.stop();
          System.out.println(" [" + stopWatch.getTotalTimeSeconds() + "s]");
          cleanUpReconstructionData();
        }
      }
      catch (Throwable e)
      {
        System.out.println("ERROR: " + e.getClass());
      }
    }
  }

  private void cleanUpReconstructionData()
  {
    reconstructionData.setReconstructionEntries(new LinkedList<>());
    reconstructionData.setNewVertex(null);
    reconstructionData.setMergeOperations(null);
    reconstructionData.setCurrentLayerBackup(null);
    reconstructionData.setPrevLayerBackup(null);
    reconstructionData.setLayerNoToRefactorizeFromOptional(Optional.empty());
    reconstructionData.setResultFactorization(null);
    reconstructionData.setCurrentFactorization(null);
    reconstructionData.setOperationOnGraph(null);
    reconstructionData.setCurrentLayerNo(0);
  }

}
