/*
 * [The "BSD licence"]
 * Copyright (c) 2010 Ben Gruver (JesusFreke)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.baksmali.Adaptors;

import org.jf.baksmali.BaksmaliOptions;
import org.jf.baksmali.CommentProvider;
import org.jf.baksmali.formatter.BaksmaliWriter;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.HiddenApiRestriction;
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.Field;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.iface.value.IntEncodedValue;
import org.jf.dexlib2.iface.value.LongEncodedValue;
import org.jf.dexlib2.util.EncodedValueUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

public class FieldDefinition {
    public static void writeTo(BaksmaliWriter writer, Field field,
                               boolean setInStaticConstructor) throws IOException {
        EncodedValue initialValue = field.getInitialValue();
        int accessFlags = field.getAccessFlags();

        boolean isLikelyResourceIdField = false;
        if (initialValue != null &&
                AccessFlags.STATIC.isSet(accessFlags) &&
                AccessFlags.FINAL.isSet(accessFlags)) {
            if(setInStaticConstructor){
                if (!EncodedValueUtils.isDefaultValue(initialValue)) {
                    writer.write("# The value of this static final field might be set in the static constructor\n");
                } else {
                    // don't write out the default initial value for static final fields that get set in the static
                    // constructor
                    initialValue = null;
                }
            }
            isLikelyResourceIdField = AccessFlags.PUBLIC.isSet(accessFlags);
        }

        writer.write(".field ");
        writeAccessFlagsAndRestrictions(writer, field.getAccessFlags(), field.getHiddenApiRestrictions());
        writer.writeSimpleName(field.getName());
        writer.write(':');
        writer.writeType(field.getType());

        if (initialValue != null) {
            writer.write(" = ");
            writer.writeEncodedValue(initialValue);
            if(isLikelyResourceIdField){
                writeResourceIdCommentIfRequired(writer, initialValue);
            }
        }

        writer.write('\n');

        Collection<? extends Annotation> annotations = field.getAnnotations();
        if (annotations.size() > 0) {
            writer.indent(4);

            AnnotationFormatter.writeTo(writer, annotations);
            writer.deindent(4);
            writer.write(".end field\n");
        }
    }
    private static void writeResourceIdCommentIfRequired(BaksmaliWriter writer, EncodedValue initialValue) throws IOException {
        int type = initialValue.getValueType();
        int value;
        if(type == ValueType.INT){
            value = ((IntEncodedValue) initialValue).getValue();
        }else if(type == ValueType.LONG){
            value = (int) ((LongEncodedValue) initialValue).getValue();
        }else {
            return;
        }
        BaksmaliOptions options = writer.getOptions();
        CommentProvider commentProvider = options.getCommentProvider();
        String comment = commentProvider.getComment(value);
        if(comment == null){
            return;
        }
        writer.write(" # ");
        writer.write(comment);
    }

    private static void writeAccessFlagsAndRestrictions(
            BaksmaliWriter writer, int accessFlags, Set<HiddenApiRestriction> hiddenApiRestrictions)
            throws IOException {
        for (AccessFlags accessFlag: AccessFlags.getAccessFlagsForField(accessFlags)) {
            writer.write(accessFlag.toString());
            writer.write(' ');
        }
        for (HiddenApiRestriction hiddenApiRestriction : hiddenApiRestrictions) {
            writer.write(hiddenApiRestriction.toString());
            writer.write(' ');
        }
    }
}
