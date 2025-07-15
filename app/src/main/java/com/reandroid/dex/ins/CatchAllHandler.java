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
package com.reandroid.dex.ins;

import com.reandroid.dex.base.Ule128Item;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.smali.SmaliDirective;

public class CatchAllHandler extends ExceptionHandler {

    public CatchAllHandler() {
        super(0);
    }
    CatchAllHandler(boolean forCopy) {
        super();
    }

    CatchAllHandler newCopy(TryItem parent){
        CatchAllHandler handler = new Copy(this);
        handler.setIndex(getIndex());
        handler.setParent(parent);
        return handler;
    }

    @Override
    TypeId getTypeId(){
        return null;
    }
    @Override
    public SmaliDirective getSmaliDirective(){
        return SmaliDirective.CATCH_ALL;
    }

    static class Copy extends CatchAllHandler {

        private final CatchAllHandler catchAllHandler;

        Copy(CatchAllHandler catchAllHandler){
            super(true);
            this.catchAllHandler = catchAllHandler;
        }
        @Override
        public boolean isRemoved() {
            return super.isRemoved() || catchAllHandler.isRemoved();
        }

        @Override
        Ule128Item getCatchAddressUle128(){
            return catchAllHandler.getCatchAddressUle128();
        }
    }
}
