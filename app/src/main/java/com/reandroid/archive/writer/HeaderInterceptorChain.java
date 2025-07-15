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

import com.reandroid.archive.ArchiveInfo;
import com.reandroid.archive.block.CentralEntryHeader;
import com.reandroid.archive.block.DataDescriptor;
import com.reandroid.archive.block.LocalFileHeader;

public class HeaderInterceptorChain implements HeaderInterceptor{

    private ArchiveInfo archiveInfo;
    private HeaderInterceptor headerInterceptor;
    private DataDescriptorFactory dataDescriptorFactory;

    private HeaderInterceptorChain(){
    }

    public DataDescriptorFactory getDataDescriptorFactory() {
        DataDescriptorFactory factory = this.dataDescriptorFactory;
        if(factory == null) {
            factory = DataDescriptorFactory.NO_ACTION;
            this.dataDescriptorFactory = factory;
        }
        return factory;
    }
    public void setDataDescriptorFactory(DataDescriptorFactory dataDescriptorFactory) {
        this.dataDescriptorFactory = dataDescriptorFactory;
    }

    public ArchiveInfo getArchiveInfo() {
        return archiveInfo;
    }
    public void setArchiveInfo(ArchiveInfo archiveInfo) {
        this.archiveInfo = archiveInfo;
    }

    public boolean isDisabled() {
        return getArchiveInfo() == null &&
                getHeaderInterceptor() == null &&
                getDataDescriptorFactory() == DataDescriptorFactory.NO_ACTION;
    }

    public void setHeaderInterceptor(HeaderInterceptor headerInterceptor) {
        this.headerInterceptor = headerInterceptor;
    }
    public HeaderInterceptor getHeaderInterceptor() {
        return headerInterceptor;
    }
    @Override
    public void onWriteLfh(LocalFileHeader header) {
        getDataDescriptorFactory().createDataDescriptor(header);
        HeaderInterceptor interceptor = getArchiveInfo();
        if(interceptor != null){
            interceptor.onWriteLfh(header);
        }
        interceptor = getHeaderInterceptor();
        if(interceptor != null){
            interceptor.onWriteLfh(header);
        }
    }

    @Override
    public void onWriteDD(DataDescriptor dataDescriptor) {
        HeaderInterceptor interceptor = getArchiveInfo();
        if(interceptor != null){
            interceptor.onWriteDD(dataDescriptor);
        }
        interceptor = getHeaderInterceptor();
        if(interceptor != null){
            interceptor.onWriteDD(dataDescriptor);
        }
    }
    @Override
    public void onWriteCeh(CentralEntryHeader header) {
        HeaderInterceptor interceptor = getArchiveInfo();
        if(interceptor != null){
            interceptor.onWriteCeh(header);
        }
        interceptor = getHeaderInterceptor();
        if(interceptor != null){
            interceptor.onWriteCeh(header);
        }
    }

    public static HeaderInterceptorChain createDefault(){
        HeaderInterceptorChain interceptorChain = new HeaderInterceptorChain();
        interceptorChain.setArchiveInfo(ArchiveInfo.apk());
        interceptorChain.setDataDescriptorFactory(DataDescriptorFactory.NO_ACTION);
        return interceptorChain;
    }
}
