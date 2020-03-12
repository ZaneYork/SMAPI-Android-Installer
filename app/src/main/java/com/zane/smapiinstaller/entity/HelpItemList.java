package com.zane.smapiinstaller.entity;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class HelpItemList extends UpdatableList  {
    private List<HelpItem> items;
}
