package com.gtocore.utils;

import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.BigInteger;

@UtilityClass
@SuppressWarnings("unused")
public class GsonHelperPatches {

    public boolean isStringValue(JsonObject json, String memberName) {
        return json != null && json.get(memberName) instanceof JsonPrimitive primitive && primitive.isString();
    }

    public boolean isNumberValue(JsonObject json, String memberName) {
        return json != null && json.get(memberName) instanceof JsonPrimitive primitive && primitive.isNumber();
    }

    public boolean isBooleanValue(JsonObject json, String memberName) {
        return json != null && json.get(memberName) instanceof JsonPrimitive primitive && primitive.isBoolean();
    }

    public boolean isArrayNode(JsonObject json, String memberName) {
        return json != null && json.get(memberName) instanceof JsonArray;
    }

    public boolean isObjectNode(JsonObject json, String memberName) {
        return json != null && json.get(memberName) instanceof JsonObject;
    }

    public boolean isValidPrimitive(JsonObject json, String memberName) {
        return json != null && json.get(memberName) instanceof JsonPrimitive;
    }

    public JsonObject getAsJsonObject(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToJsonObject(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a JsonObject");
    }

    public JsonObject getAsJsonObject(JsonObject json, String memberName, JsonObject fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToJsonObject(element, memberName) : fallback;
    }

    public JsonArray getAsJsonArray(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToJsonArray(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a JsonArray");
    }

    public JsonArray getAsJsonArray(JsonObject json, String memberName, JsonArray fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToJsonArray(element, memberName) : fallback;
    }

    public String getAsString(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToString(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a string");
    }

    public String getAsString(JsonObject json, String memberName, String fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToString(element, memberName) : fallback;
    }

    public Item getAsItem(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToItem(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find an item");
    }

    public Item getAsItem(JsonObject json, String memberName, Item fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToItem(element, memberName) : fallback;
    }

    public boolean getAsBoolean(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToBoolean(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Boolean");
    }

    public boolean getAsBoolean(JsonObject json, String memberName, boolean fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToBoolean(element, memberName) : fallback;
    }

    public double getAsDouble(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToDouble(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Double");
    }

    public double getAsDouble(JsonObject json, String memberName, double fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToDouble(element, memberName) : fallback;
    }

    public float getAsFloat(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToFloat(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Float");
    }

    public float getAsFloat(JsonObject json, String memberName, float fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToFloat(element, memberName) : fallback;
    }

    public long getAsLong(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToLong(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Long");
    }

    public long getAsLong(JsonObject json, String memberName, long fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToLong(element, memberName) : fallback;
    }

    public int getAsInt(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToInt(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Int");
    }

    public int getAsInt(JsonObject json, String memberName, int fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToInt(element, memberName) : fallback;
    }

    public byte getAsByte(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToByte(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Byte");
    }

    public byte getAsByte(JsonObject json, String memberName, byte fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToByte(element, memberName) : fallback;
    }

    public short getAsShort(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToShort(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Short");
    }

    public short getAsShort(JsonObject json, String memberName, short fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToShort(element, memberName) : fallback;
    }

    public char getAsCharacter(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToCharacter(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a Character");
    }

    public char getAsCharacter(JsonObject json, String memberName, char fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToCharacter(element, memberName) : fallback;
    }

    public BigDecimal getAsBigDecimal(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToBigDecimal(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a BigDecimal");
    }

    public BigDecimal getAsBigDecimal(JsonObject json, String memberName, BigDecimal fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToBigDecimal(element, memberName) : fallback;
    }

    public BigInteger getAsBigInteger(JsonObject json, String memberName) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToBigInteger(element, memberName);
        }
        throw new JsonSyntaxException("Missing " + memberName + ", expected to find a BigInteger");
    }

    public BigInteger getAsBigInteger(JsonObject json, String memberName, BigInteger fallback) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToBigInteger(element, memberName) : fallback;
    }

    public <T> T getAsObject(JsonObject json, String memberName, JsonDeserializationContext context, Class<? extends T> clazz) {
        JsonElement element = json.get(memberName);
        if (element != null) {
            return GsonHelper.convertToObject(element, memberName, context, clazz);
        }
        throw new JsonSyntaxException("Missing " + memberName);
    }

    public <T> T getAsObject(JsonObject json, String memberName, T fallback, JsonDeserializationContext context, Class<? extends T> clazz) {
        JsonElement element = json.get(memberName);
        return element != null ? GsonHelper.convertToObject(element, memberName, context, clazz) : fallback;
    }
}
