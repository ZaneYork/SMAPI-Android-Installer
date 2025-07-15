/*
 * Copyright 2012, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.dexlib2.immutable;

import org.jf.dexlib2.HiddenApiRestriction;
import org.jf.dexlib2.base.reference.BaseFieldReference;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableEncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableEncodedValueFactory;
import org.jf.util.ImmutableConverter;
import org.jf.util.ImmutableUtils;
import org.jf.util.collection.ArraySet;
import org.jf.util.collection.SetUtil;

import java.util.Collection;
import java.util.Set;

public class ImmutableField extends BaseFieldReference implements Field {

    protected final String definingClass;

    protected final String name;

    protected final String type;
    protected final int accessFlags;
     protected final ImmutableEncodedValue initialValue;

    protected final Set<? extends ImmutableAnnotation> annotations;

    protected final Set<HiddenApiRestriction> hiddenApiRestrictions;

    public ImmutableField( String definingClass,
                           String name,
                           String type,
                          int accessFlags,
                           EncodedValue initialValue,
                           Collection<? extends Annotation> annotations,
                           Set<HiddenApiRestriction> hiddenApiRestrictions) {
        this.definingClass = definingClass;
        this.name = name;
        this.type = type;
        this.accessFlags = accessFlags;
        this.initialValue = ImmutableEncodedValueFactory.ofNullable(initialValue);
        this.annotations = ImmutableAnnotation.immutableSetOf(annotations);
        this.hiddenApiRestrictions =
                hiddenApiRestrictions == null ? SetUtil.of() : ArraySet.copyOf(hiddenApiRestrictions);
    }

    public ImmutableField( String definingClass,
                           String name,
                           String type,
                          int accessFlags,
                           ImmutableEncodedValue initialValue,
                           Set<? extends ImmutableAnnotation> annotations,
                           Set<HiddenApiRestriction> hiddenApiRestrictions) {
        this.definingClass = definingClass;
        this.name = name;
        this.type = type;
        this.accessFlags = accessFlags;
        this.initialValue = initialValue;
        this.annotations = ImmutableUtils.nullToEmptySet(annotations);
        this.hiddenApiRestrictions = ImmutableUtils.nullToEmptySet(hiddenApiRestrictions);
    }

    public static ImmutableField of(Field field) {
        if (field instanceof  ImmutableField) {
            return (ImmutableField)field;
        }
        return new ImmutableField(
                field.getDefiningClass(),
                field.getName(),
                field.getType(),
                field.getAccessFlags(),
                field.getInitialValue(),
                field.getAnnotations(),
                field.getHiddenApiRestrictions());
    }


    @Override
    public String getDefiningClass() { return definingClass; }

    @Override
    public String getName() { return name; }

    @Override
    public String getType() { return type; }
    @Override
    public int getAccessFlags() { return accessFlags; }
    @Override
    public EncodedValue getInitialValue() { return initialValue;}

    @Override
    public Set<? extends ImmutableAnnotation> getAnnotations() { return annotations; }

    @Override
    public Set<HiddenApiRestriction> getHiddenApiRestrictions() { return hiddenApiRestrictions; }


    public static Set<ImmutableField> immutableSetOf( Iterable<? extends Field> list) {
        return CONVERTER.toSortedSet(list);
    }

    private static final ImmutableConverter<ImmutableField, Field> CONVERTER =
            new ImmutableConverter<ImmutableField, Field>() {
                @Override
                protected boolean isImmutable( Field item) {
                    return item instanceof ImmutableField;
                }


                @Override
                protected ImmutableField makeImmutable( Field item) {
                    return ImmutableField.of(item);
                }
            };
}
