package com.zane.smapiinstaller.entity;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帮助内容列表
 * @author Zane
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HelpItemList extends UpdatableList  {
    /**
     * 列表
     */
    private List<HelpItem> items;
}
