package at.ac.unileoben.mat.dissertation.printout;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.LabelOperationDetail;
import at.ac.unileoben.mat.dissertation.structure.MergeTagEnum;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 20.12.15
 * Time: 19:29
 * To change this template use File | Settings | File Templates.
 */
public interface GraphPrinter
{
  void printFactorization();

  void createLayerSnapshot();

  void createMergeSnapshot(List<Edge> edges, MergeTagEnum mergeTag);

  void createLabelSnapshot(Edge edge, int color, int name, LabelOperationDetail labelOperationDetail);
}
