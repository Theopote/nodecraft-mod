package com.nodecraft.gui.model;

import java.util.List;

/**
 * 代表UI中的一个节点类别，包含一组节点头信息。
 */
public record Category(String id, String name, List<NodeHeader> nodes) {} 