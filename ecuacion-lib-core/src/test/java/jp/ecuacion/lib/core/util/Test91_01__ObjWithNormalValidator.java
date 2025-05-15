package jp.ecuacion.lib.core.util;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class Test91_01__ObjWithNormalValidator {
  @NotNull
  public String str1 = null;

  @Min(3)
  public int int1 = 2;
}
