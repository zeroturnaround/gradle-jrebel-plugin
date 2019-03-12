package org.zeroturnaround.jrebel.gradle;

import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;

public interface BaseRebelGenerateTask {

  String getPackaging();

  void skipWritingRebelXml();

  void generate();

  RebelMainModel getRebelModel();

  void propagateConventionMappingSettings();

  boolean getShowGenerated();

  boolean getAlwaysGenerate();

  RebelWar getWar();
}
