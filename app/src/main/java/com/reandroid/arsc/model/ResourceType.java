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
import com.reandroid.arsc.container.SpecTypePair;
import com.reandroid.arsc.item.TypeString;
import com.reandroid.arsc.pool.SpecStringPool;
import com.reandroid.arsc.pool.TypeStringPool;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.EmptyIterator;
import com.reandroid.utils.collection.FilterIterator;

import java.util.Iterator;

public class ResourceType implements Comparable<ResourceType>, Iterable<ResourceEntry> {

    private final PackageBlock packageBlock;
    private final int id;

    public ResourceType(PackageBlock packageBlock, int id) {
        this.packageBlock = packageBlock;
        this.id = id;
    }
    public ResourceType(SpecTypePair specTypePair) {
        this(specTypePair.getPackageBlock(), specTypePair.getId());
    }

    public ResourceEntry get(int resourceId) {
        if(resourceId == 0 || resourceId >>> 24 != getPackageId()) {
            return null;
        }
        int t = getId();
        if(t == 0 || (resourceId >> 16 & 0xff) != t) {
            return null;
        }
        if((resourceId & 0xffff) > size()) {
            return null;
        }
        return new ResourceEntry(getPackageBlock(), resourceId);
    }
    public int getIdentifier(String name) {
        SpecStringPool specStringPool = getPackageBlock().getSpecStringPool();
        return specStringPool.resolveResourceId(getId(), name);
    }

    @Override
    public Iterator<ResourceEntry> iterator() {
        return FilterIterator.of(getResources(), ResourceEntry::isDefined);
    }
    public int size() {
        SpecTypePair specTypePair = getSpecTypePair();
        if(specTypePair != null) {
            return specTypePair.getHighestEntryCount();
        }
        return 0;
    }
    public boolean isEmpty() {
        return size() == 0 || !iterator().hasNext();
    }
    private Iterator<ResourceEntry> getResources() {
        SpecTypePair specTypePair = getSpecTypePair();
        if(specTypePair != null) {
            return specTypePair.getResources();
        }
        return EmptyIterator.of();
    }
    private SpecTypePair getSpecTypePair() {
        return getPackageBlock().getSpecTypePair(getId());
    }
    public String getName() {
        return getPackageBlock().typeNameOf(getId());
    }
    public void setName(String name) {
        TypeStringPool typeStringPool = getPackageBlock().getTypeStringPool();
        int typeId = getId();
        TypeString typeString = typeStringPool.getById(typeId);
        if(typeString == null) {
            typeStringPool.getOrCreate(typeId, name);
        } else {
            typeString.set(name);
        }
    }
    public PackageBlock getPackageBlock() {
        return packageBlock;
    }
    public int getId() {
        return id;
    }
    public int getPackageId() {
        return getPackageBlock().getId();
    }

    @Override
    public int compareTo(ResourceType resourceType) {
        if(resourceType == null) {
            return -1;
        }
        if(resourceType == this) {
            return 0;
        }
        int i = CompareUtil.compare(getPackageId(), resourceType.getPackageId());
        if(i == 0) {
            i = CompareUtil.compare(getId(), resourceType.getId());
        }
        return i;
    }
    @Override
    public int hashCode() {
        return getPackageId() << 24 | getId() << 16;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ResourceType)) {
            return false;
        }
        ResourceType other = (ResourceType) obj;
        return this.getPackageBlock() == other.getPackageBlock();
    }
    @Override
    public String toString() {
        return getName();
    }
}
