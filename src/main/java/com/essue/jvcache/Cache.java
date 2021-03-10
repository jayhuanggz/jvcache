package com.essue.jvcache;

import rx.Observable;

import java.util.Collection;


public interface Cache {

	String getName();

	Observable<Void> put(String key, Object value);

	Observable<Object> get(String key);

	Observable<Void> evict(String key);

	Observable<Void> evict(Collection<String> keys);

	Observable<Void> clear();

}
