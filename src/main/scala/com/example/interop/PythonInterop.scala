package com.example.interop

import scala.sys.process._
import scala.util.{Try, Success, Failure}

object PythonInterop {
  
  def executePython(script: String, args: List[String] = Nil): Try[String] = {
    Try {
      val cmd = "python" :: script :: args
      val result = cmd.!!
      result.trim
    } recoverWith {
      case _ => Try {
        val cmd = "python3" :: script :: args
        val result = cmd.!!
        result.trim
      }
    }
  }
  
  def evaluatePython(code: String): Try[String] = {
    Try {
      val tempFile = java.io.File.createTempFile("scala_python_", ".py")
      try {
        val writer = new java.io.PrintWriter(tempFile)
        writer.write(code)
        writer.close()
        executePython(tempFile.getAbsolutePath).get
      } finally {
        tempFile.delete()
      }
    }
  }
  
  def callPythonFunction(script: String, functionName: String, args: List[String]): Try[String] = {
    val pythonCode = s"""
import json
import sys
exec(open('$script').read())
result = $functionName(${args.map(a => s"'$a'").mkString(", ")})
print(json.dumps(result))
"""
    evaluatePython(pythonCode)
  }
}

