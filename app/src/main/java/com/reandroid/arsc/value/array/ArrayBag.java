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
 package com.reandroid.arsc.value.array;

 import com.reandroid.arsc.array.ResValueMapArray;
 import com.reandroid.arsc.value.Entry;
 import com.reandroid.arsc.value.ResTableMapEntry;
 import com.reandroid.arsc.value.ResValueMap;
 import com.reandroid.arsc.value.bag.Bag;

 import java.util.AbstractList;
 import java.util.Iterator;
 import java.util.RandomAccess;

 public class ArrayBag extends AbstractList<ArrayBagItem> implements Bag, RandomAccess {
     private final Entry entry;

     private ArrayBag(Entry entry) {
         this.entry = entry;
     }

     private ResTableMapEntry getTableEntry() {
         return (ResTableMapEntry) entry.getTableEntry();
     }

     private ResValueMapArray getMapArray() {
         return getTableEntry().getValue();
     }

     private void updateStructure(int regenStart) {
         getTableEntry().setValuesCount(size());
         modCount += 1;
         if (regenStart < 1) {
             return;
         }

         ResValueMapArray array = getMapArray();
         for (int i = regenStart; i < array.size(); i++) {
             setIndex(array.get(i), i);
         }
     }

     @Override
     public Entry getEntry() {
         return entry;
     }

     public ArrayBagItem[] getBagItems() {
         return toArray(new ArrayBagItem[0]);
     }

     @Override
     public int size() {
         return getMapArray().size();
     }

     @Override
     public ArrayBagItem get(int i) {
         return ArrayBagItem.create(getMapArray().get(i));
     }

     @Override
     public ArrayBagItem set(int index, ArrayBagItem value) {
         ArrayBagItem target = get(index);
         value.copyTo(target.getBagItem());
         return target;
     }

     private void setIndex(ResValueMap valueMap, int index) {
         valueMap.setNameId(0x01000001 + index);
     }

     @Override
     public void add(int index, ArrayBagItem value) {
         if (index < 0 || index > size()) {
             throw new IndexOutOfBoundsException();
         }
         if (value == null) {
             throw new NullPointerException("value is null");
         }

         ResValueMap valueMap = new ResValueMap();
         setIndex(valueMap, index);
         getMapArray().add(index, valueMap);
         value.copyTo(valueMap);
         updateStructure(index);
     }

     @Override
     public ArrayBagItem remove(int index) {
         ResValueMapArray array = getMapArray();
         ResValueMap target = array.get(index);
         array.remove(target);
         updateStructure(index);
         return ArrayBagItem.copyOf(target);
     }

     @Override
     public void clear() {
         getMapArray().clear();
         updateStructure(-1);
     }

     @Override
     public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("<");
         String type = getTypeName();
         builder.append(type);
         builder.append(" name=\"");
         builder.append(getName());
         builder.append("\">");
         ArrayBagItem[] allItems = getBagItems();
         for (ArrayBagItem allItem : allItems) {
             builder.append("\n    ");
             builder.append(allItem.toString());
         }
         builder.append("\n</");
         builder.append(type);
         builder.append(">");
         return builder.toString();
     }

     /**
      * The result of this is not always 100% accurate,
      * in addition to this use your methods to cross check like type-name == "array"
      **/
     public static boolean isArray(Entry entry) {
         ArrayBag array = create(entry);
         if (array == null) {
             return false;
         }
         ResTableMapEntry tableEntry = array.getTableEntry();
         if (tableEntry.getParentId() != 0) {
             return false;
         }
         Iterator<ResValueMap> iterator = tableEntry.iterator();
         if (!iterator.hasNext()) {
             return false;
         }

         int i = 0;
         while (iterator.hasNext()) {
             ResValueMap resValueMap = iterator.next();
             int name = resValueMap.getNameId();
             int high = (name >> 16) & 0xffff;
             if(high != 0x0100){
                 return false;
             }
             int low = name & 0xffff;
             if(low != (i + 1)){
                 return false;
             }
             i ++;
         }
         return true;
     }

     public static ArrayBag create(Entry entry) {
         if (entry == null || !entry.isComplex()) {
             return null;
         }
         return new ArrayBag(entry);
     }
 }
