package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionBackupLayerService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionService;
import at.ac.unileoben.mat.dissertation.reconstruction.services.ReconstructionShiftLayerService;
import at.ac.unileoben.mat.dissertation.structure.*;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Marcin on 20.08.2017.
 */
@Component
public class ReconstructionShiftLayerServiceImpl implements ReconstructionShiftLayerService
{

  @Autowired
  Graph graph;

  @Autowired
  ReconstructionData reconstructionData;

  @Autowired
  ReconstructionService reconstructionService;

  @Autowired
  ReconstructionBackupLayerService reconstructionBackupLayerService;

  @Autowired
  EdgeService edgeService;

  @Override
  public boolean isVertexToShiftAvailable()
  {
    return reconstructionData.getOperationOnGraph() == OperationOnGraph.IN_PLACE_RECONSTRUCTION
            && graph.getGraphColoring().getActualColors().size() == 1
            && reconstructionData.getNewVertex() != null
            && findVertexToShift().isPresent();
  }

  private Optional<Vertex> findVertexToShift()
  {
    Optional<Vertex> vertexToShiftOptional = Optional.empty();
    Vertex newVertex = reconstructionData.getNewVertex();
    int layerNumberWithVerticesShiftable = newVertex.getBfsLayer() + 3;
    if (graph.getLayers().size() - 1 >= layerNumberWithVerticesShiftable)
    {
      List<Vertex> possiblyShiftableVertices = graph.getLayers().get(layerNumberWithVerticesShiftable);
      vertexToShiftOptional = possiblyShiftableVertices.stream().filter(v -> v.getDownEdges().getEdges().size() == 1).findAny();
    }
    vertexToShiftOptional.ifPresent(v -> reconstructionData.setShiftVertex(v));
    return vertexToShiftOptional;
  }

  @Override
  public void shiftVertex()
  {
    reconstructionBackupLayerService.recoverAfterCompleteMerge();

    Vertex shiftVertex = reconstructionData.getShiftVertex();
    int shiftVertexCurrentBfsLayerNo = shiftVertex.getBfsLayer();
    int shiftVertexNewBfsLayerNo = shiftVertexCurrentBfsLayerNo - 2;
    int reindexShiftVertexNo = graph.getReverseReindexArray()[shiftVertex.getVertexNo()];
    int shiftVertexNewNo = reconstructionService.findVertexNoForNewVertexAndReindexFollowers(shiftVertexNewBfsLayerNo);
    updateReverseReindexArrayForShiftVertex(shiftVertexNewNo, reindexShiftVertexNo);

    modifyGraphLayersForShiftVertex(shiftVertex, shiftVertexCurrentBfsLayerNo, shiftVertexNewBfsLayerNo);
    clearLabelingOfCertainEdges(shiftVertexNewBfsLayerNo);
    updateShiftVertex(shiftVertex, shiftVertexNewBfsLayerNo, shiftVertexNewNo);

    reconstructionData.setLayerNoToRefactorizeFromOptional(Optional.of(shiftVertexNewBfsLayerNo));

    Edge exampleEdge = new Edge(new Vertex(Integer.MIN_VALUE, null), new Vertex(Integer.MIN_VALUE, null));
    reconstructionService.addEdgesToReconstruction(Collections.singletonList(exampleEdge), shiftVertex, EdgeType.DOWN);
    reconstructionService.reconstructWithCollectedData();

  }

  private void updateReverseReindexArrayForShiftVertex(int shiftVertexNo, int reindexShiftVertexNo)
  {
    Integer[] reverseReindexArray = graph.getReverseReindexArray();
    Integer[] updatedReverseReindexArray = Arrays.copyOf(reverseReindexArray, reverseReindexArray.length);
    for (int i = reverseReindexArray.length - 2; i >= shiftVertexNo; i--)
    {
      updatedReverseReindexArray[i + 1] = updatedReverseReindexArray[i];
    }
    updatedReverseReindexArray[shiftVertexNo] = reindexShiftVertexNo;
    graph.setReverseReindexArray(updatedReverseReindexArray);
  }

  private void modifyGraphLayersForShiftVertex(Vertex shiftVertex, int shiftVertexCurrentBfsLayerNo, int shiftVertexNewBfsLayerNo)
  {
    List<List<Vertex>> graphBfsLayers = graph.getLayers();
    graphBfsLayers.get(shiftVertexNewBfsLayerNo).add(shiftVertex);
    List<Vertex> graphLayerAfterShiftVertexDeletion = graphBfsLayers.get(shiftVertexCurrentBfsLayerNo).stream()
            .filter(v -> v.getVertexNo() != shiftVertex.getVertexNo())
            .collect(Collectors.toCollection(ArrayList::new));
    if (CollectionUtils.isNotEmpty(graphLayerAfterShiftVertexDeletion))
    {
      graphBfsLayers.set(shiftVertexCurrentBfsLayerNo, graphLayerAfterShiftVertexDeletion);
    }
    else
    {
      graph.getLayers().remove(shiftVertexCurrentBfsLayerNo);
    }
  }

  private void clearLabelingOfCertainEdges(int shiftVertexNewBfsLayerNo)
  {
    List<List<Vertex>> graphBfsLayers = graph.getLayers();
    IntStream.range(shiftVertexNewBfsLayerNo, graphBfsLayers.size())
            .mapToObj(i -> graphBfsLayers.get(i))
            .flatMap(vertices -> vertices.stream())
            .peek(v ->
            {
              v.getDownEdges().setEdgesRef(null);
              v.getCrossEdges().setEdgesRef(null);
              v.getUpEdges().setEdgesRef(null);
            })
            .map(v -> v.getEdges())
            .flatMap(edges -> edges.stream())
            .forEach(edgeService::clearEdgeLabeling);

    graph.getLayers().get(shiftVertexNewBfsLayerNo - 1).stream()
            .peek(v -> v.getUpEdges().setEdgesRef(null))
            .map(v -> v.getUpEdges().getEdges())
            .flatMap(edges -> edges.stream())
            .forEach(edgeService::clearEdgeLabeling);
  }

  private void updateShiftVertex(Vertex shiftVertex, int shiftVertexNewBfsLayerNo, int shiftVertexNewNumber)
  {
    shiftVertex.setUnitLayer(true);
    shiftVertex.setVertexNo(shiftVertexNewNumber);
    shiftVertex.setBfsLayer(shiftVertexNewBfsLayerNo);

    shiftVertex.getUpEdges().getEdges().addAll(shiftVertex.getDownEdges().getEdges());
    shiftVertex.getUpEdges().getEdges().stream().forEach(edge -> edge.setEdgeType(EdgeType.UP));
    shiftVertex.getDownEdges().setEdges(new ArrayList<>(1));
    shiftVertex.getUpEdges().getEdges().stream()
            .map(edge -> edge.getOpposite())
            .peek(edge -> edge.setEdgeType(EdgeType.DOWN))
            .forEach(edge ->
            {
              Vertex v = edge.getOrigin();
              v.getDownEdges().getEdges().add(edge);
              Iterator<Edge> vUpEdgesIt = v.getUpEdges().getEdges().iterator();
              while (vUpEdgesIt.hasNext())
              {
                Edge nextEdge = vUpEdgesIt.next();
                if (nextEdge == edge)
                {
                  vUpEdgesIt.remove();
                  break;
                }
              }
            });
  }
}
