package com.nao20010128nao.Kliket;

import com.nao20010128nao.Kliket.deobfuscators.Fopo;

import java.util.function.Function;

/**
 * Created by nao on 2017/03/16.
 */
public class JavaUtils {
    public static String rot13(String value) {
        char[] array = value.toCharArray();
        for (int i = 0; i < array.length; i++) {
            int number = (int)array[i];
            if (number >= 'a' && number <= 'z') {
                if (number > 'm') {
                    number -= 13;
                } else {
                    number += 13;
                }
            } else if (number >= 'A' && number <= 'Z') {
                if (number > 'M') {
                    number -= 13;
                } else {
                    number += 13;
                }
            }
            array[i] = (char)number;
        }
        return new String(array);
    }
    public static Function<String,String> deflateBase64StringAndRot13(){
        return Fopo::deflateBase64StringAndRot13;
    }

}
