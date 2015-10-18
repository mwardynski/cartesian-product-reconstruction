package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.CrossEdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.DownEdgesLabeler;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.impl.UpEdgesLabeler;
import at.ac.unileoben.mat.dissertation.printout.GraphPrinter;
import at.ac.unileoben.mat.dissertation.structure.Graph;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 14-01-29
 * Time: 20:15
 * To change this template use File | Settings | File Templates.
 */
public class GraphFactorizer
{

  DownEdgesLabeler downEdgesLabeler;
  CrossEdgesLabeler crossEdgesLabeler;
  UpEdgesLabeler upEdgesLabeler;
  ConsistencyChecker consistencyChecker;
  GraphPrinter graphPrinter;

  public GraphFactorizer(Graph graph)
  {
    this.downEdgesLabeler = new DownEdgesLabeler(graph);
    this.crossEdgesLabeler = new CrossEdgesLabeler(graph);
    this.upEdgesLabeler = new UpEdgesLabeler(graph);
    this.consistencyChecker = new ConsistencyChecker(graph);
    this.graphPrinter = new GraphPrinter();
  }

  public void factorize(Graph graph)
  {
    graphPrinter.addStep(graph);
    int layersAmount = graph.getLayersAmount();
    for (int currentLayerNo = 2; currentLayerNo < layersAmount; currentLayerNo++)
    {
      downEdgesLabeler.labelEdges(currentLayerNo);
      crossEdgesLabeler.labelEdges(currentLayerNo);
      upEdgesLabeler.labelEdges(currentLayerNo);
      consistencyChecker.checkConsistency(currentLayerNo);
      graphPrinter.addStep(graph);
    }
    graphPrinter.printFactorization();
  }
}
