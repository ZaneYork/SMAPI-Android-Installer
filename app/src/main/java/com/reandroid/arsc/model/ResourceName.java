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
package com.reandroid.arsc.model;

import android.text.TextUtils;

import com.reandroid.arsc.coder.ReferenceString;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.ObjectsUtil;

public class ResourceName implements Comparable<ResourceName> {

    private String packageName;
    private final String type;
    private final String name;

    public ResourceName(String packageName, String type, String name) {
        this.packageName = packageName;
        this.type = type;
        this.name = name;
    }
    public ResourceName(String type, String name) {
        this(null, type, name);
    }

    public String getPackageName() {
        return packageName;
    }
    public void setPackageName(String packageName) {
        if(TextUtils.isEmpty(packageName)) {
            packageName = null;
        }
        this.packageName = packageName;
    }
    public boolean matchesPackageName(String packageName) {
        String p = this.getPackageName();
        if(p == null || TextUtils.isEmpty(packageName)) {
            return true;
        }
        return p.equals(packageName);
    }

    public String getType() {
        return type;
    }
    public String getName() {
        return name;
    }

    public String buildReference(boolean attribute, String packageContext) {
        String packageName = getPackageName();
        boolean appendPackage = packageContext != null && packageName != null &&
                !packageName.equals(packageContext);
        return buildReference(attribute, appendPackage);
    }
    public String buildReference(Boolean attribute, boolean appendPackage) {
        StringBuilder builder = new StringBuilder();
        if(attribute != null) {
            if(attribute) {
                builder.append('?');
            }else {
                builder.append('@');
            }
        }
        if(appendPackage) {
            String packageName = getPackageName();
            if(packageName != null){
                builder.append(packageName);
                builder.append(':');
            }
        }
        builder.append(getType());
        builder.append('/');
        builder.append(getName());
        return builder.toString();
    }

    @Override
    public int compareTo(ResourceName resourceName) {
        if(resourceName == this) {
            return 0;
        }
        int i = CompareUtil.compare(getPackageName(), resourceName.getPackageName());
        if(i == 0) {
            i = CompareUtil.compare(getType(), resourceName.getType());
        }
        if(i == 0) {
            i = CompareUtil.compare(getName(), resourceName.getName());
        }
        return i;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if(!(obj instanceof ResourceName)) {
            return false;
        }
        ResourceName that = (ResourceName) obj;
        String p1 = this.getPackageName();
        String p2 = that.getPackageName();
        if(p1 != null && p2 != null && !p1.equals(p2)) {
            return false;
        }
        return ObjectsUtil.equals(this.getType(), that.getType()) &&
                ObjectsUtil.equals(this.getName(), that.getName());
    }
    @Override
    public int hashCode() {
        return ObjectsUtil.hash(getType(), getName());
    }
    @Override
    public String toString() {
        return buildReference(false, true);
    }
    public static ResourceName parse(String reference) {
        return from(ReferenceString.parseReference(reference));
    }
    public static ResourceName from(ReferenceString referenceString) {
        if(referenceString != null) {
            return new ResourceName(referenceString.packageName, referenceString.type, referenceString.name);
        }
        return null;
    }
}
