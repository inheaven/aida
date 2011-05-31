package ru.inhell.aida.entity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 28.05.11 15:47
 */
public class AlphaOracleSchool {
    private final static Pattern PATTERN = Pattern.compile("(.*)-n(.*)l(.*)p(.*)m(.*)([ac])s(.*)");

    private Long id;
    private String name;
    private float balance;
    private int order;
    private int stop;
    private String start;
    private String end;


    private String getGroup(int index){
        Matcher m = PATTERN.matcher(name);
        m.matches();
        return m.group(index);
    }

    public String getSymbol(){
        return getGroup(1);
    }

    public int getN(){
        return Integer.parseInt(getGroup(2));
    }

    public int getL(){
        return Integer.parseInt(getGroup(3));
    }

    public int getP(){
        return Integer.parseInt(getGroup(4));
    }

    public int getM(){
        return Integer.parseInt(getGroup(5));
    }

    public boolean isClose(){
        return getGroup(6).equals("c");
    }

    public boolean isAverage(){
        return getGroup(6).equals("a");
    }

    public float getStopFactor(){
        return Float.parseFloat(getGroup(7));
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
