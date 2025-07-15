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
package com.reandroid.archive.writer;

import com.reandroid.archive.Archive;
import com.reandroid.archive.block.LocalFileHeader;

/**
 * Creates {@link com.reandroid.archive.block.DataDescriptor}
 * */
public interface DataDescriptorFactory {

    void createDataDescriptor(LocalFileHeader lfh);

    /**
     * Keeps as it is
     * */
    DataDescriptorFactory NO_ACTION = lfh ->
            lfh.setHasDataDescriptor(lfh.hasDataDescriptor());
    /**
     * Clears all
     * */
    DataDescriptorFactory NONE = lfh ->
            lfh.setHasDataDescriptor(false);
    /**
     * Generates for compressed/deflated entries only
     * */
    DataDescriptorFactory FOR_DEFLATED = lfh ->
            lfh.setHasDataDescriptor(lfh.getMethod() == Archive.DEFLATED);
    /**
     * Generates for un-compressed/stored entries only
     * */
    DataDescriptorFactory FOR_STORED = lfh ->
            lfh.setHasDataDescriptor(lfh.getMethod() == Archive.STORED);
    /**
     * Generates for all entries
     * */
    DataDescriptorFactory FOR_ALL = lfh ->
            lfh.setHasDataDescriptor(true);
}
