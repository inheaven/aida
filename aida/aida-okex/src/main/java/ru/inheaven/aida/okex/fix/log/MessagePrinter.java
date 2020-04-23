package ru.inheaven.aida.okex.fix.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.MsgType;

import java.util.Iterator;

public class MessagePrinter {
    private Logger log = LoggerFactory.getLogger(MessagePrinter.class);

    DataDictionary dataDictionary;

    public MessagePrinter() {
        try {
            dataDictionary = new DataDictionary("FIX44.xml");
        } catch (ConfigError configError) {
            configError.printStackTrace();
        }
    }

    public void printSimple(Message message) throws FieldNotFound {
        String msgType = message.getHeader().getString(MsgType.FIELD);
        printFieldMapSimple("", dataDictionary, msgType, message);
    }

    private void printFieldMapSimple(String prefix, DataDictionary dd, String msgType, FieldMap fieldMap)
            throws FieldNotFound {

        StringBuilder buf = new StringBuilder();

        Iterator fieldIterator = fieldMap.iterator();
        while (fieldIterator.hasNext()) {
            Field field = (Field) fieldIterator.next();
            if (!isGroupCountField(dd, field)) {
                String value = fieldMap.getString(field.getTag());
                if (dd.hasFieldValue(field.getTag())) {
                    value = dd.getValueName(field.getTag(), fieldMap.getString(field.getTag()));
                }
                buf.append(value).append(" ");
            }
        }

        log.info(buf.toString());
    }


    public void print(Message message) throws FieldNotFound {
        String msgType = message.getHeader().getString(MsgType.FIELD);
        printFieldMap("", dataDictionary, msgType, message.getHeader());
        printFieldMap("", dataDictionary, msgType, message);
        printFieldMap("", dataDictionary, msgType, message.getTrailer());
    }

    private void printFieldMap(String prefix, DataDictionary dd, String msgType, FieldMap fieldMap)
            throws FieldNotFound {

        Iterator fieldIterator = fieldMap.iterator();
        while (fieldIterator.hasNext()) {
            Field field = (Field) fieldIterator.next();
            if (!isGroupCountField(dd, field)) {
                String value = fieldMap.getString(field.getTag());
                if (dd.hasFieldValue(field.getTag())) {
                    value = dd.getValueName(field.getTag(), fieldMap.getString(field.getTag())) + " (" + value + ")";
                }
                log.info(prefix + dd.getFieldName(field.getTag()) + ": " + value);
            }
        }

        Iterator groupsKeys = fieldMap.groupKeyIterator();
        while (groupsKeys.hasNext()) {
            int groupCountTag = (Integer) groupsKeys.next();
            log.info(prefix + dd.getFieldName(groupCountTag) + ": count = "
                    + fieldMap.getInt(groupCountTag));
            Group g = new Group(groupCountTag, 0);
            int i = 1;
            while (fieldMap.hasGroup(i, groupCountTag)) {
                if (i > 1) {
                    log.info(prefix + "  ----");
                }
                fieldMap.getGroup(i, g);
                printFieldMap(prefix + "  ", dd, msgType, g);
                i++;
            }
        }
    }

    private boolean isGroupCountField(DataDictionary dd, Field field) {
        return dd.getFieldType(field.getTag()) == FieldType.NUMINGROUP;
    }

}
