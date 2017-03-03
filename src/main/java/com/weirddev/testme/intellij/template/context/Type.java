package com.weirddev.testme.intellij.template.context;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.psi.util.PropertyUtil;
import com.intellij.psi.util.PsiUtil;
import com.weirddev.testme.intellij.template.TypeDictionary;
import com.weirddev.testme.intellij.utils.ClassNameUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Date: 24/10/2016
 * @author Yaron Yamin
 */
public class Type {
    private final String canonicalName;
    private final String name;
    private final boolean isPrimitive;
    private final String packageName;
    private final List<Type> composedTypes;
    private final boolean array;
    private final List<String> enumValues;
    private final boolean isEnum;
    private final boolean isInterface;
    private final boolean isAbstract;
    private final List<Method> constructors=new ArrayList<Method>();
    private final List<Method> methods=new ArrayList<Method>();//resolve Setters/Getters only for now

    Type(String canonicalName, String name, String packageName, boolean isPrimitive, boolean isInterface, boolean isAbstract,  boolean array, List<Type> composedTypes) {
        this.canonicalName = canonicalName;
        this.name = name;
        this.isPrimitive = isPrimitive;
        this.packageName = packageName;
        this.isInterface = isInterface;
        this.isAbstract = isAbstract;
        this.array = array;
        this.composedTypes = composedTypes;
        enumValues = new ArrayList<String>();
        isEnum = false;
    }

    Type(String canonicalName) {
        this(ClassNameUtils.extractContainerType(canonicalName), ClassNameUtils.extractClassName(canonicalName), ClassNameUtils.extractPackageName(canonicalName),false, false,false,ClassNameUtils.isArray(canonicalName),null);
    }

    public Type(PsiType psiType, @Nullable TypeDictionary typeDictionary, int maxRecursionDepth) {
        String canonicalText = psiType.getCanonicalText();
        array = ClassNameUtils.isArray(canonicalText);
        this.canonicalName = ClassNameUtils.stripArrayDesignator(canonicalText);
        this.name = ClassNameUtils.stripArrayDesignator(psiType.getPresentableText());
        packageName = ClassNameUtils.extractPackageName(canonicalName);
        this.isPrimitive = psiType instanceof PsiPrimitiveType;
        composedTypes = resolveTypes(psiType,typeDictionary,maxRecursionDepth);
        PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
        isEnum = psiClass != null && psiClass.isEnum();
        isInterface = psiClass != null && psiClass.isInterface();
        isAbstract = psiClass != null && psiClass.getModifierList()!=null &&  psiClass.getModifierList().hasModifierProperty(PsiModifier.ABSTRACT);
        enumValues = resolveEnumValues(psiType);
        if (psiClass != null && maxRecursionDepth>0 && !canonicalText.startsWith("java.") /*todo consider replacing with just java.util.* || java.lang.*  */&& typeDictionary!=null) {
            for (PsiMethod psiMethod : psiClass.getConstructors()) {
                if (typeDictionary.isAccessible(psiMethod)) {
                    constructors.add(new Method(psiMethod,psiClass, maxRecursionDepth-1, typeDictionary));
                }
            }
            Collections.sort(constructors, new Comparator<Method>() {
                @Override
                public int compare(Method o1, Method o2) { //sort in reverse order by #no of c'tor params
                    return o2.getMethodParams().size()-o1.getMethodParams().size();
                }
            });
            final PsiMethod[] methods = psiClass.getMethods();
            for (PsiMethod method : methods) {
                if (PropertyUtil.isSimpleGetter(method) || PropertyUtil.isSimpleSetter(method)) {
                    this.methods.add(new Method(method,psiClass,maxRecursionDepth-1,typeDictionary));
                }
            }
        }
    }

    private static List<String> resolveEnumValues(PsiType psiType) {
        PsiClass psiClass = PsiUtil.resolveClassInType(psiType);
        List<String> enumValues = new ArrayList<String>();
        if (psiClass != null && psiClass.isEnum()) {
            for (PsiField field : psiClass.getFields()) {
                if (field instanceof PsiEnumConstant) {
                    final PsiEnumConstant enumConstant = (PsiEnumConstant) field;
                    final PsiEnumConstantInitializer initializingClass = enumConstant.getInitializingClass();
                    if (initializingClass == null) {
                        enumValues.add(enumConstant.getName());
                    }
                }
            }
        }
        return enumValues;
    }

    private List<Type> resolveTypes(PsiType psiType, TypeDictionary typeDictionary, int maxRecursionDepth) {
        ArrayList<Type> types = new ArrayList<Type>();
        if (psiType instanceof PsiClassReferenceType) {
            PsiClassReferenceType psiClassReferenceType = (PsiClassReferenceType) psiType;
            PsiType[] parameters = psiClassReferenceType.getParameters();
            if (parameters.length > 0) {
                for (PsiType parameter : parameters) {
                    types.add(new Type(parameter,typeDictionary, maxRecursionDepth));
                }
            }
        }
        return types;
    }

    public String getCanonicalName() {
        return canonicalName;
    }

    public String getName() {
        return name;
    }

    public boolean isPrimitive() {
        return isPrimitive;
    }

    public String getPackageName() {
        return packageName;
    }

    public List<Type> getComposedTypes() {
        return composedTypes;
    }

    public boolean isArray() {
        return array;
    }

    public List<String> getEnumValues() {
        return enumValues;
    }

    public boolean isEnum() {
        return isEnum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Type type = (Type) o;

        return canonicalName.equals(type.canonicalName);
    }

    @Override
    public int hashCode() {
        return canonicalName.hashCode();
    }

    @Override
    public String toString() {
        return "Type{" +
                "canonicalName='" + canonicalName + '\'' +
                ", name='" + name + '\'' +
                ", isPrimitive=" + isPrimitive +
                ", packageName='" + packageName + '\'' +
                ", composedTypes=" + composedTypes +
                ", array=" + array +
                ", enumValues=" + enumValues +
                ", isEnum=" + isEnum +
                ", constructors=" + constructors +
                ", methods=" + methods +
                '}';
    }

    public List<Method> getConstructors() {
        return constructors;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public boolean isAbstract() {
        return isAbstract;
    }
}
