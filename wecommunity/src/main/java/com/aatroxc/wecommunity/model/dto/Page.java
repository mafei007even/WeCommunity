package com.aatroxc.wecommunity.model.dto;


/**
 * 封装分页相关信息
 *
 * @author mafei007
 * @date 2020/3/30 22:35
 */

public class Page {

    /**
     * 当前页
     */
    private int current = 1;

    /**
     * 每页个数
     */
    private int limit = 10;

    /**
     * 数据总数 total
     */
    private int rows;

    /**
     * 查询路径（用于复用分页链接）
     */
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if (current >= 1) {
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit >= 1 && limit <= 100) {
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if (rows >= 0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页的起始行，就是limit 的参数
     */
    public int getOffset() {
        return (current - 1) * limit;
    }

    public int getTotalPage() {
        // rows / limit [+1]
        return rows % limit == 0 ? rows / limit : rows / limit + 1;
    }

    /**
     * 获取起始页码
     * @return
     */
    public int getFrom(){
        int from = current - 2;
        return from < 1 ? 1 : from;
    }

    /**
     * 获取结束页码
     * @return
     */
    public int getTo(){
        int to = current + 2;
        int total = getTotalPage();
        return to > total ? total : to;
    }

}
