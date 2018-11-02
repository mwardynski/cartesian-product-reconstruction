package at.ac.unileoben.mat.dissertation.printout;

import at.ac.unileoben.mat.dissertation.printout.data.RgbColor;

import java.util.List;

public interface EdgeColorsGenerator
{
  List<RgbColor> generateColors(int colorsAmount);
}
