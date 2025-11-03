package com.example.interop

import scala.sys.process._
import scala.util.{Try, Success, Failure}

object JavaScriptInterop {
  
  def executeNode(script: String, args: List[String] = Nil): Try[String] = {
    Try {
      val cmd = "node" :: script :: args
      val result = cmd.!!
      result.trim
    }
  }
  
  def evaluateJavaScript(code: String): Try[String] = {
    Try {
      val tempFile = java.io.File.createTempFile("scala_js_", ".js")
      try {
        val writer = new java.io.PrintWriter(tempFile)
        writer.write(code)
        writer.close()
        executeNode(tempFile.getAbsolutePath).get
      } finally {
        tempFile.delete()
      }
    }
  }
  
  def callJavaScriptFunction(code: String, functionName: String, args: List[Any]): Try[String] = {
    val jsCode = s"""
$code
const result = $functionName(${args.map(formatJsArg).mkString(", ")});
console.log(JSON.stringify(result));
"""
    evaluateJavaScript(jsCode)
  }
  
  private def formatJsArg(arg: Any): String = arg match {
    case s: String => s""""$s""""
    case n: Number => n.toString
    case b: Boolean => b.toString
    case _ => s""""${arg.toString}""""
  }
}

