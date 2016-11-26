package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.reconstruction.ReconstructionAfterFindingAllFactors;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by mwardynski on 26/11/16.
 */
public class AbstractReconstructionAfterFindingAllFactorsTest
{

  @Autowired
  GraphHelper graphHelper;

  @Autowired
  GraphFactorizationPreparer graphFactorizationPreparer;


  void checkExamples(ReconstructionAfterFindingAllFactors reconstruction, List<FactorizationCase> examplesList)
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

          try
          {
            Graph graph = reconstruction.reconstruct(incompleteVertices);
            assertThat("file: " + factorizationCase.getFileName() + ", removed vertex no: " + vertexNumberToRemove,
                    graph.getGraphColoring().getActualColors().size(), is(factorizationCase.getAmountOfFactors()));
            System.out.print("OK - file: " + factorizationCase.getFileName() + ", removed vertex no: " + vertexNumberToRemove);
          }
          catch (Exception e)
          {
            System.out.print("EXCEPTION " + e + ", file: " + factorizationCase.getFileName() + ", removed vertex no: " + vertexNumberToRemove);
            e.printStackTrace();
          }
          stopWatch.stop();
          System.out.println(" [" + stopWatch.getTotalTimeSeconds() + "s]");
        }
      }
      catch (IllegalArgumentException | IllegalStateException e)
      {
      }
    }
  }

}
