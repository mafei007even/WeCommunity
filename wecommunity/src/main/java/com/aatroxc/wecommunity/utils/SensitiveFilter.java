package com.aatroxc.wecommunity.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * @author mafei007
 * @date 2020/4/5 17:48
 */

@Component
@Slf4j
public class SensitiveFilter {


    /**
     * 替换的敏感词
     */
    private static final String REPLACEMENT = "***";

    /**
     * 根节点
     */
    private TrieNode rootNode = new TrieNode();


    /**
     * 初始化方法，读取类路径下的敏感词文件，将其加载到前缀树中.
     */
    @PostConstruct
    public void init() {
        try (
                InputStream input = getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                Scanner scanner = new Scanner(input)
        ) {

            String keyword;
            while (scanner.hasNextLine()) {
                keyword = scanner.nextLine();
                // 添加到前缀树
                this.addKeyword(keyword);
            }

        } catch (IOException e) {
            log.error("加载敏感词文件失败：" + e.getMessage(), e);
        }

    }

    /**
     * 将一个敏感词添加到前缀树
     *
     * @param keyword
     */
    private void addKeyword(String keyword) {

        TrieNode tempNode = rootNode;
        final char[] chars = keyword.toCharArray();

        for (int p = 0; p < chars.length; p++) {

            char c = chars[p];

            // 取此字符节点
            TrieNode subNode = tempNode.getSubNode(c);
            // 如果这个字符节点已经添加过，那就在其基础上添加，否则创建新的子节点
            if (subNode == null) {
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            // 指向子节点，进入下一轮循环
            tempNode = subNode;
        }
        tempNode.setKeywordEnd(true);

    }


    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */
    public String filter(String text) {

        if (StringUtils.isBlank(text)) {
            return null;
        }

        // 指针1
        TrieNode tempNode = this.rootNode;
        // 指针2
        int begin = 0;
        // 指针3
        int position = 0;
        // 结果
        StringBuilder sb = new StringBuilder();

        while (position < text.length()) {
            char c = text.charAt(position);

            // 跳过符号
            if (isSymbol(c)) {
                // 若指针1指向根节点，将此符号留着，计入结果
                // 让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    begin++;
                }
                // 无论符号在开头或中间，指针3都向下走一步
                position++;
                continue;
            }

            // 检测下级节点
            tempNode = tempNode.getSubNode(c);
            // 为空说明以begin开头的字符不在敏感词中，指针向下移动一个
            if (tempNode == null) {
                sb.append(text.charAt(begin));
                // 进入下一个位置
                begin ++;
                position = begin;
                // 重新指向根节点
                tempNode = rootNode;
                continue;
            }

            // 当前字符在敏感词中
            // 为敏感词结尾，则说明发现了敏感词，将begin-position字符串替换掉
            if (tempNode.isKeywordEnd()) {

                sb.append(REPLACEMENT);
                // 进入下一个位置
                position++;
                begin = position;
                // 重新指向根节点
                tempNode = rootNode;

            } else {
                // 不是敏感词结尾，还有嫌疑，继续检查下一个字符
                position ++;
            }
        }

        // 将最后一批字符计入结果

        // for (int i = begin; i < position; i++){
        //     sb.append(text.charAt(i));
        // }
        sb.append(text.substring(begin));

        return sb.toString();
    }


    /**
     * 判断是否为符号
     *
     * @param c
     * @return
     */
    private boolean isSymbol(Character c) {
        // 0x2E80-0x9FFF 是东亚文字范围，包含中文、日文、韩文
        // (c < 0x2E80 || c > 0x9FFF) 就是说在东亚文字范围之外，就认为是字符
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }


    /**
     * 前缀树的节点
     */
    private class TrieNode {

        /**
         * 敏感词结束标识
         */
        @Getter
        @Setter
        private boolean isKeywordEnd = false;

        /**
         * 当前节点的子节点
         * key   是下级节点的字符
         * value 是下级节点的子节点
         */
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        /**
         * 添加子节点
         *
         * @param c
         * @param node
         */
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        /**
         * 取子节点
         *
         * @param c
         * @return
         */
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }

}
