package com.juniormbe.uniql;

import java.util.Arrays;

/**
 * The SortRequest class
 *
 * @author Junior Mbe
 * @version 1.0
 * @since 28/06/2019
 */
public class SortRequest {
    private Direction direction;
    private String[] fieldNames;

    public Direction getDirection() {
        return this.direction;
    }

    public SortRequest setDirection(Direction direction) {
        this.direction = direction;
        return this;
    }

    public String[] getFieldNames() {
        return this.fieldNames;
    }

    public SortRequest setFieldNames(String[] fieldNames) {
        this.fieldNames = fieldNames;
        return this;
    }

    private SortRequest(Direction direction, String[] fieldNames) {
        this.direction = direction;
        this.fieldNames = fieldNames;
    }

    public static SortRequest of(Direction direction, String[]fieldNames) {
        return new SortRequest(direction, fieldNames);
    }

    @Override
    public String toString() {
        return "SortRequest{" +
          "direction=" + direction +
          ", fieldNames=" + Arrays.toString(fieldNames) +
          '}';
    }
}
