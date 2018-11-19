package com.link.cloud.network.bean;

import java.util.List;

/**
 * Created by 49488 on 2018/10/21.
 */

public class RequestBindFinger {
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public InParamsBean getInParams() {
        return inParams;
    }

    public void setInParams(InParamsBean inParams) {
        this.inParams = inParams;
    }

    public NotInParamsBean getNotInParams() {
        return notInParams;
    }

    public void setNotInParams(NotInParamsBean notInParams) {
        this.notInParams = notInParams;
    }

    public int getPageNo() {
        return pageNo;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public ParamsBean getParams() {
        return params;
    }

    public void setParams(ParamsBean params) {
        this.params = params;
    }

    public int getSearchType() {
        return searchType;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    public SortsBean getSorts() {
        return sorts;
    }

    public void setSorts(SortsBean sorts) {
        this.sorts = sorts;
    }

    public List<String> getContentList() {
        return contentList;
    }

    public void setContentList(List<String> contentList) {
        this.contentList = contentList;
    }

    /**
     * content : string
     * contentList : ["string"]
     * inParams : {"additionalProp1":[{}],"additionalProp2":[{}],"additionalProp3":[{}]}
     * notInParams : {"additionalProp1":[{}],"additionalProp2":[{}],"additionalProp3":[{}]}
     * pageNo : 0
     * pageSize : 0
     * params : {}
     * searchType : 0
     * sorts : {}
     */


    private String content;
    private InParamsBean inParams;
    private NotInParamsBean notInParams;
    private int pageNo;
    private int pageSize;
    private ParamsBean params;
    private int searchType;
    private SortsBean sorts;
    private List<String> contentList;

    private class InParamsBean {
    }

    private class NotInParamsBean {
    }

    private class ParamsBean {
    }

    private class SortsBean {
    }
}
