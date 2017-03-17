package com.nao20010128nao.Kliket

import java.util.regex.Pattern
import java.util.stream.Collectors

/**
 * Created by nao on 2017/03/16.
 */
class Utils {
    static String to2DigitHex(int value) {
        return new StringBuilder()
                .append(Character.forDigit(value >> 4 & 0xF, 16))
                .append(Character.forDigit(value & 0xF, 16))
                .toString()
    }
    static String fixSome(String source){
        String tmp = source.replace("; ", ";\n")
        tmp = tmp.replace("{ ", "{\n")
        tmp = tmp.replace("} ", "}\n")
        tmp = tmp.replace("class ", "\nclass ")
        tmp = Pattern.compile(" (namespace [a-zA-Z0-9_]*;)").matcher(tmp).replaceAll("\n\$1\n")
        tmp = Pattern.compile("(case '([^'\\\\]|\\\\\\\\|\\\\')*?'): ").matcher(tmp).replaceAll("\$1:\n")
        tmp = Pattern.compile("(case \"([^'\\\\]|\\\\\\\\|\\\\')*?\"): ").matcher(tmp).replaceAll("\$1:\n")
        int indent = 0
        return tmp.readLines().stream().map{line->
            indent-=line.count("}")
            String result="\t"*Math.max(indent,0)+line
            indent+=line.count("{")
            indent+=result.matches("case '([^'\\\\]|\\\\\\\\|\\\\')*?':")?1:0
            indent+=result.matches("case \"([^'\\\\]|\\\\\\\\|\\\\')*?\":")?1:0
            indent-=line.contains("break;")?1:0
            return result
        }.collect(Collectors.toList()).join("\n")
    }
    static String simplifyAscii(String input) {
        String result = input
        for (int i = 32; i <= 126; i++) {
            def curChar=JavaUtils.intAsCharToString(i)
            result = result
                    .replace("\\${Integer.toString(i,8).toUpperCase()}", curChar)
                    .replace("\\${Integer.toString(i,8).toLowerCase()}", curChar)
                    .replace("\\x${to2DigitHex(i).toUpperCase()}", curChar)
                    .replace("\\x${to2DigitHex(i).toLowerCase()}", curChar)
        }
        return result
    }
}
