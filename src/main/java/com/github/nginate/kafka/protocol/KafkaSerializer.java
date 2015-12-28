package com.github.nginate.kafka.protocol;

import com.github.nginate.kafka.exceptions.SerializationException;
import com.github.nginate.kafka.protocol.messages.Request;
import com.github.nginate.kafka.protocol.types.Type;
import com.github.nginate.kafka.protocol.types.TypeName;
import com.google.common.base.Charsets;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.nginate.kafka.protocol.types.TypeName.*;
import static com.github.nginate.kafka.util.ReflectionUtils.doWithSortedFields;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class KafkaSerializer {
    private final Map<TypeName, Function<Object, byte[]>> serializers = new EnumMap<>(TypeName.class);
    private final Map<TypeName, BiFunction<ByteBuffer, Class<?>, Object>> deserializers = new EnumMap<>(TypeName.class);

    private final Predicate<Field> fieldFilter = field -> field.isAnnotationPresent(Type.class);
    private final Comparator<Field> comparator = Comparator.comparing(field -> field.getAnnotation(Type.class).order());

    {
        serializers.put(INT8, o -> ByteBuffer.allocate(1).put(Byte.class.cast(o)).array());
        serializers.put(INT16, o -> ByteBuffer.allocate(2).putShort(Short.class.cast(o)).array());
        serializers.put(INT32, o -> ByteBuffer.allocate(4).putInt(Integer.class.cast(o)).array());
        serializers.put(INT64, o -> ByteBuffer.allocate(8).putLong(Long.class.cast(o)).array());
        serializers.put(STRING, o -> {
            short size = -1;
            byte[] data = new byte[0];
            if (o != null) {
                data = String.class.cast(o).getBytes(Charsets.UTF_8);
                size = (short) data.length;
            }
            return ByteBuffer.allocate(data.length + 2).putShort(size).put(data).array();
        });
        serializers.put(BYTES, o -> {
            byte[] data = o == null ? new byte[0] : byte[].class.cast(o);
            int size = data.length == 0 ? -1 : data.length;
            return ByteBuffer.allocate(data.length + 4).putInt(size).put(data).array();
        });
        serializers.put(WRAPPER, this::serialize);

        deserializers.put(INT8, (buffer, clazz) -> buffer.get());
        deserializers.put(INT16, (buffer, clazz) -> buffer.getShort());
        deserializers.put(INT32, (buffer, clazz) -> buffer.getInt());
        deserializers.put(INT64, (buffer, clazz) -> buffer.getLong());
        deserializers.put(STRING, (buffer, clazz) ->  {
            short size = buffer.getShort();
            if (size == -1) {
                return null;
            } else {
                byte[] rawString = new byte[size];
                buffer.get(rawString);
                return new String(rawString, Charsets.UTF_8);
            }
        });
        deserializers.put(BYTES, (buffer, clazz) -> {
            int size = buffer.getInt();
            if (size == -1) {
                return null;
            } else {
                byte[] array = new byte[size];
                buffer.get(array);
                return array;
            }
        });
        deserializers.put(WRAPPER, this::deserialize);
    }

    public byte[] serialize(Object message) throws SerializationException {
        try {
            List<byte[]> fields = new ArrayList<>();
            doWithSortedFields(message.getClass(), fieldFilter, comparator, field -> {
                Type type = field.getAnnotation(Type.class);
                Function<Object, byte[]> serializer = serializers.get(type.value());
                Object value = getFieldValue(field, message);
                if (field.getType().isArray() && value != null) {
                    List<byte[]> array = stream(Object[].class.cast(value)).map(serializer::apply).collect(toList());
                    ByteBuffer byteBuffer = ByteBuffer.allocate(array.size() + 4).putInt(array.size());
                    array.forEach(byteBuffer::put);
                    fields.add(byteBuffer.array());
                } else if (field.getType().isArray()) {
                    fields.add(ByteBuffer.allocate(4).putInt(-1).array()); // TODO check null arrays length
                } else {
                    fields.add(serializer.apply(value));
                }
            });
            byte[] apiKeyHeader = buildApiKeyHeader(message);

            int size = apiKeyHeader.length + fields.stream().map(bytes -> bytes.length).reduce((i, i2) -> i + i2).get();
            ByteBuffer byteBuffer = ByteBuffer.allocate(size);

            byteBuffer.putInt(size);
            byteBuffer.put(apiKeyHeader);
            fields.forEach(byteBuffer::put);
            return byteBuffer.array();
        } catch (Exception e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    private byte[] buildApiKeyHeader(Object message) {
        if (message instanceof Request) {
            ApiKey apiKey = message.getClass().getAnnotation(ApiKey.class);
            return ByteBuffer.allocate(2).putShort(apiKey.value().getId()).array();
        } else {
            return new byte[0];
        }
    }

    private Object getFieldValue(Field field, Object bean) {
        try {
            return field.get(bean);
        } catch (IllegalAccessException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    public <RS> Object deserialize(ByteBuffer buf, Class<RS> clazz) throws SerializationException {
        try {
            RS response = clazz.newInstance();
            doWithSortedFields(clazz, fieldFilter, comparator, field -> {
                Type type = field.getAnnotation(Type.class);
                BiFunction<ByteBuffer, Class<?>, Object> deserializer = deserializers.get(type.value());
                if (field.getType().isArray()) {
                    int size = buf.getInt();
                    if (size != -1) {
                        Object[] value = range(0, size).mapToObj(value1 -> deserializer.apply(buf, clazz)).toArray();
                        setField(response, field, value);
                    }
                } else {
                    Object value = deserializer.apply(buf, clazz);
                    setField(response, field, value);
                }
            });
            return response;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    private <RS> void setField(RS response, Field field, Object value) {
        try {
            field.set(response, value);
        } catch (IllegalAccessException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }
}
