/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.dex.smali;

import com.reandroid.arsc.chunk.PackageBlock;
import com.reandroid.dex.key.MethodKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.dex.model.DexClassRepository;
import com.reandroid.dex.smali.formatters.ClassComment;
import com.reandroid.dex.smali.formatters.MethodComment;
import com.reandroid.dex.smali.formatters.ResourceIdComment;
import com.reandroid.utils.collection.ArrayCollection;

import java.io.IOException;
import java.util.List;

public class SmaliWriterSetting {

    private ResourceIdComment resourceIdComment;
    private List<MethodComment> methodCommentList;
    private List<ClassComment> classCommentList;
    private boolean sequentialLabel;
    private boolean commentUnicodeStrings;

    public SmaliWriterSetting() {
        this.sequentialLabel = true;
        this.commentUnicodeStrings = false;
    }

    public boolean isSequentialLabel() {
        return sequentialLabel;
    }
    public void setSequentialLabel(boolean sequentialLabel) {
        this.sequentialLabel = sequentialLabel;
    }

    public boolean isCommentUnicodeStrings() {
        return commentUnicodeStrings;
    }
    public void setCommentUnicodeStrings(boolean commentUnicodeStrings) {
        this.commentUnicodeStrings = commentUnicodeStrings;
    }

    public void writeResourceIdComment(SmaliWriter writer, long l) throws IOException {
        ResourceIdComment resourceIdComment = getResourceIdComment();
        if(resourceIdComment != null){
            resourceIdComment.writeComment(writer, (int)l);
        }
    }
    public void writeResourceIdComment(SmaliWriter writer, int i) throws IOException {
        ResourceIdComment resourceIdComment = getResourceIdComment();
        if(resourceIdComment != null){
            resourceIdComment.writeComment(writer, i);
        }
    }
    public ResourceIdComment getResourceIdComment() {
        return resourceIdComment;
    }
    public void setResourceIdComment(ResourceIdComment resourceIdComment) {
        this.resourceIdComment = resourceIdComment;
    }
    public void setResourceIdComment(PackageBlock packageBlock) {
        this.setResourceIdComment(new ResourceIdComment.ResourceTableComment(packageBlock));
    }

    public void writeMethodComment(SmaliWriter writer, MethodKey methodKey) throws IOException {
        List<MethodComment> methodCommentList = getMethodCommentList();
        if(methodCommentList != null) {
            for(MethodComment methodComment : methodCommentList) {
                methodComment.writeComment(writer, methodKey);
            }
        }
    }
    public List<MethodComment> getMethodCommentList() {
        return methodCommentList;
    }
    public void clearMethodComments() {
        List<MethodComment> commentList = this.methodCommentList;
        if(commentList != null) {
            commentList.clear();
        }
    }
    public void addMethodComment(MethodComment methodComment) {
        if(methodComment == null) {
            return;
        }
        List<MethodComment> commentList = this.methodCommentList;
        if(commentList == null) {
            commentList = new ArrayCollection<>();
            this.methodCommentList = commentList;
        }
        if(!commentList.contains(methodComment)) {
            commentList.add(methodComment);
        }
    }
    public void addMethodComments(DexClassRepository classRepository) {
        addMethodComment(new MethodComment.MethodOverrideComment(classRepository));
        addMethodComment(new MethodComment.MethodImplementComment(classRepository));
    }
    public void writeClassComment(SmaliWriter writer, TypeKey typeKey) throws IOException {
        List<ClassComment> commentList = getClassCommentList();
        if(commentList != null) {
            for(ClassComment comment : commentList) {
                comment.writeComment(writer, typeKey);
            }
        }
    }

    public List<ClassComment> getClassCommentList() {
        return classCommentList;
    }
    public void clearClassComments() {
        List<ClassComment> commentList = this.classCommentList;
        if(commentList != null) {
            commentList.clear();
        }
    }
    public void addClassComment(ClassComment classComment) {
        if(classComment == null) {
            return;
        }
        List<ClassComment> commentList = this.classCommentList;
        if(commentList == null) {
            commentList = new ArrayCollection<>();
            this.classCommentList = commentList;
        }
        if(!commentList.contains(classComment)) {
            commentList.add(classComment);
        }
    }
    public void addClassComments(DexClassRepository classRepository) {
        addClassComment(new ClassComment.ClassExtendComment(classRepository));
        addClassComment(new ClassComment.ClassImplementComment(classRepository));
    }
}
