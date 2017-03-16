package com.nao20010128nao.Kliket.deobfuscators

import com.nao20010128nao.Kliket.JavaUtils
import com.nao20010128nao.Kliket.Utils

import java.util.regex.Pattern
import java.util.stream.Collectors
import java.util.zip.DeflaterInputStream

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
        def last=simplifyFormat(source5)

        //write into output
        output.text=last
    }

    static String decodeBase64(String input){
        new String(Base64.decoder.decode(input))
    }

    static String cutString(String input, String head){
        def start=input.indexOf(head+"\"")
        if(start==-1)return null
        start+=head.length()+1
        def end=input.indexOf("\"",start)
        input.substring(start,end-start)
    }

    static String applySubstitutions(String input) {
        String a = simplifyAscii(input)
        return applySubstitutions(a, findAllSubstitution(a))
    }

    static String simplifyAscii(String input) {
        String result = input
        for (int i = 32; i <= 126; i++) {
            def curChar=Character.valueOf(i as char).toString()
            result = result
                    .replace("\\${Integer.toString(i,8)}", curChar)
                    .replace("\\x${Utils.to2DigitHex(i).toUpperCase()}", curChar)
                    .replace("\\x${Integer.toString(i,8).toUpperCase()}", curChar)
        }
        return result
    }

    static Map<String, String> findAllSubstitution(String input) {
        Map<String, String> result = [:]
        def first=Pattern.compile('\\$(?<varName>[a-z0-9]*)=\"(?<text>[a-zA-Z0-9_]*)\";')
        def firstMatcher=first.matcher(input)
        (firstMatcher.groupCount()/2).times{
            result[firstMatcher.group(it*2)]=firstMatcher.group(it*2+1)
        }
        def second=Pattern.compile('\\$(?<varName>[a-z0-9]*)\\.=\"(?<text>[a-zA-Z0-9_]*)\";')
        def secondMatcher=second.matcher(input)
        (secondMatcher.groupCount()/2).times{
            result[secondMatcher.group(it*2)]+=secondMatcher.group(it*2+1)
        }
        return result
    }

    static String applySubstitutions(String input, Map<String, String> substitutions) {
        String result = input
        substitutions.each {
            result=result.replace("\$$it.key(", "$it.value(")
        }
        return result
    }

    static List<String> fromTwoStringArgs(String input, String header) {
        int start = input.indexOf(header + "\"")
        if (start == -1) return null
        start += header.length() + 1
        int end = input.indexOf("\"", start)
        String r1 = input.substring(start, end - start)
        start = input.indexOf(header + "\"", end + 1)
        if (start == -1) return null
        start += header.length() + 1
        end = input.indexOf("\"", start)
        String r2 = input.substring(start, end - start)
        return [ r1, r2 ]
    }

    static String deflateBase64StringAndRot13(String input) {
        return deflateBase64StringIntoString(JavaUtils.rot13(input))
    }

    static String deflateBase64StringIntoString(String input) {
        new String(new DeflaterInputStream(new ByteArrayInputStream(Base64.decoder.decode(input))).bytes,"UTF-8")
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

    static String simplifyFormat(String source){
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
            String result="\t"*indent+line
            indent+=line.count("{")
            indent+=result.matches("case '([^'\\\\]|\\\\\\\\|\\\\')*?':")?1:0
            indent+=result.matches("case \"([^'\\\\]|\\\\\\\\|\\\\')*?\":")?1:0
            indent-=line.contains("break;")?1:0
            return result
        }.collect(Collectors.toList()).join("\n")
    }
}
