package ru.inhell.aida.entity;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 28.12.10 18:33
 */
public class VFDType {
    public static enum TYPE{MIN5, MIN10, MIN15, MIN20, MAX5, MAX10, MAX15, MAX20}

    private Long id;

    private int n;
    private int l;
    private TYPE type;

    public VFDType() {
    }

    public VFDType(Long id, int n, int l, TYPE type) {
        this.id = id;
        this.n = n;
        this.l = l;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getL() {
        return l;
    }

    public void setL(int l) {
        this.l = l;
    }

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "VFDType{" +
                "id=" + id +
                ", n=" + n +
                ", l=" + l +
                ", type=" + type +
                '}';
    }
}
