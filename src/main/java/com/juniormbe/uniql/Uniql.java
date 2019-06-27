package com.juniormbe.uniql;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * The Direction enum
 *
 * @author Junior Mbe
 * @version 1.0
 * @since 28/06/2019
 */
public class Uniql {
    private String name;
    private Map<String, Uniql> fields;
    private String query;
    private PageRequest page;
    private SortRequest sort;

    public String getName() {
        return name;
    }

    public Uniql setName(String name) {
        this.name = name;
        return this;
    }

    public Map<String, Uniql> getFields() {
        return fields;
    }

    public Uniql setFields(Map<String, Uniql> fields) {
        this.fields = fields;
        return this;
    }

    private void initFields() {
        if(this.fields == null) {
            this.fields = new HashMap<String, Uniql>();
        }
    }

    public Uniql addField(String name) {
        initFields();
        fields.put(name, new Uniql(name));
        return this;
    }

    public Uniql addField(Uniql field) {
        initFields();
        fields.put(field.name, field);
        return this;
    }

    public Uniql removeField(String name) {
        initFields();
        if(fields.containsKey(name)) {
            fields.remove(name);
        }
        return this;
    }

    public Uniql removeField(Field field) {
        initFields();
        fields.remove(field);
        return this;
    }

    public String getQuery() {
        return query;
    }

    public Uniql setQuery(String query) {
        this.query = query;
        return this;
    }

    public PageRequest getPage() {
        return page;
    }

    public Uniql setPage(PageRequest pageRequest) {
        this.page = pageRequest;
        return this;
    }

    public SortRequest getSort() {
        return sort;
    }

    public Uniql setSort(SortRequest sortRequest) {
        this.sort = sortRequest;
        return this;
    }

    public boolean hasFields() {
        return this.fields.size() > 0;
    }

    public boolean hasField(String name) {
        return this.fields.containsKey(name);
    }

    public boolean hasQuery() {
        return this.query != null && !this.query.isEmpty();
    }

    public boolean hasSort() {
        return this.sort != null;
    }

    public boolean hasPage() {
        return this.page != null;
    }

    private Uniql(String name) {
        this.name = name;
    }

    private Uniql(String name, Map<String, Uniql> fields, String query, PageRequest pageRequest, SortRequest sortRequest) {
        this.name = name;
        this.fields = fields;
        this.query = query;
        this.page = pageRequest;
        this.sort = sortRequest;
    }

    public static Uniql build(String name) {
        return new Uniql(name);
    }

    public static Uniql parse(String model) throws UniqlParseException {
        return UniqlParser.parse(model);
    }

    public String toModel() {
        return UniqlParser.toModel(this);
    }

    public String toFormattedModel() {
        return UniqlParser.toModel(this, true);
    }

    @Override
    public String toString() {
        return "Uniql{" +
          "setName='" + name + '\'' +
          ", setFields=" + fields +
          ", setQuery='" + query + '\'' +
          ", setPage=" + page +
          ", setSort=" + sort +
          '}';
    }
}
