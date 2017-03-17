package com.nao20010128nao.Kliket

import com.nao20010128nao.Kliket.deobfuscators.Deobfuscator
import com.nao20010128nao.Kliket.deobfuscators.Fopo
import com.nao20010128nao.Kliket.deobfuscators.Pipsomania
import joptsimple.OptionParser

import java.util.regex.Pattern

final deobfuscators=["fopo":Fopo,"pipsomania":Pipsomania]

def opt=new OptionParser()
opt.accepts("input").withRequiredArg()
opt.accepts("output").withOptionalArg()
opt.accepts("mode").withOptionalArg()

def result=opt.parse(args)

File input,output
Deobfuscator deobs

if(!result.has("input")){
    usage("input")
    return
}else{
    input=new File(result.valueOf("input").toString()).absoluteFile
    println "IN: $input"
}
if(!result.has("output")){
    def renameFilename={String s->
        def filename=s.split(Pattern.quote(File.separator)).last()
        def nonExtFn=filename.substring(0,filename.lastIndexOf("."))
        def ext=filename.substring(filename.lastIndexOf(".")+1)
        nonExtFn+=".deobs"
        return nonExtFn+"."+ext
    }
    output = new File(input.parentFile, renameFilename(input.absolutePath))
}else{
    output=new File(result.valueOf("output").toString()).absoluteFile
}
println "OUT: $output"
if(!result.has("mode")){
    usage("mode")
    return
}else{
    def localMode=result.valueOf("mode").toString().toLowerCase()
    if(!deobfuscators.containsKey(localMode)){
        usage("mode",detail:"invalid"){
            println ""
            println "Available modes are: ${deobfuscators.keySet().sort().join(" ")}"
            println "Please use one of them."
        }
    }
    deobs=deobfuscators[localMode].newInstance()
    println "MODE: $localMode"
}

check(input)
// Deobfuscator class has call() so this is valid on Groovy
deobs(input,output)

static usage(cmd,detail="required",Closure doInFinal=null){
    println "$cmd argument is $detail."
    println "Usage:"
    println "--input=(filename) - Input file name (full path) of the PHP file. (required)"
    println "--output=(dirname) - Output directory to save deobfuscated result (default is to append \".deobs\" on the \"input\"'s filename)"
    println "--mode=(mode) - Which mode to use for debobfuscation. (required)"
    if(doInFinal!=null)
        doInFinal()
    System.exit 1
}
static check(object){
    def wasError=false
    if(object instanceof File){
        if(!object.exists()){
            println "$object doesn't exist."
            wasError=true
        }
    }
    if(wasError) System.exit 1
}