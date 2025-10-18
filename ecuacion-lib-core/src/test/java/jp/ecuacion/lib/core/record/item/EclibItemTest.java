package jp.ecuacion.lib.core.record.item;

import static org.junit.jupiter.api.Assertions.assertFalse;
import jp.ecuacion.lib.core.util.ObjectsUtil.RequireNonEmptyException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EclibItemTest {

  @Test
  public void constructorTest() {

    // itemPropertyPath is empty
    try {
      new EclibItem(null);
      assertFalse(true);
    } catch (RequireNonEmptyException ex) {
      // okay
    }

    try {
      new EclibItem("");
      assertFalse(true);
    } catch (RequireNonEmptyException ex) {
      // okay
    }
  }

  @Test
  public void itemNameKeyTest() {

    // No itemNameKey settings / itemPropertyPath does not have "."
    String result = new EclibItem("itemPropertyPath").getItemNameKey("rootRecordName");
    Assertions.assertEquals("rootRecordName.itemPropertyPath", result);

    // No itemNameKey settings / itemPropertyPath have 1 "."
    result = new EclibItem("itemProperty.Path").getItemNameKey("rootRecordName");
    Assertions.assertEquals("itemProperty.Path", result);

    // No itemNameKey settings / itemPropertyPath have 2 "."
    result = new EclibItem("item.Property.Path").getItemNameKey("rootRecordName");
    Assertions.assertEquals("Property.Path", result);

    // itemNameKeyField settings / itemPropertyPath does not have "."
    result = new EclibItem("itemPropertyPath").itemNameKey("itemNameKeyField")
        .getItemNameKey("rootRecordName");
    Assertions.assertEquals("rootRecordName.itemNameKeyField", result);

    // itemNameKeyField settings / itemPropertyPath has 1 "."
    result = new EclibItem("itemProperty.Path").itemNameKey("itemNameKeyField")
        .getItemNameKey("rootRecordName");
    Assertions.assertEquals("itemProperty.itemNameKeyField", result);

    // itemNameKeyField settings / itemPropertyPath has 2 "."
    result = new EclibItem("item.Property.Path").itemNameKey("itemNameKeyField")
        .getItemNameKey("rootRecordName");
    Assertions.assertEquals("Property.itemNameKeyField", result);
    
    // full itemNameKey settings / itemPropertyPath does not have "."
    result = new EclibItem("itemPropertyPath").itemNameKey("itemNameKeyClass.itemNameKeyField")
        .getItemNameKey("rootRecordName");
    Assertions.assertEquals("itemNameKeyClass.itemNameKeyField", result);

    // full itemNameKey settings / itemPropertyPath has 1 "."
    result = new EclibItem("itemProperty.Path").itemNameKey("itemNameKeyClass.itemNameKeyField")
        .getItemNameKey("rootRecordName");
    Assertions.assertEquals("itemNameKeyClass.itemNameKeyField", result);

    // full itemNameKey settings / itemPropertyPath has 2 "."
    result = new EclibItem("item.Property.Path").itemNameKey("itemNameKeyClass.itemNameKeyField")
        .getItemNameKey("rootRecordName");
    Assertions.assertEquals("itemNameKeyClass.itemNameKeyField", result);
  }
}
