package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.FactorizationCase;
import at.ac.unileoben.mat.dissertation.config.FactorizationConfig;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Marcin on 16.03.2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class InPlaceReconstructionOfCubesTest extends AbstractReconstructionAfterFindingAllFactorsTest
{

  private final static List<FactorizationCase> examplesList = new LinkedList<FactorizationCase>();

  @Autowired
  @Qualifier("inPlaceReconstructionOfCubesImpl")
  Reconstruction inPlaceReconstructionOfCubes;

  @Test
  public void checkExamples()
  {
    checkExamples(inPlaceReconstructionOfCubes, examplesList);
  }

  static
  {
    examplesList.add(new FactorizationCase("cubes/q3.txt", 3));
    examplesList.add(new FactorizationCase("cubes/q4.txt", 4));
    examplesList.add(new FactorizationCase("cubes/q5.txt", 5));
    examplesList.add(new FactorizationCase("cubes/q6.txt", 6));
    examplesList.add(new FactorizationCase("cubes/q7.txt", 7));
    examplesList.add(new FactorizationCase("cubes/q8.txt", 8));
    examplesList.add(new FactorizationCase("cubes/q9.txt", 9));
    examplesList.add(new FactorizationCase("cubes/q10.txt", 10));
  }
}
