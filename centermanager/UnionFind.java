package com.f1.ami.web.centermanager;

public class UnionFind {
	public int[] parent;
	public int size;
	public int[] sizes;
	public int numComponents;

	public UnionFind(int n) {
		size = n;
		sizes = new int[n];
		numComponents = n;
		parent = new int[n];
		for (int i = 0; i < n; i++) {
			parent[i] = i;
			sizes[i] = 1;
		}

	}

	int componentSize(int p) {
		return sizes[find(p)];
	}

	boolean isConnected(int p, int q) {
		return find(p) == find(q);
	}

	int find(int x) {
		if (parent[x] != x)
			parent[x] = find(parent[x]); // path compression
		return parent[x];
	}

	void union(int x, int y) {

		int root_x = find(x);
		int root_y = find(y);

		if (root_x == root_y)
			return;
		//merge smaller components into the larger one
		if (sizes[root_x] < sizes[root_y]) {
			sizes[root_y] += sizes[root_x];
			parent[root_x] = parent[root_y];
		} else {
			sizes[root_x] += sizes[root_y];
			parent[root_y] = parent[root_x];
		}
		numComponents--;

	}
}
