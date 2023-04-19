package com.example.agriculturenavigation.Database;

public class FieldModal {

    private String fieldname;
    private String fieldlocation;
    private String fieldarea;
    private String fieldpattern;
    private int id;

    public String getFieldName(){
        return fieldname;
    }

    public void setFieldName(String fieldname){
        this.fieldname = fieldname;
    }

    public String getFieldLocation(){
        return fieldlocation;
    }

    public void  setFieldLocation(String fieldlocation){
        this.fieldlocation = fieldlocation;
    }

    public String getFieldArea(){
        return fieldarea;
    }

    public void  setFieldArea(String fieldarea){
        this.fieldarea = fieldarea;
    }

    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public FieldModal(String fieldname, String fieldlocation,String fieldarea){
        this.fieldname = fieldname;
        this.fieldlocation = fieldlocation;
        this.fieldarea = fieldarea;
    }
}
