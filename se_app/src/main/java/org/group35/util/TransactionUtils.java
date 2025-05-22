package org.group35.util;

import java.util.List;

import org.group35.controller.TransactionManager;
import org.group35.model.Transaction;
import org.group35.runtime.ApplicationRuntime;

/**
 * Manages loading, saving, and querying financial transactions.
 * <br><br>
 * Usage Example:
 * <br>
 * {@code ApplicationRuntime runtime = ApplicationRuntime.getInstance();}
 * <br>
 * {@code TransactionManager txManager = runtime.getTranscationManager();}
 * <br>
 * {@code List<Transaction> tx = txManager.getByUser(runtime.getCurrentUser().getUsername());}
 * <br>
 *
 */
public class TransactionUtils {
    public static List<Transaction> getTransaction(){
        ApplicationRuntime runtime = ApplicationRuntime.getInstance();
        TransactionManager txManager = runtime.getTranscationManager();
        return txManager.getByUser(runtime.getCurrentUser().getUsername());
    }
    public static String transferTransaction() {
        List<Transaction> transactions = getTransaction();
        StringBuilder result = new StringBuilder();
        result.append("["); // 假设输出为 JSON 数组格式

        for (int i = 0; i < transactions.size(); i++) {
            Transaction t = transactions.get(i);
            if (i > 0) result.append(", ");
            result.append(convertTransactionToEscapedString(t));
        }

        result.append("]");
        return result.toString();
    }

    // 单个 Transaction 对象转字符串（含转义）
    private static String convertTransactionToEscapedString(Transaction t) {
        return String.format(
                "{\"id\":\"%s\",\"username\":\"%s\",\"name\":\"%s\",\"timestamp\":\"%s\"," +
                        "\"amount\":%.2f,\"category\":\"%s\",\"currency\":\"%s\"}",
                escapeString(t.getId()),
                escapeString(t.getUsername()),
                escapeString(t.getName()),
                escapeString(t.getTimestamp().toString()),
                t.getAmount(), // 金额保留两位小数且不转义
                escapeString(t.getCategory()),
                escapeString(t.getCurrency().name())
        );
    }

    // 转义特殊字符（支持常见转义符）
    private static String escapeString(String input) {
        if (input == null) return "";
        return input
                .replace("\\", "\\\\")  // 反斜杠
                .replace("\"", "\\\"")  // 双引号
                .replace("\n", "\\n")   // 换行符
                .replace("\r", "\\r")   // 回车符
                .replace("\t", "\\t");  // 制表符
    }

}