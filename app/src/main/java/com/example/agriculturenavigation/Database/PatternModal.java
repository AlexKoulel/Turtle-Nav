package com.example.agriculturenavigation.Database;

public class PatternModal
{
    private String patternname;
    private String pattern;
    private String belongsto;
    private int pid;

    public String getPatternname() {
        return patternname;
    }

    public void setPatternname(String patternname) {
        this.patternname = patternname;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public String getBelongsto() {
        return belongsto;
    }

    public void setBelongsto(String belongsto) {
        this.belongsto = belongsto;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public PatternModal(String patternname, String pattern, String belongsto) {
        this.patternname = patternname;
        this.pattern = pattern;
        this.belongsto = belongsto;
    }

}
