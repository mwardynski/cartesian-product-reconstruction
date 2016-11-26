package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.reconstruction.ReconstructionAfterFindingAllFactors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mwardynski on 24/04/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class InPlaceReconstructionAfterFindingAllFactorsTest extends AbstractReconstructionAfterFindingAllFactorsTest
{

  private final static List<FactorizationCase> examplesList = new LinkedList<FactorizationCase>();

  @Autowired
  @Qualifier("inPlaceReconstructionAfterFindingAllFactorsImpl")
  ReconstructionAfterFindingAllFactors reconstruction;

  @Test
  public void checkExamples()
  {
    checkExamples(reconstruction, examplesList);
  }

  static
  {

    examplesList.add(new FactorizationCase("newExCart.txt", 2));
//    examplesList.add(new FactorizationCase("additionalVertex.txt", 2));
//    examplesList.add(new FactorizationCase("breakExample2.txt", 2));
//    examplesList.add(new FactorizationCase("breakExample3.txt", 3));
//    examplesList.add(new FactorizationCase("breakExample4.txt", 2));
//    examplesList.add(new FactorizationCase("g1", 2));
//    examplesList.add(new FactorizationCase("exampleOfCartesianProduct.txt", 2));
//    examplesList.add(new FactorizationCase("notAllEdgesLabeled-root_v3.txt", 2, 3));
//    examplesList.add(new FactorizationCase("c.txt", 3));
//    examplesList.add(new FactorizationCase("cartFactExample.txt", 2));
//    examplesList.add(new FactorizationCase("CartesianProductWithCrossEdges.txt", 2));
//    examplesList.add(new FactorizationCase("exampleOfCartesianProduct3.txt", 3));
//    examplesList.add(new FactorizationCase("breakExample.txt", 2));
    //not working
//    examplesList.add(new FactorizationCase("handP2.txt", 2));
//    examplesList.add(new FactorizationCase("handP3.txt", 2));
    //not needed:
//    examplesList.add(new FactorizationCase("victory.txt", 3));
//    examplesList.add(new FactorizationCase("cd.txt", 1));
//    examplesList.add(new FactorizationCase("g3", 1));
//    examplesList.add(new FactorizationCase("newExCart-mod.txt", 1));
//    examplesList.add(new FactorizationCase("przyklad.txt", 1));
//    examplesList.add(new FactorizationCase("simpleExample.txt", 1));
//    examplesList.add(new FactorizationCase("example.txt", 1));
  }
}
