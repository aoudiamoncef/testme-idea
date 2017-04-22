package com.weirddev.testme.intellij.template.context;

import com.weirddev.testme.intellij.template.LangTestBuilderFactory;

import java.util.Map;

/**
 * Date: 22/04/2017
 *
 * @author Yaron Yamin
 */
public class TestBuilderImpl implements TestBuilder{
    private final LangTestBuilderFactory langTestBuilderFactory;
    private int maxRecursionDepth;

    public TestBuilderImpl(int maxRecursionDepth, boolean shouldIgnoreUnusedProperties) {
        this.maxRecursionDepth = maxRecursionDepth;
        langTestBuilderFactory = new LangTestBuilderFactory(maxRecursionDepth, shouldIgnoreUnusedProperties);
    }

    @Override
    public String renderMethodParams(String language, Method method, Map<String, String> replacementTypes, Map<String, String> defaultTypeValues) throws Exception {
        return langTestBuilderFactory.createTestBuilder(language, method).renderJavaCallParams(method.getMethodParams(), replacementTypes, defaultTypeValues);
    }

}