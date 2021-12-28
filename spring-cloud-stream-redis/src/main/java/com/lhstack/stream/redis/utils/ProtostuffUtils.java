package com.lhstack.stream.redis.utils;


import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.runtime.RuntimeSchema;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author lhstack
 * @date 2021/9/7
 * @class ProtostuffUtils
 * @since 1.8
 */
public class ProtostuffUtils {

    public static class ProtostuffData {
        private final Map<String, Object> data = new HashMap<>();

        public void put(String key, Object value) {
            this.data.put(key, value);
        }

        public <T> T get(String key) {
            return (T) this.data.get(key);
        }

        public Set<String> keys() {
            return this.data.keySet();
        }

        public Collection<Object> values() {
            return this.data.values();
        }

    }

    /**
     * 缓存
     */
    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE * 2 * 4);
    private static final RuntimeSchema<ProtostuffData> SCHEMA = (RuntimeSchema<ProtostuffData>) RuntimeSchema.getSchema(ProtostuffData.class);


    /**
     * 对象序列化
     */
    public static <T> byte[] serialize(T obj) {
        ProtostuffData protostuffData = new ProtostuffData();
        protostuffData.put("data", obj);
        byte[] bytes = ProtostuffIOUtil.toByteArray(protostuffData, SCHEMA, BUFFER);
        BUFFER.clear();
        return bytes;
    }

    /**
     * 对象序列化
     */
    public static <T> T deserialize(byte[] bytes) {
        ProtostuffData data = SCHEMA.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes, data, SCHEMA);
        return data.get("data");
    }

}
