package com.nao20010128nao.Kliket.deobfuscators

import com.nao20010128nao.Kliket.JavaUtils
import com.nao20010128nao.Kliket.Utils

import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

import static com.nao20010128nao.Kliket.Utils.*

/**
 * ref: https://github.com/NewDelion/Deobfuscator/blob/master/FOPO.cs
 */
class Fopo implements Deobfuscator{
    void call(File input, File output) {
        def original=input.text
        //phase1
        def source1 = decodeBase64(cutString(applySubstitutions(original).replace("\r", "").replace("\n", ""), "base64_decode("))
        //phase2
        def source2=fromTwoStringArgs(applySubstitutions(source1), "gzinflate(base64_decode(str_rot13(")
                .stream()
                .map(JavaUtils.deflateBase64StringAndRot13())
                .collect(Collectors.toList())
        //phase3
        def source3=source2
                .stream()
                .map({doDecompressLoop(applySubstitutions(it as String), false)})
                .filter({(it as String).contains("gzinflate(base64_decode(")})
                .findFirst()
                .orElse(null)
                .toString()
        //phase4
        def source4=doDecompressLoop(source3,true)
        //phase5
        def source5=source4.substring(2)
        //last (Java has "final" modifier so use this word)
        def last=fixSome(source5)

        //write into output
        output.text=last
    }

    static String decodeBase64(String input){
        new String(Base64.decoder.decode(input as String))
    }

    static String cutString(String input, String head){
        def start=input.indexOf(head+"\"")
        if(start==-1)return null
        start+=head.length()+1
        def end=input.indexOf("\"",start)
        input.substring(start,end)
    }

    static String applySubstitutions(String input) {
        String a = simplifyAscii(input)
        return applySubstitutions(a, findAllSubstitution(a))
    }



    static Map<String, String> findAllSubstitution(String input) {
        Map<String, String> result = [:]
        def first = Pattern.compile('\\$([a-z0-9]*)=\"([a-zA-Z0-9_]*)\";')
        def firstMatcher = first.matcher(input)
        while(firstMatcher.find()) {
            result[firstMatcher.group(1)] = firstMatcher.group(2)
        }
        def second = Pattern.compile('\\$([a-z0-9]*)\\.=\"([a-zA-Z0-9_]*)\";')
        def secondMatcher = second.matcher(input)
        while(secondMatcher.find()) {
            result[secondMatcher.group(1)] += secondMatcher.group(2)
        }
        return result
    }

    static String applySubstitutions(String input, Map<String, String> substitutions) {
        String result = input
        substitutions.each {
            result=result.replace('$'+it.key+'(', it.value+'(')
        }
        return result
    }

    static List<String> fromTwoStringArgs(String input, String header) {
        int start = input.indexOf(header + "\"")
        if (start == -1) return null
        start += header.length() + 1
        int end = input.indexOf("\"", start)
        String r1 = input.substring(start, end)
        start = input.indexOf(header + "\"", end + 1)
        if (start == -1) return null
        start += header.length() + 1
        end = input.indexOf("\"", start)
        String r2 = input.substring(start, end)
        return [ r1, r2 ]
    }

    static String deflateBase64StringAndRot13(String input) {
        return deflateBase64StringIntoString(JavaUtils.rot13(input))
    }

    static String deflateBase64StringIntoString(String input) {
        new String(new InflaterInputStream(new ByteArrayInputStream(Base64.decoder.decode(input)),new Inflater(true)).bytes,"UTF-8")
    }

    static String doDecompressLoop(String input, boolean disableRot13) {
        String tmp = input
        while (true) {
            if (tmp.contains("gzinflate(base64_decode(str_rot13("))
                tmp = applySubstitutions(deflateBase64StringAndRot13(cutString(tmp, "gzinflate(base64_decode(str_rot13(")))
            else if (disableRot13 && tmp.contains("gzinflate(base64_decode("))
                tmp = applySubstitutions(deflateBase64StringIntoString(cutString(tmp, "gzinflate(base64_decode(")))
            else
                break
        }
        return tmp
    }
}
