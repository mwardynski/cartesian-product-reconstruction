package at.ac.unileoben.mat.dissertation.reconstruction.services.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.OnlyOneSidedMergeData;
import at.ac.unileoben.mat.dissertation.structure.SquareReconstructionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class FinalMergeServiceImpl
{

  @Autowired
  Graph graph;

  @Autowired
  ColoringService coloringService;

  public void showStatisticsOfPotentialMerges(SquareReconstructionData squareReconstructionData)
  {
    Map<Set<Integer>, Integer> colorsWithCardinality = new HashMap<>();

    for (OnlyOneSidedMergeData onlyOneSidedMergeData : squareReconstructionData.getOnlyOneSidedMerges())
    {
      Set<Integer> colors = new HashSet<>();
      int squareBaseEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), onlyOneSidedMergeData.getSquareBaseEdge().getLabel().getColor());
      int squareBaseExtensionEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), onlyOneSidedMergeData.getSquareExtensionEdge().getLabel().getColor());
      int squareOtherEdgeMappedColor = coloringService.getCurrentColorMapping(graph.getGraphColoring(), onlyOneSidedMergeData.getOtherEdge().getLabel().getColor());

      colors.add(squareBaseEdgeMappedColor);
      colors.add(squareBaseExtensionEdgeMappedColor);
      colors.add(squareOtherEdgeMappedColor);


      if (!colorsWithCardinality.containsKey(colors))
      {
        colorsWithCardinality.put(colors, 1);
      }
      else
      {
        Integer cardinality = colorsWithCardinality.get(colors);
        colorsWithCardinality.put(colors, cardinality + 1);
      }
    }

    colorsWithCardinality.entrySet().stream()
            .forEach(entry -> System.out.println(String.format("colors: %s - cardinality: %d", entry.getKey(), entry.getValue())));


  }
}
