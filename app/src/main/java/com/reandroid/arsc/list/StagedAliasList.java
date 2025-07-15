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
package com.reandroid.arsc.list;

import com.reandroid.arsc.chunk.StagedAlias;
import com.reandroid.arsc.container.BlockList;

public class StagedAliasList extends BlockList<StagedAlias> {
    public StagedAliasList(){
        super();
    }
    private StagedAlias pickOne(){
        for(StagedAlias stagedAlias:getChildes()){
            if(stagedAlias!=null){
                return stagedAlias;
            }
        }
        return null;
    }
    public void merge(StagedAliasList stagedAliasList){
        if(stagedAliasList==null || stagedAliasList==this || stagedAliasList.size()==0){
            return;
        }
        StagedAlias exist = pickOne();
        if(exist==null){
            exist=new StagedAlias();
            add(exist);
        }
        for(StagedAlias stagedAlias:stagedAliasList.getChildes()){
            exist.merge(stagedAlias);
        }
    }
}
