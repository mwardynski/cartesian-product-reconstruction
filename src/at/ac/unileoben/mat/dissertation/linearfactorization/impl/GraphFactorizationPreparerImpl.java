package at.ac.unileoben.mat.dissertation.linearfactorization.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.GraphFactorizationPreparer;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 13-11-29
 * Time: 16:47
 * To change this template use File | Settings | File Templates.
 */
@Component
public class GraphFactorizationPreparerImpl implements GraphFactorizationPreparer
{
  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

  @Autowired
  EdgeService edgeService;

  @Override
  public void removeVertex(List<Vertex> vertices, int vertexIndex)
  {
    Iterator<Vertex> vertexIterator = vertices.iterator();
    while (vertexIterator.hasNext())
    {
      Vertex v = vertexIterator.next();
      Iterator<Edge> edgeIterator = v.getEdges().iterator();
      while (edgeIterator.hasNext())
      {
        Edge e = edgeIterator.next();
        if (e.getOrigin().getVertexNo() == vertexIndex || e.getEndpoint().getVertexNo() == vertexIndex)
        {
          edgeIterator.remove();
        }
      }
      if (v.getVertexNo() == vertexIndex)
      {
        vertexIterator.remove();
      }
    }
    for (Vertex v : vertices)
    {
      if (v.getVertexNo() > vertexIndex)
      {
        v.setVertexNo(v.getVertexNo() - 1);
      }
    }
  }

  @Override
  public void arrangeFirstLayerEdges()
  {
    Vertex root = graph.getRoot();
    root.setUnitLayer(true);
    EdgesGroup upEdgesGroup = root.getUpEdges();
    List<Edge> upEdges = upEdgesGroup.getEdges();

    EdgesRef edgesRef = new EdgesRef();
    int[] colorLengths = new int[upEdges.size()];
    for (int i = 0; i < upEdges.size(); i++)
    {
      colorLengths[i] = 1;
    }
    coloringService.setColorAmounts(edgesRef, colorLengths);

    upEdgesGroup.setEdgesRef(edgesRef);

    for (int i = 0; i < upEdges.size(); i++)
    {
      Edge upEdge = upEdges.get(i);

      edgeService.addLabel(upEdge, i, 0, null, new LabelOperationDetail.Builder(LabelOperationEnum.PREPARE).build());
      upEdge.getEndpoint().setUnitLayer(true);

      addLabelAndRefToDownEdgesL1(upEdge, i, graph.getGraphColoring().getOriginalColorsAmount());
      addLabelAndRefToCrossEdgesL1(upEdge);
    }

  }

  private void addLabelAndRefToDownEdgesL1(Edge upEdge, int i, int size)
  {
    int[] colorLengths;
    EdgesRef downEdgesRef = new EdgesRef();
    colorLengths = new int[size];
    for (int j = 0; j < size; j++)
    {
      if (j == i)
      {
        colorLengths[j] = 1;
      }
      else
      {
        colorLengths[j] = 0;
      }
    }
    coloringService.setColorAmounts(downEdgesRef, colorLengths);

    Vertex endpointVertex = upEdge.getEndpoint();
    EdgesGroup downEdgesGroup = endpointVertex.getDownEdges();
    downEdgesGroup.setEdgesRef(downEdgesRef);
    edgeService.addLabel(upEdge.getOpposite(), i, 0, null, new LabelOperationDetail.Builder(LabelOperationEnum.PREPARE).build());
  }

  private void addLabelAndRefToCrossEdgesL1(Edge upEdge)
  {
    EdgesGroup crossEdgesGroup = upEdge.getEndpoint().getCrossEdges();
    List<Edge> crossEdges = crossEdgesGroup.getEdges();
    int[] crossEdgesAmounts = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    int crossEdgesColorsAmount = 0;
    for (int i = 0; i < crossEdges.size(); i++)
    {
      Edge crossEdge = crossEdges.get(i);
      int mergedColor = mergeCrossEdgesColors(crossEdge, upEdge);
      edgeService.addLabel(crossEdge, mergedColor, crossEdgesAmounts[mergedColor], null, new LabelOperationDetail.Builder(LabelOperationEnum.PREPARE).build());
      if (crossEdgesAmounts[mergedColor] == 0)
      {
        crossEdgesColorsAmount++;
      }
      crossEdgesAmounts[mergedColor]++;

    }
    EdgesRef crossEdgesRef = new EdgesRef();
    coloringService.setColorAmounts(crossEdgesRef, crossEdgesAmounts);
    crossEdgesGroup.setEdgesRef(crossEdgesRef);
  }

  private int mergeCrossEdgesColors(Edge crossEdge, Edge proposedColorEdge)
  {
    Edge oppositeEdge = crossEdge.getOpposite();
    coloringService.mergeColorsForEdges(Arrays.asList(proposedColorEdge, oppositeEdge), MergeTagEnum.PREPARE);

    int proposedColor = proposedColorEdge.getLabel().getColor();
    Label oppositeEdgeLabel = oppositeEdge.getLabel();
    if (oppositeEdgeLabel != null)
    {
      int oppositeEdgeColor = oppositeEdgeLabel.getColor();
      return proposedColor < oppositeEdgeColor ? proposedColor : oppositeEdgeColor;
    }
    else
    {
      return proposedColor;
    }
  }
}
