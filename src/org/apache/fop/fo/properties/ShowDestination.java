package org.apache.fop.fo.properties;

import org.apache.fop.datastructs.ROStringArray;
import org.apache.fop.fo.expr.PropertyException;
import org.apache.fop.datatypes.PropertyValue;
import org.apache.fop.datatypes.EnumType;
import org.apache.fop.fo.PropNames;
import org.apache.fop.fo.properties.Property;

public class ShowDestination extends Property  {
    public static final int dataTypes = ENUM;
    public static final int traitMapping = ACTION;
    public static final int initialValueType = ENUM_IT;
    public static final int REPLACE = 1;
    public static final int NEW = 2;
    public /**/static/**/ PropertyValue getInitialValue(int property)
        throws PropertyException
    {
        return new EnumType (PropNames.SHOW_DESTINATION, REPLACE);
    }
    public static final int inherited = NO;

    private static final String[] rwEnums = {
        null
        ,"replace"
        ,"new"
    };
    public /**/static/**/ int getEnumIndex(String enum) throws PropertyException {
        return enumValueToIndex(enum, rwEnums);
    }
    public /**/static/**/ String getEnumText(int index) {
        return rwEnums[index];
    }
}

