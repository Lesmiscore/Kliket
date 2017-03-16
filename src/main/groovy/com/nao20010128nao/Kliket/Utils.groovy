package com.nao20010128nao.Kliket

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
}
