package com.campus.lostfound.observer;

/**
 * 物品匹配主题接口
 * 观察者模式中的主题角色
 */
public interface ItemMatchSubject {
    
    /**
     * 添加观察者
     * @param observer 观察者
     */
    void addObserver(ItemMatchObserver observer);
    
    /**
     * 移除观察者
     * @param observer 观察者
     */
    void removeObserver(ItemMatchObserver observer);
    
    /**
     * 通知所有观察者匹配已找到
     * @param match 匹配记录
     */
    void notifyMatchFound(com.campus.lostfound.entity.ItemMatch match);
    
    /**
     * 通知所有观察者匹配已更新
     * @param match 匹配记录
     */
    void notifyMatchUpdated(com.campus.lostfound.entity.ItemMatch match);
    
    /**
     * 通知所有观察者匹配已取消
     * @param match 匹配记录
     */
    void notifyMatchCancelled(com.campus.lostfound.entity.ItemMatch match);
}




