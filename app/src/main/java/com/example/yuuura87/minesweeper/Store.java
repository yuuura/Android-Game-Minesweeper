package com.example.yuuura87.minesweeper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class Store implements Serializable{

    private final int NUM_PERSONS = 10;

    private int level;
    private ArrayList<Person> recordsArrEasy = new ArrayList<Person>();
    private ArrayList<Person> recordsArrNormal = new ArrayList<Person>();
    private ArrayList<Person> recordsArrExpert = new ArrayList<Person>();

    public ArrayList<Person> getRecordsArrEasy() {
        return recordsArrEasy;
    }

    public ArrayList<Person> getRecordsArrNormal() { return recordsArrNormal; }

    public ArrayList<Person> getRecordsArrExpert() {
        return recordsArrExpert;
    }

    public int getNUM_PERSONS() {
        return NUM_PERSONS;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

}

class Person implements Serializable {

    private String name;
    private int time;
    private double place[] = new double[2];
    private String address;
    private int level;

    Person(String name, int time, double[] place, String address, int level) {
        this.name = name;
        this.time = time;
        this.place = Arrays.copyOf(place, place.length);
        this.address = address;
        this.level = level;
    }

    public String getAddress() { return address; }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTime() {
        return time;
    }

    public double[] getPlace() {
        return place;
    }

}