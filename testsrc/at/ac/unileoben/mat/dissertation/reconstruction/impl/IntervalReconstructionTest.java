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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {FactorizationConfig.class})
public class IntervalReconstructionTest extends AbstractReconstructionAfterFindingAllFactorsTest
{
  private final static List<FactorizationCase> examplesList = new LinkedList<FactorizationCase>();

  @Autowired
  @Qualifier("intervalReconstructionImpl")
  Reconstruction intervalReconstruction;

  @Test
  public void checkExamples()
  {
    checkExamples(intervalReconstruction, examplesList);
  }

  static
  {

    examplesList.add(new FactorizationCase("hxh.txt", 2, 0));
    examplesList.add(new FactorizationCase("cube-vxcube-v.txt", 2, 0));
    examplesList.add(new FactorizationCase("cube-VxP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("cube-ExP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("cube-2ExP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("S6xP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("S6xP4.txt", 2, 0));
    examplesList.add(new FactorizationCase("S3xS3.txt", 2, 0));
    examplesList.add(new FactorizationCase("bP3xP6mVbxP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("bP3xP6mVbxP4.txt", 2, 0));
    examplesList.add(new FactorizationCase("hxP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("P6xP4.txt", 2, 0));
    examplesList.add(new FactorizationCase("P6xP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("bP3xK2xK2bmVxP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("YxP3xP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("P3xP4.txt", 2, 0));
    examplesList.add(new FactorizationCase("K4-ExS2.txt", 2, 10));
    examplesList.add(new FactorizationCase("K3pExP3.txt", 2));
    examplesList.add(new FactorizationCase("bP3xK2bpExP3.txt", 2, 0));
    examplesList.add(new FactorizationCase("bP3xP3bpExP3.txt", 2, 4));
    examplesList.add(new FactorizationCase("C3xS2.txt", 2));
    examplesList.add(new FactorizationCase("C4xS2.txt", 2));
    examplesList.add(new FactorizationCase("C4pExS2.txt", 2));
    examplesList.add(new FactorizationCase("C6xS2.txt", 2));
    examplesList.add(new FactorizationCase("K23xP3.txt", 2, 13));
    examplesList.add(new FactorizationCase("handP3.txt", 2));
//    examplesList.add(new FactorizationCase("hxhxh.txt", 2, 0));
//    examplesList.add(new FactorizationCase("victory.txt", 2));


//    examplesList.add(new FactorizationCase("P3xK2.txt", 2));
//    examplesList.add(new FactorizationCase("K23xK2-mirrored.txt", 2));
//    examplesList.add(new FactorizationCase("notAllEdgesLabeled-root_v3.txt", 2, 3));
//    examplesList.add(new FactorizationCase("cartFactExample.txt", 2));
//    examplesList.add(new FactorizationCase("K4-ExK2.txt", 2));
//    examplesList.add(new FactorizationCase("C3xK2xK2.txt", 3));
//    examplesList.add(new FactorizationCase("C3xK2.txt", 2));
//    examplesList.add(new FactorizationCase("C4-ExC4-E.txt", 2));
//    examplesList.add(new FactorizationCase("handP2.txt", 2));
//    examplesList.add(new FactorizationCase("S2xK2xK2.txt", 3));
//    examplesList.add(new FactorizationCase("S2xK2.txt", 2));
  }
}
