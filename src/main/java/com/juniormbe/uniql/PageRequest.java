package com.juniormbe.uniql;

/**
 * The PageRequest class
 *
 * @author Junior Mbe
 * @version 1.0
 * @since 28/06/2019
 */
public class PageRequest {
    private int number;
    private int size;

    public int getNumber() {
        return number;
    }

    public PageRequest setNumber(int number) {
        this.number = number;
        return this;
    }

    public int getSize() {
        return size;
    }

    private PageRequest(int number, int size) {
        this.number = number;
        this.size = size;
    }

    public static PageRequest of(int number, int size) {
        return new PageRequest(number, size);
    }

    @Override
    public String toString() {
        return "PageRequest{" +
          "number=" + number +
          ", size=" + size +
          '}';
    }
}
