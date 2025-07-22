package com.f1.ami.web.centermanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class SetMerger<T> {
	UnionFind uf;

	public List<Set<T>> mergeSets(List<Set<T>> sets) {
		int n = sets.size();
		uf = new UnionFind(n);
		Map<T, Integer> itemToSet = new HashMap<>();

		// Union sets that share at least one common element
		for (int i = 0; i < n; i++) {
			for (T item : sets.get(i)) {
				if (itemToSet.containsKey(item)) {
					uf.union(i, itemToSet.get(item));
				} else {
					itemToSet.put(item, i);
				}
			}
		}

		// Merge sets according to union-find roots
		Map<Integer, Set<T>> merged = new HashMap<>();
		for (int i = 0; i < n; i++) {
			int root = uf.find(i);
			merged.computeIfAbsent(root, new Function<Integer, Set<T>>() {
				@Override
				public Set<T> apply(Integer k) {
					return new HashSet<>();
				}
			}).addAll(sets.get(i));
		}

		return new ArrayList<>(merged.values());
	}
	public static void main(String[] args) {
		SetMerger<String> m = new SetMerger<String>();

		List<Set<String>> sets = new ArrayList<Set<String>>();
		Set<String> s1 = new HashSet<String>(Arrays.asList("a", "b"));
		Set<String> s2 = new HashSet<String>(Arrays.asList("b", "c"));
		Set<String> s3 = new HashSet<String>(Arrays.asList("x"));
		Set<String> s4 = new HashSet<String>(Arrays.asList("y", "z"));
		Set<String> s5 = new HashSet<String>(Arrays.asList("z", "x"));

		sets.add(s1);
		sets.add(s2);
		sets.add(s3);
		sets.add(s4);
		sets.add(s5);

		System.out.println(m.mergeSets(sets));
	}
}
