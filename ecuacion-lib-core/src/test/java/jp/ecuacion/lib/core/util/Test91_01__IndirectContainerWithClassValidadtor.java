package jp.ecuacion.lib.core.util;

import jakarta.validation.Valid;

public class Test91_01__IndirectContainerWithClassValidadtor {
  @Valid
  public Test91_01__DirectContainerWithClassValidadtor directContainer =
      new Test91_01__DirectContainerWithClassValidadtor();
}
