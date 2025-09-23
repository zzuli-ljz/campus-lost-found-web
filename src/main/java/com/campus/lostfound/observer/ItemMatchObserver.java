package com.campus.lostfound.observer;

import com.campus.lostfound.entity.ItemMatch;

/**
 * 物品匹配观察者接口
 * 观察者模式中的观察者角色
 */
public interface ItemMatchObserver {
    
    /**
     * 当找到匹配时被调用
     * @param match 匹配记录
     */
    void onMatchFound(ItemMatch match);
    
    /**
     * 当匹配状态更新时被调用
     * @param match 匹配记录
     */
    void onMatchUpdated(ItemMatch match);
    
    /**
     * 当匹配被取消时被调用
     * @param match 匹配记录
     */
    void onMatchCancelled(ItemMatch match);
}






