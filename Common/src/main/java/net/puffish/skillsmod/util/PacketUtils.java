package net.puffish.skillsmod.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.IntFunction;

public class PacketUtils {
	public static  <E, C extends Collection<E>> C readCollection(
			FriendlyByteBuf buf,
			IntFunction<C> ctor,
			Function<? super FriendlyByteBuf, E> elementReader) {
		var count = Math.min(buf.readVarInt(), ByteBufCodecs.MAX_INITIAL_COLLECTION_SIZE);
		C result = ctor.apply(count);
		for (var i = 0; i < count; i++) {
			result.add(elementReader.apply(buf));
		}
		return result;
	}

	public static <E> List<E> readList(
			FriendlyByteBuf buf,
			Function<? super FriendlyByteBuf, E> elementReader
	) {
		return readCollection(buf, Lists::newArrayListWithCapacity, elementReader);
	}

	public static <K, V, M extends Map<K, V>> M readMap(
			FriendlyByteBuf buf,
			IntFunction<M> ctor,
			Function<? super FriendlyByteBuf, K> keyReader,
			Function<? super FriendlyByteBuf, V> valueReader
	) {
		var count = Math.min(buf.readVarInt(), ByteBufCodecs.MAX_INITIAL_COLLECTION_SIZE);
		M result = ctor.apply(count);
		for (var i = 0; i < count; i++) {
			K key = keyReader.apply(buf);
			V value = valueReader.apply(buf);
			result.put(key, value);
		}
		return result;
	}

	public static <K, V> Map<K, V> readMap(
			FriendlyByteBuf buf,
			Function<? super FriendlyByteBuf, K> keyReader,
			Function<? super FriendlyByteBuf, V> valueReader
	) {
		return readMap(buf, Maps::newHashMapWithExpectedSize, keyReader, valueReader);
	}

	public static <E> void writeCollection(
			FriendlyByteBuf buf,
			Collection<E> collection,
			BiConsumer<? super FriendlyByteBuf, E> elementWriter
	) {
		buf.writeVarInt(collection.size());
		for (E e : collection) {
			elementWriter.accept(buf, e);
		}
	}

	public static <K, V> void writeMap(
			FriendlyByteBuf buf,
			Map<K, V> map, BiConsumer<? super FriendlyByteBuf, K> keyWriter,
			BiConsumer<? super FriendlyByteBuf, V> valueWriter
	) {
		buf.writeVarInt(map.size());
		map.forEach((k, v) -> {
			keyWriter.accept(buf, k);
			valueWriter.accept(buf, v);
		});
	}

	public static <T extends Enum<T>> T readEnum(
			FriendlyByteBuf buf,
			Class<T> clazz
	) {
		return clazz.getEnumConstants()[buf.readVarInt()];
	}

	public static FriendlyByteBuf writeEnum(
			FriendlyByteBuf buf,
			Enum<?> value
	) {
		return buf.writeVarInt(value.ordinal());
	}
}
