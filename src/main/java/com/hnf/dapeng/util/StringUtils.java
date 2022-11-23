package com.hnf.dapeng.util;

import javafx.scene.control.TextField;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gj
 * @date 2021/4/8 14:27
 */
public class StringUtils {
    private static final List<Integer> ID_CARD_NUM_AREA = Arrays.asList(18, 15);

    /**
     * 限制输入框的最大字数
     */
    public static void setTextFieldMaxCount(TextField jfxTextField, int count) {
        jfxTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() > count) {
                jfxTextField.setText(oldValue);
            }
        });
    }

    /**
     * 统一处理非法字符串转换
     * @param str
     * @return
     */
    public static String formatNullStr(String str){
        String nullChar = isNullChar(str);
        if (nullChar.isEmpty()){
            return "无";
        }
        return nullChar;
    }

    /**
     * Linux路径转换
     *
     * @param path
     * @return
     */
    public static String formatLinuxPath(String path) {
        if (path != null && !path.isEmpty()) {
            return path.trim().replace("\\", "/").replace("//", "/").replaceAll("\\./", "");
        }
        return "";
    }

    /**
     * 获取时分秒
     *
     * @param time
     * @return char HH:mm:ss
     */
    public static String getHhMmSs(long time) {
        int ten = 10;
        long seconds = time % 60;
        long minutes = (time % (60 * 60)) / 60;
        long hours = time / (60 * 60);
        return (hours >= ten ? hours : "0" + hours) + ":" + (minutes >= ten ? minutes : "0" + minutes) + ":" + (seconds >= ten ? seconds : "0" + seconds);
    }

    /**
     * 获取时分秒
     *
     * @param time
     * @return char HH:mm:ss
     */
    public static String getHhMmSsStr(long time) {
        if (time <= 0) {
            return "未知";
        }
        long seconds = time % 60;
        long minutes = (time % (60 * 60)) / 60;
        long hours = time / (60 * 60);
        return hours + "小时" + minutes + "分" + seconds + "秒";
    }

    public static boolean isJsonArray(String str) {
        return str.startsWith("[") && str.endsWith("]");
    }

    public static boolean isJsonObject(String str) {
        return str.startsWith("{") && str.endsWith("}");
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.length() == 0;
    }


    public static String isNullChar(String str) {
        return str == null || Objects.equals("null", str) ? "" : str;
    }

    public static String historyIsNullChar(String str) {
        return str == null || Objects.equals("null", str) || str.isEmpty() ? "--------" : str;
    }

    /**
     * * 校验身份证号
     * *
     * * @param idCardNo 身份证号
     * * @return true通过，false未通过
     */
    public static boolean checkIdCardNo(String idCardNo) {
        if (null == idCardNo) {
            return false;
        }
        idCardNo = idCardNo.toLowerCase();
        int idCardLength = idCardNo.length();
        if (!ID_CARD_NUM_AREA.contains(idCardLength)) {
            return false;
        }
        String[] rc = {"1", "0", "x", "9", "8", "7", "6", "5", "4", "3", "2"};
        boolean checkBirthday;
        String birthday;
        try {
            // 18位
            if (idCardNo.length() == 18) {
                birthday = idCardNo.substring(6, 14);
                checkBirthday = isDate(birthday, "yyyyMMdd");

                int[] w = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
                int sum = 0;
                for (int i = 0; i < idCardNo.length() - 1; i++) {
                    int c = Integer.parseInt(idCardNo.substring(i, i + 1));
                    sum += w[i] * c;
                }
                int r = sum % 11;
                if (rc[r].equals(idCardNo.substring(idCardLength - 1)) && checkBirthday) {
                    return true;
                } else {
                    return false;
                }
            } else {//15位
                birthday = "19" + idCardNo.substring(6, 12);
                checkBirthday = isDate(birthday, "yyyyMMdd");
                if (checkBirthday) {
                    return true;
                } else {
                    return false;
                }
            }
        } catch (NumberFormatException e) {
            Logger.getGlobal().log(Level.SEVERE, null, e);
            return false;
        }
    }

    /**
     * 是否为合法日期
     *
     * @param dateString 日期
     * @param format     指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
     * @return
     */
    private static boolean isDate(String dateString, String format) {
        boolean convertSuccess = true;
        //指定日期格式为四位年/两位月份/两位日期，注意yyyy/MM/dd区分大小写；
        SimpleDateFormat smf = new SimpleDateFormat(format);
        try {
            //设置lenient为false. 否则SimpleDateFormat会比较宽松地验证日期，比如2007/02/29会被接受，并转换成2007/03/01
            smf.setLenient(false);
            smf.parse(dateString);
        } catch (ParseException e) {
            //如果throw java.text.ParseException或者NullPointerException，就说明格式不对
            Logger.getGlobal().log(Level.SEVERE, null, e);
            convertSuccess = false;
        }
        return convertSuccess;
    }
}

