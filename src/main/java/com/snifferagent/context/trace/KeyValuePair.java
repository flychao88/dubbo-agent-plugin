
package com.snifferagent.context.trace;


import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * The <code>KeyValuePair</code> represents a object which contains a string key and a string value.
 *
 * @author chao.cheng
 */
public class KeyValuePair {
    private String key;
    private String value;

    public KeyValuePair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }


    @Override
    public String toString() {
        try {
            return ToStringBuilder.reflectionToString(this);
        } catch (Exception e) {
            return "";
        }
    }
}
