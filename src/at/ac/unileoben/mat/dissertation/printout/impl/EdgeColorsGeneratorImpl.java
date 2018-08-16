package at.ac.unileoben.mat.dissertation.printout.impl;

import at.ac.unileoben.mat.dissertation.printout.EdgeColorsGenerator;
import at.ac.unileoben.mat.dissertation.printout.data.RgbColor;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;


@Component
public class EdgeColorsGeneratorImpl implements EdgeColorsGenerator
{

  private static final int BITWISE_RGB_SETS_AMOUNT = 6;
  private static final int RGB_MIN_VALUE = 155;
  private static final int RGB_MAX_VALUE = 255;


  @Override
  public List<RgbColor> generateColors(int colorsAmount)
  {
    int generatedColorsPerGroupAmount = divideAndRoundUp(colorsAmount, BITWISE_RGB_SETS_AMOUNT);
    int generatedColorsPerGroupMaxIdex = generatedColorsPerGroupAmount - 1;


    List<RgbColor> edgeColors = new LinkedList<>();
    int generatedColorsPerGroupIndex = generatedColorsPerGroupMaxIdex;


    for (int j = 0; j < generatedColorsPerGroupAmount; j++)
    {
      for (int i = 1; i <= BITWISE_RGB_SETS_AMOUNT; i++)
      {
        int rgbValue = calculateSingleRgbValue(generatedColorsPerGroupIndex, generatedColorsPerGroupMaxIdex);

        int red = rgbValue * getBitAtPosition(i, 2);
        int green = rgbValue * getBitAtPosition(i, 1);
        int blue = rgbValue * getBitAtPosition(i, 0);

        RgbColor edgeColor = new RgbColor(red, green, blue);
        edgeColors.add(edgeColor);
      }

      generatedColorsPerGroupIndex = adjustGeneratedColorsPerGroupIndex(generatedColorsPerGroupIndex, generatedColorsPerGroupAmount);
    }

    return edgeColors;
  }

  private static int divideAndRoundUp(int number, int divisor)
  {
    return (number + divisor - 1) / divisor;
  }

  private static int calculateSingleRgbValue(int generatedColorsPerGroupIndex, int generatedColorsPerGroupMaxIndex)
  {
    int singleRgbValue = 255;
    if (generatedColorsPerGroupMaxIndex != 0)
    {
      singleRgbValue = (int) (((double) generatedColorsPerGroupIndex / generatedColorsPerGroupMaxIndex) * (RGB_MAX_VALUE - RGB_MIN_VALUE) + RGB_MIN_VALUE);
    }
    return singleRgbValue;
  }

  private int getBitAtPosition(int number, int position)
  {
    return (number >> position) & 1;
  }

  private int adjustGeneratedColorsPerGroupIndex(int generatedColorsPerGroupIndex, int generatedColorsPerGroupAmount)
  {
    int generatedColorsPerGroupStep = divideAndRoundUp(generatedColorsPerGroupAmount, 2);
    if (generatedColorsPerGroupIndex >= generatedColorsPerGroupAmount / 2)
    {
      generatedColorsPerGroupIndex -= generatedColorsPerGroupStep;
    }
    else
    {
      generatedColorsPerGroupIndex += generatedColorsPerGroupStep - 1;
    }
    return generatedColorsPerGroupIndex;
  }
}
