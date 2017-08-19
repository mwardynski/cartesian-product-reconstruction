package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.ReconstructionData;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

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

          Graph graph;
          if (factorizationCase.getRootVertexNo() == null)
          {
            graph = reconstruction.reconstruct(incompleteVertices);
          }
          else
          {
            Vertex rootVertex = incompleteVertices.get(factorizationCase.getRootVertexNo());
            graph = reconstruction.reconstruct(incompleteVertices, rootVertex);
          }

          assertThat("file: " + factorizationCase.getFileName() + ", removed vertex no: " + vertexNumberToRemove,
                  graph.getGraphColoring().getActualColors().size(), is(factorizationCase.getAmountOfFactors()));
          assertThat(reconstructionData.getNewVertex(), notNullValue());

          System.out.print("OK - file: " + factorizationCase.getFileName() + ", removed vertex no: " + vertexNumberToRemove);
          stopWatch.stop();
          System.out.println(" [" + stopWatch.getTotalTimeSeconds() + "s]");
          cleanUpReconstructionData();
        }
      }
      catch (IllegalArgumentException | IllegalStateException e)
      {
        System.out.println("ERROR");
      }
    }
  }

  private void cleanUpReconstructionData()
  {
    reconstructionData.setNewVertex(null);
    reconstructionData.setMergeTags(null);
    reconstructionData.setCurrentLayerBackup(null);
    reconstructionData.setPrevLayerBackup(null);
    reconstructionData.setCurrentLayerToBeRefactorized(false);
    reconstructionData.setCurrentBackupLayerNo(0);
    reconstructionData.setResultFactorization(null);
    reconstructionData.setCurrentFactorization(null);
    reconstructionData.setOperationOnGraph(null);
    reconstructionData.setCurrentLayerNo(0);
    reconstructionData.setReconstructionEntries(new LinkedList<>());
  }

}
