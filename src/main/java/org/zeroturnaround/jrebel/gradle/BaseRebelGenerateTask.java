package org.zeroturnaround.jrebel.gradle;

import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.zeroturnaround.jrebel.gradle.model.RebelMainModel;
import org.zeroturnaround.jrebel.gradle.model.RebelWar;

public interface BaseRebelGenerateTask {

  @Input
  String getPackaging();

  void skipWritingRebelXml();

  void generate();

  @Input
  @Optional
  RebelMainModel getRebelModel();

  void propagateConventionMappingSettings();

  @Input
  boolean getShowGenerated();

  @Input
  boolean getAlwaysGenerate();

  @Input
  @Optional
  RebelWar getWar();
}
