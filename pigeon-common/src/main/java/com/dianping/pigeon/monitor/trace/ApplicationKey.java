package com.dianping.pigeon.monitor.trace;

/**
 * @author qi.yin
 *         2016/11/17  下午1:25.
 */
public class ApplicationKey implements SourceKey {

    private String appName;

    public ApplicationKey(String appName) {
        this.appName = appName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ApplicationKey that = (ApplicationKey) o;

        return !(appName != null ? !appName.equals(that.appName) : that.appName != null);

    }

    @Override
    public int hashCode() {
        return appName != null ? appName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ApplicationKey{" + "appName=" + appName + "}";
    }
}
