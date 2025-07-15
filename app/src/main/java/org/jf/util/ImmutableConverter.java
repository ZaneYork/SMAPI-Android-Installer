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

package org.jf.util;

import org.jf.util.collection.ArraySet;
import org.jf.util.collection.ListUtil;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

public abstract class ImmutableConverter<ImmutableItem, Item> {
    protected abstract boolean isImmutable( Item item);

    protected abstract ImmutableItem makeImmutable( Item item);


    @SuppressWarnings("unchecked")
    public List<ImmutableItem> toList( final Iterable<? extends Item> iterable) {
        if (iterable == null) {
            return ListUtil.of();
        }

        boolean needsCopy = false;
        if (iterable instanceof List) {
            for (Item element: iterable) {
                if (!isImmutable(element)) {
                    needsCopy = true;
                    break;
                }
            }
        } else {
            needsCopy = true;
        }

        if (!needsCopy) {
            return (List<ImmutableItem>)iterable;
        }

        final Iterator<? extends Item> iter = iterable.iterator();

        return ListUtil.copyOf(new Iterator<ImmutableItem>() {
            @Override public boolean hasNext() { return iter.hasNext(); }
            @Override public ImmutableItem next() { return makeImmutable(iter.next()); }
            @Override public void remove() { iter.remove(); }
        });
    }


    public ArraySet<ImmutableItem> toSet( final Iterable<? extends Item> iterable) {
        if (iterable == null) {
            return ArraySet.of();
        }

        boolean needsCopy = false;
        if (iterable instanceof ArraySet) {
            for (Item element: iterable) {
                if (!isImmutable(element)) {
                    needsCopy = true;
                    break;
                }
            }
        } else {
            needsCopy = true;
        }

        if (!needsCopy) {
            return (ArraySet<ImmutableItem>)iterable;
        }

        final Iterator<? extends Item> iter = iterable.iterator();

        return ArraySet.copyOf(new Iterator<ImmutableItem>() {
            @Override public boolean hasNext() { return iter.hasNext(); }
            @Override public ImmutableItem next() { return makeImmutable(iter.next()); }
            @Override public void remove() { iter.remove(); }
        }).sort();
    }


    @SuppressWarnings("unchecked")
    public ArraySet<ImmutableItem> toSortedSet(final Iterable<? extends Item> iterable) {
        if (iterable == null) {
            return ArraySet.of();
        }

        boolean needsCopy = false;
        if (iterable instanceof ArraySet) {
            for (Item element: iterable) {
                if (!isImmutable(element)) {
                    needsCopy = true;
                    break;
                }
            }
        } else {
            needsCopy = true;
        }

        if (!needsCopy) {
            return (ArraySet<ImmutableItem>)iterable;
        }

        final Iterator<? extends Item> iter = iterable.iterator();


        return ArraySet.copyOf(new Iterator<ImmutableItem>() {
            @Override public boolean hasNext() { return iter.hasNext(); }
            @Override public ImmutableItem next() { return makeImmutable(iter.next()); }
            @Override public void remove() { iter.remove(); }
        }).sort();
    }


    @SuppressWarnings("unchecked")
    public SortedSet<ImmutableItem> toSortedSet( Comparator<? super ImmutableItem> comparator,
                                                 final SortedSet<? extends Item> sortedSet) {
        if (sortedSet == null || sortedSet.size() == 0) {
            return (SortedSet<ImmutableItem>) ArraySortedSet.of((Comparator)comparator, new Object[0]);
        }

        ImmutableItem[] newItems = (ImmutableItem[])new Object[sortedSet.size()];
        int index = 0;
        for (Item item: sortedSet) {
            newItems[index++] = makeImmutable(item);
        }

        return ArraySortedSet.of(comparator, newItems);
    }
}
