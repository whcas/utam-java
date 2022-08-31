/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.compiler.grammar;

import static utam.compiler.diagnostics.ValidationUtilities.VALIDATION;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import utam.compiler.UtamCompilationError;
import utam.compiler.helpers.LocatorCodeGeneration.SelectorType;
import utam.core.element.Locator;
import utam.core.selenium.element.LocatorBy;

/**
 * root selector mapping class, base class for element selector. Separated to a base class because
 * root selector can't have args or be marked as a list. Regular element selector requires context
 * to be processed, root selector does not.
 *
 * @author elizaveta.ivanova
 * @since 236
 */
class UtamRootSelector {

  /**
   * The supported selector types as a comma-delimited string
   */
  private static final String SUPPORTED_SELECTOR_TYPES_STRING =
      String.join(", ", Stream.of(SelectorType.values())
          .map(Enum::name).collect(Collectors.toSet()));

  private final Locator locator;
  private final SelectorType selectorType;

  @JsonCreator
  UtamRootSelector(
      @JsonProperty(value = "css") String css,
      @JsonProperty(value = "accessid") String accessid,
      @JsonProperty(value = "classchain") String classchain,
      @JsonProperty(value = "uiautomator") String uiautomator) {
    if (css != null) {
      locator = LocatorBy.byCss(css);
      VALIDATION.validateNotEmptyString(css, "selector", "css");
      selectorType = SelectorType.css;
    } else if (accessid != null) {
      locator = LocatorBy.byAccessibilityId(accessid);
      VALIDATION.validateNotEmptyString(accessid, "selector", "accessid");
      selectorType = SelectorType.accessid;
    } else if (classchain != null) {
      locator = LocatorBy.byClassChain(classchain);
      VALIDATION.validateNotEmptyString(classchain, "selector", "classchain");
      selectorType = SelectorType.classchain;
    } else if (uiautomator != null) {
      locator = LocatorBy.byUiAutomator(uiautomator);
      VALIDATION.validateNotEmptyString(uiautomator, "selector", "uiautomator");
      selectorType = SelectorType.uiautomator;
    } else {
      throw new UtamCompilationError(VALIDATION.getErrorMessage(1002,  SUPPORTED_SELECTOR_TYPES_STRING));
    }
    if (Stream.of(css, classchain, uiautomator, accessid)
        .filter(Objects::nonNull).toArray().length > 1) {
      throw new UtamCompilationError(VALIDATION.getErrorMessage(1003, SUPPORTED_SELECTOR_TYPES_STRING));
    }
  }

  Locator getLocator() {
    return locator;
  }

  SelectorType getSelectorType() {
    return selectorType;
  }
}
