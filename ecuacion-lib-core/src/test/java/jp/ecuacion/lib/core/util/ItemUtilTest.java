/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ecuacion.lib.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validation;
import java.util.List;
import java.util.Map;
import jp.ecuacion.lib.core.annotation.ItemNameKeyClass;
import jp.ecuacion.lib.core.item.Item;
import jp.ecuacion.lib.core.item.ItemContainer;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassAlwaysFalse;
import jp.ecuacion.lib.core.jakartavalidation.constraints.MethodAlwaysFalse;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link ItemUtil}. */
@DisplayName("ItemUtil")
public class ItemUtilTest {

  // --- Test fixtures ---

  private static class SimpleBeanWithoutContainer {
    @SuppressWarnings("unused")
    private @Nullable String name;
  }

  static class SimpleContainer implements ItemContainer {
    @SuppressWarnings("unused")
    private @Nullable String name;

    @Override
    public Item[] customizedItems() {
      return new Item[]{};
    }
  }

  static class ContainerWithCustomItem implements ItemContainer {
    @SuppressWarnings("unused")
    private @Nullable String email;

    @Override
    public Item[] customizedItems() {
      return new Item[]{
          new Item("email").itemNameKey("custom.email").hideValue(),
      };
    }
  }

  @ItemNameKeyClass("myAlias")
  private static class ContainerWithAnnotation implements ItemContainer {
    @SuppressWarnings("unused")
    private @Nullable String name;

    @Override
    public Item[] customizedItems() {
      return new Item[]{};
    }
  }

  static class RootWithContainerChild {
    private final SimpleContainer child;

    RootWithContainerChild(SimpleContainer child) {
      this.child = child;
    }

    public SimpleContainer getChild() {
      return child;
    }
  }

  static class RootWithCustomChild {
    private final ContainerWithCustomItem order;

    RootWithCustomChild(ContainerWithCustomItem order) {
      this.order = order;
    }

    public ContainerWithCustomItem getOrder() {
      return order;
    }
  }

  @ItemNameKeyClass("department")
  static class Dept {
    @SuppressWarnings("unused")
    private @Nullable String name;
  }

  private static class Company {
    @SuppressWarnings({"unused", "null"})
    @ItemNameKeyClass("dept")
    private List<Dept> deptList = List.of(new Dept());
  }

  private static class Employee implements ItemContainer {
    @SuppressWarnings("unused")
    @ItemNameKeyClass("ignored")
    private @Nullable String code;

    @SuppressWarnings("unused")
    private Dept belongingDept = new Dept();

    @SuppressWarnings({"unused", "null"})
    private List<Dept> plainDeptList = List.of(new Dept());

    @SuppressWarnings({"unused", "null"})
    @ItemNameKeyClass("dept")
    private List<Dept> deptList = List.of(new Dept());

    @SuppressWarnings("unused")
    @ItemNameKeyClass("Dept")
    private Map<String, Dept> deptMap = Map.of("key", new Dept());

    @SuppressWarnings("unused")
    @ItemNameKeyClass("dept")
    private Dept[] deptArray = new Dept[] {new Dept()};

    @SuppressWarnings({"unused", "null"})
    @ItemNameKeyClass("dept")
    private List<Dept> customizedDeptList = List.of(new Dept());

    @SuppressWarnings("unused")
    private Company company = new Company();

    @Override
    public Item[] customizedItems() {
      return new Item[] {new Item("customizedDeptList[].name").itemNameKey("custom.name")};
    }
  }

  static class EmployeeWithGetters implements ItemContainer {
    @SuppressWarnings({"unused", "null"})
    private List<Dept> fieldDeptList = List.of(new Dept());

    @ItemNameKeyClass("dept")
    public List<Dept> getDeptList() {
      return List.of(new Dept());
    }

    public Dept getBelongingDept() {
      return new Dept();
    }

    @ItemNameKeyClass("ignored")
    public List<Dept> getFieldDeptList() {
      return fieldDeptList;
    }

    @Override
    public Item[] customizedItems() {
      return new Item[] {};
    }
  }

  private static class EmployeeWithoutContainer {
    @SuppressWarnings({"unused", "null"})
    @ItemNameKeyClass("dept")
    private List<Dept> deptList = List.of(new Dept());
  }

  // --- Tests ---

  @Nested
  @DisplayName("resolveItem")
  class ResolveItem {

    @Test
    @DisplayName("rootBean not ItemContainer: itemNameKey derived from class name and field name")
    void rootBeanNotItemContainer() {
      Item item = ItemUtil.resolveItem("name", new SimpleBeanWithoutContainer());
      assertThat(item.getItemNameKey())
          .isEqualTo("simpleBeanWithoutContainer.name");
    }

    @Test
    @DisplayName("rootBean implements ItemContainer with no customization: itemNameKey from class name")
    void rootBeanIsItemContainerNoCustomization() {
      Item item = ItemUtil.resolveItem("name", new SimpleContainer());
      assertThat(item.getItemNameKey()).isEqualTo("simpleContainer.name");
    }

    @Test
    @DisplayName("rootBean implements ItemContainer with customized item: uses custom itemNameKey")
    void rootBeanIsItemContainerWithCustomItem() {
      Item item = ItemUtil.resolveItem("email", new ContainerWithCustomItem());
      assertThat(item.getItemNameKey()).isEqualTo("custom.email");
    }

    @Test
    @DisplayName("customized item's showsValue=false is preserved in resolved item")
    void customItemShowsValuePreserved() {
      Item item = ItemUtil.resolveItem("email", new ContainerWithCustomItem());
      assertThat(item.getShowsValue()).isFalse();
    }

    @Test
    @DisplayName("@ItemNameKeyClass annotation affects the class part of itemNameKey")
    void itemNameKeyClassAnnotation() {
      Item item = ItemUtil.resolveItem("name", new ContainerWithAnnotation());
      assertThat(item.getItemNameKey()).isEqualTo("myAlias.name");
    }

    @Test
    @DisplayName("resolved item's displayPropertyPath preserves original path including index")
    void displayPropertyPathPreservesIndex() {
      Item item = ItemUtil.resolveItem("child.name", new RootWithContainerChild(new SimpleContainer()));
      assertThat(item.getDisplayPropertyPath()).isEqualTo("child.name");
    }

    @Test
    @DisplayName("rootBean is not ItemContainer but first child is: item resolved from child")
    void firstChildIsItemContainer() {
      RootWithCustomChild root = new RootWithCustomChild(new ContainerWithCustomItem());
      Item item = ItemUtil.resolveItem("order.email", root);
      assertThat(item.getItemNameKey()).isEqualTo("custom.email");
    }
  }

  @Nested
  @DisplayName("resolveItem with nested itemPropertyPath")
  class ResolveItemNested {

    @Test
    @DisplayName("class part is the field name, not @ItemNameKeyClass at the field type's class")
    void classPartIsFieldName() {
      assertThat(ItemUtil.resolveItem("belongingDept.name", new Employee()).getItemNameKey())
          .isEqualTo("belongingDept.name");
      assertThat(
          ItemUtil.resolveItem("plainDeptList[0].name", new Employee()).getItemNameKey())
          .isEqualTo("plainDeptList.name");
    }

    @Test
    @DisplayName("@ItemNameKeyClass at a collection / array / map field replaces the field name")
    void itemNameKeyClassAtField() {
      assertThat(ItemUtil.resolveItem("deptList[0].name", new Employee()).getItemNameKey())
          .isEqualTo("dept.name");
      assertThat(ItemUtil.resolveItem("deptArray[0].name", new Employee()).getItemNameKey())
          .isEqualTo("dept.name");
      assertThat(ItemUtil.resolveItem("deptMap[key].name", new Employee()).getItemNameKey())
          .isEqualTo("dept.name");
    }

    @Test
    @DisplayName("@ItemNameKeyClass at a field is ignored when itemPropertyPath is not nested")
    void itemNameKeyClassAtFieldIgnoredWhenNotNested() {
      assertThat(ItemUtil.resolveItem("code", new Employee()).getItemNameKey())
          .isEqualTo("employee.code");
    }

    @Test
    @DisplayName("@ItemNameKeyClass at a field is looked up in the class of the preceding node")
    void itemNameKeyClassAtFieldOfDeeperNode() {
      assertThat(
          ItemUtil.resolveItem("company.deptList[0].name", new Employee()).getItemNameKey())
          .isEqualTo("dept.name");
    }

    @Test
    @DisplayName("class part set by itemNameKey() takes priority over @ItemNameKeyClass at field")
    void explicitItemNameKeyTakesPriority() {
      assertThat(ItemUtil.resolveItem("customizedDeptList[0].name", new Employee())
          .getItemNameKey()).isEqualTo("custom.name");
    }

    @Test
    @DisplayName("class part is the property name when the node refers to a getter")
    void classPartIsGetterPropertyName() {
      assertThat(
          ItemUtil.resolveItem("belongingDept.name", new EmployeeWithGetters()).getItemNameKey())
          .isEqualTo("belongingDept.name");
    }

    @Test
    @DisplayName("@ItemNameKeyClass at a getter replaces the property name when no field exists")
    void itemNameKeyClassAtGetter() {
      assertThat(
          ItemUtil.resolveItem("deptList[0].name", new EmployeeWithGetters()).getItemNameKey())
          .isEqualTo("dept.name");
    }

    @Test
    @DisplayName("@ItemNameKeyClass at a getter is ignored when the field exists")
    void itemNameKeyClassAtGetterIgnoredWhenFieldExists() {
      assertThat(ItemUtil.resolveItem("fieldDeptList[0].name", new EmployeeWithGetters())
          .getItemNameKey()).isEqualTo("fieldDeptList.name");
    }

    @Test
    @DisplayName("@ItemNameKeyClass at field applies also when no ItemContainer is found")
    void itemNameKeyClassAtFieldWithoutContainer() {
      assertThat(ItemUtil.resolveItem("deptList[0].name", new EmployeeWithoutContainer())
          .getItemNameKey()).isEqualTo("dept.name");
    }
  }

  @ClassAlwaysFalse(propertyPath = "name")
  static class CvChild {
    @SuppressWarnings("unused")
    private @Nullable String name;

    @MethodAlwaysFalse(propertyPath = "name")
    public boolean isAlwaysFalse() {
      return false;
    }
  }

  static class CvRoot {
    @SuppressWarnings("unused")
    @Valid
    private CvChild child = new CvChild();
  }

  private static ConstraintViolation<?> violationOf(Object bean,
      Class<?> annotationType) {
    return Validation.buildDefaultValidatorFactory().getValidator().validate(bean).stream()
        .filter(cv -> cv.getConstraintDescriptor().getAnnotation().annotationType()
            .equals(annotationType))
        .findFirst().orElseThrow();
  }

  @Nested
  @DisplayName("resolveItem / getFullPropertyPath with ConstraintViolation")
  class ResolveItemFromConstraintViolation {

    @Test
    @DisplayName("class constraint at the root bean: propertyPath is used as-is")
    void classConstraintAtRoot() {
      ConstraintViolation<?> cv = violationOf(new CvChild(), ClassAlwaysFalse.class);

      assertThat(ItemUtil.getFullPropertyPath(cv, "name")).isEqualTo("name");
    }

    @Test
    @DisplayName("method constraint at the root bean: propertyPath is used as-is")
    void methodConstraintAtRoot() {
      ConstraintViolation<?> cv = violationOf(new CvChild(), MethodAlwaysFalse.class);

      assertThat(ItemUtil.getFullPropertyPath(cv, "name")).isEqualTo("name");
    }

    @Test
    @DisplayName("class constraint at a nested bean: prefixed with the path to the bean")
    void classConstraintAtNestedBean() {
      ConstraintViolation<?> cv = violationOf(new CvRoot(), ClassAlwaysFalse.class);

      assertThat(ItemUtil.getFullPropertyPath(cv, "name")).isEqualTo("child.name");
    }

    @Test
    @DisplayName("method constraint at a nested bean: prefixed with the path to the bean")
    void methodConstraintAtNestedBean() {
      ConstraintViolation<?> cv = violationOf(new CvRoot(), MethodAlwaysFalse.class);

      assertThat(ItemUtil.getFullPropertyPath(cv, "name")).isEqualTo("child.name");
    }

    @Test
    @DisplayName("resolveItem resolves the item from the path relative to the root bean")
    void resolveItem() {
      ConstraintViolation<?> cv = violationOf(new CvRoot(), ClassAlwaysFalse.class);
      Item item = ItemUtil.resolveItem(cv, "name");

      assertThat(item.getPropertyPath()).isEqualTo("child.name");
      assertThat(item.getItemNameKey()).isEqualTo("child.name");
    }
  }
}
