

package com.dubboagent.context.trace;


import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author chao.cheng
 */
public class LogDataEntity {

    private long timestamp = 0;
    private List<KeyValuePair> logs;

    private LogDataEntity(long timestamp, List<KeyValuePair> logs) {
        this.timestamp = timestamp;
        this.logs = logs;
    }

    public List<KeyValuePair> getLogs() {
        return logs;
    }

    public static class Builder {
        protected List<KeyValuePair> logs;

        public Builder() {
            logs = new LinkedList<KeyValuePair>();
        }

        public Builder add(KeyValuePair... fields) {
            for (KeyValuePair field : fields) {
                logs.add(field);
            }
            return this;
        }

        public LogDataEntity build(long timestamp) {
            return new LogDataEntity(timestamp, logs);
        }
    }


}
