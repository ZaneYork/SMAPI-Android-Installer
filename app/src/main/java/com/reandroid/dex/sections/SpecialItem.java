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
package com.reandroid.dex.sections;

import com.reandroid.arsc.base.OffsetSupplier;
import com.reandroid.dex.base.OffsetReceiver;
import com.reandroid.dex.base.PositionedItem;
import com.reandroid.dex.base.UsageMarker;
import com.reandroid.dex.common.SectionItemContainer;

public class SpecialItem extends SectionItemContainer
        implements PositionedItem, OffsetSupplier, OffsetReceiver {

    public SpecialItem(int childesCount) {
        super(childesCount);
    }

    @Override
    public boolean containsUsage(int usage) {
        return usage != UsageMarker.USAGE_NONE;
    }
}
