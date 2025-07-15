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

import com.reandroid.arsc.chunk.PackageBlock;

import java.util.Iterator;

public class ResourcePackage implements Iterable<ResourceType> {

    private final PackageBlock packageBlock;

    public ResourcePackage(PackageBlock packageBlock) {
        this.packageBlock = packageBlock;
    }

    @Override
    public Iterator<ResourceType> iterator() {
        return getPackageBlock().getTypes();
    }
    public PackageBlock getPackageBlock() {
        return packageBlock;
    }
    public int getId() {
        return getPackageBlock().getId();
    }
    public String getName() {
        return getPackageBlock().getName();
    }

    @Override
    public int hashCode() {
        return getId();
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResourcePackage)) {
            return false;
        }
        ResourcePackage other = (ResourcePackage) obj;
        return this.getId() == other.getId();
    }
    @Override
    public String toString() {
        return getName();
    }
}
