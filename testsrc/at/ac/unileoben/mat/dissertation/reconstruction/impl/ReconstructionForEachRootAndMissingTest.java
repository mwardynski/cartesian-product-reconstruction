package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class ReconstructionForEachRootAndMissingTest extends AbstractReconstructionAfterFindingAllFactorsTest
{

  private final static List<FactorizationCase> examplesList = new LinkedList<FactorizationCase>();

  @Autowired
  @Qualifier("inPlaceReconstructionOfCubesImpl")
  Reconstruction inPlaceReconstructionOfCubes;

  @Autowired
  Graph graph;

  @Autowired
  EdgeService edgeService;

  @Test
  public void checkExamples()
  {
    checkExamples(inPlaceReconstructionOfCubes, examplesList);
  }

  static
  {
    examplesList.add(new FactorizationCase("K4-ExK2xK2.txt", 2));
//    examplesList.add(new FactorizationCase("K23xK2.txt", 2));
//    examplesList.add(new FactorizationCase("S2xK2.txt", 2));
//    examplesList.add(new FactorizationCase("S3xK2.txt", 2));
//    examplesList.add(new FactorizationCase("S3xS2.txt", 2));
//    examplesList.add(new FactorizationCase("S2xK2xK2.txt", 3));
//    examplesList.add(new FactorizationCase("C6xS2.txt", 2));
//    examplesList.add(new FactorizationCase("P3xK2.txt", 2));
//    examplesList.add(new FactorizationCase("K23xK2-mirrored.txt", 2));
//    examplesList.add(new FactorizationCase("C3xK2xK2.txt", 3));
//    examplesList.add(new FactorizationCase("K4-ExK2.txt", 2));
//    examplesList.add(new FactorizationCase("C3xK2.txt", 2));
//    examplesList.add(new FactorizationCase("C4-ExC4-E.txt", 2));
  }

  @Override
  void checkExamples(Reconstruction reconstruction, List<FactorizationCase> examplesList)
  {
    for (FactorizationCase factorizationCase : examplesList)
    {
      List<Vertex> vertices = graphHelper.parseGraph(factorizationCase.getFileName());
      for (int vertexNumberToRemove = 0; vertexNumberToRemove < vertices.size(); vertexNumberToRemove++)
      {
        Vertex vertexToRemove = vertices.get(vertexNumberToRemove);
        graphHelper.prepareGraphBfsStructure(vertices, vertexToRemove);
        int vertexToRemoveOrigNo = graph.getReverseReindexArray()[vertexToRemove.getVertexNo()];
        List<Integer> vertexToRemoveNeighbors = listVerticesOfGivenLayerNo(1, vertexToRemoveOrigNo);
        String distances = prepareStringWithDistancesToTheMissingVertex(vertexToRemoveOrigNo);
        graphHelper.revertGraphBfsStructure();


        for (int rootVertexNo = 0; rootVertexNo < vertices.size() - 1; rootVertexNo++)
        {
          try
          {
            List<Vertex> incompleteVertices = graphHelper.parseGraph(factorizationCase.getFileName());
            graphFactorizationPreparer.removeVertex(incompleteVertices, vertexNumberToRemove);

            System.out.println(distances);
            System.out.println("Reconstructing from: " + rootVertexNo);

            Vertex rootVertex = incompleteVertices.get(rootVertexNo);
            reconstruction.reconstruct(incompleteVertices, rootVertex);

            Optional<Boolean> missingInFirstLayerOptional = reconstructionData.getMissingInFirstLayerReconstructionData().getMissingInFirstLayer();
            System.out.println("Missing in first layer: " + (missingInFirstLayerOptional.isPresent() && missingInFirstLayerOptional.get()));
            if (missingInFirstLayerOptional.isPresent() && missingInFirstLayerOptional.get() != vertexToRemoveNeighbors.contains(rootVertexNo))
            {
              System.out.println("WRONG L1 RECONSTRUCTION");
            }
            else
            {
              System.out.println("CORRECT L1 RECONSTRUCTION");
            }
          }
          catch (Throwable e)
          {
            System.out.println("ERROR: " + e.getClass());
          } finally
          {
            graphHelper.revertGraphBfsStructure();
            cleanUpReconstructionData();
          }
        }
      }
    }
  }

  private String prepareStringWithDistancesToTheMissingVertex(int removedVertexOrigNo)
  {
    StringBuilder sb = new StringBuilder("");
    IntStream.range(0, graph.getLayers().size())
            .forEach(i ->
            {
              if (i == 0)
              {
                sb.append("\n* missing vertex no: ");
                sb.append(removedVertexOrigNo + "\n");
              }
              else
              {
                sb.append("* d" + i + ": ");
                String verticesFromLayer = listVerticesOfGivenLayerNo(i, removedVertexOrigNo).stream()

                        .map(vNo -> vNo.toString())
                        .collect(Collectors.joining(","));
                sb.append(verticesFromLayer + "\n");
              }
            });
    return sb.toString();
  }

  List<Integer> listVerticesOfGivenLayerNo(int layerNo, int removedVertexOrigNo)
  {
    return graph.getLayers().get(layerNo).stream()
            .map(v -> graph.getReverseReindexArray()[v.getVertexNo()])
            .map(vNo -> vNo < removedVertexOrigNo ? vNo : vNo - 1)
            .collect(Collectors.toList());
  }
}
