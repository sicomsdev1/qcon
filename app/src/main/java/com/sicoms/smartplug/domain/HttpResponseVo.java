package com.sicoms.smartplug.domain;

/**
 * Created by gudnam on 2015. 6. 24..
 */
public class HttpResponseVo {
    private String jsonStr;
    private String result;
    private String errMsg;

    public HttpResponseVo(){}
    public HttpResponseVo(String jsonStr, String result, String errMsg){
        this.jsonStr = jsonStr;
        this.result = result;
        this.errMsg = errMsg;
    }

    public String getJsonStr() {
        return jsonStr;
    }

    public void setJsonStr(String jsonStr) {
        this.jsonStr = jsonStr;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
