/*
 * Copyright (c) 2021, salesforce.com, inc.
 * All rights reserved.
 * SPDX-License-Identifier: MIT
 * For full license text, see the LICENSE file in the repo root
 * or https://opensource.org/licenses/MIT
 */
package utam.compiler.representation;

import utam.compiler.helpers.ParameterUtils;
import utam.core.declarative.representation.MethodDeclaration;
import utam.core.declarative.representation.MethodParameter;
import utam.core.declarative.representation.PageObjectMethod;
import utam.core.declarative.representation.TypeProvider;
import utam.compiler.helpers.ElementContext;
import utam.compiler.helpers.MatcherType;

import java.util.ArrayList;
import java.util.List;

import static utam.compiler.helpers.TypeUtilities.BASIC_ELEMENT;
import static utam.compiler.helpers.TypeUtilities.wrapAsList;
import static utam.compiler.representation.ElementMethod.getElementLocationCode;
import static utam.compiler.representation.ElementMethod.getPredicateCode;
import static utam.compiler.representation.ElementMethod.getScopeElementCode;
import static utam.compiler.translator.TranslationUtilities.getElementGetterMethodName;

/**
 * generate code of getter method for custom element
 *
 * @author elizaveta.ivanova
 * @since 224
 */
public abstract class CustomElementMethod implements PageObjectMethod {

  /**
   * getter method that returns custom single element
   * @since 224
   */
  public static final class Single implements PageObjectMethod {

    private final List<String> codeLines = new ArrayList<>();
    private final List<TypeProvider> classImports = new ArrayList<>();
    private final List<TypeProvider> interfaceImports = new ArrayList<>();
    private final boolean isPublic;
    private final String methodName;
    private final MethodParametersTracker parametersTracker;
    private final TypeProvider returnType;

    public Single(
        boolean isPublic,
        String componentName,
        List<MethodParameter> locatorParameters,
        ElementContext scopeElement,
        TypeProvider returnType) {
      String scopeElementLine = getScopeElementCode(scopeElement);
      codeLines.add(scopeElementLine);
      String locationCode = getElementLocationCode(componentName, locatorParameters);
      String statement = String.format("return custom(%s, %s).build(%s.class)",
          scopeElement.getName(),
          locationCode,
          returnType.getSimpleName());
      codeLines.add(statement);
      ParameterUtils.setImport(interfaceImports, returnType);
      ParameterUtils.setImport(classImports, returnType);
      ParameterUtils.setImport(classImports, BASIC_ELEMENT);
      this.isPublic = isPublic;
      this.methodName = getElementGetterMethodName(componentName, isPublic);
      this.parametersTracker = new MethodParametersTracker(String.format("method '%s'", methodName));
      parametersTracker.setMethodParameters(scopeElement.getParameters());
      parametersTracker.setMethodParameters(locatorParameters);
      this.returnType = returnType;
    }

    @Override
    public List<String> getCodeLines() {
      return codeLines;
    }

    @Override
    public final List<TypeProvider> getClassImports() {
      return classImports;
    }

    @Override
    public MethodDeclaration getDeclaration() {
      return new MethodDeclarationImpl(methodName, parametersTracker.getMethodParameters(), returnType, interfaceImports);
    }

    @Override
    public boolean isPublic() {
      return this.isPublic;
    }
  }

  /**
   * getter method that returns custom single element or list using filter
   * @since 224
   */
  public static class Filtered implements PageObjectMethod {

    private final List<String> codeLines = new ArrayList<>();
    private final List<TypeProvider> classImports = new ArrayList<>();
    private final List<TypeProvider> interfaceImports = new ArrayList<>();
    private final boolean isPublic;
    private final String methodName;
    private final MethodParametersTracker parametersTracker;
    private final TypeProvider returnType;

    public Filtered(
        boolean isPublic,
        String componentName,
        List<MethodParameter> locatorParameters,
        ElementContext scopeElement,
        TypeProvider returnType,
        String applyMethod,
        List<MethodParameter> applyParameters,
        MatcherType matcherType,
        List<MethodParameter> matcherParameters,
        boolean isFindFirst) {
      this.returnType = isFindFirst ? returnType : wrapAsList(returnType);
      ParameterUtils.setImport(interfaceImports, this.returnType);
      ParameterUtils.setImport(classImports, this.returnType);
      ParameterUtils.setImport(classImports, BASIC_ELEMENT);
      this.isPublic = isPublic;
      this.methodName = getElementGetterMethodName(componentName, isPublic);
      this.parametersTracker = new MethodParametersTracker(String.format("method '%s'", methodName));
      parametersTracker.setMethodParameters(scopeElement.getParameters());
      parametersTracker.setMethodParameters(locatorParameters);
      parametersTracker.setMethodParameters(applyParameters);
      parametersTracker.setMethodParameters(matcherParameters);
      String scopeElementLine = getScopeElementCode(scopeElement);
      codeLines.add(scopeElementLine);
      String locationCode = getElementLocationCode(componentName, locatorParameters);
      String predicate = getPredicateCode(applyMethod, applyParameters, matcherType, matcherParameters);
      String methodName = isFindFirst? "build" : "buildList";
      String statement = String.format("return custom(%s, %s).%s(%s.class, %s)",
          scopeElement.getName(),
          locationCode,
          methodName,
          returnType.getSimpleName(),
          predicate);
      codeLines.add(statement);
    }

    @Override
    public List<String> getCodeLines() {
      return codeLines;
    }

    @Override
    public final List<TypeProvider> getClassImports() {
      return classImports;
    }

    @Override
    public MethodDeclaration getDeclaration() {
      return new MethodDeclarationImpl(methodName, parametersTracker.getMethodParameters(), returnType, interfaceImports);
    }

    @Override
    public boolean isPublic() {
      return this.isPublic;
    }
  }

  /**
   * getter method that returns list of custom elements
   * @since 224
   */
  public static final class Multiple implements PageObjectMethod {

    private final TypeProvider returnType;
    private final List<String> codeLines = new ArrayList<>();
    private final List<TypeProvider> classImports = new ArrayList<>();
    private final List<TypeProvider> interfaceImports = new ArrayList<>();
    private final boolean isPublic;
    private final String methodName;
    private final MethodParametersTracker parametersTracker;

    public Multiple(
        boolean isPublic,
        String componentName,
        List<MethodParameter> locatorParameters,
        ElementContext scopeElement,
        TypeProvider returnType) {
      this.returnType = wrapAsList(returnType);
      ParameterUtils.setImport(interfaceImports, this.returnType);
      ParameterUtils.setImport(classImports, this.returnType);
      ParameterUtils.setImport(classImports, BASIC_ELEMENT);
      String scopeElementLine = getScopeElementCode(scopeElement);
      codeLines.add(scopeElementLine);
      String locationCode = getElementLocationCode(componentName, locatorParameters);
      String statement = String.format("return custom(%s, %s).buildList(%s.class)",
          scopeElement.getName(),
          locationCode,
          returnType.getSimpleName());
      codeLines.add(statement);
      this.isPublic = isPublic;
      this.methodName = getElementGetterMethodName(componentName, isPublic);
      this.parametersTracker = new MethodParametersTracker(String.format("method '%s'", methodName));
      parametersTracker.setMethodParameters(scopeElement.getParameters());
      parametersTracker.setMethodParameters(locatorParameters);
    }

    @Override
    public MethodDeclarationImpl getDeclaration() {
      return new MethodDeclarationImpl(methodName, parametersTracker.getMethodParameters(), returnType, interfaceImports);
    }

    @Override
    public final List<TypeProvider> getClassImports() {
      return this.classImports;
    }

    @Override
    public List<String> getCodeLines() {
      return this.codeLines;
    }

    @Override
    public boolean isPublic() {
      return this.isPublic;
    }
  }
}
