package me.drakeet.inmessage.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.drakeet.inmessage.constant.StaticObjectInterface;
import me.drakeet.inmessage.model.Message;

/**
 * Created by Administrator on 2014/12/24 0024.
 */
public class StringUtils implements StaticObjectInterface{

    private StringUtils() {
    }

    /**
     * 判断字符串中子字符串出现次数
     *
     * @param str
     * @param key
     * @return
     */
    public static int getSubCount(String str, String key) {
        int count = 0;
        int index = 0;
        String strOperation = str;
        while (index != -1) {
            index = strOperation.indexOf(key);
            if (index == -1) break;
            int length = key.length();
            strOperation = strOperation.substring(index + length);
            count++;
        }
        return count;
    }


    public static String getContentInBracket(String str, String address) {
        Pattern pattern = Pattern.compile("\\【(.*?)\\】");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            if(matcher.group(1) != null && matcher.group(1).length() < 10) {

                return analyseSpecialCompany(matcher.group(1), str, address);
            }
        }
        Pattern pattern1 = Pattern.compile("\\[(.*?)\\]");
        Matcher matcher1 = pattern1.matcher(str);
        while (matcher1.find()) {
            if(matcher1.group(1) != null && matcher1.group(1).length() < 10) {

                return analyseSpecialCompany(matcher1.group(1), str, address);
            }
        }
        Pattern pattern2 = Pattern.compile("\\((.*?)\\)");
        Matcher matcher2 = pattern2.matcher(str);
        while (matcher2.find()) {
            if(matcher2.group(1) != null && matcher2.group(1).length() < 10) {

                return analyseSpecialCompany(matcher2.group(1), str, address);
            }
        }
        return null;
    }

    private static String analyseSpecialCompany(String company, String content, String address) {
        String companyName = company;
        if(company.equals("掌淘科技")) {
           int index = content.indexOf("的验证码");
           companyName = content.substring(0, index);
           companyName = companyName.replaceAll("【掌淘科技】", "").trim();
        }
        else {
            if(content.contains("贝壳单词的验证码")) {
                companyName = "贝壳单词";
            }
        }
        if(address.equals("10010")) {
            companyName = "中国联通";
        }
        if(address.equals("10086")) {
            companyName = "中国移动";
        }
        if(address.equals("10000")) {
            companyName = "中国电信";
        }
        return companyName;
    }

    public static boolean isPersonalMoblieNO(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static String tryToGetCaptchas(String str) {
        Pattern continuousNumberPattern = Pattern.compile("[a-zA-Z0-9\\.]+");
        Matcher m = continuousNumberPattern.matcher(str);
        while (m.find()) {
            if (m.group().length() > 3 && m.group().length() < 8 && !m.group().contains(".")) {
                return m.group();
            }
        }
        return "";
    }

    public static boolean isCaptchasMessage(String content) {
       Boolean isCaptchasMessage = false;
       for(int i = 0;i < CPATCHAS_KEYWORD.length;i++) {
           if(content.contains(CPATCHAS_KEYWORD[i])) {
               isCaptchasMessage = true;
               break;
           }
       }
       return  isCaptchasMessage;
    }

    /**
     * 根据短信获取描述文字
     * @return
     */
    public static String getResultText(Message message, Boolean isNotificationText) {
        String resultStr = "";
        if(message.getCompanyName() != null && !isNotificationText) {
            resultStr += "来自" + message.getCompanyName() + "的验证码：";
        }
        else {
            resultStr += "当前验证码为：";
        }
        if(message.getCaptchas() != null) {
            resultStr += message.getCaptchas();
        }
        else {
            resultStr += "点击查看详情.";
        }
        return resultStr;
    }

    /**
     * @prama: str 要判断是否包含特殊字符的目标字符串
     */
    public static boolean compileExChar(String str) {

        String limitEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？ ]";

        Pattern pattern = Pattern.compile(limitEx);
        Matcher m = pattern.matcher(str);

        if (m.find()) {
            return true;
        } else {
            return false;
        }
    }
}
