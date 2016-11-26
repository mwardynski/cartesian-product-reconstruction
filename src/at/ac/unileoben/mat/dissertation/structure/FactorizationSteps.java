package at.ac.unileoben.mat.dissertation.structure;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-01-29
 * Time: 18:43
 * To change this template use File | Settings | File Templates.
 */
public class FactorizationSteps
{
  private FactorizationStep findSquareFirstPhase;
  private FactorizationStep findSquareSecondPhase;
  private FactorizationStep labelVerticesPhase;

  public FactorizationSteps(List<Vertex> firstLayer, int graphSize)
  {
    findSquareFirstPhase = new FactorizationStep(firstLayer.get(0).getVertexNo(), firstLayer.size(), graphSize);
    findSquareSecondPhase = new FactorizationStep(firstLayer.get(0).getVertexNo(), firstLayer.size(), graphSize);
  }

  public FactorizationSteps(List<Vertex> firstLayer, List<Vertex> secondLayer, int graphSize)
  {
    findSquareFirstPhase = new FactorizationStep(secondLayer.get(0).getVertexNo(), secondLayer.size(), graphSize);
    findSquareSecondPhase = new FactorizationStep(secondLayer.get(0).getVertexNo(), secondLayer.size(), graphSize);
    labelVerticesPhase = new FactorizationStep(firstLayer.get(0).getVertexNo(), firstLayer.size(), graphSize);
  }

  public FactorizationStep getFindSquareFirstPhase()
  {
    return findSquareFirstPhase;
  }

  public FactorizationStep getFindSquareSecondPhase()
  {
    return findSquareSecondPhase;
  }

  public FactorizationStep getLabelVerticesPhase()
  {
    return labelVerticesPhase;
  }
}
