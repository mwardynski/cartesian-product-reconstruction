package at.ac.unileoben.mat.dissertation.linearfactorization.label;

import at.ac.unileoben.mat.dissertation.common.impl.GraphReaderImpl;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.data.LayerLabelingData;
import at.ac.unileoben.mat.dissertation.linearfactorization.label.pivotsquare.strategies.PivotSquareFinderStrategy;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.FactorizationStepService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.VertexService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: marcin
 * Date: 2/1/14
 * Time: 7:44 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class LabelUtils
{
  @Autowired
  Graph graph;

  @Autowired
  FactorizationStepService factorizationStepService;

  @Autowired
  ColoringService coloringService;

  @Autowired
  EdgeService edgeService;

  @Autowired
  VertexService vertexService;

  public EdgesRef getEdgesRef(int[] colorsCounter)
  {
    int colorsAmount = 0;
    for (int singleColorCounter : colorsCounter)
    {
      if (singleColorCounter != 0)
      {
        colorsAmount++;
      }
    }
    EdgesRef edgesRef = new EdgesRef();
    coloringService.setColorsOrderAndAmount(edgesRef, colorsCounter);
    return edgesRef;
  }

  public void sortEdgesAccordingToLabels(EdgesGroup edgesGroup, GraphColoring graphColoring)
  {
    List<Edge>[] edgesGroupedByColor = groupEdgesByColor(graphColoring, edgesGroup.getEdges());
    labelGroupedByColorEdges(edgesGroup, edgesGroupedByColor);
  }

  private List<Edge>[] groupEdgesByColor(GraphColoring graphColoring, List<Edge> edgesToSort)
  {
    List<Edge>[] colorOccurrence = (List<Edge>[]) new LinkedList<?>[graphColoring.getOriginalColorsAmount()];
    for (Edge edge : edgesToSort)
    {
      Label label = edge.getLabel();
      int color = label.getColor();
      if (colorOccurrence[color] == null)
      {
        colorOccurrence[color] = new LinkedList<>();
      }
      colorOccurrence[color].add(edge);
    }
    return colorOccurrence;
  }

  private void labelGroupedByColorEdges(EdgesGroup edgesGroup, List<Edge>[] edgesGroupedByColor)
  {
    List<Edge> sortedEdges = new ArrayList<>(edgesGroup.getEdges().size());
    int[] colorsCounter = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    for (List<Edge> edges : edgesGroupedByColor)
    {
      if (CollectionUtils.isNotEmpty(edges))
      {
        colorsCounter[edges.get(0).getLabel().getColor()] = edges.size();

        List<Edge> edgesToLabel = new LinkedList<>();
        Edge[] labelsInUse = new Edge[GraphReaderImpl.MAX_NEIGHBOURS_AMOUNT];
        int namedEdgesCounter = 0;
        for (Edge edge : edges)
        {
          int edgeName = edge.getLabel().getName();
          if (edgeName == -1)
          {
            edgesToLabel.add(edge);
          }
          else
          {
            if (labelsInUse[edgeName] != null)
            {
              edgesToLabel.add(edge);
            }
            else
            {
              labelsInUse[edgeName] = edge;
              namedEdgesCounter++;
            }
          }

        }

        Iterator<Edge> edgesToLabelIterator = edgesToLabel.iterator();
        for (int i = 0; i < labelsInUse.length; i++)
        {
          if (labelsInUse[i] != null)
          {
            Edge edge = labelsInUse[i];
            sortedEdges.add(edge);
            namedEdgesCounter--;
          }
          else if (edgesToLabelIterator.hasNext())
          {
            Edge edgeToLabel = edgesToLabelIterator.next();
            edgeToLabel.getLabel().setName(i);
            labelsInUse[i] = edgeToLabel;
            sortedEdges.add(edgeToLabel);
          }

          if (namedEdgesCounter == 0 && !edgesToLabelIterator.hasNext())
          {
            break;
          }
        }
      }
    }
    EdgesRef downEdgesRef = getEdgesRef(colorsCounter);
    edgesGroup.setEdgesRef(downEdgesRef);
    edgesGroup.setEdges(sortedEdges);
  }

  public void singleFindPivotSquarePhase(PivotSquareFinderStrategy pivotSquareFinderStrategy, FactorizationStep thisPhase, FactorizationStep nextPhase, LayerLabelingData layerLabelingData)
  {
    for (Vertex x : thisPhase.getVerticesInLayer())
    {
      if (x == null || CollectionUtils.isEmpty(factorizationStepService.getAssignedVertices(thisPhase, x)))
      {
        continue;
      }
      AdjacencyVector xAdjacencyVector = new AdjacencyVector(graph.getVertices().size(), x);
      findPivotSquareForReferenceVertex(pivotSquareFinderStrategy, x, xAdjacencyVector, thisPhase, nextPhase, layerLabelingData);
    }
  }

  public void findPivotSquareForReferenceVertex(PivotSquareFinderStrategy pivotSquareFinderStrategy, Vertex x, AdjacencyVector xAdjacencyVector, FactorizationStep thisPhase, FactorizationStep nextPhase, LayerLabelingData layerLabelingData)
  {
    List<Vertex> assignedVertices = factorizationStepService.getAssignedVertices(thisPhase, x);
    Iterator<Vertex> assignedVerticesIterator = assignedVertices.iterator();
    while (assignedVerticesIterator.hasNext())
    {
      Vertex u = assignedVerticesIterator.next();
      pivotSquareFinderStrategy.findPivotSquare(u, xAdjacencyVector, thisPhase, nextPhase, layerLabelingData);
      if (nextPhase != null)
      {
        findPivotSquareForReferenceVertex(pivotSquareFinderStrategy, x, xAdjacencyVector, nextPhase, null, layerLabelingData);
      }
      assignedVerticesIterator.remove();
    }
  }


  public void setVertexAsUnitLayer(Vertex u, int colorToLabel, EdgeType edgeType)
  {
    EdgesGroup edgesGroup = edgeService.getEdgeGroupForEdgeType(u, edgeType);
    List<Edge> uEdges = edgesGroup.getEdges();
    int nameCounter = 0;
    for (Edge e : uEdges)
    {
      edgeService.addLabel(e, colorToLabel, nameCounter++, null, new LabelOperationDetail.Builder(LabelOperationEnum.UNIT_LAYER_FOLLOWING).build());
    }
    MergeTagEnum mergeTagEnum = edgeType == EdgeType.DOWN ? MergeTagEnum.LABEL_DOWN : MergeTagEnum.LABEL_CROSS;
    vertexService.assignVertexToUnitLayerAndMergeColors(u, mergeTagEnum);

    int[] colorLengths = new int[graph.getGraphColoring().getOriginalColorsAmount()];
    colorLengths[colorToLabel] = uEdges.size();
    EdgesRef edgesRef = new EdgesRef();
    coloringService.setColorAmounts(edgesRef, colorLengths);
    edgesGroup.setEdgesRef(edgesRef);
  }
}
