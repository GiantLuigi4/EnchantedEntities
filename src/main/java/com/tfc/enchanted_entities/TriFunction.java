package com.tfc.enchanted_entities;

@FunctionalInterface
public interface TriFunction<T,V,U,E> {
	E apply(T t, V v, U u);
}
