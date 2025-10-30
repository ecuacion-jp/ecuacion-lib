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
package jp.ecuacion.lib.core.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Accepts and store data from user input, external system, and so on.
 * 
 * <p>This is an interface, not a class 
 *     because record can be customized according to the specification of page templates.
 *     Each record in apps always extends EclibRecord or its extended class, 
 *     so interface is needed to customize records.<br>
 *     At that time in that interface you want to record feature like getItems(),
 *     but if this is a class it can't be used.</p>
 * 
 * <p>It is frequently validated with jakarta validation. So it should have features below.</p>
 * <ul>
 * <li>To resolve item name from propertyPath. {@code getItems()} is used for it.<br>
 *     It's used especially for error message to users.</li>
 * </ul>
 */
public interface EclibItemContainer {

  /**
   * Returns an array of items.
   */
  public abstract EclibItem[] getItems();

  /**
   * Returns a new instance.
   */
  public default EclibItem getNewItem(String itemPropertyPath) {
    return new EclibItem(itemPropertyPath);
  }

  /**
   * Returns {@code EclibItem} from {@code EclibItem[]} and {@code fieldId}. 
   * 
   * @param itemPropertyPath itemPropertyPath
   * @return HtmlItem
   */
  default EclibItem getItem(String itemPropertyPath) {

    Map<String, EclibItem> map = Arrays.asList(getItems()).stream()
        .collect(Collectors.toMap(e -> e.getItemPropertyPath(), e -> e));

    EclibItem field = map.get(itemPropertyPath);

    return field == null ? getNewItem(itemPropertyPath) : field;
  }

  /**
   * Merge common items and record dependent items.
   * 
   * <p>This is an utility method so it can be defined in Util class, 
   *     but it's frequently used in record instance and not used outside 
   *     so let it be defined here.</p>
   */
  default EclibItem[] mergeItems(EclibItem[] items1, EclibItem[] items2) {
    List<EclibItem> list = new ArrayList<>(Arrays.asList(items1));

    // Throw an exception if item is duplicated.
    List<String> propertyPath1List =
        Arrays.asList(items1).stream().map(e -> e.getItemPropertyPath()).toList();

    for (String propertyPath2 : Arrays.asList(items2).stream().map(e -> e.getItemPropertyPath())
        .toList()) {
      if (propertyPath1List.contains(propertyPath2)) {
        throw new RuntimeException(
            "'itemPropertyPath' of EclibItem[] duplicated in Items. key: "
                + propertyPath2);
      }
    }

    list.addAll(Arrays.asList(items2));

    return list.toArray(new EclibItem[list.size()]);
  }
}
