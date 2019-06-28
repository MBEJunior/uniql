package com.juniormbe.uniql;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The UniqlParser class
 *
 * @author Junior Mbe
 * @version 1.0
 * @since 28/06/2019
 */
public class UniqlParser {

    /**
     * Uniql model parts
     */
    private static enum Part {
        NAME,
        FIELDS,
        QUERY,
        PAGE,
        SORT
    }

    private static enum FieldType {
        SIMPLE,
        UNIQL
    }

    private static final char START_DEF_CHAR = '{';
    private static final char END_DEF_CHAR = '}';
    private static final char PART_GROUP_DELIMITER_CHAR = '|';
    private static final char LIST_SEPARATOR_CHAR = ',';
    private static final char PAGE_SEPARATOR_CHAR = '-';
    private static final char ASC_DIRECTION_CHAR = '+';
    private static final char DESC_DIRECTION_CHAR = '-';
    private static final char BACKSPACE_CHAR = '\n';
    private static final char SPACE_CHAR = ' ';
    private static final char TAB_SIZE = 2;
    private static final String identifierChars = "_abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final String separatorChars = "{}|,";


    /**
     * Build Uniql string model from Uniql object
     * @param uniql the Uniql object
     * @param format define if generated object will be formatted with backspace and tab
     * @param depth the recursive current depth used to format
     * @return the string model
     */
    private static String toModel(Uniql uniql, boolean format, int depth) {

        if(uniql == null) {
            return "";
        }

        char backspace = format ? BACKSPACE_CHAR : '\0';
        char space = format ? SPACE_CHAR : '\0';

        String tabDepth = "";
        String partTabDepth = "";

        if(format) {
            StringBuilder tabDepthBuilder = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                for (int j = 0; j < TAB_SIZE; j++) {
                    tabDepthBuilder.append(space);
                }
            }
            tabDepth = tabDepthBuilder.toString();
            for (int j = 0; j < TAB_SIZE; j++) {
                tabDepthBuilder.append(space);
            }
            partTabDepth = tabDepthBuilder.toString();
        }

        StringBuilder modelBuilder = new StringBuilder();
        modelBuilder.append(depth>0 ? backspace : '\0').append(tabDepth).append(uniql.getName());

        Map<String, Uniql> fields = uniql.getFields();
        String query = uniql.getQuery();
        PageRequest pageRequest = uniql.getPage();
        SortRequest sortRequest = uniql.getSort();

        if( fields == null && query == null && pageRequest == null && sortRequest == null) {
            return modelBuilder.toString();
        }

        modelBuilder.append(START_DEF_CHAR);

        // filter setFields
        if(fields != null) {
            boolean isNotFirst = false;
            for (Map.Entry<String, Uniql> entry : fields.entrySet()) {
                modelBuilder.append(isNotFirst ? LIST_SEPARATOR_CHAR : "");
                modelBuilder.append(toModel(entry.getValue(), format, depth+1));
                isNotFirst = true;
            }
        }

        if(query == null && pageRequest == null && sortRequest == null) {
            return modelBuilder.append(backspace).append(tabDepth).append(END_DEF_CHAR).toString();
        }

        // filter setQuery
        modelBuilder.append(backspace).append(partTabDepth).append(PART_GROUP_DELIMITER_CHAR);
        if(query != null) {
            modelBuilder.append(query);
        }

        if(pageRequest == null && sortRequest == null) {
            return modelBuilder.append(backspace).append(tabDepth).append(END_DEF_CHAR).toString();
        }

        // filter setPage
        modelBuilder.append(backspace).append(partTabDepth).append(PART_GROUP_DELIMITER_CHAR);
        if(pageRequest != null) {
            modelBuilder.append(pageRequest.getNumber()).append(PAGE_SEPARATOR_CHAR).append(pageRequest.getSize());
        }

        // filter setSort
        modelBuilder.append(backspace).append(partTabDepth).append(PART_GROUP_DELIMITER_CHAR);
        if(sortRequest != null) {
            char direction = sortRequest.getDirection() == Direction.DESC ? DESC_DIRECTION_CHAR : ASC_DIRECTION_CHAR;
            modelBuilder.append(direction).append(String.join(String.valueOf(LIST_SEPARATOR_CHAR), sortRequest.getFieldNames()));
        }

        modelBuilder.append(backspace).append(tabDepth).append(END_DEF_CHAR);

        return modelBuilder.toString();
    }

    /**
     * This method parse Uniql string model to Uniql object
     * @param model the Uniql string model
     * @param fullLength the length of the full model base ancestor
     * @param startPosition the start position of the current string model in the base ancestor model
     * @return parsed Uniql object
     * @throws UniqlParseException when parse fail
     */
    private static Uniql parse(String model, int fullLength, int startPosition) throws UniqlParseException {

        if(model == null) {
            throw new UniqlParseException("Null content at column " + startPosition);
        }
        model = model.replaceAll("[\\s\\u0000]", "");
        if(model.length() == 0) {
            throw new UniqlParseException("Empty content arround column " + startPosition);
        }

        StringBuilder stringBuilder = new StringBuilder();
        Part currentPart = Part.NAME;
        FieldType lastFieldType = null;
        int fullPosition = startPosition;
        boolean hasFinish = false;
        Uniql uniql = Uniql.build(null);

        for (int i = 0; i < model.length(); i++) {
            char c = model.charAt(i);
            fullPosition = startPosition + i;
            if(hasFinish) {
                throw new UniqlParseException("Unexpected character '"+c+"' when parsing was finish at position " + fullPosition);
            }
            if(identifierChars.indexOf(c) != -1 || separatorChars.indexOf(c) == -1) {
                stringBuilder.append(c);
                continue;
            }
            switch (c) {

                /*
                 * When found list separator <,>, when current part is :
                 * - setName : we throws UniqlParseException because setName cannot be follow by list separator
                 * - setFields : previous identifier string is a simple Uniql addField setName
                 * - setQuery : we continue, setQuery can contain list separator
                 * - setPage : throws UniqlParseException because setPage cannot contains list separator
                 * - setSort : we continue, setSort cant contains list seprator
                 */
                case LIST_SEPARATOR_CHAR :
                    switch (currentPart) {
                        case NAME:
                            throw new UniqlParseException("Unexpected list separator char <"+LIST_SEPARATOR_CHAR+"> after '"+stringBuilder.toString()+"' at position "+(fullPosition));
                        case FIELDS:
                            if(stringBuilder.length() == 0 && lastFieldType != FieldType.UNIQL) {
                                throw new UniqlParseException("Unexpected list separator <"+LIST_SEPARATOR_CHAR+"> in '"+uniql.getName()+"' field at position "+(fullPosition));
                            }
                            String lastFieldName = stringBuilder.toString();
                            if(lastFieldType != FieldType.UNIQL && !lastFieldName.isEmpty()) {
                                uniql.addField(lastFieldName);
                                lastFieldType = FieldType.SIMPLE;
                            }
                            stringBuilder.setLength(0);
                            break;
                        case QUERY:
                            stringBuilder.append(c);
                            break;
                        case PAGE:
                            throw new UniqlParseException("Unexpected list separator <"+LIST_SEPARATOR_CHAR+">  in '"+uniql.getName()+"' page at position "+(fullPosition));
                        case SORT:
                            stringBuilder.append(c);
                            break;
                    }
                    break;

                /*
                 * When found start definition caracter <{>, when current part is :
                 * - setName : we build Uniql with previous part which is the setName and current part become setFields
                 * - setFields : the current addField is a composed addField, recursively parse them with his model
                 * - setQuery : we continue, setQuery can start defeinition caracter
                 * - setPage : throws UniqlParseException because setPage cannot contains start definition caracter
                 * - setSort : throws UniqlParseException because setSort cannot contains start definition caracter
                 */
                case START_DEF_CHAR:
                    switch (currentPart) {
                        case NAME:
                            if(stringBuilder.length() == 0) {
                                throw new UniqlParseException("Unexpected start definition char <"+START_DEF_CHAR+"> in name at position "+(fullPosition));
                            }
                            uniql.setName(stringBuilder.toString());
                            stringBuilder.setLength(0);
                            currentPart = Part.FIELDS;
                            break;
                        case FIELDS:
                            if(stringBuilder.length() == 0) {
                                throw new UniqlParseException("Unexpected start definition char <"+START_DEF_CHAR+"> in '"+uniql.getName()+"' field at position "+(fullPosition));
                            }
                            String prevIdentifier = stringBuilder.toString();
                            StringBuilder defBuilder = new StringBuilder();
                            int startDefCharCount = 0;
                            int endDefCharCount = 0;
                            int cStartPosition = i - prevIdentifier.length();
                            int j = cStartPosition;
                            while (j < model.length()) {
                                char cc = model.charAt(j);
                                if(cc == START_DEF_CHAR) {
                                    startDefCharCount++;
                                }
                                if(cc == END_DEF_CHAR) {
                                    endDefCharCount++;
                                }
                                defBuilder.append(cc);
                                if(startDefCharCount > 0 && startDefCharCount == endDefCharCount) {
                                    String fieldDef = defBuilder.toString();
                                    uniql.addField(parse(fieldDef, fullLength, cStartPosition ));
                                    break;
                                }else {
                                    j++;
                                }
                            }
                            if(startDefCharCount != endDefCharCount) {
                                throw new UniqlParseException("Unclosed part char <"+END_DEF_CHAR+"> for '"+defBuilder.toString()+"' at position "+(fullPosition));
                            }
                            i = cStartPosition + defBuilder.length() - 1;
                            lastFieldType = FieldType.UNIQL;
                            stringBuilder.setLength(0);
                            break;
                        case QUERY:
                            stringBuilder.append(c);
                            break;
                        case PAGE:
                            throw new UniqlParseException("Unexpected start definition char <"+START_DEF_CHAR+">  in '"+uniql.getName()+"' query at position "+(fullPosition));
                        case SORT:
                            throw new UniqlParseException("Unexpected start definition char <"+START_DEF_CHAR+">  in '"+uniql.getName()+"' sort at position "+(fullPosition));
                    }
                    break;

                /*
                 * When found part group delimiter <|>, when current part is :
                 * - setName : throws UniqlParseException because setName cannot be follow by part group delimiter
                 * - setFields : is previous is a simple addField, we add it, if it is a composed addField, currentPart become setQuery
                 * - setQuery : we build setQuery and current part become setPage
                 * - setPage : we build setPage and current part become setSort
                 * - setSort : throws UniqlParseException because setSort cannot be follow part group delimiter
                 */
                case PART_GROUP_DELIMITER_CHAR:
                    switch (currentPart) {
                        case NAME:
                            throw new UniqlParseException("Unexpected part delimiter <"+PART_GROUP_DELIMITER_CHAR+"> after '"+stringBuilder.toString()+"' at position "+(fullPosition));
                        case FIELDS:
                            String lastFieldName = stringBuilder.toString();
                            if(lastFieldType != FieldType.UNIQL && !lastFieldName.isEmpty()) {
                                uniql.addField(lastFieldName);
                            }
                            stringBuilder.setLength(0);
                            currentPart = Part.QUERY;
                            break;
                        case QUERY:
                            uniql.setQuery(stringBuilder.toString());
                            stringBuilder.setLength(0);
                            currentPart = Part.PAGE;
                            break;
                        case PAGE:
                            if(stringBuilder.length()>0) {
                                String pageString = stringBuilder.toString();
                                int pageNumber = 1;
                                int pageSize = 0;
                                Pattern pattern = Pattern.compile("^(\\d+)-(\\d+)$");
                                Matcher matcher = pattern.matcher(pageString);
                                if(matcher.matches()) {
                                    pageNumber = Integer.parseInt(matcher.group(1));
                                    pageSize = Integer.parseInt(matcher.group(2));
                                }else {
                                    throw new UniqlParseException("Bad setPage definition in '"+uniql.getName()+"' at position "+(fullPosition-pageString.length()));
                                }
                                uniql.setPage(PageRequest.of(pageNumber, pageSize));
                            }
                            stringBuilder.setLength(0);
                            currentPart = Part.SORT;
                            break;
                        case SORT:
                            throw new UniqlParseException("Unexpected part delimiter <"+PART_GROUP_DELIMITER_CHAR+"> arround sort, in '"+uniql.getName()+"' at position "+(fullPosition));
                    }
                    break;

                /*
                 * When found end definition char <}>, when current part is :
                 * - setName : throws UniqlParseException because setName cannot be follow by end definition char
                 * - setFields : is previous is a simple addField, we add it
                 * - setQuery : we build setQuery
                 * - setPage : we build setPage
                 * - setSort : we build order
                 */
                case END_DEF_CHAR:
                    switch (currentPart) {
                        case NAME:
                            throw new UniqlParseException("Unexpected end definition char <"+END_DEF_CHAR+"> after '"+stringBuilder.toString()+"' at position "+(fullPosition));
                        case FIELDS:
                            String lastFieldName = stringBuilder.toString();
                            if(lastFieldType != FieldType.UNIQL && !lastFieldName.isEmpty()) {
                                uniql.addField(lastFieldName);
                            }
                            break;
                        case QUERY:
                            uniql.setQuery(stringBuilder.toString());
                            break;
                        case PAGE:
                            if(stringBuilder.length()>0) {
                                String pageString = stringBuilder.toString();
                                // todo remove
                                System.out.println(pageString);
                                Pattern pattern = Pattern.compile("^(\\d+)-(\\d+)$");
                                Matcher matcher = pattern.matcher(pageString);
                                if(matcher.matches()) {
                                    int pageNumber = Integer.parseInt(matcher.group(1));;
                                    int pageSize = Integer.parseInt(matcher.group(2));;
                                    uniql.setPage(PageRequest.of(pageNumber, pageSize));
                                }else {
                                    throw new UniqlParseException("Bad setPage definition in '"+uniql.getName()+"' at position "+(fullPosition-pageString.length()));
                                }
                            }
                            break;
                        case SORT:
                            if(stringBuilder.length()>0) {
                                String sortString = stringBuilder.toString();
                                Pattern pattern = Pattern.compile("^([+-]?)((?:\\w+)(?:,\\s*\\w+)*)$");
                                Matcher matcher = pattern.matcher(sortString);
                                if(matcher.matches()) {
                                    Direction dir = Direction.ASC;
                                    if(matcher.group(1).length() > 0) {
                                        if(matcher.group(1).charAt(0) == DESC_DIRECTION_CHAR) {
                                            dir = Direction.DESC;
                                        }
                                    }
                                    String[] fieldsList = matcher.group(2).split(",");
                                    uniql.setSort(SortRequest.of(dir, fieldsList));
                                }else {
                                    throw new UniqlParseException("Bad setSort definition in '"+uniql.getName()+"' at position "+(fullPosition-sortString.length()));
                                }
                            }
                            break;
                    }
                    hasFinish = true;
                    break;
                default:
                    stringBuilder.append(c);
            }
        }
        if(!hasFinish) {
            if(currentPart == Part.NAME) {
                uniql.setName(stringBuilder.toString());
            }
        }
        return uniql;
    }

    public static String toModel(Uniql uniql) {
        return toModel(uniql, false, 0);
    }

    public static String toModel(Uniql uniql, boolean format) {
        return toModel(uniql, format, 0);
    }

    public static Uniql parse(String model) throws UniqlParseException {
        if(model == null) {
            throw new UniqlParseException("Null content at column " + 0);
        }
        return parse(model, model.length(), 0);
    }
}

