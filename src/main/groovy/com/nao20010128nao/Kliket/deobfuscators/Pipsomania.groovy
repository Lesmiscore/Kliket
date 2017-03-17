package com.nao20010128nao.Kliket.deobfuscators

import com.nao20010128nao.Kliket.Utils

import java.util.regex.Pattern

/**
 * Created by nao on 2017/03/17.
 */
class Pipsomania implements Deobfuscator {
    @Override
    void call(File input, File output) {
        def original = input.text
        //phase0.5~1
        def source1 = Utils.simplifyAscii(original)
        //phase2
        def source2=source1
        def source2Matcher=Pattern.compile('(?<dummy>\\$\\{"GLOBALS"}\\["[a-z]+"])="(?<name>[_a-zA-Z][_0-9a-zA-Z]*)";').matcher(source1)
        while(source2Matcher.find()){
            source2=source2
                    .replace(source2Matcher.group(0),"")
                    .replace(source2Matcher.group(1),source2Matcher.group(2))
                    .replace('${'+source2Matcher.group(2)+'}','$'+source2Matcher.group(2))
        }
        //phase3
        def source3=Fopo.applySubstitutions(source2)
        //last
        def last=Utils.fixSome(source3)

        //write into output
        output.text=last
    }
}