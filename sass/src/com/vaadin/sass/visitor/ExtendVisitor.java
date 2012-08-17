/*
 * Copyright 2011 Vaadin Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.vaadin.sass.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.css.sac.SelectorList;

import com.vaadin.sass.parser.SelectorListImpl;
import com.vaadin.sass.selector.SelectorUtil;
import com.vaadin.sass.tree.BlockNode;
import com.vaadin.sass.tree.ExtendNode;
import com.vaadin.sass.tree.Node;

public class ExtendVisitor implements Visitor {
    private Map<String, List<SelectorList>> extendsMap = new HashMap<String, List<SelectorList>>();

    @Override
    public void traverse(Node node) throws Exception {
        buildExtendsMap(node);
        modifyTree(node);
    }

    private void modifyTree(Node node) throws Exception {
        for (Node child : node.getChildren()) {
            if (child instanceof BlockNode) {
                BlockNode blockNode = (BlockNode) child;
                String selectorString = SelectorUtil.toString(blockNode
                        .getSelectorList());
                if (extendsMap.get(selectorString) != null) {
                    for (SelectorList sList : extendsMap.get(selectorString)) {
                        SelectorList newList = SelectorUtil
                                .createNewSelectorListFromAnOldOneWithSomPartReplaced(
                                        blockNode.getSelectorList(),
                                        selectorString, sList);
                        addAdditionalSelectorListToBlockNode(blockNode, newList);
                    }
                } else {
                    for (Entry<String, List<SelectorList>> entry : extendsMap
                            .entrySet()) {
                        if (selectorString.contains(entry.getKey())) {
                            for (SelectorList sList : entry.getValue()) {
                                SelectorList newList = SelectorUtil
                                        .createNewSelectorListFromAnOldOneWithSomPartReplaced(
                                                blockNode.getSelectorList(),
                                                entry.getKey(), sList);
                                addAdditionalSelectorListToBlockNode(blockNode,
                                        newList);
                            }
                        }
                    }
                }
            } else {
                buildExtendsMap(child);
            }
        }

    }

    private void buildExtendsMap(Node node) {
        if (node instanceof BlockNode) {
            BlockNode blockNode = (BlockNode) node;
            for (Node child : new ArrayList<Node>(node.getChildren())) {
                if (child instanceof ExtendNode) {
                    ExtendNode extendNode = (ExtendNode) child;
                    String extendedString = SelectorUtil.toString(extendNode
                            .getList());
                    if (extendsMap.get(extendedString) == null) {
                        extendsMap.put(extendedString,
                                new ArrayList<SelectorList>());
                    }
                    extendsMap.get(extendedString).add(
                            blockNode.getSelectorList());
                    node.removeChild(child);
                } else {
                    buildExtendsMap(child);
                }
            }
        } else {
            for (Node child : node.getChildren()) {
                buildExtendsMap(child);
            }
        }

    }

    private void addAdditionalSelectorListToBlockNode(BlockNode blockNode,
            SelectorList list) {
        if (list != null) {
            for (int i = 0; i < list.getLength(); i++) {
                ((SelectorListImpl) blockNode.getSelectorList())
                        .addSelector(list.item(i));
            }
        }
    }
}
