package jp.ecuacion.lib.core.util;

import jakarta.validation.Valid;

public class Test91_01__IndirectContainerWithNormalValidator {
  @Valid
  public Test91_01__DirectContainerWithNormalValidator directContainer =
      new Test91_01__DirectContainerWithNormalValidator();
}
