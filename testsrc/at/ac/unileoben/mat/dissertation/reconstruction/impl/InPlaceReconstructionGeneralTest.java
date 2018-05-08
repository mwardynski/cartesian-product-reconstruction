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
 * Created by Marcin on 23.03.2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class InPlaceReconstructionGeneralTest extends AbstractReconstructionAfterFindingAllFactorsTest
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
    examplesList.add(new FactorizationCase("K4-ExS2.txt", 2, 0));
    examplesList.add(new FactorizationCase("K23xK2.txt", 2, 0));
    examplesList.add(new FactorizationCase("S2xK2.txt", 2, 0));
    examplesList.add(new FactorizationCase("S3xK2.txt", 2, 0));
    examplesList.add(new FactorizationCase("S3xS2.txt", 2, 0));
    examplesList.add(new FactorizationCase("S2xK2xK2.txt", 3, 0));
    examplesList.add(new FactorizationCase("C6xS2.txt", 2, 0));
    examplesList.add(new FactorizationCase("P3xK2.txt", 2, 0));
    examplesList.add(new FactorizationCase("K23xK2-mirrored.txt", 2, 0));
    examplesList.add(new FactorizationCase("C3xK2xK2.txt", 3, 0));
    examplesList.add(new FactorizationCase("K4-ExK2.txt", 2, 0));
    examplesList.add(new FactorizationCase("C3xK2.txt", 2, 0));
    examplesList.add(new FactorizationCase("C4-ExC4-E.txt", 2, 0));
  }
}
