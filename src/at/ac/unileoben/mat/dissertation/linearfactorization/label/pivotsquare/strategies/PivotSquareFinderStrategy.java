package at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.LayerLabelingData;
import at.ac.unileoben.mat.dissertation.structure.AdjacencyVector;
import at.ac.unileoben.mat.dissertation.structure.FactorizationStep;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 1/31/14
 * Time: 4:34 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PivotSquareFinderStrategy
{

  void findPivotSquare(Vertex u, AdjacencyVector xAdjacencyVector, FactorizationStep currentPhase, FactorizationStep nextPhase, LayerLabelingData layerLabelingData);
}
